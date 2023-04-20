package com.syncodec.graphite.service.syncService

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SyncStats
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.NetworkIOException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.DeleteErrorException
import com.dropbox.core.v2.files.DownloadErrorException
import com.dropbox.core.v2.files.ListFolderContinueErrorException
import com.dropbox.core.v2.files.ListFolderErrorException
import com.dropbox.core.v2.files.UploadErrorException
import com.dropbox.core.v2.files.WriteMode
import com.google.common.collect.MapDifference
import com.google.common.collect.Maps
import com.syncodec.graphite.di.model.BaseObject
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.DeletedAttachment
import com.syncodec.graphite.di.model.DeletedObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.model.serializer.RealmUUIDSerializer
import com.syncodec.graphite.di.sync.dropbox.DBox
import com.syncodec.graphite.service.SyncerService
import com.syncodec.graphite.utils.toDbxHashString
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.io.InputStream
import java.util.Date


class DropboxService : SyncerService() {

	private val json = Json { ignoreUnknownKeys = true }

	private var dropboxServiceBinder : IBinder? = DropboxServiceBinder()

	var isUnbounded = false

	override fun onCreate() {
		super.onCreate()

		lifecycleScope.launch(Dispatchers.Default) {
			while (true) {
				delay(1000)
				if (isUnbounded) {
					stopper()
					break
				}
			}
		}

		onClickSyncNow()
	}

	private fun stopper() {
		lifecycleScope.launch(Dispatchers.Default) {
			while (true) {
				delay(1000)
				if (
					syncStatus.value is SyncerService.Companion.SyncStatus.Init ||
					syncStatus.value is SyncerService.Companion.SyncStatus.Idle ||
					syncStatus.value is SyncerService.Companion.SyncStatus.Locked ||
					syncStatus.value is SyncerService.Companion.SyncStatus.Paused ||
					syncStatus.value is SyncerService.Companion.SyncStatus.Failed ||
					syncStatus.value is SyncerService.Companion.SyncStatus.CredentialError
				) {
					break
				}
			}
			dropboxServiceBinder = null
			stopSelf()
		}
	}


	init {
		lifecycleScope.launch(Dispatchers.IO) {
			syncStatus.collect {
				Log.i("npr71", "DropboxService: syncStatus: $it")
			}
		}
	}

	override fun onBind(intent : Intent) : IBinder? {
		super.onBind(intent)
		Toast.makeText(this, "service bind", Toast.LENGTH_SHORT).show()
		return dropboxServiceBinder
	}

	val dBox : DBox by inject()

	fun onClickSyncNow() {
		lifecycleScope.launch(Dispatchers.IO) {
			val isSyncEnabled = dataStoreInstance.isSyncEnabled.first()
			if (!isSyncEnabled) syncStatus.tryEmit(SyncerService.Companion.SyncStatus.Paused)
			when (syncStatus.value) {
				is SyncerService.Companion.SyncStatus.Init -> sync()
				is SyncerService.Companion.SyncStatus.Idle -> {
					reSyncCoroutine?.cancel()
					reSyncCoroutine = null
					sync()
				}

				is SyncerService.Companion.SyncStatus.Locked -> {
					reSyncCoroutine?.cancel()
					reSyncCoroutine = null
					syncCoroutine?.cancel()
					syncCoroutine = null
					sync()
				}

				is SyncerService.Companion.SyncStatus.Connected -> null
				is SyncerService.Companion.SyncStatus.Syncing -> null
				is SyncerService.Companion.SyncStatus.Paused -> null
				is SyncerService.Companion.SyncStatus.Failed -> sync()
				is SyncerService.Companion.SyncStatus.CredentialError -> null
			}
		}
	}

	fun onClickForceSync() {
		lifecycleScope.launch(Dispatchers.IO) {
			val isSyncEnabled = dataStoreInstance.isSyncEnabled.first()
			if (!isSyncEnabled) syncStatus.tryEmit(SyncerService.Companion.SyncStatus.Paused)
			else {
				reSyncCoroutine?.cancel()
				reSyncCoroutine = null
				syncCoroutine?.cancel()
				syncCoroutine = null
				sync(forced = true)
			}
		}
	}

	private fun getDbxClient(callback : (DbxClientV2?) -> Unit) {
		val dbxRequestConfig = DbxRequestConfig.newBuilder("graphite").build()
		dBox.getAccessToken {
			if (it is DBox.Companion.AccessTokenResponseResponse.Success) callback(DbxClientV2(dbxRequestConfig, it.accessToken))
			else callback(null)
		}
	}

	private var syncCoroutine : CoroutineScope? = null
	private val previousSyncSessionIdList = mutableSetOf<String>()
	private fun sync(forced : Boolean = false) {
		if (syncCoroutine == null) {
			lifecycleScope.launch(Dispatchers.IO) {
				syncCoroutine = this
				getDbxClient { dbxClient ->
					dbxClient?.tryHoldLockAndContinue(forced = forced) ?: syncStatus.tryEmit(SyncerService.Companion.SyncStatus.CredentialError)
				}
			}
		}
	}

	private fun DbxClientV2.tryHoldLockAndContinue(forced : Boolean = false) {
		Log.i("npr71", "DropboxService: tryHoldLockAndContinue forced: $forced")
		syncStatus.tryEmit(SyncerService.Companion.SyncStatus.Connected)
		try {
			files()
				.download(Path.Lock.path)
				.inputStream.use { inputStream ->
					val jsonObject = JSONObject(inputStream.bufferedReader().readText())
					val timestamp = jsonObject.getLong("timestamp")
					val sessionId = jsonObject.getString("sessionId")
					inputStream.close()
					when {
						forced -> breakLockAndContinue()
						sessionId in previousSyncSessionIdList -> lockAndContinue(rectify = false)
						(System.currentTimeMillis() - timestamp) > 60000 -> breakLockAndContinue()
						else -> waitToUnlock()
					}
				}
		} catch (e : DownloadErrorException) {
			if (e.errorValue.pathValue.isNotFound) lockAndContinue(rectify = false)
			else unknownError()
		} catch (e : Exception) {
			unknownError()
		}
	}

	private fun DbxClientV2.networkError() {
		syncStatus.tryEmit(SyncerService.Companion.SyncStatus.Failed("network error"))
		syncCoroutine?.cancel()
		syncCoroutine = null
		reSyncCoroutine?.cancel()
		reSyncCoroutine = null
	}

	private fun DbxClientV2.unknownError(exception : Exception? = null) {
		syncStatus.tryEmit(SyncerService.Companion.SyncStatus.Failed(exception?.message ?: "unknown error"))
		syncCoroutine?.cancel()
		syncCoroutine = null
		reSyncCoroutine?.cancel()
		reSyncCoroutine = null
	}

	private fun DbxClientV2.waitToUnlock() {
		syncStatus.tryEmit(SyncerService.Companion.SyncStatus.Locked)
		syncCoroutine?.launch(Dispatchers.IO) {
			delay(30000)
			tryHoldLockAndContinue()
		}
	}

	private fun DbxClientV2.lockAndContinue(rectify : Boolean) {
		val sessionId = RealmUUID.random().toString()
		previousSyncSessionIdList.add(sessionId)
		syncStatus.tryEmit(SyncerService.Companion.SyncStatus.Syncing(sessionId))
		keepLockAlive(sessionId)
		gatherData(rectify = rectify)
		unlock(sessionId = sessionId)
		syncStatus.tryEmit(SyncerService.Companion.SyncStatus.Idle(syncedTimestamp = System.currentTimeMillis()))
		reSync()
	}

	private fun DbxClientV2.breakLockAndContinue() {
		Log.i("npr71", "DropboxService: breakLockAndContinue")
		unlock(sessionId = null)
		lockAndContinue(rectify = true)
	}

	private var reSyncCoroutine : CoroutineScope? = null
	private fun reSync() {
		Log.i("npr71", "DropboxService: reSync")
		syncCoroutine?.cancel()
		syncCoroutine = null
		reSyncCoroutine?.cancel()
		lifecycleScope.launch(Dispatchers.IO) {
			val isSyncEnabled = dataStoreInstance.isSyncEnabled.first()
			if (!isSyncEnabled) syncStatus.tryEmit(SyncerService.Companion.SyncStatus.Paused)
			else {
				reSyncCoroutine = this
				delay(30000)
				sync()
			}
		}
	}

	private var keepLockAliveCoroutine : CoroutineScope? = null
	private fun DbxClientV2.keepLockAlive(sessionId : String) {
		lifecycleScope.launch(Dispatchers.IO) {
			keepLockAliveCoroutine?.cancel()
			keepLockAliveCoroutine = this
			while (true) {
				val jsonObject = JSONObject()
				jsonObject.put("timestamp", System.currentTimeMillis())
				jsonObject.put("sessionId", sessionId)

				try {
					when (val downloadResult = downloadData(Path.Lock.path)) {
						is DownloadResult.Success.ByteArray -> {
							val remoteJsonObject = JSONObject(String(downloadResult.data, Charsets.UTF_8))
							val remoteSessionId = remoteJsonObject.getString("sessionId")
							if (remoteSessionId !in previousSyncSessionIdList) {
								keepLockAliveCoroutine?.cancel()
								keepLockAliveCoroutine = null
								syncCoroutine?.cancel()
								syncCoroutine = null
							}
						}

						is DownloadResult.Success.MetadataMap<*> -> {} // Won't happen
						is DownloadResult.NotFound -> null
						is DownloadResult.DownloadErrorException -> null
						is DownloadResult.NetworkError -> networkError()
						is DownloadResult.UnknownError -> unknownError()
					}

					files()
						.uploadBuilder(Path.Lock.path)
						.withMode(WriteMode.OVERWRITE)
						.uploadAndFinish(jsonObject.toString().byteInputStream())
				} catch (e : NetworkIOException) {
					networkError()
					break
				} catch (e : Exception) {
					unknownError()
					break
				}
				delay(30000)
			}
		}
	}

	private fun DbxClientV2.unlock(sessionId : String?) {
		keepLockAliveCoroutine?.cancel()
		when (val lockDownloadResult = downloadData(Path.Lock.path)) {
			is DownloadResult.Success.ByteArray -> {
				val jsonObject = JSONObject(String(lockDownloadResult.data, Charsets.UTF_8))
				val remoteSessionId = jsonObject.getString("sessionId")
				if (remoteSessionId == sessionId || sessionId == null) {
					try {
						files().deleteV2(Path.Lock.path)
					} catch (e : Exception) {
						unknownError()
					}
				}
			}

			is DownloadResult.Success.MetadataMap -> null // Won't happen
			is DownloadResult.NotFound -> Log.i("npr71", "DropboxService: unlock: lock not found")
			is DownloadResult.DownloadErrorException -> null
			is DownloadResult.NetworkError -> null
			is DownloadResult.UnknownError -> null
		}
	}

	private fun DbxClientV2.gatherData(rectify : Boolean) {
		Log.i("npr71", "DropboxService: gatherData, rectify : $rectify")
		val baseObject = repository.getBaseObject()
		val noteObjectList = repository.getAllNote()
		val chapterObjectList = repository.getAllChapter()
		val bucketItemObjectList = repository.getAllBucketItem()
		val bucketObjectList = repository.getAllBucket()
		val tagObjectList = repository.getAllTag()

		val localDeletedObjectIdList = baseObject?.deletedObjectSet ?: setOf()

		val (chapterMetadata, noteMetadata) = syncNotes(
			noteObjectList = noteObjectList,
			chapterObjectList = chapterObjectList,
			localDeletedObjectIdList = localDeletedObjectIdList,
			rectify = rectify,
		)

		val (bucketMetadata, bucketItemMetadata) = syncBuckets(
			bucketObjectList = bucketObjectList,
			bucketItemObjectList = bucketItemObjectList,
			localDeletedObjectIdList = localDeletedObjectIdList,
			rectify = rectify,
		)

		val tagMetadataPath = "${Path.Root.path}/${TagObject::class.simpleName}"
		calcDataDiff(
			path = tagMetadataPath,
			localObjectList = tagObjectList.associateBy { it.id },
			localDeletedObjectList = localDeletedObjectIdList,
			modifiedTimestampList = tagObjectList.associate { it.id to it.modifiedTimestamp },
			rectify = rectify,
		)

		syncAttachment(
			deletedAttachmentList = baseObject?.deletedAttachmentSet ?: setOf(),
			deletedNoteList = noteMetadata.filter { it.value.isDeleted }.map { it.key },
			rectify = rectify,
		)
		syncBaseObject(localBaseObject = baseObject)
	}

	private fun DbxClientV2.syncNotes(
		noteObjectList : List<NoteObject>,
		chapterObjectList : List<ChapterObject>,
		localDeletedObjectIdList : Set<DeletedObject>,
		rectify : Boolean,
	) : Pair<Map<RealmUUID, ObjectMetadata>, Map<RealmUUID, ObjectMetadata>> {
		val chapterMetadataPath = "${Path.Root.path}/${ChapterObject::class.simpleName}"
		val chapterMetadata = calcDataDiff(
			path = chapterMetadataPath,
			localObjectList = chapterObjectList.associateBy { it.id },
			localDeletedObjectList = localDeletedObjectIdList,
			modifiedTimestampList = chapterObjectList.associate { it.id to it.modifiedTimestamp },
			rectify = rectify,
		)

		val noteMetadataPath = "${Path.Root.path}/${NoteObject::class.simpleName}"
		val noteMetadata = calcDataDiff(
			path = noteMetadataPath,
			localObjectList = noteObjectList.associateBy { it.id },
			localDeletedObjectList = localDeletedObjectIdList,
			modifiedTimestampList = noteObjectList.associate { it.id to it.modifiedTimestamp },
			rectify = rectify,
		)

		return Pair(chapterMetadata, noteMetadata)
	}

	private fun DbxClientV2.syncBuckets(
		bucketObjectList : List<BucketObject>,
		bucketItemObjectList : List<BucketItemObject>,
		localDeletedObjectIdList : Set<DeletedObject>,
		rectify : Boolean,
	) : Pair<Map<RealmUUID, ObjectMetadata>, Map<RealmUUID, ObjectMetadata>> {
		val bucketMetadataPath = "${Path.Root.path}/${BucketObject::class.simpleName}"
		val bucketMetadata = calcDataDiff(
			path = bucketMetadataPath,
			localObjectList = bucketObjectList.associateBy { it.id },
			localDeletedObjectList = localDeletedObjectIdList,
			modifiedTimestampList = bucketObjectList.associate { it.id to it.modifiedTimestamp },
			rectify = rectify,
		)

		val bucketItemMetadataPath = "${Path.Root.path}/${BucketItemObject::class.simpleName}"
		val bucketItemMetadata = calcDataDiff(
			path = bucketItemMetadataPath,
			localObjectList = bucketItemObjectList.associateBy { it.id },
			localDeletedObjectList = localDeletedObjectIdList,
			modifiedTimestampList = bucketItemObjectList.associate { it.id to it.modifiedTimestamp },
			rectify = rectify,
		)

		return Pair(bucketMetadata, bucketItemMetadata)
	}

	private inline fun <reified T> DbxClientV2.calcDataDiff(
		path : String,
		localObjectList : Map<RealmUUID, T>,
		localDeletedObjectList : Set<DeletedObject>,
		modifiedTimestampList : Map<RealmUUID, Long>,
		rectify : Boolean,
	) : Map<RealmUUID, ObjectMetadata> {
		val leftObjectIdList = localObjectList.mapValues {
			ObjectMetadata(
				modifiedTimestamp = modifiedTimestampList[it.key] ?: 0,
				hash = it.value.let { t ->
					when (t) {
						is ChapterObject -> t.toCloudSnapshot().toDbxHashString()
						is NoteObject -> t.toCloudSnapshot().toDbxHashString()
						is BucketObject -> t.toCloudSnapshot().toDbxHashString()
						is BucketItemObject -> t.toCloudSnapshot().toDbxHashString()
						else -> ""
					}
				},
				isDeleted = false,
			)
		}.toMutableMap()
		localDeletedObjectList.filter { it.objectType == T::class.simpleName }.forEach {
			leftObjectIdList[it.id] = ObjectMetadata(
				modifiedTimestamp = it.deletedTimestamp,
				hash = "",
				isDeleted = true,
			)
		}

		val metadataDownloadResult = if (rectify) getFullMetadata(path) else downloadData("$path/metadata.json")
		when (metadataDownloadResult) {
			is DownloadResult.Success.ByteArray -> {
				val rightObjectIdList =
					json.decodeFromString(MapSerializer(RealmUUIDSerializer, ObjectMetadata.serializer()), String(metadataDownloadResult.data))
				val diff = Maps.difference(leftObjectIdList, rightObjectIdList)
				return syncDiff(diff = diff, localObjectList = localObjectList)
			}

			is DownloadResult.Success.MetadataMap<*> -> {
				try {
					val diff = Maps.difference(leftObjectIdList, metadataDownloadResult.metadataMap as Map<*, *>)
					diff as MapDifference<RealmUUID, ObjectMetadata>
					return syncDiff(diff = diff, localObjectList = localObjectList)
				} catch (e : Exception) {
					unknownError(e)
				}
			}

			is DownloadResult.NotFound -> {
				val diff = Maps.difference(leftObjectIdList, mapOf())
				return syncDiff(diff = diff, localObjectList = localObjectList)
			}

			is DownloadResult.DownloadErrorException -> unknownError(metadataDownloadResult.exception)
			is DownloadResult.NetworkError -> networkError()
			is DownloadResult.UnknownError -> unknownError(metadataDownloadResult.exception)
		}

		return mapOf()
	}

	private inline fun <reified T> DbxClientV2.syncDiff(
		diff : MapDifference<RealmUUID, ObjectMetadata>,
		localObjectList : Map<RealmUUID, T>,
	) : Map<RealmUUID, ObjectMetadata> {
		val toUpSyncObjectIdList : MutableMap<RealmUUID, Pair<ObjectMetadata, Operation>> = diff.entriesOnlyOnLeft()
			.mapValues { Pair(it.value, if (it.value.isDeleted) Operation.Delete() else Operation.Upsert) }
			.toMutableMap()

		val toDownSyncObjectIdList : MutableMap<RealmUUID, Pair<ObjectMetadata, Operation>> = diff.entriesOnlyOnRight()
			.mapValues { Pair(it.value, if (it.value.isDeleted) Operation.Delete() else Operation.Upsert) }
			.toMutableMap()

		diff.entriesDiffering().forEach { (realmUUId, difference) ->
			val localObjectMetadata = difference.leftValue()
			val remoteObjectMetadata = difference.rightValue()

//		    !!! Hash are not used for now. Is it necessary?
			if (localObjectMetadata.hash != remoteObjectMetadata.hash) {
				when {
					localObjectMetadata.modifiedTimestamp > remoteObjectMetadata.modifiedTimestamp -> {
						if (localObjectMetadata.isDeleted) toUpSyncObjectIdList[realmUUId] = Pair(localObjectMetadata, Operation.Delete(T::class.simpleName))
						else toUpSyncObjectIdList[realmUUId] = Pair(localObjectMetadata, Operation.Upsert)
					} // local is newer
					localObjectMetadata.modifiedTimestamp < remoteObjectMetadata.modifiedTimestamp -> {
						if (remoteObjectMetadata.isDeleted) toDownSyncObjectIdList[realmUUId] =
							Pair(remoteObjectMetadata, Operation.Delete(T::class.simpleName))
						else toDownSyncObjectIdList[realmUUId] = Pair(remoteObjectMetadata, Operation.Upsert)
					} // remote is newer
					else -> null // equal
				}
			}
		}

		updateSyncingState<T>(toUpSyncCount = toUpSyncObjectIdList.size, toDownSyncCount = toDownSyncObjectIdList.size, isSynced = false)

		val downSyncResultList = downSync<T>(toDownSyncObjectIdList)
		val upSyncResultList = localObjectList.mapValues { (_, t) ->
			when (t) {
				is ChapterObject -> t.toCloudSnapshot()
				is NoteObject -> t.toCloudSnapshot()
				is BucketObject -> t.toCloudSnapshot()
				is BucketItemObject -> t.toCloudSnapshot()
				else -> ""
			}
		}.filterValues { it != "" }.let { upSync<T>(objectIdList = toUpSyncObjectIdList, localObjectList = it) }

		val metadata : MutableMap<RealmUUID, ObjectMetadata> = mutableMapOf()
		upSyncResultList
			.filterValues { it is SyncResult.Success }
			.mapValues { (it.value as SyncResult.Success).objectMetadata }
			.let { metadata.putAll(it) }
		downSyncResultList
			.filterValues { it is SyncResult.Success }
			.mapValues { (it.value as SyncResult.Success).objectMetadata }
			.let { metadata.putAll(it) }
		diff.entriesInCommon()
			.mapValues { it.value }
			.let { metadata.putAll(it) }

		if (upSyncResultList.isNotEmpty() || downSyncResultList.isNotEmpty()) updateMetadata<T>(metadata = metadata)

		return metadata
	}

	private inline fun <reified T> DbxClientV2.downSync(
		objectIdList : MutableMap<RealmUUID, Pair<ObjectMetadata, Operation>>,
		toUpSyncObjectSize : Int = 0,
	) : Map<RealmUUID, SyncResult> {
		Log.i("npr71", "DropboxService: downSync ${T::class.simpleName} ${objectIdList.size}")
		val downSyncResultList : MutableMap<RealmUUID, SyncResult> = mutableMapOf()
		objectIdList.forEach { (realmUUId, pair) ->
			Log.i("npr71", "DropboxService: downSync ${T::class.simpleName} $realmUUId ${pair.second::class.simpleName}")
			if (pair.second is Operation.Delete) repository.deleteSuspended(id = realmUUId, keepHistory = false)
			else {
				when (val downloadResult = downloadData(path = "${Path.Root.path}/${T::class.simpleName}/${realmUUId}.json")) {
					is DownloadResult.Success.ByteArray -> {
						when (T::class) {
							NoteObject::class -> NoteObject.fromCloudSnapshot(downloadResult.data)?.let {
								repository.putNoteSuspended(noteObject = it, modifyTimestampAuto = false)
								downSyncResultList[realmUUId] = SyncResult.Success(pair.first)
							} ?: kotlin.run { downSyncResultList[realmUUId] = SyncResult.Failed }

							ChapterObject::class -> ChapterObject.fromCloudSnapshot(downloadResult.data)?.let {
								repository.putChapterSuspended(chapterObject = it, modifyTimestampAuto = false)
								downSyncResultList[realmUUId] = SyncResult.Success(pair.first)
							} ?: kotlin.run { downSyncResultList[realmUUId] = SyncResult.Failed }

							BucketObject::class -> BucketObject.fromCloudSnapshot(downloadResult.data)?.let {
								repository.putBucketSuspended(bucketObject = it, modifyTimestampAuto = false)
								downSyncResultList[realmUUId] = SyncResult.Success(pair.first)
							} ?: kotlin.run { downSyncResultList[realmUUId] = SyncResult.Failed }

							BucketItemObject::class -> BucketItemObject.fromCloudSnapshot(downloadResult.data)?.let {
								repository.putBucketItemSuspended(bucketItemObject = it, modifyTimestampAuto = false)
								downSyncResultList[realmUUId] = SyncResult.Success(pair.first)
							} ?: kotlin.run { downSyncResultList[realmUUId] = SyncResult.Failed }
						}
					}

					is DownloadResult.Success.MetadataMap -> null // Won't happen
					is DownloadResult.NotFound -> downSyncResultList[realmUUId] = SyncResult.Failed
					is DownloadResult.DownloadErrorException -> downSyncResultList[realmUUId] = SyncResult.Failed
					is DownloadResult.NetworkError -> null
					is DownloadResult.UnknownError -> null
				}
			}

			updateSyncingState<T>(toUpSyncCount = toUpSyncObjectSize, toDownSyncCount = objectIdList.size - downSyncResultList.size, isSynced = false)
		}

		updateSyncingState<T>(toUpSyncCount = toUpSyncObjectSize, toDownSyncCount = 0, isSynced = true)

		return downSyncResultList
	}

	private inline fun <reified T> DbxClientV2.upSync(
		objectIdList : MutableMap<RealmUUID, Pair<ObjectMetadata, Operation>>,
		localObjectList : Map<RealmUUID, String>,
	) : Map<RealmUUID, SyncResult> {
		Log.i("npr71", "DropboxService: upSync: ${T::class.simpleName} ${objectIdList.size}")
		val upSyncResultList : MutableMap<RealmUUID, SyncResult> = mutableMapOf()
		objectIdList.forEach { (realmUUId, pair) ->
			Log.i("npr71", "DropboxService: upSync: ${T::class.simpleName} ${realmUUId} ${pair.second::class.simpleName}")
			val path = "${Path.Root.path}/${T::class.simpleName}/${realmUUId}.json"
			if (pair.second is Operation.Delete) {
				val deleteResult = deleteData(path = path)
				Log.i("npr71", "DropboxService: deleteResult: $deleteResult")
				when (deleteResult) {
					is DeleteResult.Success -> upSyncResultList[realmUUId] = SyncResult.Success(objectMetadata = pair.first)
					is DeleteResult.NotFound -> upSyncResultList[realmUUId] = SyncResult.Success(objectMetadata = pair.first)
					is DeleteResult.DeleteErrorException -> upSyncResultList[realmUUId] = SyncResult.Failed
					is DeleteResult.NetworkError -> upSyncResultList[realmUUId] = SyncResult.Failed
					is DeleteResult.UnknownError -> upSyncResultList[realmUUId] = SyncResult.Failed
				}
			} else {
				localObjectList[realmUUId]?.let {
					val uploadResult = uploadData(
						path = path,
						inputStream = it.byteInputStream(),
					)

					when (uploadResult) {
						is UploadResult.Success -> upSyncResultList[realmUUId] = SyncResult.Success(objectMetadata = pair.first)
						is UploadResult.UploadErrorException -> upSyncResultList[realmUUId] = SyncResult.Failed
						is UploadResult.NetworkError -> upSyncResultList[realmUUId] = SyncResult.Failed
						is UploadResult.UnknownError -> upSyncResultList[realmUUId] = SyncResult.Failed
					}
				}
			}

			updateSyncingState<T>(toUpSyncCount = objectIdList.size - upSyncResultList.size, toDownSyncCount = 0, isSynced = false)
		}

		updateSyncingState<T>(toUpSyncCount = 0, toDownSyncCount = 0, isSynced = true)
		return upSyncResultList
	}

	private fun DbxClientV2.syncAttachment(
		deletedAttachmentList : Set<DeletedAttachment>,
		deletedNoteList : List<RealmUUID>,
		rectify : Boolean = false,
	) {
		val localAttachmentMetadataMap = repository.attachmentRepository.getAttachmentMetadataMap().toMutableMap()
		deletedAttachmentList.groupBy { it.parentId }.mapValues { entry ->
			entry.value.map { AttachmentMetadata(fileName = it.fileName, parentId = entry.key, isDeleted = true) }
		}.let { localAttachmentMetadataMap.putAll(it) }
		when (val metadataDownloadResult = getAttachmentMetadata(rectify = rectify)) {
//			Previous sync was successful. Directly sync using metadata.json
			is DownloadResult.Success.MetadataMap<*> -> {
				val cloudAttachmentMetadataMap = metadataDownloadResult.data as Map<RealmUUID, List<AttachmentMetadata>>
//				sync attachment
				val attachmentMetadataList = syncAttachmentDiff(
					localNoteAttachmentMetadataMap = localAttachmentMetadataMap,
					cloudNoteAttachmentMetadataMap = cloudAttachmentMetadataMap.toMutableMap(),
					deletedNoteList = deletedNoteList,
				)
				updateAttachmentMetadata(attachmentMetadataList = attachmentMetadataList)
			}

//			Should not happen
			is DownloadResult.Success -> null

//			!!! What if metadata is not found but attachment files are present in the cloud?
//			!!! Don't know when this will happen. But this will skip downSync. Can be solved by rectify
//			!!! Only happens if rectify is false and metadata is not found
//			When metadata is not found, upload all attachment
			is DownloadResult.NotFound -> {
//				upload all attachment
				val attachmentMetadataList = syncAttachmentDiff(
					localNoteAttachmentMetadataMap = localAttachmentMetadataMap,
					cloudNoteAttachmentMetadataMap = mutableMapOf(),
					deletedNoteList = deletedNoteList,
				)
				updateAttachmentMetadata(attachmentMetadataList = attachmentMetadataList)
			}

			is DownloadResult.DownloadErrorException -> unknownError(metadataDownloadResult.exception)
			is DownloadResult.NetworkError -> networkError()
			is DownloadResult.UnknownError -> unknownError(metadataDownloadResult.exception)
		}
	}

	private fun DbxClientV2.syncAttachmentDiff(
		localNoteAttachmentMetadataMap : MutableMap<RealmUUID, List<AttachmentMetadata>>,
		cloudNoteAttachmentMetadataMap : MutableMap<RealmUUID, List<AttachmentMetadata>>,
		deletedNoteList : List<RealmUUID>,
	) : Set<AttachmentMetadata> {
		val toUpSyncAttachmentList = mutableListOf<AttachmentMetadata>()
		val toDownSyncAttachmentList = mutableListOf<AttachmentMetadata>()
		val commonAttachmentList = mutableListOf<AttachmentMetadata>()

		deletedNoteList.forEach { deletedNoteId ->
			localNoteAttachmentMetadataMap[deletedNoteId]?.map { it.asDeleted() }?.let { localNoteAttachmentMetadataMap[deletedNoteId] = it }
		}

		val localAttachmentMetadataMap =
			localNoteAttachmentMetadataMap.values.flatten().associateBy { AttachmentMetadata.Companion.AttachmentIdentity(it.fileName, it.parentId) }
		val cloudAttachmentMetadataMap =
			cloudNoteAttachmentMetadataMap.values.flatten().associateBy { AttachmentMetadata.Companion.AttachmentIdentity(it.fileName, it.parentId) }

		val diff = Maps.difference(localAttachmentMetadataMap, cloudAttachmentMetadataMap)
		val commonAttachmentMap = diff.entriesInCommon()
		val localOnlyAttachmentMap = diff.entriesOnlyOnLeft()
		val cloudOnlyAttachmentMap = diff.entriesOnlyOnRight()
		val differentAttachmentMap = diff.entriesDiffering()

		commonAttachmentList.addAll(commonAttachmentMap.map { it.value })
		toUpSyncAttachmentList.addAll(localOnlyAttachmentMap.map { it.value })
		toDownSyncAttachmentList.addAll(cloudOnlyAttachmentMap.map { it.value })

		differentAttachmentMap.forEach { (attachmentIdentity, valueDifference) ->
			val localAttachmentMetadata = valueDifference.leftValue()
			val cloudAttachmentMetadata = valueDifference.rightValue()
			when {
				localAttachmentMetadata.isDeleted && ! cloudAttachmentMetadata.isDeleted -> toUpSyncAttachmentList.add(localAttachmentMetadata)
				! localAttachmentMetadata.isDeleted && cloudAttachmentMetadata.isDeleted -> toDownSyncAttachmentList.add(cloudAttachmentMetadata)
			}
		}

		val uploadedAttachmentSet = upSyncAttachment(attachmentMetadataList = toUpSyncAttachmentList, deletedNoteList = deletedNoteList)
		val downloadedAttachmentSet = downSyncAttachment(attachmentMetadataList = toDownSyncAttachmentList)

		return uploadedAttachmentSet + downloadedAttachmentSet + commonAttachmentList
	}

	private fun DbxClientV2.upSyncAttachment(
		attachmentMetadataList : List<AttachmentMetadata>,
		deletedNoteList : List<RealmUUID>,
	) : Set<AttachmentMetadata> {
		Log.i("npr71", "upSyncAttachment: ${attachmentMetadataList.size}")
		val uploadedAttachmentSet = mutableSetOf<AttachmentMetadata>()
		attachmentMetadataList.forEach { attachmentMetadata ->
			val folderPath = "${Path.Root.path}/Attachment/${attachmentMetadata.parentId}"
			val filePath = "$folderPath/${attachmentMetadata.fileName}"
			Log.i("npr71", "upSyncAttachment: $filePath")
			when {
				attachmentMetadata.parentId in deletedNoteList -> {
					val deleteResult = deleteData(path = folderPath)
					if (deleteResult is DeleteResult.Success || deleteResult is DeleteResult.NotFound)
						uploadedAttachmentSet.addAll(attachmentMetadataList.filter { it.parentId == attachmentMetadata.parentId }.map { it.asDeleted() })
				}

				attachmentMetadata.isDeleted -> {
					val deleteResult = deleteData(path = filePath)
					if (deleteResult is DeleteResult.Success || deleteResult is DeleteResult.NotFound)
						uploadedAttachmentSet.add(attachmentMetadata.copy(isDeleted = true)
					)
				}

				else -> {
					repository.attachmentRepository.getAttachment(attachmentMetadata.parentId, attachmentMetadata.fileName)?.let { file ->
						val uploadResult = uploadData(path = filePath, inputStream = file.inputStream())
						if (uploadResult is UploadResult.Success) uploadedAttachmentSet.add(attachmentMetadata)
					}
				}
			}
		}
		return uploadedAttachmentSet
	}

	private fun DbxClientV2.downSyncAttachment(attachmentMetadataList : MutableList<AttachmentMetadata>) : Set<AttachmentMetadata> {
		Log.i("npr71", "downSyncAttachment: ${attachmentMetadataList.size}")
		val downloadedAttachmentList = mutableSetOf<AttachmentMetadata>()
		attachmentMetadataList.forEach { attachmentMetadata ->
			Log.i("npr71", "downSyncAttachment: ${attachmentMetadata.fileName}")
			val path = "${Path.Root.path}/Attachment/${attachmentMetadata.parentId}/${attachmentMetadata.fileName}"
			if (attachmentMetadata.isDeleted) repository.attachmentRepository.delete(attachmentMetadata.parentId, attachmentMetadata.fileName)
			else {
				val downloadResult = downloadData(path = path)
				if (downloadResult is DownloadResult.Success.ByteArray) {
					repository.attachmentRepository.putAttachment(
						parentId = attachmentMetadata.parentId,
						fileName = attachmentMetadata.fileName,
						byteArray = downloadResult.data
					)
					downloadedAttachmentList.add(attachmentMetadata)
				}
			}
		}
		return downloadedAttachmentList
	}

	private fun DbxClientV2.updateAttachmentMetadata(attachmentMetadataList : Set<AttachmentMetadata>) {
		Log.d("CloudSync", "updateAttachmentMetadata : $attachmentMetadataList")
		val path = "${Path.Root.path}/Attachment/metadata.json"
		try {
			json.encodeToString(SetSerializer(AttachmentMetadata.serializer()), attachmentMetadataList).let { jsonString ->
				uploadData(path = path, inputStream = jsonString.byteInputStream())
			}
		} catch (e : Exception) {
			e.printStackTrace()
		}
	}

	private fun DbxClientV2.syncBaseObject(
		localBaseObject : BaseObject?
	) {
		when (val downloadResult = downloadData(path = "baseObject.json")) {
			is DownloadResult.Success.ByteArray -> {
				val remoteBaseObject = BaseObject.fromCloudSnapshot(downloadResult.data)
				when {
					localBaseObject == null && remoteBaseObject == null -> {
						uploadData(
							path = "baseObject.json",
							inputStream = BaseObject().toCloudSnapshot().byteInputStream()
						)
					}

					localBaseObject == null && remoteBaseObject != null -> repository.putBaseObjectSuspended(remoteBaseObject)
					localBaseObject != null && remoteBaseObject == null -> uploadData(
						path = "baseObject.json",
						inputStream = localBaseObject.toCloudSnapshot().byteInputStream()
					)

					localBaseObject != null && remoteBaseObject != null -> {
						when {
							localBaseObject.modifiedTimestamp < remoteBaseObject.modifiedTimestamp -> repository.putBaseObjectSuspended(remoteBaseObject)
							localBaseObject.modifiedTimestamp > remoteBaseObject.modifiedTimestamp -> uploadData(
								path = "baseObject.json",
								inputStream = localBaseObject.toCloudSnapshot().byteInputStream()
							)

							else -> null
						}
					}
				}
			}

			is DownloadResult.Success.MetadataMap -> null // Won't happen
			is DownloadResult.NotFound -> null
			is DownloadResult.DownloadErrorException -> null
			is DownloadResult.NetworkError -> null
			is DownloadResult.UnknownError -> null
		}
	}

	private inline fun <reified T> DbxClientV2.updateMetadata(metadata : Map<RealmUUID, ObjectMetadata>) {

		val metadataPath = "${Path.Root.path}/${T::class.simpleName}/metadata.json"
		try {
			val inputStream = json.encodeToString(MapSerializer(RealmUUIDSerializer, ObjectMetadata.serializer()), metadata).byteInputStream()
			uploadData(
				path = metadataPath,
				inputStream = inputStream,
			)
		} catch (e : Exception) {
			e.printStackTrace()
		}
	}

	private fun DbxClientV2.getFullMetadata(path : String) : DownloadResult<Map<RealmUUID, ObjectMetadata>> {
		var cursor : String?    //  = null
		var hasMore : Boolean   //  = false

		val filePathList : MutableList<String> = mutableListOf()

		try {
			files()
				.listFolderBuilder(path)
				.withIncludeDeleted(false)
				.start()
				.let { listFolderResult ->
					cursor = listFolderResult.cursor
					hasMore = listFolderResult.hasMore
					filePathList.addAll(listFolderResult.entries.filter { it.name != "metadata.json" }.map { it.pathLower })
				}
		} catch (e : ListFolderErrorException) {
			if (e.errorValue.pathValue.isNotFound) return DownloadResult.NotFound
			else return DownloadResult.DownloadErrorException(e)
		} catch (e : NetworkIOException) {
			return DownloadResult.NetworkError(e)
		} catch (e : Exception) {
			return DownloadResult.UnknownError(e)
		}

		try {
			while (hasMore) {
				files()
					.listFolderContinue(cursor)
					.let { listFolderResult ->
						cursor = listFolderResult.cursor
						hasMore = listFolderResult.hasMore
						filePathList.addAll(listFolderResult.entries.filter { it.name != "metadata.json" }.map { it.pathLower })
					}
			}
		} catch (e : ListFolderContinueErrorException) {
			if (e.errorValue.pathValue.isNotFound) return DownloadResult.DownloadErrorException(e)
			else return DownloadResult.DownloadErrorException(e)
		} catch (e : NetworkIOException) {
			return DownloadResult.NetworkError(e)
		} catch (e : Exception) {
			return DownloadResult.UnknownError(e)
		}

		val objectMetadataMap : MutableMap<RealmUUID, ObjectMetadata> = mutableMapOf()
		filePathList.forEach { filePath ->
			files()
				.download(filePath)
				.result
				.let { fileMetadata ->
					try {
						val realmUUID = RealmUUID.from(fileMetadata.name.substringBeforeLast("."))
						objectMetadataMap[realmUUID] = ObjectMetadata(
							modifiedTimestamp = fileMetadata.clientModified.time,
							hash = fileMetadata.contentHash,
							isDeleted = false
						)
					} catch (_ : Exception) {
					}
				}
		}

		return DownloadResult.Success.MetadataMap(objectMetadataMap)
	}

	/**
	 * @author pushpull
	 * @since 2.3.0
	 * @param rectify if true, scans the entire attachment folder recursively and downloads metadata of all attachments
	 * @return Map<RealmUUID, List<AttachmentMetadata>> Map of parentId to list of attachment metadata in [DownloadResult]
	 */
	private fun DbxClientV2.getAttachmentMetadata(rectify : Boolean) : DownloadResult<Map<RealmUUID, List<AttachmentMetadata>>> {
		val attachmentPath = "${Path.Root.path}/Attachment"

		var cursor : String?    //  = null
		var hasMore : Boolean   //  = false

		val attachmentList : MutableList<String> = mutableListOf()

		if (rectify) {
			try {
				files()
					.listFolderBuilder(attachmentPath)
					.withIncludeDeleted(false)
					.withRecursive(true)
					.start()
					.let { listFolderResult ->
						cursor = listFolderResult.cursor
						hasMore = listFolderResult.hasMore
						listFolderResult.entries
							.filter { it.pathLower.split("/").size == 5 }
							.map { it.pathLower }
							.let { attachmentList.addAll(it) }
					}
			} catch (e : ListFolderErrorException) {
				return if (e.errorValue.pathValue.isNotFound) DownloadResult.NotFound
				else DownloadResult.DownloadErrorException(e)
			} catch (e : NetworkIOException) {
				return DownloadResult.NetworkError(e)
			} catch (e : Exception) {
				return DownloadResult.UnknownError(e)
			}

			try {
				while (hasMore) {
					files()
						.listFolderContinue(cursor)
						.let { listFolderResult ->
							cursor = listFolderResult.cursor
							hasMore = listFolderResult.hasMore
							listFolderResult.entries
								.filter { it.pathLower.split("/").size == 4 }
								.map { it.pathLower }
								.let { attachmentList.addAll(it) }
						}
				}
			} catch (e : ListFolderContinueErrorException) {
				return if (e.errorValue.pathValue.isNotFound) DownloadResult.DownloadErrorException(e) else DownloadResult.DownloadErrorException(e)
			} catch (e : NetworkIOException) {
				return DownloadResult.NetworkError(e)
			} catch (e : Exception) {
				return DownloadResult.UnknownError(e)
			}

			val noteAttachmentMetadataMap : MutableMap<RealmUUID, MutableList<AttachmentMetadata>> = mutableMapOf()
			attachmentList.forEach { filePath ->
				try {
					val parentId = RealmUUID.from(filePath.split("/")[3])
					val fileName = filePath.split("/")[4]
					val attachmentMetadata = AttachmentMetadata(
						fileName = fileName,
						parentId = parentId,
						isDeleted = false
					)
					noteAttachmentMetadataMap[parentId]?.add(attachmentMetadata) ?: noteAttachmentMetadataMap.put(parentId, mutableListOf(attachmentMetadata))
				} catch (_ : Exception) {
				}
			}

			return DownloadResult.Success.MetadataMap(noteAttachmentMetadataMap)

		} else {
			val metadataDownloadResult = downloadData(path = "$attachmentPath/metadata.json")
			return if (metadataDownloadResult is DownloadResult.Success.ByteArray) {
				val attachmentMetadataMap =
					json.decodeFromString(ListSerializer(AttachmentMetadata.serializer()), String(metadataDownloadResult.data)).groupBy { it.parentId }
				DownloadResult.Success.MetadataMap(attachmentMetadataMap)
			} else metadataDownloadResult.clone()
		}
	}

	/**
	 * @author pushpull
	 * @since 2.3.0
	 * @param path Exact path of the file to be downloaded
	 * @return ByteArray in [DownloadResult]
	 */
	private fun DbxClientV2.downloadData(path : String) : DownloadResult<ByteArray> {
		Log.i("npr71", "DropboxService: downloadData : $path")
		try {
			files()
				.download(path)
				.inputStream
				.readBytes()
				.let { return DownloadResult.Success.ByteArray(it) }
		} catch (e : DownloadErrorException) {
			if (e.errorValue.pathValue.isNotFound) return DownloadResult.NotFound
			else return DownloadResult.DownloadErrorException(e)
		} catch (e : NetworkIOException) {
			return DownloadResult.NetworkError(e)
		} catch (e : Exception) {
			return DownloadResult.UnknownError(e)
		}
	}

	private fun DbxClientV2.uploadData(
		path : String,
		inputStream : InputStream,
		modifiedTimestamp : Long = System.currentTimeMillis(),
		hash : String? = null,
	) : UploadResult {
		Log.i("npr71", "DropboxService: uploadData : $path")
		try {
			files()
				.uploadBuilder(path)
				.withMode(WriteMode.OVERWRITE)
				.withClientModified(Date(modifiedTimestamp))
				.withContentHash(hash)
				.uploadAndFinish(inputStream)
				.size
				.let { return UploadResult.Success }
		} catch (e : UploadErrorException) {
			return UploadResult.UploadErrorException(e)
		} catch (e : NetworkIOException) {
			return UploadResult.NetworkError(e)
		} catch (e : Exception) {
			return UploadResult.UnknownError(e)
		}
	}

	private fun DbxClientV2.deleteData(path : String) : DeleteResult {
		Log.i("npr71", "DropboxService: deleteData : $path")
		try {
			files()
				.deleteV2(path)
				.let { return DeleteResult.Success }
		} catch (e : DeleteErrorException) {
			return if (e.errorValue.pathLookupValue.isNotFound) DeleteResult.NotFound
			else DeleteResult.DeleteErrorException(e)
		} catch (e : NetworkIOException) {
			return DeleteResult.NetworkError(e)
		} catch (e : Exception) {
			return DeleteResult.UnknownError(e)
		}
	}

	private inline fun <reified T> updateSyncingState(
		toUpSyncCount : Int,
		toDownSyncCount : Int,
		isSynced : Boolean,
	) {
		if (syncStatus.value is SyncerService.Companion.SyncStatus.Syncing) {
			val currentSyncStatus = syncStatus.value as SyncerService.Companion.SyncStatus.Syncing

			val syncObjectStatus = SyncerService.Companion.SyncObjectStatus(
				toUpSyncCount = toUpSyncCount,
				toDownSyncCount = toDownSyncCount,
				isSynced = isSynced,
			)

			when (T::class) {
				ChapterObject::class -> (syncStatus.value as SyncerService.Companion.SyncStatus.Syncing).copy(chapterSyncObjectStatus = syncObjectStatus)
				NoteObject::class -> (syncStatus.value as SyncerService.Companion.SyncStatus.Syncing).copy(noteSyncObjectStatus = syncObjectStatus)
				BucketObject::class -> (syncStatus.value as SyncerService.Companion.SyncStatus.Syncing).copy(bucketSyncObjectStatus = syncObjectStatus)
				BucketItemObject::class -> (syncStatus.value as SyncerService.Companion.SyncStatus.Syncing).copy(bucketItemSyncObjectStatus = syncObjectStatus)
				TagObject::class -> (syncStatus.value as SyncerService.Companion.SyncStatus.Syncing).copy(tagSyncObjectStatus = syncObjectStatus)
				else -> syncStatus.value as SyncerService.Companion.SyncStatus.Syncing
			}.let { syncStatus.tryEmit(it) }
		}
	}

	inner class DropboxServiceBinder : Binder() {
		val service : DropboxService
			get() = this@DropboxService
	}

	companion object {
		enum class Path(val path : String) {
			Root("/sync"),
			Lock("/sync/graphite.lock"),
		}

		@Serializable
		data class ObjectMetadata(
			val modifiedTimestamp : Long,
			val hash : String,
			val isDeleted : Boolean
		)

		@Serializable
		data class AttachmentMetadata(
			val fileName : String,
			@Serializable(with = RealmUUIDSerializer::class)
			val parentId : RealmUUID,
			val isDeleted : Boolean,
		) {
			fun asDeleted() : AttachmentMetadata = AttachmentMetadata(fileName, parentId, true)
			override fun hashCode() : Int {
				var result = fileName.hashCode()
				result = 31 * result + parentId.hashCode()
				result = 31 * result + isDeleted.hashCode()
				return result
			}

			override fun equals(other : Any?) : Boolean {
				if (this === other) return true
				if (other !is AttachmentMetadata) return false

				if (fileName != other.fileName) return false
				if (parentId != other.parentId) return false
				if (isDeleted != other.isDeleted) return false

				return true
			}

			companion object {
				data class AttachmentIdentity(val fileName : String, val parentId : RealmUUID)
			}
		}

		sealed class Operation {
			object Upsert : Operation()
			class Delete(val objectType : String? = null) : Operation()
		}

		sealed class DownloadResult<out T> {
			sealed class Success<out T>(val data : T) : DownloadResult<T>() {
				data class ByteArray(val byteArray : kotlin.ByteArray) : Success<kotlin.ByteArray>(byteArray)
				data class MetadataMap<T>(val metadataMap : T) : Success<T>(metadataMap)
			}

			object NotFound : DownloadResult<Nothing>()
			data class DownloadErrorException(val exception : Exception) : DownloadResult<Nothing>()
			data class NetworkError(val exception : Exception) : DownloadResult<Nothing>()
			data class UnknownError(val exception : Exception) : DownloadResult<Nothing>()

			fun <R> clone() : DownloadResult<R> {
				return when (this) {
					is Success -> getNullInstance()
					is NotFound -> NotFound
					is DownloadErrorException -> DownloadErrorException(exception)
					is NetworkError -> NetworkError(exception)
					is UnknownError -> UnknownError(exception)
				}
			}

			companion object {
				fun getNullInstance() : DownloadResult<Nothing> = NotFound
			}
		}

		sealed class UploadResult {
			object Success : UploadResult()
			data class UploadErrorException(val exception : com.dropbox.core.v2.files.UploadErrorException) : UploadResult()
			data class NetworkError(val exception : Exception) : UploadResult()
			data class UnknownError(val exception : Exception) : UploadResult()
		}

		sealed class DeleteResult {
			object Success : DeleteResult()
			object NotFound : DeleteResult()
			data class DeleteErrorException(val exception : com.dropbox.core.v2.files.DeleteErrorException) : DeleteResult()
			data class NetworkError(val exception : Exception) : DeleteResult()
			data class UnknownError(val exception : Exception) : DeleteResult()
		}

		sealed class SyncResult {
			data class Success(val objectMetadata : ObjectMetadata) : SyncResult()
			object Failed : SyncResult()
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
		attemptingToBind = false
		if (bound) {
			service?.isUnbounded = true
			context.unbindService(this)
			bound = false
		}
	}
}
