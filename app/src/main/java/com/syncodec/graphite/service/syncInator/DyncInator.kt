package com.syncodec.graphite.service.syncInator

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.common.collect.Maps
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.cloud.dropbox.CombinedAttachmentMetadata
import com.syncodec.graphite.di.cloud.dropbox.DropboxApi
import com.syncodec.graphite.di.cloud.dropbox.DropboxConnector
import com.syncodec.graphite.di.cloud.dropbox.DropboxOperation
import com.syncodec.graphite.di.cloud.dropbox.DropboxOperation.ActiveOperationResult.DeleteFileResult
import com.syncodec.graphite.di.cloud.dropbox.DropboxOperation.ActiveOperationResult.UploadFileResult
import com.syncodec.graphite.di.cloud.dropbox.DropboxOperation.PassiveOperationResult.DownloadFileResult
import com.syncodec.graphite.di.cloud.dropbox.DropboxOperation.PassiveOperationResult.ListFolderResult
import com.syncodec.graphite.di.model.local.AttachmentIdentity
import com.syncodec.graphite.di.model.local.BucketItemObject
import com.syncodec.graphite.di.model.local.BucketObject
import com.syncodec.graphite.di.model.local.ChapterObject
import com.syncodec.graphite.di.model.local.NoteObject
import com.syncodec.graphite.di.model.local.ext.Syncable
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.di.model.serializer.RealmUUIDSerializer
import com.syncodec.graphite.service.syncInator.SyncInatorService.Companion.ObjectOperation
import com.syncodec.graphite.service.syncInator.SyncInatorService.Companion.AttachmentOperation
import com.syncodec.graphite.di.cloud.dropbox.DropboxApi.Companion.DropboxPath
import com.syncodec.graphite.di.cloud.dropbox.DropboxAttachmentMetadata
import com.syncodec.graphite.di.cloud.dropbox.DropboxObjectMetadata
import com.syncodec.graphite.di.network.NetworkRequest
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.TypedRealmObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.time.Instant
import kotlin.reflect.KFunction1
import kotlin.reflect.KMutableProperty1


class DyncInator : SyncInatorService() {

	inner class DyncInatorBinder : Binder() {
		val service: DyncInator
			get() = this@DyncInator
	}

	private var dyncInatorBinder: IBinder? = DyncInatorBinder()

	private val json = Json {
		ignoreUnknownKeys = true
		encodeDefaults = true
		allowStructuredMapKeys = true
	}

	val dropBoxConnector: DropboxConnector by inject()
	private var dropboxApi: DropboxApi? = null
	val dropboxAccountInfo = MutableStateFlow<NetworkRequest<DropboxApi.Companion.DropboxAccountInfo>>(NetworkRequest.Init)

	private var syncJob: Job? = null
	private var lockerJob: Job? = null
	private val previousSyncSessionIdList = mutableSetOf<String>()

	override fun onBind(intent: Intent): IBinder? {
		super.onBind(intent)
		return dyncInatorBinder
	}

	override fun onCreate() {
		super.onCreate()
		Log.d("rits", "DropboxSyncInatorService.onCreate: ")
		lifecycleScope.launch(Dispatchers.IO) {
			super.syncStat.tryEmit(SyncStat.Connecting)
			this@DyncInator.dropboxAccountInfo.tryEmit(NetworkRequest.Loading)
			try {
				dropboxApi = dropBoxConnector.connect()
				if (dropboxApi == null) {
					super.syncStat.tryEmit(SyncStat.NotConnected)
					this@DyncInator.dropboxAccountInfo.tryEmit(NetworkRequest.Error())
				} else {
					super.syncStat.tryEmit(SyncStat.Idle(isAutoSyncEnabled = true, lastSyncedAt = null))
					this@DyncInator.dropboxAccountInfo.tryEmit(dropboxApi?.getAccountInfo() ?: NetworkRequest.Error())
				}
			} catch (e: Exception) {
				if (BuildConfig.DEBUG) e.printStackTrace()
				super.syncStat.tryEmit(SyncStat.Error.UnknownError())
			}
			startShowStopper()
			startAutoSyncer()
		}
	}

	private suspend fun startAutoSyncer() {
		Log.d("rits", "DropboxSyncInatorService.startAutoSyncer: ")
		lifecycleScope.launch(Dispatchers.IO) {
			while (true) {
				if (super.isAutoSyncEnabled.first()) syncNow(forced = false)
				else super.syncStat.value.let { if (it is SyncStat.Idle) super.syncStat.tryEmit(SyncStat.Idle(isAutoSyncEnabled = false, lastSyncedAt = it.lastSyncedAt)) }
				delay(30_000)
			}
		}
	}

	private fun startShowStopper() {
		lifecycleScope.launch(Dispatchers.Default) {
			while (true) {
				delay(1000)
				if (super.isUnbounded && super.syncStat.value !is SyncStat.Syncing) {
					dyncInatorBinder = null
					syncJob?.cancel()
					lockerJob?.cancel()
					stopSelf()
				}
			}
		}
	}

	override fun syncNow(forced: Boolean) {
		Log.d("rits", "DropboxSyncInatorService.syncNow: ")
		when (super.syncStat.value) {
			is SyncStat.Init -> Unit
			is SyncStat.Connecting -> Unit
			is SyncStat.NotConnected -> Unit
			is SyncStat.Idle -> sync(forced = forced)
			is SyncStat.Locked -> sync(forced = forced)
			is SyncStat.Syncing -> Unit
			is SyncStat.Error.NetworkError -> sync(forced = forced)
			is SyncStat.Error.CredentialsError -> sync(forced = forced)
			is SyncStat.Error.UnknownError -> sync(forced = forced)
		}
	}

	private fun sync(forced: Boolean) {
		syncJob?.cancel()
		lockerJob?.cancel()
		syncJob = lifecycleScope.launch(Dispatchers.IO) {
			dropboxApi?.tryAndHoldLock(forced) ?: super.syncStat.tryEmit(SyncStat.Error.UnknownError())
		}
	}

	private fun onUnknownError(exception: Exception? = null) {
		syncJob?.cancel()
		lockerJob?.cancel()
		super.syncStat.tryEmit(SyncStat.Error.UnknownError(message = exception?.message))
	}

	private fun onNetworkError() {
		syncJob?.cancel()
		lockerJob?.cancel()
		super.syncStat.tryEmit(SyncStat.Error.NetworkError)
	}

	private fun onCredentialsError() {
		syncJob?.cancel()
		lockerJob?.cancel()
		super.syncStat.tryEmit(SyncStat.Error.CredentialsError)
	}

	private fun onHoldLockError() {
		syncJob?.cancel()
		lockerJob?.cancel()
		super.syncStat.tryEmit(SyncStat.Error.UnknownError())
	}

	private suspend fun DropboxApi.tryAndHoldLock(forced: Boolean) {
		super.syncStat.tryEmit(SyncStat.Connecting)
		if (forced) {
			val currentSyncSessionId = RealmUUID.random().toString()
			previousSyncSessionIdList.add(currentSyncSessionId)
			lockDropboxAndKeepAlive(currentSyncSessionId = currentSyncSessionId)
			sync(rectify = true)
			unlockDropbox(currentSyncSessionId = currentSyncSessionId)
		} else when (checkIfLocked()) {
			true -> {
				super.syncStat.tryEmit(SyncStat.Locked)
				syncJob?.cancel()
			}

			false -> withConnection(
				onError = {
					super.syncStat.tryEmit(SyncStat.Error.UnknownError())
					syncJob?.cancel()
				}
			) {
				val currentSyncSessionId = RealmUUID.random().toString()
				previousSyncSessionIdList.add(currentSyncSessionId)
				lockDropboxAndKeepAlive(currentSyncSessionId = currentSyncSessionId)
				sync(rectify = false)
				unlockDropbox(currentSyncSessionId = currentSyncSessionId)
			}

			null -> {
				super.syncStat.tryEmit(SyncStat.Error.UnknownError())
				syncJob?.cancel()
			}
		}
	}

	/**
	 * Checks if the dropbox is locked by another sync process
	 * @return true if locked, false if not locked, null if unknown error. Proceed if false or proceed with caution
	 */
	private suspend fun DropboxApi.checkIfLocked(): Boolean? {
		return withConnection(onError = ::onUnknownError) {
			when (val lockerDownloadFileResult = downloadFile(DropboxPath.Lock.path)) {
				is DownloadFileResult.Success -> {
					val remoteLockerJsonObject = JSONObject(lockerDownloadFileResult.byteArray.decodeToString())
					val remoteSessionId = remoteLockerJsonObject.getString("sessionId")
					val remoteTimestamp = remoteLockerJsonObject.getLong("timestamp")
					when {
						remoteSessionId in previousSyncSessionIdList -> return@withConnection false
						remoteTimestamp < Instant.now().toEpochMilli() - 60_000 -> return@withConnection false
						else -> return@withConnection true
					}
				}

				is DownloadFileResult.FileNotFound -> return@withConnection false
				is DownloadFileResult.Error.DownloadError -> {
					onUnknownError(lockerDownloadFileResult.exception); return@withConnection null
				}

				is DownloadFileResult.Error.CredentialsError -> {
					onCredentialsError(); return@withConnection null
				}

				is DownloadFileResult.Error.NetworkError -> {
					onNetworkError(); return@withConnection null
				}

				is DownloadFileResult.Error.UnknownError -> {
					onUnknownError(lockerDownloadFileResult.exception); return@withConnection null
				}
			}
		}
	}

	/**
	 * Locks the dropbox and keeps the connection alive. Renew the lock every 30 seconds so that other sync processes can't start
	 * @param currentSyncSessionId current sync session id
	 */
	private suspend fun DropboxApi.lockDropboxAndKeepAlive(currentSyncSessionId: String) {
		lockerJob = lifecycleScope.launch(Dispatchers.IO) {
			while (true) {
				withConnection(onError = ::onUnknownError) {

//					Check if the lock is still valid and not updated by another sync process
					when (val lockerDownloadFileResult = downloadFile(DropboxPath.Lock.path)) {
						is DownloadFileResult.Success -> {
							try {
								val remoteLockerJsonObject = JSONObject(lockerDownloadFileResult.byteArray.decodeToString())
								val remoteSessionId = remoteLockerJsonObject.getString("sessionId")
								if (remoteSessionId !in previousSyncSessionIdList) onUnknownError()
							} catch (e: Exception) {
								if (BuildConfig.DEBUG) e.printStackTrace()
								onHoldLockError()
							}
						}

						is DownloadFileResult.FileNotFound -> Unit
						is DownloadFileResult.Error.DownloadError -> onHoldLockError()
						is DownloadFileResult.Error.CredentialsError -> {
							onCredentialsError(); return@withConnection null
						}

						is DownloadFileResult.Error.NetworkError -> {
							onNetworkError(); return@withConnection null
						}

						is DownloadFileResult.Error.UnknownError -> onHoldLockError()
					}

//					Renew lock
					val dropboxLocker = JSONObject()
					dropboxLocker.put("timestamp", Instant.now().toEpochMilli())
					dropboxLocker.put("sessionId", currentSyncSessionId)
					val lockerString = dropboxLocker.toString()

					when (uploadFile(DropboxPath.Lock.path, lockerString.encodeToByteArray())) {
						is UploadFileResult.Success -> super.syncStat.tryEmit(SyncStat.Syncing(sessionId = currentSyncSessionId))
						is UploadFileResult.Error.UploadError -> onHoldLockError()
						is UploadFileResult.Error.CredentialsError -> onCredentialsError()
						is UploadFileResult.Error.NetworkError -> onNetworkError()
						is UploadFileResult.Error.UnknownError -> onHoldLockError()
					}
				}

				delay(30_000)
			}
		}
	}

	private suspend fun DropboxApi.unlockDropbox(currentSyncSessionId: String) {
		lockerJob?.cancel()
		withConnection(onError = ::onUnknownError) {
			when (val downloadFileResult = downloadFile(path = DropboxPath.Lock.path)) {
				is DownloadFileResult.Success -> {
					val remoteLockerJsonObject = JSONObject(downloadFileResult.byteArray.decodeToString())
					val remoteSessionId = remoteLockerJsonObject.getString("sessionId")
					if (remoteSessionId == currentSyncSessionId) {
						when (val deleteFileResult = deleteFile(path = DropboxPath.Lock.path)) {
							is DeleteFileResult.Success -> {
								super.syncStat.tryEmit(SyncStat.Idle(isAutoSyncEnabled = super.isAutoSyncEnabled.first(), lastSyncedAt = Clock.System.now()))
								return@withConnection
							}

							is DeleteFileResult.Error.DeleteError -> {
								super.syncStat.tryEmit(SyncStat.Idle(isAutoSyncEnabled = super.isAutoSyncEnabled.first(), lastSyncedAt = Clock.System.now()))
								return@withConnection
							}

							is DeleteFileResult.Error.CredentialsError -> onCredentialsError()
							is DeleteFileResult.Error.NetworkError -> onNetworkError()
							is DeleteFileResult.Error.UnknownError -> onUnknownError(deleteFileResult.exception)
						}
					} else super.syncStat.tryEmit(SyncStat.Locked)
				}

				is DownloadFileResult.FileNotFound -> super.syncStat.tryEmit(SyncStat.Idle(isAutoSyncEnabled = super.isAutoSyncEnabled.first(), lastSyncedAt = Clock.System.now()))
				is DownloadFileResult.Error.DownloadError -> super.syncStat.tryEmit(SyncStat.Idle(isAutoSyncEnabled = super.isAutoSyncEnabled.first(), lastSyncedAt = Clock.System.now()))
				is DownloadFileResult.Error.CredentialsError -> onCredentialsError()
				is DownloadFileResult.Error.NetworkError -> onNetworkError()
				is DownloadFileResult.Error.UnknownError -> onUnknownError(downloadFileResult.exception)
			}
		}
	}

	private suspend fun DropboxApi.sync(rectify: Boolean) {
		Log.d("rits", "DropboxSyncInatorService.sync: chapterSyncObject")
		val chapterMetadataMap = syncObjects<ChapterObject>(
			parentPath = DropboxPath.Chapter.path,
			rectify = rectify,
			idGetter = ChapterObject::id,
			modifiedTimestampGetter = ChapterObject::modifiedTimestamp,
			toObjectMetadata = ChapterObject::toObjectMetadata
		) {
			super.syncStat.value.let { syncStat1 -> if (syncStat1 is SyncStat.Syncing) super.syncStat.tryEmit(syncStat1.copy(chapterSyncObjectStatus = it)) }
		}
		upSyncMetadata(parentPath = DropboxPath.Chapter.path, metadataMap = chapterMetadataMap)
		super.syncStat.value.let { syncStat1 -> if (syncStat1 is SyncStat.Syncing) super.syncStat.tryEmit(syncStat1.copy(chapterSyncObjectStatus = ObjectStatus(objectSyncStatus = ObjectSyncStatus.Success))) }

		Log.d("rits", "DropboxSyncInatorService.sync: noteSyncObject")
		val noteMetadataMap = syncObjects<NoteObject>(
			parentPath = DropboxPath.Note.path,
			rectify = rectify,
			idGetter = NoteObject::id,
			modifiedTimestampGetter = NoteObject::modifiedTimestamp,
			toObjectMetadata = NoteObject::toObjectMetadata
		) {
			super.syncStat.value.let { syncStat1 -> if (syncStat1 is SyncStat.Syncing) super.syncStat.tryEmit(syncStat1.copy(noteSyncObjectStatus = it)) }
		}
		upSyncMetadata(parentPath = DropboxPath.Note.path, metadataMap = noteMetadataMap)
		super.syncStat.value.let { syncStat1 -> if (syncStat1 is SyncStat.Syncing) super.syncStat.tryEmit(syncStat1.copy(noteSyncObjectStatus = ObjectStatus(objectSyncStatus = ObjectSyncStatus.Success))) }

		Log.d("rits", "DropboxSyncInatorService.sync: bucketSyncObject")
		val bucketMetadataMap = syncObjects<BucketObject>(
			parentPath = DropboxPath.Bucket.path,
			rectify = rectify,
			idGetter = BucketObject::id,
			modifiedTimestampGetter = BucketObject::modifiedTimestamp,
			toObjectMetadata = BucketObject::toObjectMetadata
		) {
			super.syncStat.value.let { syncStat1 -> if (syncStat1 is SyncStat.Syncing) super.syncStat.tryEmit(syncStat1.copy(bucketSyncObjectStatus = it)) }
		}
		upSyncMetadata(parentPath = DropboxPath.Bucket.path, metadataMap = bucketMetadataMap)
		super.syncStat.value.let { syncStat1 -> if (syncStat1 is SyncStat.Syncing) super.syncStat.tryEmit(syncStat1.copy(bucketSyncObjectStatus = ObjectStatus(objectSyncStatus = ObjectSyncStatus.Success))) }

		Log.d("rits", "DropboxSyncInatorService.sync: bucketItemSyncObject")
		val bucketItemMetadataMap = syncObjects<BucketItemObject>(
			parentPath = DropboxPath.BucketItem.path,
			rectify = rectify,
			idGetter = BucketItemObject::id,
			modifiedTimestampGetter = BucketItemObject::modifiedTimestamp,
			toObjectMetadata = BucketItemObject::toObjectMetadata
		) {
			super.syncStat.value.let { syncStat1 -> if (syncStat1 is SyncStat.Syncing) super.syncStat.tryEmit(syncStat1.copy(bucketItemSyncObjectStatus = it)) }
		}
		upSyncMetadata(parentPath = DropboxPath.BucketItem.path, metadataMap = bucketItemMetadataMap)
		super.syncStat.value.let { syncStat1 -> if (syncStat1 is SyncStat.Syncing) super.syncStat.tryEmit(syncStat1.copy(bucketItemSyncObjectStatus = ObjectStatus(objectSyncStatus = ObjectSyncStatus.Success))) }

		Log.d("rits", "DropboxSyncInatorService.sync: tagSyncObject")
		val tagMetadataMap = syncObjects<TagObject>(
			parentPath = DropboxPath.Tag.path,
			rectify = rectify,
			idGetter = TagObject::id,
			modifiedTimestampGetter = TagObject::modifiedTimestamp,
			toObjectMetadata = TagObject::toObjectMetadata
		) {
			super.syncStat.value.let { syncStat1 -> if (syncStat1 is SyncStat.Syncing) super.syncStat.tryEmit(syncStat1.copy(tagSyncObjectStatus = it)) }
		}
		upSyncMetadata(parentPath = DropboxPath.Tag.path, metadataMap = tagMetadataMap)
		super.syncStat.value.let { syncStat1 -> if (syncStat1 is SyncStat.Syncing) super.syncStat.tryEmit(syncStat1.copy(tagSyncObjectStatus = ObjectStatus(objectSyncStatus = ObjectSyncStatus.Success))) }

		Log.d("rits", "DropboxSyncInatorService.sync: attachmentSyncObject")
		val attachmentMetadataList = syncAttachments(noteMetadataMap = noteMetadataMap, rectify = rectify) {
			super.syncStat.value.let { syncStat1 -> if (syncStat1 is SyncStat.Syncing) super.syncStat.tryEmit(syncStat1.copy(attachmentSyncObjectStatus = it)) }
		}
		attachmentMetadataList?.let { uploadFile(path = DropboxPath.AttachmentMetadata.path, byteArray = json.encodeToString(it).encodeToByteArray()) }
		super.syncStat.value.let { syncStat1 -> if (syncStat1 is SyncStat.Syncing) super.syncStat.tryEmit(syncStat1.copy(attachmentSyncObjectStatus = ObjectStatus(objectSyncStatus = ObjectSyncStatus.Success))) }
	}

	/**
	 * Syncs a specific type realm objects. [ChapterObject], [NoteObject], [BucketObject], [BucketItemObject], [TagObject].
	 * @param parentPath path of the remote directory
	 * @param rectify if true, hard sync else soft sync. [getFullRemoteMetadata]
	 * @param idGetter id getter of the object
	 * @param toObjectMetadata toObjectMetadata function of the object
	 * @return metadata map of the synced objects. Contains everything needed for a soft sync
	 */
	private suspend inline fun <reified T : TypedRealmObject> DropboxApi.syncObjects(
		parentPath: String,
		rectify: Boolean,
		idGetter: KMutableProperty1<T, RealmUUID>,
		modifiedTimestampGetter: KMutableProperty1<T, Long>,
		toObjectMetadata: KFunction1<T, DropboxObjectMetadata>,
		crossinline objectStatusCallback: (ObjectStatus) -> Unit = {},
	): Map<RealmUUID, DropboxObjectMetadata> {

//		isRectifying : true indicates broken sync. Dropbox only stores timestamps in seconds. So if rectifying, divide the timestamps by 1000
		val (isRectifying, remoteMetadataMap) = getRemoteMetadata(parentPath = parentPath, rectify = rectify).let {
			Pair(it.first, if (it.first) it.second.mapValues { it.value.copy(modifiedTimestamp = it.value.modifiedTimestamp / 1000) } else it.second)
		}
		val localMetadataMap = getLocalMetadata<T>(idGetter = idGetter, toObjectMetadata = toObjectMetadata).mapValues { if (isRectifying) it.value.copy(modifiedTimestamp = it.value.modifiedTimestamp / 1000) else it.value }

		val localRemoteDifference = Maps.difference(localMetadataMap, remoteMetadataMap)

//		Contains deleted as well as existing entries
		val localOnlyEntries = localRemoteDifference.entriesOnlyOnLeft()
		val remoteOnlyEntries = localRemoteDifference.entriesOnlyOnRight()
		val differentEntries = localRemoteDifference.entriesDiffering()

		val objectOperationMap: MutableMap<RealmUUID, ObjectOperation> = mutableMapOf()

//		Local deleted entries
		objectOperationMap.putAll(
			localOnlyEntries
				.filter { it.value.isDeleted }
				.mapValues { ObjectOperation.UpSync.Delete(realmUUID = it.key, path = "$parentPath/${it.key}.json", modifiedTimestamp = it.value.modifiedTimestamp) }
		)
//		Local new entries
		repository
			?.getObjectFromId<T>(idList = localOnlyEntries.filter { !it.value.isDeleted }.keys.toList(), includeLocked = true)
			?.associate { idGetter(it) to ObjectOperation.UpSync.Upsert(realmUUID = idGetter(it), path = "$parentPath/${idGetter(it)}.json", modifiedTimestamp = modifiedTimestampGetter(it)) }
			?.let { objectOperationMap.putAll(it) }

//		Remote only entries
		objectOperationMap.putAll(
			remoteOnlyEntries.mapValues {
				if (it.value.isDeleted) ObjectOperation.DownSync.Delete(realmUUID = it.key)
				else ObjectOperation.DownSync.Upsert(realmUUID = it.key, path = "$parentPath/${it.key}.json")
			}
		)

		differentEntries.forEach { (id, diff) ->
			val localObject = diff.leftValue()
			val remoteObject = diff.rightValue()

			objectOperationMap[id] = when {
				localObject.hash == remoteObject.hash -> ObjectOperation.NoOp.Skip(realmUUID = id, path = "$parentPath/${id}.json")

//				upSync_delete
				localObject.modifiedTimestamp > remoteObject.modifiedTimestamp && localObject.isDeleted ->
					ObjectOperation.UpSync.Delete(realmUUID = id, path = "$parentPath/$id.json", modifiedTimestamp = localObject.modifiedTimestamp)

//				upSync_upsert
				localObject.modifiedTimestamp > remoteObject.modifiedTimestamp && !localObject.isDeleted ->
					ObjectOperation.UpSync.Upsert(realmUUID = id, path = "$parentPath/$id.json", modifiedTimestamp = localObject.modifiedTimestamp)

//				downSync_delete
				localObject.modifiedTimestamp < remoteObject.modifiedTimestamp && remoteObject.isDeleted ->
					ObjectOperation.DownSync.Delete(realmUUID = id)

//				downSync_upsert
				localObject.modifiedTimestamp < remoteObject.modifiedTimestamp && !remoteObject.isDeleted ->
					ObjectOperation.DownSync.Upsert(realmUUID = id, path = "$parentPath/$id.json")

//				downSync_upsert
				localObject.modifiedTimestamp == remoteObject.modifiedTimestamp && localObject.isDeleted != remoteObject.isDeleted && localObject.isDeleted ->
					ObjectOperation.DownSync.Upsert(realmUUID = id, path = "$parentPath/$id.json")

//				upSync_upsert
				localObject.modifiedTimestamp == remoteObject.modifiedTimestamp && localObject.isDeleted != remoteObject.isDeleted && remoteObject.isDeleted ->
					ObjectOperation.UpSync.Upsert(realmUUID = id, path = "$parentPath/$id.json", modifiedTimestamp = localObject.modifiedTimestamp)

//				No op
				else -> ObjectOperation.NoOp.Skip(realmUUID = id, path = "$parentPath/${id}.json")
			}
		}

//		Metadata map of the synced objects
		val newMetadataMap: MutableMap<RealmUUID, DropboxObjectMetadata> = mutableMapOf()

		val toDownSyncObjectList = objectOperationMap.filterValues { it is ObjectOperation.DownSync } as Map<RealmUUID, ObjectOperation.DownSync>
		val toUpSyncObjectList = objectOperationMap.filterValues { it is ObjectOperation.UpSync } as Map<RealmUUID, ObjectOperation.UpSync>
		val objectStatus = ObjectStatus(toUpSync = toUpSyncObjectList.size, toDownSync = toDownSyncObjectList.size, objectSyncStatus = ObjectSyncStatus.Syncing)
		objectStatusCallback(objectStatus)
		Log.d("rits", "DropboxSyncInatorService.syncObjects: $objectStatus")

		val downSyncObjectOperationResultMap = downSyncObjects<T>(downSyncObjectOperationMap = toDownSyncObjectList) { objectStatusCallback(objectStatus.copy(toDownSync = objectStatus.toDownSync - 1)) }
		val upSyncObjectOperationResultList = upSyncObjects<T>(upSyncObjectOperationMap = toUpSyncObjectList) { objectStatusCallback(objectStatus.copy(toUpSync = objectStatus.toUpSync - 1)) }

//		Entries in common
		newMetadataMap.putAll(localRemoteDifference.entriesInCommon())

//		Upsyced entries
		upSyncObjectOperationResultList.forEach { (id, upSyncActiveOperationResult) ->
			val operation = upSyncActiveOperationResult.first
			when (val operationResult = upSyncActiveOperationResult.second) {
				is UploadFileResult.Success -> newMetadataMap[id] = DropboxObjectMetadata.fromMetadata(operationResult.metadata)
				is DeleteFileResult.Success -> newMetadataMap[id] = DropboxObjectMetadata(modifiedTimestamp = operation.modifiedTimestamp, hash = "", isDeleted = true)
				is DeleteFileResult.Error.DeleteError -> newMetadataMap[id] = DropboxObjectMetadata(modifiedTimestamp = operation.modifiedTimestamp, hash = "", isDeleted = false)
				is UploadFileResult.Error -> if (BuildConfig.DEBUG) operationResult.exception?.printStackTrace()
				else -> Unit
			}
		}

//		Downsynced entries
		downSyncObjectOperationResultMap.forEach { (id, downSyncOperationResult) ->
//			val operation = downSyncOperationResult.first
			val operationResult = downSyncOperationResult.second
			when (operationResult) {
//				downSync_upsert downSync_delete success
				true -> remoteMetadataMap[id]?.let { newMetadataMap[id] = it }
//				downSync_upsert error
				false -> remoteMetadataMap[id]?.let { newMetadataMap[id] = it }
			}
		}

//		No op entries
		differentEntries.filterValues { it is ObjectOperation.NoOp }.mapValues { it.value.rightValue() }.let { newMetadataMap.putAll(it) }

//		Merges metadata of synced entries and no op entries. That's everything required for a soft sync
		return newMetadataMap + objectOperationMap.filterValues { it is ObjectOperation.NoOp }.mapValues { localMetadataMap[it.key]!! }
	}

	/**
	 * Syncs all attachments with remote
	 * @param noteMetadataMap metadata map of the synced notes. Obtained from [syncObjects] of [NoteObject]
	 * @param rectify if true, hard sync else soft sync. [getFullRemoteAttachmentMetadata]
	 * @param objectStatusCallback callback to update the sync status
	 */
	private suspend fun DropboxApi.syncAttachments(
		noteMetadataMap: Map<RealmUUID, DropboxObjectMetadata>,
		rectify: Boolean,
		objectStatusCallback: (ObjectStatus) -> Unit = {}
	): List<DropboxAttachmentMetadata>? {

//		https://graphite.youtrack.cloud/articles/GRAPHITE-A-29/Attachment-sync

		val localAttachmentMetadataList = getLocalAttachmentMetadata() ?: return null
		val remoteAttachmentMetadataList = getRemoteAttachmentMetadata(rectify = rectify)

		val deletedNoteIdList = noteMetadataMap.filterValues { it.isDeleted }.keys

		val combinedAttachmentMetadataMap: MutableMap<DropboxAttachmentMetadata, CombinedAttachmentMetadata> = mutableMapOf()

		localAttachmentMetadataList.forEach { attachmentMetadata ->
			combinedAttachmentMetadataMap[attachmentMetadata] = CombinedAttachmentMetadata(
				attachmentIdentity = attachmentMetadata.attachmentIdentity,
				isParentDeleted = attachmentMetadata.attachmentIdentity.parentId in deletedNoteIdList,
				isRemoteExist = false,
				isRemoteDeleted = false,
				isLocalExist = !attachmentMetadata.isDeleted,
				isLocalDeleted = attachmentMetadata.isDeleted,
			)
		}

		remoteAttachmentMetadataList.forEach { attachmentMetadata ->
			combinedAttachmentMetadataMap[attachmentMetadata] = combinedAttachmentMetadataMap[attachmentMetadata]?.copy(
				isRemoteExist = !attachmentMetadata.isDeleted,
				isRemoteDeleted = attachmentMetadata.isDeleted,
			) ?: CombinedAttachmentMetadata(
				attachmentIdentity = attachmentMetadata.attachmentIdentity,
				isParentDeleted = attachmentMetadata.attachmentIdentity.parentId in deletedNoteIdList,
				isRemoteExist = !attachmentMetadata.isDeleted,
				isRemoteDeleted = attachmentMetadata.isDeleted,
				isLocalExist = false,
				isLocalDeleted = false,
			)
		}

		val toDownSyncAttachmentList = combinedAttachmentMetadataMap.filter { it.value.downSyncOp != AttachmentOperation.NoOp }.values.toList()
		val toUpSyncAttachmentList = combinedAttachmentMetadataMap.filter { it.value.upSyncOp != AttachmentOperation.NoOp }.values.toList()
		val objectStatus = ObjectStatus(toUpSync = toUpSyncAttachmentList.size, toDownSync = toDownSyncAttachmentList.size, objectSyncStatus = ObjectSyncStatus.Syncing)
		objectStatusCallback(objectStatus)
		Log.d("rits", "DropboxSyncInatorService.syncAttachments: $objectStatus")

		downSyncAttachments(toDownSyncOperationList = combinedAttachmentMetadataMap.filter { it.value.downSyncOp != AttachmentOperation.NoOp }.values.toList()) { objectStatusCallback(objectStatus.copy(toDownSync = objectStatus.toDownSync - 1)) }

		val upSyncOperationResultMap =
			upSyncAttachments(toUpSyncOperationList = combinedAttachmentMetadataMap.filter { it.value.upSyncOp != AttachmentOperation.NoOp }.values.toList()) { objectStatusCallback(objectStatus.copy(toUpSync = objectStatus.toUpSync - 1)) }

		val newAttachmentMetadataMap = combinedAttachmentMetadataMap.map { it.key.attachmentIdentity to it.value.attachmentMetadata }.toMap().toMutableMap()
		upSyncOperationResultMap.filter { it.value is UploadFileResult.Error }.forEach { (attachmentIdentity, _) ->
			newAttachmentMetadataMap.remove(attachmentIdentity)
		}
		upSyncOperationResultMap.filter { it.value is DeleteFileResult.Error }.forEach { (attachmentIdentity, _) ->
			newAttachmentMetadataMap[attachmentIdentity]?.let { newAttachmentMetadataMap[attachmentIdentity] = it.copy(isDeleted = false) }
		}

		return newAttachmentMetadataMap.values.toList().filterNotNull()
	}

	/**
	 * Get the metadata map of the local data. Includes deleted entries as well as existing entries
	 * @param idGetter id getter of the object
	 * @param toObjectMetadata toObjectMetadata function of the object
	 * @return metadata map of the local data
	 */
	private inline fun <reified T : TypedRealmObject> getLocalMetadata(idGetter: KMutableProperty1<T, RealmUUID>, toObjectMetadata: KFunction1<T, DropboxObjectMetadata>): Map<RealmUUID, DropboxObjectMetadata> {
		val deletedObjectMetadata = repository?.getDeletedObjectOfType<T>() ?: mapOf()
		val storedObjectMetadata = (repository?.getAllObjectOfType<T>(includeLocked = true)?.associate { idGetter(it) to toObjectMetadata(it) } ?: mapOf())
		return deletedObjectMetadata + storedObjectMetadata
	}

	/**
	 * Get the metadata map of the remote directory, if the metadata.json is not found, scan all the files in the remote directory and return the metadata map with [rectify] = true
	 * @param parentPath path of the remote directory
	 * @param rectify if true, scan all the files in the remote directory and return the metadata map. [getFullRemoteMetadata]
	 * @return Pair of shouldRectify and metadataMap
	 * @see [getFullRemoteMetadata]
	 */
	private suspend fun DropboxApi.getRemoteMetadata(parentPath: String, rectify: Boolean): Pair<Boolean, Map<RealmUUID, DropboxObjectMetadata>> = withConnection {
		if (rectify) return@withConnection Pair(true, getFullRemoteMetadata(path = parentPath))
		else when (val remoteMetadataDownloadFileResult = downloadFile(path = "$parentPath/metadata.json")) {
			is DownloadFileResult.Success -> {
				try {
					return@withConnection Pair(false, json.decodeFromString(MapSerializer(RealmUUIDSerializer, DropboxObjectMetadata.serializer()), remoteMetadataDownloadFileResult.byteArray.decodeToString()))
				} catch (e: Exception) {
					if (BuildConfig.DEBUG) e.printStackTrace()
					Pair(true, getFullRemoteMetadata(path = parentPath))
				}
			}

			is DownloadFileResult.FileNotFound -> return@withConnection Pair(true, getFullRemoteMetadata(path = parentPath))
			is DownloadFileResult.Error.DownloadError -> onUnknownError(remoteMetadataDownloadFileResult.exception)
			is DownloadFileResult.Error.CredentialsError -> onCredentialsError()
			is DownloadFileResult.Error.NetworkError -> onNetworkError()
			is DownloadFileResult.Error.UnknownError -> onUnknownError(remoteMetadataDownloadFileResult.exception)
		}

		return@withConnection Pair(false, mapOf())
	} ?: Pair(false, mapOf())

	/**
	 * Scan all the files in remote directory and return the metadata map. If the remote directory is not found, return an empty map
	 * @param path path of the remote directory
	 */
	private suspend fun DropboxApi.getFullRemoteMetadata(path: String): Map<RealmUUID, DropboxObjectMetadata> = withConnection {
		val metadataMap: MutableMap<RealmUUID, DropboxObjectMetadata> = mutableMapOf()
		when (val listFolderResult = listFolder(path = path)) {
			is ListFolderResult.Success -> {
				listFolderResult.metadataList.forEach { metadata ->
					try {
						val realmUUID = RealmUUID.from(metadata.name.substringBeforeLast("."))
						metadataMap[realmUUID] = DropboxObjectMetadata.fromMetadata(metadata)
					} catch (e: Exception) {
						if (BuildConfig.DEBUG) e.printStackTrace()
					}
				}
				return@withConnection metadataMap
			}

			is ListFolderResult.Error.FolderNotFound -> return@withConnection metadataMap
			is ListFolderResult.Error.ListFolderError -> return@withConnection metadataMap
			is ListFolderResult.Error.CredentialsError -> onCredentialsError()
			is ListFolderResult.Error.NetworkError -> onNetworkError()
			is ListFolderResult.Error.UnknownError -> onUnknownError(listFolderResult.exception)
		}
		return@withConnection metadataMap
	} ?: mapOf()

	private fun getLocalAttachmentMetadata(): List<DropboxAttachmentMetadata>? {
		val baseObject = repository?.getBaseObject() ?: return null
		val localDeletedAttachmentList = baseObject.deletedAttachmentSet.map { DropboxAttachmentMetadata(attachmentIdentity = it, isDeleted = true) }
		val localExistingAttachmentList = (repository?.attachmentRepository?.getAllAttachmentLite() ?: listOf()).map { DropboxAttachmentMetadata(attachmentIdentity = it, isDeleted = false) }

		return (localDeletedAttachmentList + localExistingAttachmentList).toList()
	}

	private suspend fun DropboxApi.getRemoteAttachmentMetadata(rectify: Boolean): List<DropboxAttachmentMetadata> {
		if (rectify) return getFullRemoteAttachmentMetadata()
		else when (val remoteMetadataDownloadFileResult = downloadFile(path = DropboxPath.AttachmentMetadata.path)) {
			is DownloadFileResult.Success -> {
				return try {
					json.decodeFromString(ListSerializer(DropboxAttachmentMetadata.serializer()), remoteMetadataDownloadFileResult.byteArray.decodeToString())
				} catch (e: Exception) {
					if (BuildConfig.DEBUG) e.printStackTrace()
					getFullRemoteAttachmentMetadata()
				}
			}

			is DownloadFileResult.FileNotFound -> return getFullRemoteAttachmentMetadata()
			is DownloadFileResult.Error.DownloadError -> onUnknownError(remoteMetadataDownloadFileResult.exception)
			is DownloadFileResult.Error.CredentialsError -> onCredentialsError()
			is DownloadFileResult.Error.NetworkError -> onNetworkError()
			is DownloadFileResult.Error.UnknownError -> onUnknownError(remoteMetadataDownloadFileResult.exception)
		}

		return listOf()
	}

	private suspend fun DropboxApi.getFullRemoteAttachmentMetadata(): List<DropboxAttachmentMetadata> = withConnection {
		val metadataList: MutableList<DropboxAttachmentMetadata> = mutableListOf()
		when (val listFolderResult = listFolder(path = DropboxPath.Attachment.path)) {
			is ListFolderResult.Success -> {
				listFolderResult.metadataList.forEach { metadata ->
//					val attachmentIdentity = AttachmentIdentity.from(metadata.name.substringBeforeLast("."))
//					metadataList.add(attachmentIdentity)
				}
				return@withConnection metadataList
			}

			is ListFolderResult.Error.FolderNotFound -> return@withConnection metadataList
			is ListFolderResult.Error.ListFolderError -> return@withConnection metadataList
			is ListFolderResult.Error.CredentialsError -> onCredentialsError()
			is ListFolderResult.Error.NetworkError -> onNetworkError()
			is ListFolderResult.Error.UnknownError -> onUnknownError(listFolderResult.exception)
		}
		return@withConnection metadataList
	} ?: listOf()

	/**
	 * Upload the metadata map to the remote directory, Overwrites the existing metadata.json
	 * @param parentPath path of the remote directory
	 * @param metadataMap metadata map to be uploaded
	 * @see [getRemoteMetadata]
	 */
	private fun DropboxApi.upSyncMetadata(parentPath: String, metadataMap: Map<RealmUUID, DropboxObjectMetadata>) {
		val metadataString = json.encodeToString(MapSerializer(RealmUUIDSerializer, DropboxObjectMetadata.serializer()), metadataMap)
		when (val uploadFileResult = uploadFile(path = "$parentPath/metadata.json", byteArray = metadataString.encodeToByteArray())) {
			is UploadFileResult.Success -> Unit
			is UploadFileResult.Error.UploadError -> onUnknownError(uploadFileResult.exception)
			is UploadFileResult.Error.CredentialsError -> onCredentialsError()
			is UploadFileResult.Error.NetworkError -> onNetworkError()
			is UploadFileResult.Error.UnknownError -> onUnknownError(uploadFileResult.exception)
		}
	}

	/**
	 * Download the files from the remote directory. Performs the downsync operations, [ObjectOperation.DownSync.Upsert] and [ObjectOperation.DownSync.Delete]
	 * @param downSyncObjectOperationMap Map of [RealmUUID] and [ObjectOperation.DownSync]
	 * @param singleOperationCallback callback after operation on each object. True if the operation is successful, false on error
	 * @return Returns result of the downsync operations. Map of [RealmUUID] to pair of [ObjectOperation.DownSync] and [Boolean]. Boolean is true if the operation is successful, false on error
	 * @see [downSyncObjects]
	 */
	private suspend inline fun <reified T : TypedRealmObject> DropboxApi.downSyncObjects(
		downSyncObjectOperationMap: Map<RealmUUID, ObjectOperation.DownSync>,
		crossinline singleOperationCallback: (Boolean) -> Unit
	): Map<RealmUUID, Pair<ObjectOperation.DownSync, Boolean>> {
		val downSyncObjectOperationResultMap: MutableMap<RealmUUID, Pair<ObjectOperation.DownSync, Boolean>> = mutableMapOf()
		downSyncObjectOperationMap.forEach { (id, downSyncOperation) ->
			when (downSyncOperation) {
				is ObjectOperation.DownSync.Upsert -> withConnection {
					val downloadFileResult = downloadFile(path = downSyncOperation.path)
					if (downloadFileResult is DownloadFileResult.Success) {
						val jsonObject = JSONObject(downloadFileResult.byteArray.decodeToString())
						try {
							when (T::class) {
								ChapterObject::class -> {
									val chapterObject = ChapterObject(byteArray = downloadFileResult.byteArray)
									repository?.putChapter(chapterObject = chapterObject, modifyTimestampAuto = false)
								}

								NoteObject::class -> {
									val noteObject = NoteObject(byteArray = downloadFileResult.byteArray)
									repository?.putNote(noteObject = noteObject, modifyTimestampAuto = false)
								}

								BucketObject::class -> {
									val bucketObject = BucketObject(byteArray = downloadFileResult.byteArray)
									repository?.putBucket(bucketObject = bucketObject, modifyTimestampAuto = false)
								}

								BucketItemObject::class -> {
									val bucketItemObject = BucketItemObject(byteArray = downloadFileResult.byteArray)
									repository?.putBucketItem(bucketItemObject = bucketItemObject, modifyTimestampAuto = false)
								}

								TagObject::class -> {
									val tagObject = TagObject(byteArray = downloadFileResult.byteArray)
									repository?.putTag(tagObject = tagObject, modifyTimestampAuto = false)
								}
							}
							singleOperationCallback(true)
							downSyncObjectOperationResultMap[id] = Pair(downSyncOperation, true)
						} catch (e: Exception) {
							if (BuildConfig.DEBUG) e.printStackTrace()
							downSyncObjectOperationResultMap[id] = Pair(downSyncOperation, false)
							singleOperationCallback(false)
						}
					}
				}

				is ObjectOperation.DownSync.Delete -> {
					repository?.delete(id = id, keepHistory = true)
					downSyncObjectOperationResultMap[id] = Pair(downSyncOperation, true)
					singleOperationCallback(true)
				}
			}
		}

		return downSyncObjectOperationResultMap
	}

	/**
	 * Upload the files to the remote directory. Performs the upsync operations, [ObjectOperation.UpSync.Upsert] and [ObjectOperation.UpSync.Delete]
	 * @param upSyncObjectOperationMap upsync operation map
	 * @param singleOperationCallback callback after operation on each object. True if the operation is successful, false on error
	 * @return Returns result of the upsync operations
	 * @see [upSyncObjects]
	 */
	private suspend inline fun <reified T : TypedRealmObject> DropboxApi.upSyncObjects(
		upSyncObjectOperationMap: Map<RealmUUID, ObjectOperation.UpSync>,
		singleOperationCallback: (Boolean) -> Unit
	): Map<RealmUUID, Pair<ObjectOperation.UpSync, DropboxOperation.ActiveOperationResult>> {
		val upSyncObjectOperationResultMap: MutableMap<RealmUUID, Pair<ObjectOperation.UpSync, DropboxOperation.ActiveOperationResult>> = mutableMapOf()
		upSyncObjectOperationMap.forEach { (id, upSyncOperation) ->
			val objectOperationResult = withConnection {
				when (upSyncOperation) {
					is ObjectOperation.UpSync.Upsert -> {
						val syncable = repository?.getObjectFromId<T>(id = upSyncOperation.realmUUID) as? Syncable
						return@withConnection syncable?.toCloudSnapshot()
							?.let { uploadFile(path = upSyncOperation.path, byteArray = it.encodeToByteArray(), modifiedTimestamp = upSyncOperation.modifiedTimestamp) }
							?: UploadFileResult.Error.UnknownError()
					}

					is ObjectOperation.UpSync.Delete -> return@withConnection deleteFile(path = upSyncOperation.path)
				}
			}
			singleOperationCallback(objectOperationResult is UploadFileResult.Success)
			objectOperationResult?.let { upSyncObjectOperationResultMap[upSyncOperation.realmUUID] = Pair(upSyncOperation, it) }
		}

		return upSyncObjectOperationResultMap
	}

	/**
	 * Downloads attachments from the remote directory. Performs the downsync operations, [AttachmentOperation.Upsert] and [AttachmentOperation.Delete]
	 * @param toDownSyncOperationList downsync operation list
	 * @param singleOperationCallback callback after operation on each attachment. True if the operation is successful, false on error
	 */
	private fun DropboxApi.downSyncAttachments(
		toDownSyncOperationList: List<CombinedAttachmentMetadata>,
		singleOperationCallback: (Boolean) -> Unit
	) {
		toDownSyncOperationList.forEach { combinedAttachmentMetadata ->
			when (combinedAttachmentMetadata.downSyncOp) {
				is AttachmentOperation.Upsert -> {
					when (val downloadFileResult = downloadFile(path = combinedAttachmentMetadata.dropboxRemotePath)) {
						is DownloadFileResult.Success -> {
							repository?.attachmentRepository?.putAttachment(attachmentIdentity = combinedAttachmentMetadata.attachmentIdentity, byteArray = downloadFileResult.byteArray)
							singleOperationCallback(true)
						}

						is DownloadFileResult.FileNotFound -> singleOperationCallback(false)
						is DownloadFileResult.Error -> singleOperationCallback(false)
					}
				}

				is AttachmentOperation.Delete -> {
					repository?.deleteAttachment(attachmentIdentity = combinedAttachmentMetadata.attachmentIdentity, keepHistory = false)
					singleOperationCallback(true)
				}

				is AttachmentOperation.NoOp -> Unit
			}
		}
	}

	/**
	 * Uploads attachments to the remote directory. Performs the upsync operations, [AttachmentOperation.Upsert] and [AttachmentOperation.Delete]
	 * @param toUpSyncOperationList upsync operation list
	 * @param singleOperationCallback callback after operation on each attachment. True if the operation is successful, false on error
	 * @return Returns result of the upsync operations
	 */
	private fun DropboxApi.upSyncAttachments(
		toUpSyncOperationList: List<CombinedAttachmentMetadata>,
		singleOperationCallback: (Boolean) -> Unit
	): MutableMap<AttachmentIdentity, DropboxOperation.ActiveOperationResult> {
		val operationResultMap: MutableMap<AttachmentIdentity, DropboxOperation.ActiveOperationResult> = mutableMapOf()
		repository?.let { repository1 ->
			toUpSyncOperationList.forEach { combinedAttachmentMetadata ->
				when (combinedAttachmentMetadata.upSyncOp) {
					is AttachmentOperation.Upsert -> {
						operationResultMap[combinedAttachmentMetadata.attachmentIdentity] =
							uploadFile(path = combinedAttachmentMetadata.dropboxRemotePath, byteArray = combinedAttachmentMetadata.getFile(repository = repository1).readBytes())
						singleOperationCallback(true)
					}

					is AttachmentOperation.Delete -> {
						operationResultMap[combinedAttachmentMetadata.attachmentIdentity] = deleteFile(path = combinedAttachmentMetadata.dropboxRemotePath)
						singleOperationCallback(true)
					}

					is AttachmentOperation.NoOp -> Unit
				}
			}
		}

		return operationResultMap
	}
}
