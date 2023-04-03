package com.syncodec.graphite.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.lifecycle.lifecycleScope
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.NetworkIOException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.DeleteErrorException
import com.dropbox.core.v2.files.DownloadErrorException
import com.dropbox.core.v2.files.WriteMode
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.collect.Maps
import com.syncodec.graphite.di.model.BaseObject
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.DeletedObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.network.OpenLibraryApi
import com.syncodec.graphite.di.network.TMDbApi
import com.syncodec.graphite.di.sync.dropbox.DBox
import com.syncodec.graphite.utils.encodeBase64
import com.syncodec.graphite.utils.toDbxHashString
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.io.InputStream
import kotlin.reflect.KMutableProperty1


class DropboxService : SyncerService() {

	private var dropboxServiceBinder : IBinder? = DropboxServiceBinder()

	var isUnbounded = false

	override fun onCreate() {
		super.onCreate()
		startSync()
		lifecycleScope.launch(Dispatchers.IO) {
			getDbxClient { dbxClientV2 ->
				localDbxClientV2 = dbxClientV2
				startSync()
			}
		}
		lifecycleScope.launch(Dispatchers.Default) {
			while (true) {
				delay(1000)
				if (isUnbounded) {
					stopper()
					break
				}
			}
		}
	}

	fun stopper() {
		lifecycleScope.launch(Dispatchers.Default) {
			while (true) {
				delay(1000)
				if (
					syncStatus.value is SyncerService.Companion.SyncStatus.Idle ||
					syncStatus.value is SyncerService.Companion.SyncStatus.Failed ||
					syncStatus.value is SyncerService.Companion.SyncStatus.Disconnected ||
					syncStatus.value is SyncerService.Companion.SyncStatus.Init ||
					syncStatus.value is SyncerService.Companion.SyncStatus.Locked
				) {
					break
				}
			}
			dropboxServiceBinder = null
			stopSelf()
		}
	}

	override fun onBind(intent : Intent) : IBinder? {
		super.onBind(intent)
		Log.i("npr71", "DropboxService: onBind")
		Toast.makeText(this, "service bind", Toast.LENGTH_SHORT).show()
		return dropboxServiceBinder
	}

	val dBox : DBox by inject()
	private var localDbxClientV2 : DbxClientV2? = null
	private fun getDbxClient(callback : (DbxClientV2) -> Unit) {
		val config = DbxRequestConfig("Graphite")
		dBox.getAccessToken {
			if (it is DBox.Companion.AccessTokenResponseResponse.Success) {
				callback(DbxClientV2(config, it.accessToken))
			}
		}
	}

	private var syncCoroutineScope : CoroutineScope? = null

	fun startSync(cancelScope : Boolean = false) {
		when (syncStatus.value) {
			is SyncerService.Companion.SyncStatus.Init -> {
				syncStatus.tryEmit(SyncerService.Companion.SyncStatus.Loading)
				startSync()
			}

			is SyncerService.Companion.SyncStatus.Idle -> null
			is SyncerService.Companion.SyncStatus.Loading -> {
				lifecycleScope.launch(Dispatchers.IO) {
					if (cancelScope) syncCoroutineScope?.cancel()
					syncCoroutineScope = this
					if (dataStoreInstance.isSyncEnabled()) {
						localDbxClientV2?.let { dbxClientV2 ->
							checkLock(dbxClientV2 = dbxClientV2) { driveState ->
								when (driveState) {
									is SyncerService.Companion.DriveState.Available -> {
										val sessionId = RealmUUID.random().toString()
										lock(dbxClientV2, sessionId)
										sync(dbxClientV2, false)
										unlock(dbxClientV2, sessionId) { unlockResult -> }
									}

									is SyncerService.Companion.DriveState.Locked -> {
										syncStatus.tryEmit(SyncerService.Companion.SyncStatus.Locked)
										delay(10000)
										startSync(cancelScope = true)
									}

									is SyncerService.Companion.DriveState.LockExpired -> {
										val sessionId = RealmUUID.random().toString()
										lock(dbxClientV2, sessionId)
										sync(dbxClientV2, true)
										unlock(dbxClientV2, sessionId) { unlockResult -> }
									}

									is SyncerService.Companion.DriveState.Unknown ->
										syncStatus.tryEmit(SyncerService.Companion.SyncStatus.Failed(driveState.exception.message ?: "Unknown Error"))
								}
							}
						}
					} else syncStatus.tryEmit(SyncerService.Companion.SyncStatus.Paused)
				}
			}

			is SyncerService.Companion.SyncStatus.Locked -> null
			is SyncerService.Companion.SyncStatus.Connected -> null
			is SyncerService.Companion.SyncStatus.Disconnected -> null
			is SyncerService.Companion.SyncStatus.Syncing -> null
			is SyncerService.Companion.SyncStatus.Paused -> null
			is SyncerService.Companion.SyncStatus.Failed -> null
		}
	}

	private suspend fun checkLock(dbxClientV2 : DbxClientV2, callback : suspend (SyncerService.Companion.DriveState) -> Unit) {
		try {
			dbxClientV2
				.files()
				.download(Path.Lock.path)
				.inputStream.use { inputStream ->
					val jsonObject = JSONObject(inputStream.bufferedReader().readText())
					val timestamp = jsonObject.getLong("timestamp")
					inputStream.close()
					if ((System.currentTimeMillis() - timestamp) > 60000) callback(SyncerService.Companion.DriveState.LockExpired)
					else callback(SyncerService.Companion.DriveState.Locked)
				}
		} catch (e : DownloadErrorException) {
			if (e.errorValue.pathValue.isNotFound) callback(SyncerService.Companion.DriveState.Available)
			else callback(SyncerService.Companion.DriveState.Unknown(exception = e))
		} catch (e : Exception) {
			callback(SyncerService.Companion.DriveState.Unknown(exception = e))
		}
	}

	var lockCoroutineScope : CoroutineScope? = null
	fun lock(dbxClientV2 : DbxClientV2, sessionId : String) {
		lifecycleScope.launch(Dispatchers.IO) {
			lockCoroutineScope?.cancel()
			lockCoroutineScope = this

			while (true) {
				val timestamp = System.currentTimeMillis()
				val jsonObject = JSONObject()
				jsonObject.put("sessionId", sessionId)
				jsonObject.put("timestamp", timestamp)

				try {
					dbxClientV2
						.files()
						.uploadBuilder(Path.Lock.path)
						.withMode(WriteMode.OVERWRITE)
						.uploadAndFinish(jsonObject.toString().byteInputStream())
				} catch (e : NetworkIOException) {
				}
				delay(30000)
			}
		}
	}

	private suspend fun unlock(dbxClientV2 : DbxClientV2, sessionId : String, callback : suspend (SyncerService.Companion.UnlockResult) -> Unit) {
		lockCoroutineScope?.cancel()

		dbxClientV2.download(Path.Lock.path) {
			when (it) {
				is SyncerService.Companion.DownloadResult.Success -> {
					val jsonObject = JSONObject(it.byteArray.toString(Charsets.UTF_8))
					val timestamp = jsonObject.getLong("timestamp")
					val remoteSessionId = jsonObject.getString("sessionId")

					if (remoteSessionId == sessionId && (System.currentTimeMillis() - timestamp) < 60000) {
						try {
							dbxClientV2.files().deleteV2(Path.Lock.path)
							callback(SyncerService.Companion.UnlockResult.Success)
						} catch (e : DownloadErrorException) {
							if (e.errorValue.pathValue.isNotFound) callback(SyncerService.Companion.UnlockResult.Success)
							else callback(SyncerService.Companion.UnlockResult.Failed)
						} catch (e : Exception) {
							callback(SyncerService.Companion.UnlockResult.Failed)
						}
					} else callback(SyncerService.Companion.UnlockResult.LockExpired)
				}

				is SyncerService.Companion.DownloadResult.NetworkError -> {}
				is SyncerService.Companion.DownloadResult.UnknownError -> {}
			}
		}
	}

	private suspend fun sync(
		dbxClientV2 : DbxClientV2,
		rectify : Boolean,
	) {
		val baseObject = repository.getBaseObject()
		val noteObjectList = repository.getAllNote()
		val chapterObjectList = repository.getAllChapter()
		val bucketItemObjectList = repository.getAllBucketItem()
		val bucketObjectList = repository.getAllBucket()
		val tagObjectList = repository.getAllTag()

		val localDeletedObjectIdList = baseObject?.deletedObjectSet ?: setOf()

		val chapterMetadataPath = "${Path.Root.path}/${ChapterObject::class.simpleName}"
		dbxClientV2.calculateDiffAndSync(
			metadataPath = chapterMetadataPath,
			localObjectList = chapterObjectList.associateBy { it.id },
			localDeletedLocalObjectList = localDeletedObjectIdList,
			modifiedTimestamp = ChapterObject::modifiedTimestamp,
			rectify = rectify,
		)

		val noteMetadataPath = "${Path.Root.path}/${NoteObject::class.simpleName}"
		dbxClientV2.calculateDiffAndSync(
			metadataPath = noteMetadataPath,
			localObjectList = noteObjectList.associateBy { it.id },
			localDeletedLocalObjectList = localDeletedObjectIdList,
			modifiedTimestamp = NoteObject::modifiedTimestamp,
			rectify = rectify,
		)

		val bucketMetadataPath = "${Path.Root.path}/${BucketObject::class.simpleName}"
		dbxClientV2.calculateDiffAndSync(
			metadataPath = bucketMetadataPath,
			localObjectList = bucketObjectList.associateBy { it.id },
			localDeletedLocalObjectList = localDeletedObjectIdList,
			modifiedTimestamp = BucketObject::modifiedTimestamp,
			rectify = rectify,
		)

		val bucketItemMetadataPath = "${Path.Root.path}/${BucketItemObject::class.simpleName}"
		dbxClientV2.calculateDiffAndSync(
			metadataPath = bucketItemMetadataPath,
			localObjectList = bucketItemObjectList.associateBy { it.id },
			localDeletedLocalObjectList = localDeletedObjectIdList,
			modifiedTimestamp = BucketItemObject::modifiedTimestamp,
			rectify = rectify,
		)

		val tagMetadataPath = "${Path.Root.path}/${TagObject::class.simpleName}"
		dbxClientV2.calculateDiffAndSync(
			metadataPath = tagMetadataPath,
			localObjectList = tagObjectList.associateBy { it.id },
			localDeletedLocalObjectList = localDeletedObjectIdList,
			modifiedTimestamp = TagObject::modifiedTimestamp,
			rectify = rectify,
		)

		val baseObjectMetadataPath = "${Path.Root.path}/${BaseObject::class.simpleName}"
		dbxClientV2.syncBaseObject(
			metadataPath = baseObjectMetadataPath,
			localBaseObject = baseObject,
		)
	}

	private inline suspend fun <reified T> DbxClientV2.calculateDiffAndSync(
		metadataPath : String,
		localObjectList : Map<RealmUUID, T>,
		localDeletedLocalObjectList : Set<DeletedObject>,
		modifiedTimestamp : KMutableProperty1<T, Long>,
		rectify : Boolean
	) {
		downloadMetadata(metadataPath, rectify) { remoteObjectList ->
			val toDownSyncObjectIdList = mutableMapOf<RealmUUID, SyncerService.Companion.Operation>()
			val toUpSyncObjectIdList = mutableMapOf<RealmUUID, SyncerService.Companion.Operation>()

			val leftObjectIdList = localObjectList.mapValues {
				SyncerService.Companion.ObjectMetadata(
					modifiedTimestamp = modifiedTimestamp.get(it.value),
					hash = it.value.let {
						when (it) {
							is ChapterObject -> it.toCloudSnapshot().toDbxHashString()
							is NoteObject -> it.toCloudSnapshot().toDbxHashString()
							is BucketObject -> it.toCloudSnapshot().toDbxHashString()
							is BucketItemObject -> it.toCloudSnapshot().toDbxHashString()
							else -> ""
						}
					},
					isDeleted = false,
				)
			}.toMutableMap()
			localDeletedLocalObjectList.filter { it.objectType == T::class.java.simpleName }.forEach {
				leftObjectIdList[it.id] = SyncerService.Companion.ObjectMetadata(modifiedTimestamp = it.deletedTimestamp, hash = "", isDeleted = true)
			}
			val rightObjectIdList = remoteObjectList.toMap()
			val diff = Maps.difference(leftObjectIdList, rightObjectIdList)
			diff.entriesOnlyOnLeft().forEach { (id, objectMetadata) ->
				if (objectMetadata.isDeleted) toUpSyncObjectIdList[id] =
					localDeletedLocalObjectList.find { it.id == id }?.let { SyncerService.Companion.Operation.Delete(it.objectType) }
						?: SyncerService.Companion.Operation.Delete()
				else toUpSyncObjectIdList[id] = SyncerService.Companion.Operation.Create
			}
			diff.entriesOnlyOnRight().forEach { (id, objectMetadata) ->
				if (objectMetadata.isDeleted) toDownSyncObjectIdList[id] = SyncerService.Companion.Operation.Delete()
				else toDownSyncObjectIdList[id] = SyncerService.Companion.Operation.Create
			}
			diff.entriesDiffering().forEach { (id, difference) ->
				val leftValue = difference.leftValue()
				val rightValue = difference.rightValue()
				if (leftValue.hash != rightValue.hash) {
					when {
						leftValue.modifiedTimestamp == rightValue.modifiedTimestamp -> null
						leftValue.modifiedTimestamp > rightValue.modifiedTimestamp && leftValue.isDeleted -> {
							localDeletedLocalObjectList.find { it.id == id }?.let { SyncerService.Companion.Operation.Delete(it.objectType) }
								?.let { toUpSyncObjectIdList[id] = it }
						}

						leftValue.modifiedTimestamp > rightValue.modifiedTimestamp -> toUpSyncObjectIdList[id] = SyncerService.Companion.Operation.Update
						rightValue.isDeleted -> toDownSyncObjectIdList[id] = SyncerService.Companion.Operation.Delete()
						else -> toDownSyncObjectIdList[id] = SyncerService.Companion.Operation.Update
					}
				}
			}

			toDownSyncObjectIdList.filter { it.value !is SyncerService.Companion.Operation.Delete }.forEach { downSync<T>(it.key, it.value) }
			toDownSyncObjectIdList.filter { it.value is SyncerService.Companion.Operation.Delete }
				.let { repository.deleteSuspended(idList = it.keys, keepHistory = false) }
			toUpSyncObjectIdList.forEach { upSync(it.key, localObjectList[it.key], it.value) }

			updateMetadata(
				localObjectList = localObjectList,
				remoteObjectList = remoteObjectList,
				deletedLocalObjectList = localDeletedLocalObjectList,
				modifiedTimestamp = modifiedTimestamp,
			)
		}
	}

	private suspend inline fun <reified T> DbxClientV2.downSync(id : RealmUUID, operation : SyncerService.Companion.Operation) {
		if (operation is SyncerService.Companion.Operation.Update || operation is SyncerService.Companion.Operation.Create) {
			download("${Path.Root.path}/${T::class.simpleName}/${id}.json") { operationResult ->
				when (operationResult) {
					is SyncerService.Companion.DownloadResult.Success -> {
						when (T::class) {
							ChapterObject::class -> ChapterObject.fromCloudSnapshot(operationResult.byteArray)
								?.let { repository.putChapter(it, modifyTimestampAuto = false) }

							NoteObject::class -> NoteObject.fromCloudSnapshot(operationResult.byteArray)
								?.let { repository.putNote(it, modifyTimestampAuto = false) }

							BucketObject::class -> BucketObject.fromCloudSnapshot(operationResult.byteArray)
								?.let { repository.putBucket(it, modifyTimestampAuto = false) }

							BucketItemObject::class -> BucketItemObject.fromCloudSnapshot(operationResult.byteArray)
								?.let { bucketItemObject ->
									try {
										when(val bucketItemData = bucketItemObject.getData()) {
											is BucketItemObject.Companion.BucketItemData.BookData -> {
												bucketItemData.coverI?.let { coverI ->
													bucketItemObject.thumbnail = OpenLibraryApi.retrieveBookCover(coverI)?.encodeBase64()
												}
											}

											is BucketItemObject.Companion.BucketItemData.ShowData -> {
												bucketItemData.posterPath()?.let { posterPath ->
													bucketItemObject.thumbnail = TMDbApi.retrieveShowPoster(posterPath)?.encodeBase64()
												}
											}

											else -> null
										}
									} catch (e : Exception) {
									}
									repository.putBucketItem(bucketItemObject, modifyTimestampAuto = false)
								}

							else -> null
						}
					}

					is SyncerService.Companion.DownloadResult.NetworkError -> null  // Skip it. It will be downloaded next time.
					is SyncerService.Companion.DownloadResult.UnknownError -> null // Skip it. It will be downloaded next time.
				}
			}
		} else if (operation is SyncerService.Companion.Operation.Delete) {
			repository.deleteSuspended(id = id, keepHistory = false)
		}
	}

	private inline fun <reified T> DbxClientV2.upSync(
		id : RealmUUID,
		localObject : T?,
		operation : SyncerService.Companion.Operation,
		noinline callback : (SyncerService.Companion.UpSyncResult) -> Unit = {},
	) {
		val cloudSnapshot = when (localObject) {
			is ChapterObject -> localObject.toCloudSnapshot()
			is NoteObject -> localObject.toCloudSnapshot()
			is BucketObject -> localObject.toCloudSnapshot()
			is BucketItemObject -> localObject.toCloudSnapshot()

			else -> null
		}?.byteInputStream()

		cloudSnapshot?.let {
			when (operation) {
				is SyncerService.Companion.Operation.Create -> upload("${Path.Root.path}/${T::class.simpleName}/${id}.json", it, callback)
				is SyncerService.Companion.Operation.Update -> upload("${Path.Root.path}/${T::class.simpleName}/${id}.json", it, callback)
				is SyncerService.Companion.Operation.Delete -> {
					try {
						files().deleteV2("${Path.Root.path}/${operation.objectType}/${id}.json")
						callback(SyncerService.Companion.UpSyncResult.Success)
					} catch (_ : DeleteErrorException) {
					} catch (e : NetworkIOException) {
						onNetworkError()
					} catch (e : Exception) {
						onUnknownError()
					}
				}
			}
		}
	}

	private suspend fun DbxClientV2.syncBaseObject(
		metadataPath : String,
		localBaseObject : BaseObject?,
	) {
		try {
			download("${Path.Root.path}/Attachment/metadata.json") { operationResult ->
				when (operationResult) {
					is SyncerService.Companion.DownloadResult.Success -> {
						val localDeletedAttachmentSet = localBaseObject?.deletedAttachmentSet ?: setOf()
						val localSavedAttachmentList = repository.attachmentRepository
							.getAttachmentDir()
							.listFiles()
							?.map { attachmentDir ->
								attachmentDir.listFiles()
									?.map {
										SyncerService.Companion.AttachmentMetadata(
											parentId = RealmUUID.Companion.from(attachmentDir.name),
											name = it.name,
											isDeleted = false
										)
									} ?: listOf()
							}?.flatten() ?: listOf()

						val remoteAttachmentSet = objectMapper.readValue(
							operationResult.byteArray,
							object : TypeReference<MutableSet<SyncerService.Companion.AttachmentMetadata>>() {})

//			    	    Delete local attachment
						remoteAttachmentSet.filter { it.isDeleted }.forEach {
							repository.attachmentRepository.delete(parentId = it.parentId, name = it.name)
						}

//				        Download attachment
						remoteAttachmentSet.filter { ! it.isDeleted && it !in localSavedAttachmentList }.forEach { attachmentMetadata ->
							download("${Path.Root.path}/Attachment/${attachmentMetadata.parentId}/${attachmentMetadata.name}") { operationResult1 ->
								when (operationResult1) {
									is SyncerService.Companion.DownloadResult.Success -> repository.attachmentRepository.putAttachment(
										parentId = attachmentMetadata.parentId,
										name = attachmentMetadata.name,
										byteArray = operationResult1.byteArray
									)

									is SyncerService.Companion.DownloadResult.NetworkError -> null
									is SyncerService.Companion.DownloadResult.UnknownError -> null
								}
							}
						}

						// Delete remote attachment
						localDeletedAttachmentSet.forEach {
							try {
								files().deleteV2("${Path.Root.path}/Attachment/${it.parentId}/${it.name}")
							} catch (e : Exception) {
							}
						}

						// Upload attachment
						localSavedAttachmentList.filter { it !in remoteAttachmentSet }.forEach {
							repository.attachmentRepository.getAttachment(parentId = it.parentId, name = it.name)?.let { file ->
								upload("${Path.Root.path}/Attachment/${it.parentId}/${it.name}", file.inputStream()) {}
							}
						}

						// Upload metadata
						remoteAttachmentSet.addAll(localSavedAttachmentList)
						localDeletedAttachmentSet.map {
							SyncerService.Companion.AttachmentMetadata(parentId = it.parentId, name = it.name, isDeleted = true)
						}.let { remoteAttachmentSet.addAll(it) }

						objectMapper.writeValueAsBytes(remoteAttachmentSet).inputStream().let {
							upload("${Path.Root.path}/Attachment/metadata.json", it) {}
						}
					}

					is SyncerService.Companion.DownloadResult.NetworkError -> null
					is SyncerService.Companion.DownloadResult.UnknownError -> null
				}
			}
		} catch (e : DownloadErrorException) {
			if (e.errorValue.pathValue.isNotFound) {
				val attachmentList = mutableListOf<SyncerService.Companion.AttachmentMetadata>()
				repository.attachmentRepository
					.getAttachmentDir()
					.listFiles()
					?.forEach { attachmentDir ->
						attachmentDir
							.listFiles()
							?.forEach {
								upload("${Path.Root.path}/Attachment/${attachmentDir.name}/${it.name}", it.inputStream()) {}
								SyncerService.Companion.AttachmentMetadata(
									parentId = RealmUUID.Companion.from(attachmentDir.name),
									name = it.name,
									isDeleted = false
								).let { attachmentList.add(it) }
							}
					}
				objectMapper.writeValueAsBytes(attachmentList).inputStream().let {
					upload("${Path.Root.path}/Attachment/metadata.json", it) {}
				}
			}
		} catch (e : Exception) {
//			dropboxSyncStatus.tryEmit(DropboxSyncStatus.Failed)
		}
	}

	private suspend fun DbxClientV2.downloadMetadata(
		path : String,
		rectify : Boolean,
		callback : suspend (Map<RealmUUID, SyncerService.Companion.ObjectMetadata>) -> Unit
	) = if (rectify) downloadRectifyMetadata(path, callback) else downloadNormalMetadata(path, callback)

	private suspend fun DbxClientV2.downloadRectifyMetadata(
		path : String,
		callback : suspend (Map<RealmUUID, SyncerService.Companion.ObjectMetadata>) -> Unit
	) {
		val filePathList = mutableListOf<String>()
		var cursor : String? = null
		var hasMore = true
		try {
			files()
				.listFolder(path)
				.let { listFolderResult ->
					cursor = listFolderResult.cursor
					hasMore = listFolderResult.hasMore
					filePathList.addAll(listFolderResult.entries.filter { it.name != "metadata.json" }.map { it.pathLower })
				}
			while (hasMore) {
				files()
					.listFolderContinue(cursor)
					.let { listFolderResult ->
						cursor = listFolderResult.cursor
						hasMore = listFolderResult.hasMore
						filePathList.addAll(listFolderResult.entries.filter { it.name != "metadata.json" }.map { it.pathLower })
					}
			}
		} catch (e : NetworkIOException) {
			onNetworkError()
		} catch (e : Exception) {
			onUnknownError()
		}
		val metadataMap = mutableMapOf<RealmUUID, SyncerService.Companion.ObjectMetadata>()
		filePathList.forEach { filePath ->
			try {
				files().download(filePath).result.let { fileMetadata ->
					SyncerService.Companion.ObjectMetadata(
						modifiedTimestamp = fileMetadata.clientModified.time,
						hash = fileMetadata.contentHash,
						isDeleted = false
					).let { metadataMap[RealmUUID.Companion.from(fileMetadata.name.substringBeforeLast("."))] = it }
				}
			} catch (e : DownloadErrorException) {
				if (! e.errorValue.pathValue.isNotFound) onUnknownError()
			} catch (e : NetworkIOException) {
				onNetworkError()
			} catch (e : Exception) {
				onUnknownError()
			}
		}
		callback(metadataMap)
	}

	private suspend fun DbxClientV2.downloadNormalMetadata(
		path : String,
		callback : suspend (Map<RealmUUID, SyncerService.Companion.ObjectMetadata>) -> Unit
	) {
		try {
			files()
				.download("$path/metadata.json")
				.inputStream
				.readBytes()
				.let { objectMapper.readValue<Map<RealmUUID, SyncerService.Companion.ObjectMetadata>>(it) }
				.let { callback(it) }
		} catch (e : DownloadErrorException) {
			if (e.errorValue.pathValue.isNotFound) callback(mapOf()) else onUnknownError()
		} catch (e : NetworkIOException) {
			onNetworkError()
		} catch (e : Exception) {
			onUnknownError()
		}
	}

	@WorkerThread
	private inline fun <reified T> DbxClientV2.updateMetadata(
		localObjectList : Map<RealmUUID, T>,
		remoteObjectList : Map<RealmUUID, SyncerService.Companion.ObjectMetadata>,
		deletedLocalObjectList : Set<DeletedObject>,
		modifiedTimestamp : KMutableProperty1<T, Long>,
	) {
		val metadata : MutableMap<RealmUUID, SyncerService.Companion.ObjectMetadata> = mutableMapOf()

		remoteObjectList.forEach { (id, dropboxInnerMetadata) ->
			metadata[id] = dropboxInnerMetadata
		}
		localObjectList.forEach { (id, realmObject) ->
			when (realmObject) {
				is NoteObject -> realmObject.toCloudSnapshot()
				is ChapterObject -> realmObject.toCloudSnapshot()
				is BucketObject -> realmObject.toCloudSnapshot()
				is BucketItemObject -> realmObject.toCloudSnapshot()
				else -> return@forEach
			}.let {
				val hash = it.toDbxHashString()
				val modifiedTimestamp = modifiedTimestamp.get(realmObject)
				metadata[id] = SyncerService.Companion.ObjectMetadata(
					hash = hash,
					modifiedTimestamp = modifiedTimestamp,
					isDeleted = false,
				)
			}
		}
		deletedLocalObjectList.forEach { deletedObject ->
			metadata[deletedObject.id] = SyncerService.Companion.ObjectMetadata(modifiedTimestamp = deletedObject.deletedTimestamp, hash = "", isDeleted = true)
		}

		val metadataPath = "${Path.Root.path}/${T::class.simpleName}/metadata.json"
		upload(metadataPath, objectMapper.writeValueAsBytes(metadata).inputStream()) {
			when (it) {
				SyncerService.Companion.UpSyncResult.Success -> null
				SyncerService.Companion.UpSyncResult.NetworkError -> onNetworkError()
				SyncerService.Companion.UpSyncResult.UnknownError -> onUnknownError()
			}
		}
	}

	private fun DbxClientV2.upload(path : String, inputStream : InputStream, callback : (SyncerService.Companion.UpSyncResult) -> Unit) {
		try {
			files()
				.uploadBuilder(path)
				.withMode(WriteMode.OVERWRITE)
				.uploadAndFinish(inputStream)
			callback(SyncerService.Companion.UpSyncResult.Success)
		} catch (e : NetworkIOException) {
			callback(SyncerService.Companion.UpSyncResult.NetworkError)
		} catch (e : Exception) {
			callback(SyncerService.Companion.UpSyncResult.UnknownError)
		}
	}

	private suspend fun DbxClientV2.download(path : String, callback : suspend (SyncerService.Companion.DownloadResult) -> Unit) {
		return try {
			files()
				.download(path)
				.inputStream
				.readBytes()
				.let { callback(SyncerService.Companion.DownloadResult.Success(it)) }
		} catch (e : NetworkIOException) {
			callback(SyncerService.Companion.DownloadResult.NetworkError)
		} catch (e : Exception) {
			callback(SyncerService.Companion.DownloadResult.UnknownError)
		}
	}

	private fun onNetworkError() {
		lockCoroutineScope?.cancel()
		syncCoroutineScope?.cancel()
	}

	private fun onUnknownError() {
		lockCoroutineScope?.cancel()
		syncCoroutineScope?.cancel()
	}

	inner class DropboxServiceBinder : Binder() {
		val service : DropboxService
			get() = this@DropboxService
	}

	companion object {
		enum class Path(val path : String) {
			Root("/sync"),
			DeviceMetadata("/sync/device.metadata"),
			Lock("/sync/graphite.lock"),
			NoteMetadata("/sync/graphite.metadata"),
		}
	}
}

class DropboxServiceConnectionManager(private val context : Context, private val onBound : (DropboxService) -> Unit) : ServiceConnection {
	var service : DropboxService? = null
	private var attemptingToBind = false
	private var bound = false

	init {
		bindToService()
	}

	private fun bindToService() {
		Log.i("npr71", "dropboxServiceBinder: bindToService")
		if (! attemptingToBind) {
			attemptingToBind = true
			Intent(context, DropboxService::class.java).let { intent ->
				context.startService(intent)
				context.bindService(intent, this, Context.BIND_AUTO_CREATE)
			}
		}
	}

	override fun onServiceConnected(componentName : ComponentName, iBinder : IBinder) {
		attemptingToBind = false
		bound = true
		(iBinder as DropboxService.DropboxServiceBinder).service.let {
			service = it
			onBound(it)
		}
	}

	override fun onServiceDisconnected(componentName : ComponentName) {
		bound = false
	}

	fun unbindFromService() {
		Log.i("npr71", "dropboxServiceBinder: unbindFromService")
		attemptingToBind = false
		if (bound) {
			service?.isUnbounded = true
			context.unbindService(this)
			bound = false
		}
	}
}
