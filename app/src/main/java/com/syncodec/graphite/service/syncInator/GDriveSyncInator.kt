package com.syncodec.graphite.service.syncInator

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.common.collect.Maps
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.cloud.googleDrive.GDrive
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.DeletedAttachment
import com.syncodec.graphite.di.model.DeletedObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.io.IOException
import java.time.Instant


class GDriveSyncInatorService : SyncInatorService() {
	private val logTransfer = BuildConfig.DEBUG
	private val syncChapter = true
	private val syncNote = true
	private val syncBucket = true
	private val syncBucketItem = true
	private val syncTag = true
	private val syncAttachment = true
//	private val compress = false

	private val json = Json { ignoreUnknownKeys = true }

	private var gDriveSyncInatorBinder: IBinder? = GDriveSyncInatorBinder()

	var isUnbounded = false

	val gDrive: GDrive by inject()

	var lastSyncTime: Long? = null

	override fun onCreate() {
		super.onCreate()

		lifecycleScope.launch(Dispatchers.Default) {
			while (true) {
				delay(1000)
				if (isUnbounded) {
					startSelfDestructSequence()
					break
				}
			}
		}

		lifecycleScope.launch(Dispatchers.IO) {
			val isAutoSyncEnabled = syncDataStoreInstance.isAutoSyncEnabledFlow.first()
			Log.d("npr71", "GDriveSyncInatorService.onCreate : isAutoSyncEnabled = $isAutoSyncEnabled")
			if (isAutoSyncEnabled) onClickSyncNow() else syncStatus.tryEmit(SyncInatorService.Companion.SyncStatus.AutoSyncDisabled)
		}
	}

	private fun startSelfDestructSequence() {
		Log.d("npr71", "GDriveSyncInatorService.startSelfDestructSequence")
		lifecycleScope.launch(Dispatchers.Default) {
			while (true) {
				delay(1000)
				if (
					syncStatus.value is SyncInatorService.Companion.SyncStatus.Init ||
					syncStatus.value is SyncInatorService.Companion.SyncStatus.Idle ||
					syncStatus.value is SyncInatorService.Companion.SyncStatus.Locked ||
					syncStatus.value is SyncInatorService.Companion.SyncStatus.AutoSyncDisabled ||
					syncStatus.value is SyncInatorService.Companion.SyncStatus.Failed ||
					syncStatus.value is SyncInatorService.Companion.SyncStatus.CredentialError
				) {
					break
				}
			}
			gDriveSyncInatorBinder = null
			stopSelf()
		}
	}

	override fun onBind(intent: Intent): IBinder? {
		super.onBind(intent)
		return gDriveSyncInatorBinder
	}

	fun hardCutOff() {
		Log.d("npr71", "GDriveSyncInatorService.hardCutOff")
		reSyncCoroutine?.cancel()
		reSyncCoroutine = null
		syncCoroutine?.cancel()
		syncCoroutine = null
		gDriveSyncInatorBinder = null
		stopSelf()
	}

	fun onClickSyncNow() {
		Log.d("npr71", "GDriveSyncInatorService.onClickSyncNow : ${syncStatus.value::class.simpleName}")
		if (syncStatus.value is SyncInatorService.Companion.SyncStatus.Init
			|| syncStatus.value is SyncInatorService.Companion.SyncStatus.Idle
			|| syncStatus.value is SyncInatorService.Companion.SyncStatus.Locked
			|| syncStatus.value is SyncInatorService.Companion.SyncStatus.AutoSyncDisabled
			|| syncStatus.value is SyncInatorService.Companion.SyncStatus.Failed
			|| syncStatus.value is SyncInatorService.Companion.SyncStatus.CredentialError
		) {
			reSyncCoroutine?.cancel()
			reSyncCoroutine = null
			syncCoroutine?.cancel()
			syncCoroutine = null
			sync()
		}
	}

	fun onClickForceSync() {
		reSyncCoroutine?.cancel()
		reSyncCoroutine = null
		syncCoroutine?.cancel()
		syncCoroutine = null
		sync(forced = true)
	}

	private suspend fun getGetDriveClient(callback: suspend (Drive?) -> Unit) {
		callback(gDrive.getDrive())
	}

	private var syncCoroutine: CoroutineScope? = null
	private val previousSyncSessionIdList = mutableSetOf<String>()

	private fun sync(forced: Boolean = false) {
		Log.d("npr71", "GDriveSyncInatorService.sync : forced = $forced")
		if (syncCoroutine == null) {
			lifecycleScope.launch(Dispatchers.IO) {
				syncCoroutine = this
				getGetDriveClient { gDrive ->
					gDrive?.tryHoldLockAndContinue(forced = forced) ?: syncStatus.tryEmit(SyncInatorService.Companion.SyncStatus.CredentialError)
				}
			}
		}
	}

	private suspend fun Drive.tryHoldLockAndContinue(forced: Boolean = false) {
		syncStatus.tryEmit(SyncInatorService.Companion.SyncStatus.Connected)
		val googleDriveState = gDrive.getGoogleDriveState(drive = this)
		if (googleDriveState is GDrive.Companion.GoogleDriveState.Success) {
			if (forced) {
				breakLockAndContinue(googleDriveState = googleDriveState)
			}
			else {
//				*	Normal flow
				if (googleDriveState.lockFileId == null) lockAndContinue(rectify = false, googleDriveState = googleDriveState)
				else {
					val downloadResult = gDrive.downloadData(drive = this, googleDriveState.lockFileId)
					when(downloadResult) {
						is DownloadResult.FileNotFound -> lockAndContinue(rectify = false, googleDriveState = googleDriveState)
						is DownloadResult.Success -> {
							val jsonObject = JSONObject(downloadResult.fileContent.toString(Charsets.UTF_8))
							val timestamp = jsonObject.getLong("timestamp")
							val sessionId = jsonObject.getString("sessionId")

							when {
								sessionId in previousSyncSessionIdList -> lockAndContinue(rectify = false, googleDriveState = googleDriveState)
								(Instant.now().toEpochMilli() - timestamp) > 60000 -> breakLockAndContinue(googleDriveState = googleDriveState)
								else -> waitToUnlock()
							}
						}
						else -> unknownError()
					}
				}
			}
		}
	}

	private fun Drive.networkError() {
		syncStatus.tryEmit(SyncInatorService.Companion.SyncStatus.Failed("network error"))
		syncCoroutine?.cancel()
		syncCoroutine = null
		reSyncCoroutine?.cancel()
		reSyncCoroutine = null
	}

	private fun Drive.unknownError(exception: Exception? = null) {
		exception?.printStackTrace()
		syncStatus.tryEmit(SyncInatorService.Companion.SyncStatus.Failed(exception?.message ?: "unknown error"))
		syncCoroutine?.cancel()
		syncCoroutine = null
		reSyncCoroutine?.cancel()
		reSyncCoroutine = null
	}

	private suspend fun Drive.waitToUnlock() {
		syncStatus.tryEmit(SyncInatorService.Companion.SyncStatus.Locked)
		delay(30000)
		tryHoldLockAndContinue()
	}

	private fun Drive.lockAndContinue(
		rectify: Boolean,
		googleDriveState: GDrive.Companion.GoogleDriveState.Success,
	) {
		Log.d("npr71", "GDriveSyncInatorService.lockAndContinue : rectify = $rectify")
		val sessionId = RealmUUID.random().toString()
		previousSyncSessionIdList.add(sessionId)
		syncStatus.tryEmit(SyncInatorService.Companion.SyncStatus.Syncing(sessionId))
		keepLockAlive(sessionId = sessionId, googleDriveState = googleDriveState)
		gatherData(googleDriveState = googleDriveState, rectify = rectify)
		unlock(sessionId = sessionId)
		reSync()
	}

	private suspend fun Drive.breakLockAndContinue(
		googleDriveState: GDrive.Companion.GoogleDriveState.Success,
	) {
		unlock(sessionId = null)
		lockAndContinue(rectify = true, googleDriveState = googleDriveState)
	}

	private var reSyncCoroutine: CoroutineScope? = null
	private fun reSync() {
		syncCoroutine?.cancel()
		syncCoroutine = null
		reSyncCoroutine?.cancel()
		lifecycleScope.launch(Dispatchers.IO) {
			val isAutoSyncEnabled = syncDataStoreInstance.isAutoSyncEnabledFlow.first()
			if (isAutoSyncEnabled) {
				syncStatus.tryEmit(SyncInatorService.Companion.SyncStatus.Idle(syncedTimestamp = Instant.now().toEpochMilli(), isAutoSyncDisabled = false))
				reSyncCoroutine = this
				delay(30000)
				sync()
			}
			else {
				syncStatus.tryEmit(SyncInatorService.Companion.SyncStatus.Idle(syncedTimestamp = Instant.now().toEpochMilli(), isAutoSyncDisabled = true))
			}
		}
	}

	private var keepLockAliveCoroutine: CoroutineScope? = null
	private fun Drive.keepLockAlive(
		sessionId: String,
		googleDriveState: GDrive.Companion.GoogleDriveState.Success,
	) {
		lifecycleScope.launch(Dispatchers.IO) {
			keepLockAliveCoroutine?.cancel()
			keepLockAliveCoroutine = this

			var lockFileId = googleDriveState.lockFileId

			while (true) {
				val jsonObject = JSONObject()
				val modifiedTime = Instant.now().toEpochMilli()
				jsonObject.put("timestamp", modifiedTime)
				jsonObject.put("sessionId", sessionId)

				lockFileId?.let {
					gDrive.downloadData(drive = this@keepLockAlive, it)
					updateFile(
						fileId = it,
						fileName = GDrive.LockFileName,
						mimeType = GDrive.JsonFileMimeType,
						modifiedTime = modifiedTime,
						byteArray = jsonObject.toString().toByteArray(Charsets.UTF_8),
					)
				} ?: run {
					val retrieveFile = gDrive
						.createFile(
							drive = this@keepLockAlive,
							fileName = GDrive.LockFileName,
							mimeType = GDrive.JsonFileMimeType,
							modifiedTime = modifiedTime,
							parentId = GDrive.RootFolderId,
							byteArray = jsonObject.toString().toByteArray(Charsets.UTF_8),
						)
					when (retrieveFile) {
						is RetrieveFile.Success -> {
							Log.d("npr71", "lock file created")
							lockFileId = retrieveFile.fileId
						}

						is RetrieveFile.TooManyRetries -> unknownError(java.lang.Exception("too many retries"))
						is RetrieveFile.ParentNotFound -> unknownError(java.lang.Exception("parent not found"))
						is RetrieveFile.UnknownError -> unknownError(java.lang.Exception("unknown error"))
					}
				}

				delay(30000)
			}
		}
	}

	private fun Drive.unlock(sessionId: String?) {
		Log.d("npr71", "unlock")
		keepLockAliveCoroutine?.cancel()
		val googleDriveState = gDrive.getGoogleDriveState(drive = this)
		if (googleDriveState is GDrive.Companion.GoogleDriveState.Success) {
			googleDriveState.lockFileId?.let {
				val lockFileDownloadResult = gDrive.downloadData(drive = this, it)
				if (lockFileDownloadResult is DownloadResult.Success) {
					val jsonObject = JSONObject(String(lockFileDownloadResult.fileContent, Charsets.UTF_8))
					val remoteSessionId = jsonObject.getString("sessionId")
					if (remoteSessionId == sessionId || sessionId == null) {
						try {
							gDrive.deleteFile(drive = this, fileId = it).let {
								if (it is DeleteResult.Success) Log.d("npr71", "unlock success")
								else Log.d("npr71", "unlock failed")
							}
						} catch (e: Exception) {
							unknownError(e)
						}
					}
				}
			}
		}
		else {

		}
	}

	private fun Drive.gatherData(
		googleDriveState: GDrive.Companion.GoogleDriveState.Success,
		rectify: Boolean,
	) {
		val baseObject = repository.getBaseObject()
		val noteObjectList = repository.getAllNote()
		val chapterObjectList = repository.getAllChapter()
		val bucketItemObjectList = repository.getAllBucketItem()
		val bucketObjectList = repository.getAllBucket()
		val tagObjectList = repository.getAllTag()

		val localDeletedObjectIdList = baseObject?.deletedObjectSet ?: setOf()

		val deletedObjectDownloadResult = getDeletedObjectMap(fileId = googleDriveState.deletedObjectFileId)
		if (deletedObjectDownloadResult !is DownloadResult.Success) return

		val remoteDeletedObjectSet: Set<DeletedObject> = 			try {
			if (deletedObjectDownloadResult.fileContent.isEmpty()) setOf()
			else json.decodeFromString(String(deletedObjectDownloadResult.fileContent)) ?: setOf()
		} catch (e: Exception) {
			setOf()
		}

		Log.d("npr71", "remoteDeletedObjectSet: ${remoteDeletedObjectSet.size}")

		val deletedObjectList: MutableSet<DeletedObject> = mutableSetOf()

		if (syncChapter) syncObjects(
			objectType = ChapterObject::class.simpleName!!,
			localObjectMap = chapterObjectList.associate { it.id to Pair(it.toCloudSnapshot(), it.modifiedTimestamp) },
			remoteDeletedObjectMap = remoteDeletedObjectSet.associateBy { it.id },
			localDeletedObjectMap = localDeletedObjectIdList.filter { it.objectType == ChapterObject::class.simpleName }.associateBy { it.id },
			folderId = googleDriveState.chapterFolderId,
		) { content, fileId ->
			ChapterObject.fromCloudSnapshot(content)?.let { chapterObject ->
				repository.putChapterSuspended(chapterObject = chapterObject, modifyTimestampAuto = false)
			}
		}.let { deletedObjectList.addAll(it) }

		if (syncNote) syncObjects(
			objectType = NoteObject::class.simpleName!!,
			localObjectMap = noteObjectList.associate { it.id to Pair(it.toCloudSnapshot(), it.modifiedTimestamp) },
			remoteDeletedObjectMap = remoteDeletedObjectSet.associateBy { it.id },
			localDeletedObjectMap = localDeletedObjectIdList.filter { it.objectType == NoteObject::class.simpleName }.associateBy { it.id },
			folderId = googleDriveState.noteFolderId,
		) { content, fileId ->
			NoteObject.fromCloudSnapshot(content)?.let { noteObject ->
				repository.putNoteSuspended(noteObject = noteObject, modifyTimestampAuto = false)
			}
		}.let { deletedObjectList.addAll(it) }

		if (syncBucket) syncObjects(
			objectType = BucketObject::class.simpleName!!,
			localObjectMap = bucketObjectList.associate { it.id to Pair(it.toCloudSnapshot(), it.modifiedTimestamp) },
			remoteDeletedObjectMap = remoteDeletedObjectSet.associateBy { it.id },
			localDeletedObjectMap = localDeletedObjectIdList.filter { it.objectType == BucketObject::class.simpleName }.associateBy { it.id },
			folderId = googleDriveState.bucketFolderId,
		) { content, fileId ->
			BucketObject.fromCloudSnapshot(content)?.let { bucketObject ->
				repository.putBucketSuspended(bucketObject = bucketObject, modifyTimestampAuto = false, googleDriveId = fileId)
			}
		}.let { deletedObjectList.addAll(it) }

		if (syncBucketItem) syncObjects(
			objectType = BucketItemObject::class.simpleName!!,
			localObjectMap = bucketItemObjectList.associate { it.id to Pair(it.toCloudSnapshot(), it.modifiedTimestamp) },
			remoteDeletedObjectMap = remoteDeletedObjectSet.associateBy { it.id },
			localDeletedObjectMap = localDeletedObjectIdList.filter { it.objectType == BucketItemObject::class.simpleName }.associateBy { it.id },
			folderId = googleDriveState.bucketItemFolderId,
		) { content, fileId ->
			BucketItemObject.fromCloudSnapshot(content)?.let { bucketItemObject ->
				repository.putBucketItemSuspended(bucketItemObject = bucketItemObject, modifyTimestampAuto = false, googleDriveId = fileId)
			}
		}.let { deletedObjectList.addAll(it) }

		if (syncTag) syncObjects(
			objectType = TagObject::class.simpleName!!,
			localObjectMap = tagObjectList.associate { it.id to Pair(it.toCloudSnapshot(), it.modifiedTimestamp) },
			remoteDeletedObjectMap = remoteDeletedObjectSet.associateBy { it.id },
			localDeletedObjectMap = localDeletedObjectIdList.filter { it.objectType == TagObject::class.simpleName }.associateBy { it.id },
			folderId = googleDriveState.tagFolderId,
		) { content, fileId ->
			TagObject.fromCloudSnapshot(content)?.let { tagObject ->
				repository.putTagSuspended(tagObject = tagObject, modifyTimestampAuto = false, googleDriveId = fileId)
			}
		}.let { deletedObjectList.addAll(it) }

		if (deletedObjectList != remoteDeletedObjectSet) {
			updateFile(
				fileId = googleDriveState.deletedObjectFileId,
				fileName = GDrive.DeletedObjectFileName,
				mimeType = GDrive.JsonFileMimeType,
				modifiedTime = Instant.now().toEpochMilli(),
				byteArray = json.encodeToString(deletedObjectList).toByteArray(),
			)
		}

		val deletedAttachmentDownloadResult = getDeletedObjectMap(fileId = googleDriveState.deletedAttachmentFileId)
		val remoteDeletedAttachmentSet: Set<DeletedAttachment> = 			try {
			if (deletedAttachmentDownloadResult is DownloadResult.Success) {
				if (deletedAttachmentDownloadResult.fileContent.isEmpty()) setOf()
				else json.decodeFromString(String(deletedAttachmentDownloadResult.fileContent)) ?: setOf()
			}
			else setOf()
		} catch (e: Exception) {
			setOf()
		}

		val remoteAttachmentMap =
			scanAttachments(folderId = googleDriveState.attachmentFolderId).let { scanAttachmentsResult ->
				if (scanAttachmentsResult is ScanAttachmentsResult.Success) scanAttachmentsResult.attachmentMap else mapOf()
			}
		syncAttachments(
			remoteAttachmentMap = remoteAttachmentMap,
			remoteDeletedAttachmentSet = remoteDeletedAttachmentSet,
			localDeletedAttachmentSet = baseObject?.deletedAttachmentSet ?: setOf(),
			deletedObjectIdList = deletedObjectList.map { it.id },
			attachmentFolderId = googleDriveState.attachmentFolderId,
		).let {
			val deletedAttachmentSet = remoteDeletedAttachmentSet + it
			if (deletedAttachmentSet != remoteDeletedAttachmentSet) {
				updateFile(
					fileId = googleDriveState.deletedAttachmentFileId,
					fileName = GDrive.DeletedAttachmentFileName,
					mimeType = GDrive.JsonFileMimeType,
					modifiedTime = Instant.now().toEpochMilli(),
					byteArray = json.encodeToString(remoteDeletedAttachmentSet + it).toByteArray(),
				)
			}
		}
	}

	private fun Drive.syncObjects(
		objectType : String,
		localObjectMap: Map<RealmUUID, Pair<String, Long>>,
		remoteDeletedObjectMap: Map<RealmUUID, DeletedObject>,
		localDeletedObjectMap: Map<RealmUUID, DeletedObject>,
		folderId: String,
		putObject: (ByteArray, String) -> Unit,
	): List<DeletedObject> {
		Log.d("npr71", "syncObjects: objectType=$objectType, localObjectMap=${localObjectMap.size}, remoteDeletedObjectMap=${remoteDeletedObjectMap.size}, localDeletedObjectMap=${localDeletedObjectMap.size}, folderId=$folderId")

		val metadataResult = downloadMetadata(folderId = folderId)
		if (metadataResult is MetadataResult.Success) {

			val metadataMap = metadataResult.currentObjectMap

			val cloudMetadataMap = metadataMap.mapValues { Pair(it.value.modifiedTime, false) }.toMutableMap()
			val localMetadataMap = localObjectMap.mapValues { Pair(it.value.second, false) }.toMutableMap()
			cloudMetadataMap.putAll(remoteDeletedObjectMap.mapValues { Pair(it.value.deletedTimestamp, true) })
			localMetadataMap.putAll(localDeletedObjectMap.mapValues { Pair(it.value.deletedTimestamp, true) })

			val mapDifference = Maps.difference(localMetadataMap, cloudMetadataMap)

			val toUpSyncList: MutableMap<RealmUUID, SyncInatorService.Companion.Operation> = mutableMapOf()
			val toDownSyncList: MutableMap<RealmUUID, SyncInatorService.Companion.Operation> = mutableMapOf()

			val leftOnly = mapDifference.entriesOnlyOnLeft()
			val rightOnly = mapDifference.entriesOnlyOnRight()
			val diff = mapDifference.entriesDiffering()
			val both = mapDifference.entriesInCommon()

			val deletedObjectList = mutableListOf<DeletedObject>()
			both.map { localDeletedObjectMap[it.key] }.filterNotNull().let { deletedObjectList.addAll(it) }

			leftOnly.forEach { (id, localData) ->
				val localTimestamp = localData.first
				val isLocalDeleted = localData.second
				val remoteTimestamp = remoteDeletedObjectMap[id]?.deletedTimestamp
				if (remoteTimestamp == null) {  // remote object does not exist
					if (!isLocalDeleted) toUpSyncList[id] = SyncInatorService.Companion.Operation.Upsert
				}
				else {
					when {  // remote object exists
						(localTimestamp > remoteTimestamp) && isLocalDeleted -> toUpSyncList[id] = SyncInatorService.Companion.Operation.Delete()
						(localTimestamp > remoteTimestamp) && !isLocalDeleted -> toUpSyncList[id] = SyncInatorService.Companion.Operation.Upsert
						(localTimestamp < remoteTimestamp) && isLocalDeleted -> toDownSyncList[id] = SyncInatorService.Companion.Operation.Upsert
						(localTimestamp < remoteTimestamp) && !isLocalDeleted -> toDownSyncList[id] = SyncInatorService.Companion.Operation.Upsert
					}
				}
			}
			rightOnly.forEach { (id, remoteData) ->
				val remoteTimestamp = remoteData.first
				val isRemoteDeleted = remoteData.second
				val localTimestamp = localDeletedObjectMap[id]?.deletedTimestamp
				if (localTimestamp == null) {   //  local object does not exist
					if (!isRemoteDeleted) toDownSyncList[id] = SyncInatorService.Companion.Operation.Upsert
				}
				else {   //  local object exists
					when {
						(localTimestamp > remoteTimestamp) && isRemoteDeleted -> toUpSyncList[id] = SyncInatorService.Companion.Operation.Upsert
						(localTimestamp > remoteTimestamp) && !isRemoteDeleted -> toUpSyncList[id] = SyncInatorService.Companion.Operation.Upsert
						(localTimestamp < remoteTimestamp) && isRemoteDeleted -> toDownSyncList[id] = SyncInatorService.Companion.Operation.Delete()
						(localTimestamp < remoteTimestamp) && !isRemoteDeleted -> toDownSyncList[id] = SyncInatorService.Companion.Operation.Upsert
					}
				}
			}
			diff.forEach { (id, entry) ->
				val local = entry.leftValue()
				val remote = entry.rightValue()

				val localTimestamp = local.first
				val remoteTimestamp = remote.first
				val isLocalDeleted = local.second
				val isRemoteDeleted = remote.second

				when {
					(localTimestamp > remoteTimestamp) && isLocalDeleted -> toUpSyncList[id] = SyncInatorService.Companion.Operation.Delete()
					(localTimestamp > remoteTimestamp) && !isLocalDeleted -> toUpSyncList[id] = SyncInatorService.Companion.Operation.Upsert
					(localTimestamp < remoteTimestamp) && isRemoteDeleted -> toDownSyncList[id] = SyncInatorService.Companion.Operation.Delete()
					(localTimestamp < remoteTimestamp) && !isRemoteDeleted -> toDownSyncList[id] = SyncInatorService.Companion.Operation.Upsert
				}
			}

			Log.d("npr71", "GDriveInatorService.syncObjects : $objectType : toDownSyncList.size = ${toDownSyncList.size}")
			Log.d("npr71", "GDriveInatorService.syncObjects : $objectType : toUpSyncList.size = ${toUpSyncList.size}")

			var downSyncedCount = 0
			toDownSyncList.forEach { (id, operation) ->
				when (operation) {
					is SyncInatorService.Companion.Operation.Upsert -> {
						metadataMap[id]?.fileId?.let { fileId ->
							val downloadResult = gDrive.downloadData(drive = this, fileId = fileId)
							if (downloadResult is DownloadResult.Success) putObject(downloadResult.fileContent, fileId)
						}
					}

					is SyncInatorService.Companion.Operation.Delete -> if (id !in localDeletedObjectMap) {
						repository.deleteSuspended(id, false)
						remoteDeletedObjectMap[id]?.let { deletedObjectList.add(it) }
					}
				}

				downSyncedCount++
				Log.d("npr71", "GDriveInatorService.syncObjects : $objectType : toDownSyncList : $downSyncedCount / ${toDownSyncList.size}")
			}

			var upSyncedCount = 0
			toUpSyncList.forEach { (id, operation) ->
				when (operation) {
					is SyncInatorService.Companion.Operation.Upsert -> {
						localObjectMap[id]?.let { (objectData, modifiedTimestamp) ->
							uploadObjectData(
								objectId = id,
								fileId = metadataMap[id]?.fileId,
								modifiedTime = modifiedTimestamp,
								parentFileId = folderId,
								objectData = objectData,
							)
						}
					}

					is SyncInatorService.Companion.Operation.Delete -> {
						metadataMap[id]?.fileId?.let {
							val deleteResult = gDrive.deleteFile(drive = this, fileId = it)
							if (deleteResult is DeleteResult.Success) localDeletedObjectMap[id]?.let { deletedObjectList.add(it) }
						}
					}
				}
				upSyncedCount++
				Log.d("npr71", "GDriveInatorService.syncObjects : $objectType : toUpSyncList : $upSyncedCount / ${toUpSyncList.size}")
			}

			return deletedObjectList
		}
		else {
			metadataResult as MetadataResult.UnknownError
//			metadataResult.exception.printStackTrace()
			return listOf()
		}
	}

	private fun Drive.uploadObjectData(
		objectId: RealmUUID,
		fileId: String?,
		modifiedTime: Long,
		parentFileId: String,
		objectData: String,
	): UploadResult {
		val fileMetadata = File()
		if (fileId == null) {
			fileMetadata.name = "$objectId.json"
			fileMetadata.parents = listOf(parentFileId)
			fileMetadata.appProperties = mapOf("graphite.uuid" to objectId.toString())
			fileMetadata.mimeType = "application/json"
		}
		fileMetadata.modifiedTime = DateTime(modifiedTime)

		val data = objectData.toByteArray(Charsets.UTF_8)
		val fileContent = ByteArrayContent(GDrive.JsonFileMimeType, data)

		try {
			fileId?.let {
				files()
					.update(it, fileMetadata, fileContent)
					.setFields("id")
					.execute()
					.let {
						Log.d("npr71", "GDriveInatorService.uploadFile : it = ${it.id}")
						return UploadResult.Success(it.id)
					}
			} ?: files()
				.create(fileMetadata, fileContent)
				.setFields("id")
				.execute()
				.let {
					Log.d("npr71", "GDriveInatorService.uploadFile : it = ${it.id}")
					return UploadResult.Success(it.id)
				}
		} catch (e: GoogleJsonResponseException) {
			if (e.statusCode == 404) {
				Log.d("npr71", "GDriveInatorService.uploadFile : e = ${e.message}")
//				e.printStackTrace()
				return UploadResult.ParentNotFound
			}
			else {
				Log.d("npr71", "GDriveInatorService.uploadFile : e = ${e.message}")
//				e.printStackTrace()
				return UploadResult.UnknownError(e)
			}
		} catch (e: IOException) {
			Log.d("npr71", "GDriveInatorService.uploadFile : e = ${e.message}")
//			e.printStackTrace()
			return UploadResult.UnknownError(e)
		} catch (e: Exception) {
			Log.d("npr71", "GDriveInatorService.uploadFile : e = ${e.message}")
//			e.printStackTrace()
			return UploadResult.UnknownError(e)
		}
	}

	private fun Drive.downloadMetadata(
		folderId: String,
	): MetadataResult {
		Log.d("npr71", "GDriveInatorService.downloadMetadata : folderId = $folderId")
		val currentListFiles = gDrive.listFiles(
			drive = this,
			query = "'${folderId}' in parents and trashed = false",
			fields = "nextPageToken, files(id, modifiedTime, appProperties)"
		)

		if (currentListFiles !is ListFiles.Success) return MetadataResult.UnknownError(Exception("ListFiles is not success"))

		val currentObjectMap = try {
			currentListFiles.fileList.filter {
				it.appProperties["graphite.uuid"]?.let {
					try {
						RealmUUID.from(it)
						true
					} catch (e: Exception) {
						false
					}
				} ?: false
			}.associate { file ->
				file.appProperties["graphite.uuid"]!!.let {
					val objectId = RealmUUID.from(it)
					val modifiedTime = file.modifiedTime.value
					objectId to Metadata(modifiedTime = modifiedTime, fileId = file.id)
				}
			}
		} catch (e: Exception) {
			return MetadataResult.UnknownError(e)
		}

		return MetadataResult.Success(currentObjectMap = currentObjectMap)
	}


	private fun Drive.getDeletedObjectMap(fileId: String?): DownloadResult {
		fileId?.let {
			try {
				files()
					.get(fileId)
					.setFields("appProperties")
					.executeMediaAsInputStream()
					.readBytes()
					.let {
						return DownloadResult.Success(fileId = fileId, fileContent = it)
					}
			} catch (e: Exception) {
				return DownloadResult.UnknownError(e)
			}
		} ?: try {
			gDrive
				.createFile(
					drive = this,
					fileName = GDrive.DeletedObjectFileName,
					mimeType = GDrive.JsonFileMimeType,
					parentId = GDrive.RootFolderId,
				).let {
					return when (it) {
						is RetrieveFile.Success -> DownloadResult.Success(
							fileId = it.fileId,
							fileContent = "{}".toByteArray()
						)

						is RetrieveFile.TooManyRetries -> DownloadResult.TooManyRetries
						is RetrieveFile.ParentNotFound -> DownloadResult.FileNotFound
						is RetrieveFile.UnknownError -> DownloadResult.UnknownError(it.exception)
					}
				}
		} catch (e: Exception) {
			return DownloadResult.UnknownError(e)
		}
	}

	private fun Drive.syncAttachments(
		remoteAttachmentMap: Map<File, List<File>>,
		remoteDeletedAttachmentSet: Set<DeletedAttachment>,
		localDeletedAttachmentSet: Set<DeletedAttachment>,
		deletedObjectIdList: List<RealmUUID>,
		attachmentFolderId: String,
	): Set<DeletedAttachment> {
		Log.d("npr71", "GDriveInatorService.syncAttachments -----------------------------------------------")
		Log.d("npr71", "GDriveInatorService.syncAttachments : remoteAttachmentMap = ${remoteAttachmentMap.flatMap { it.value }.size}")
		Log.d("npr71", "GDriveInatorService.syncAttachments : remoteDeletedAttachmentSet = ${remoteDeletedAttachmentSet.size}")
		Log.d("npr71", "GDriveInatorService.syncAttachments : localDeletedAttachmentSet = ${localDeletedAttachmentSet.size}")
		Log.d("npr71", "GDriveInatorService.syncAttachments : deletedObjectIdList = ${deletedObjectIdList.size}")
		Log.d("npr71", "GDriveInatorService.syncAttachments : attachmentFolderId = ${attachmentFolderId}")

		val parentIdFileIdMap = mutableMapOf<RealmUUID, String>()
		val attachmentFileIdMap = mutableMapOf<SyncInatorService.Companion.AttachmentMetadata.Companion.AttachmentIdentity, String>()

		val remoteAttachmentMetadataMap =
			mutableMapOf<SyncInatorService.Companion.AttachmentMetadata.Companion.AttachmentIdentity, SyncInatorService.Companion.AttachmentMetadata>()
		val deleteRemoteAttachmentListNoQAsked = mutableListOf<String>()
		val deleteRemoteAttachmentFolder = mutableListOf<String>()

		remoteAttachmentMap.forEach { (parentFile, attachmentList) ->
			val parentId = try {
				RealmUUID.from(parentFile.appProperties["graphite.uuid"]!!)
			} catch (e: Exception) {
				return@forEach
			}
			parentIdFileIdMap[parentId] = parentFile.id
			val isParentDeleted = parentId in deletedObjectIdList
			attachmentList.forEach { attachmentFile ->
				val attachmentIdentity = SyncInatorService.Companion.AttachmentMetadata.Companion.AttachmentIdentity(
					fileName = attachmentFile.name,
					parentId = parentId,
				)
				val attachmentMetadata = SyncInatorService.Companion.AttachmentMetadata(
					fileName = attachmentFile.name,
					parentId = parentId,
					isDeleted = isParentDeleted,
				)
				remoteAttachmentMetadataMap[attachmentIdentity] = attachmentMetadata
				if (isParentDeleted) deleteRemoteAttachmentListNoQAsked.add(attachmentFile.id)
				else attachmentFileIdMap[attachmentIdentity] = attachmentFile.id
			}
		}
		remoteDeletedAttachmentSet.forEach {
			val attachmentIdentity = it.toAttachmentIdentity()
			val attachmentMetadata = SyncInatorService.Companion.AttachmentMetadata(
				fileName = it.fileName,
				parentId = it.parentId,
				isDeleted = true,
			)
			if (attachmentIdentity in remoteAttachmentMetadataMap) attachmentFileIdMap[attachmentIdentity]?.let { deleteRemoteAttachmentListNoQAsked.add(it) }
			remoteAttachmentMetadataMap[attachmentIdentity] = attachmentMetadata
		}

		parentIdFileIdMap.forEach { (parentId, fileId) -> if (parentId in deletedObjectIdList) deleteRemoteAttachmentFolder.add(fileId) }

		deleteRemoteAttachmentFolder(toDeleteAttachmentFolderList = deleteRemoteAttachmentFolder)

		val localAttachmentMetadataMap: MutableMap<SyncInatorService.Companion.AttachmentMetadata.Companion.AttachmentIdentity, SyncInatorService.Companion.AttachmentMetadata> =
			mutableMapOf()
		val deleteLocalAttachmentListNoQAsked = mutableListOf<SyncInatorService.Companion.AttachmentMetadata>()
		repository.attachmentRepository.getAttachmentMetadataSet().forEach {
			val attachmentIdentity = SyncInatorService.Companion.AttachmentMetadata.Companion.AttachmentIdentity(
				fileName = it.fileName,
				parentId = it.parentId,
			)
			val isParentDeleted = it.parentId in deletedObjectIdList
			if (isParentDeleted) {
				localAttachmentMetadataMap[attachmentIdentity] = it.asDeleted()
				deleteLocalAttachmentListNoQAsked.add(it.asDeleted())
			}
			else localAttachmentMetadataMap[attachmentIdentity] = it
		}
		localDeletedAttachmentSet.forEach {
			val attachmentIdentity = it.toAttachmentIdentity()
			val attachmentMetadata = SyncInatorService.Companion.AttachmentMetadata(
				fileName = it.fileName,
				parentId = it.parentId,
				isDeleted = true,
			)
			if (attachmentIdentity in localAttachmentMetadataMap) deleteLocalAttachmentListNoQAsked.add(attachmentMetadata)
			(localAttachmentMetadataMap as MutableMap)[attachmentIdentity] = attachmentMetadata
		}

		deleteLocalAttachmentNoQAsked(toDeleteAttachmentList = deleteLocalAttachmentListNoQAsked)
		deleteRemoteAttachmentNoQAsked(toDeleteAttachmentFileIdList = deleteRemoteAttachmentListNoQAsked)

		val diff = Maps.difference(localAttachmentMetadataMap, remoteAttachmentMetadataMap)
		val localOnlyAttachmentMap = diff.entriesOnlyOnLeft()
		val remoteOnlyAttachmentMap = diff.entriesOnlyOnRight()
		val differingAttachmentMap = diff.entriesDiffering()
		val commonAttachmentMap = diff.entriesInCommon()

		val deletedAttachmentSet = mutableSetOf<DeletedAttachment>()

		val toUpSyncAttachmentList = mutableListOf<SyncInatorService.Companion.AttachmentMetadata>()
		val toDownSyncAttachmentList = mutableListOf<SyncInatorService.Companion.AttachmentMetadata>()

		localOnlyAttachmentMap.forEach { attachmentIdentity, attachmentMetadata ->
			DeletedAttachment
			if (attachmentMetadata.isDeleted) deletedAttachmentSet.add(attachmentMetadata.toDeletedAttachment())
			else toUpSyncAttachmentList.add(attachmentMetadata)
		}

		remoteOnlyAttachmentMap.forEach { attachmentIdentity, attachmentMetadata ->
			if (attachmentMetadata.isDeleted) deletedAttachmentSet.add(attachmentMetadata.toDeletedAttachment())
			else toDownSyncAttachmentList.add(attachmentMetadata)
		}

		differingAttachmentMap.forEach { attachmentIdentity, attachmentMetadata ->
			val localAttachmentMetadata = attachmentMetadata.leftValue()
			val remoteAttachmentMetadata = attachmentMetadata.rightValue()
			when {
				localAttachmentMetadata.isDeleted && remoteAttachmentMetadata.isDeleted -> deletedAttachmentSet.add(localAttachmentMetadata.toDeletedAttachment())
				localAttachmentMetadata.isDeleted && !remoteAttachmentMetadata.isDeleted -> toUpSyncAttachmentList.add(remoteAttachmentMetadata.asDeleted())
				!localAttachmentMetadata.isDeleted && remoteAttachmentMetadata.isDeleted -> toDownSyncAttachmentList.add(localAttachmentMetadata.asDeleted())
				else -> null    //	!!	!localAttachmentMetadata.isDeleted && !remoteAttachmentMetadata.isDeleted	WTF
			}
		}

		commonAttachmentMap.forEach { attachmentIdentity, attachmentMetadata ->
			if (attachmentMetadata.isDeleted) deletedAttachmentSet.add(attachmentMetadata.toDeletedAttachment())
		}

		toDownSyncAttachmentList.forEach {
			if (it.isDeleted) repository.deleteAttachment(it.toAttachmentIdentity())
			else attachmentFileIdMap[it.toAttachmentIdentity()]?.let { fileId -> downSyncAttachment(fileId = fileId, attachmentMetadata = it) }
		}

		toUpSyncAttachmentList.forEach { attachmentMetadata ->
			val fileId = attachmentFileIdMap[attachmentMetadata.toAttachmentIdentity()]
			val parentId = parentIdFileIdMap[attachmentMetadata.parentId]

			if (attachmentMetadata.isDeleted) {
				fileId?.let {
					val deleteResult = gDrive.deleteFile(drive = this, fileId = it)
					if (deleteResult is DeleteResult.Success) deletedAttachmentSet.add(attachmentMetadata.toDeletedAttachment())
				} ?: deletedAttachmentSet.add(attachmentMetadata.toDeletedAttachment())
			}
			else {
				repository.attachmentRepository.getAttachment(attachmentMetadata.parentId, attachmentMetadata.fileName)?.let { file ->
					val uploadAttachmentResult = upSyncAttachment(
						fileName = attachmentMetadata.fileName,
						parentObjectId = attachmentMetadata.parentId,
						parentFileId = parentId,
						attachmentFolderId = attachmentFolderId,
						byteArray = file.readBytes(),
					)

					if (uploadAttachmentResult is UploadAttachmentResult.Success) parentIdFileIdMap[attachmentMetadata.parentId] =
						uploadAttachmentResult.parentFileId
				}
			}
		}

		return deletedAttachmentSet
	}

	private fun deleteLocalAttachmentNoQAsked(toDeleteAttachmentList: List<SyncInatorService.Companion.AttachmentMetadata>) {
		Log.d("npr71", "deleteLocalAttachmentNoQAsked: toDeleteAttachmentList = ${toDeleteAttachmentList.size}")
		toDeleteAttachmentList.forEach { repository.deleteAttachment(it.toAttachmentIdentity()) }
	}

	private fun Drive.deleteRemoteAttachmentNoQAsked(toDeleteAttachmentFileIdList: List<String>) {
		Log.d("npr71", "deleteRemoteAttachmentNoQAsked: toDeleteAttachmentFileIdList = ${toDeleteAttachmentFileIdList.size}")
		toDeleteAttachmentFileIdList.forEach { deleteFile(it) }
	}

	private fun Drive.deleteRemoteAttachmentFolder(toDeleteAttachmentFolderList: List<String>) {
		Log.d("npr71", "deleteRemoteAttachmentFolder : toDeleteAttachmentFolderList = ${toDeleteAttachmentFolderList.size}")
		toDeleteAttachmentFolderList.forEach { deleteFile(it) }
	}

	private fun Drive.upSyncAttachment(
		fileName: String,
		parentObjectId: RealmUUID,
		parentFileId: String?,
		attachmentFolderId: String,
		byteArray: ByteArray,
	): UploadAttachmentResult {

		Log.d(
			"npr71",
			"upSyncAttachment: fileName = $fileName, parentObjectId = $parentObjectId, parentFileId = $parentFileId, attachmentFolderId = $attachmentFolderId"
		)

		val pFileId = parentFileId ?: gDrive.createFile(
			drive = this,
			fileName = parentObjectId.toString(),
			mimeType = GDrive.FolderMimeType,
			parentId = attachmentFolderId,
			appProperties = mapOf("graphite.uuid" to parentObjectId.toString()),
		).let { retrieveFile ->
			if (retrieveFile is RetrieveFile.Success) retrieveFile.fileId
			else return UploadAttachmentResult.UnknownError()
		}

		val retrieveFile = gDrive.createFile(
			drive = this,
			fileName = fileName,
			mimeType = GDrive.OctetStreamFileMimeType,
			parentId = pFileId,
			appProperties = mapOf("graphite.uuid" to parentObjectId.toString()),
			byteArray = byteArray,
		)

		return if (retrieveFile is RetrieveFile.Success) UploadAttachmentResult.Success(fileId = retrieveFile.fileId, parentFileId = pFileId)
		else UploadAttachmentResult.UnknownError()
	}

	private fun Drive.downSyncAttachment(
		fileId: String,
		attachmentMetadata: SyncInatorService.Companion.AttachmentMetadata,
	) {
		val downloadResult = gDrive.downloadData(drive = this, fileId = fileId)
		if (downloadResult is DownloadResult.Success) {
			val data = downloadResult.fileContent
			repository.attachmentRepository.putAttachment(
				parentId = attachmentMetadata.parentId,
				fileName = attachmentMetadata.fileName,
				byteArray = data,
			)
		}
	}

	private fun Drive.updateFile(
		fileId: String,
		fileName: String,
		mimeType: String,
		modifiedTime: Long = Instant.now().toEpochMilli(),
		byteArray: ByteArray,
	): UploadResult {
//		Log.d("npr71", "GDriveInatorService.updateFile : fileId = $fileId")

		val folderMetadata = File()
		folderMetadata.name = fileName
		folderMetadata.mimeType = mimeType
		folderMetadata.modifiedTime = DateTime(modifiedTime)

		val data = ByteArrayContent(mimeType, byteArray)
		try {
			val file = files()
				.update(fileId, folderMetadata, data)
				.setFields("id")
				.execute()
				.let {
					Log.d("npr71", "GDriveInatorService.updateFile : updated file file.id = ${it.id}")
					it
				}
			return UploadResult.Success(file.id)
		} catch (e: GoogleJsonResponseException) {
//			e.printStackTrace()
			return if (e.details.code == 404) UploadResult.ParentNotFound else UploadResult.UnknownError(e)
		} catch (e: Exception) {
//			e.printStackTrace()
			return UploadResult.UnknownError(e)
		}
	}

	private fun Drive.scanAttachments(folderId: String): ScanAttachmentsResult {
		val folderList: MutableList<File> = mutableListOf()
		gDrive.listFiles(
			drive = this,
			query = "'$folderId' in parents and mimeType = 'application/vnd.google-apps.folder'",
			fields = "nextPageToken, files(id, name, mimeType, parents, appProperties)",
		).let {
			when (it) {
				is ListFiles.Success -> folderList.addAll(it.fileList)
				is ListFiles.ParentNotFound -> return ScanAttachmentsResult.UnknownError()
				is ListFiles.UnknownError -> return ScanAttachmentsResult.UnknownError()
				is ListFiles.TooManyRetries -> return ScanAttachmentsResult.UnknownError()
			}
		}

		val attachmentList: MutableMap<File, List<File>> = mutableMapOf()

		for (folder in folderList) {
			gDrive.listFiles(
				drive = this,
				query = "'${folder.id}' in parents",
				fields = "nextPageToken, files(id, name, mimeType, parents,  modifiedTime)",
			).let {
				when (it) {
					is ListFiles.Success -> attachmentList[folder] = it.fileList
					is ListFiles.ParentNotFound -> return ScanAttachmentsResult.UnknownError()
					is ListFiles.UnknownError -> return ScanAttachmentsResult.UnknownError()
					is ListFiles.TooManyRetries -> return ScanAttachmentsResult.UnknownError()
				}
			}
		}

		return ScanAttachmentsResult.Success(attachmentList)
	}

//	private fun <T> T.compress(): ByteArray? {
//		try {
//			val baos = ByteArrayOutputStream()
//			val gzipOut = GZIPOutputStream(baos)
//			val objectOut = ObjectOutputStream(gzipOut)
//			objectOut.writeObject(this)
//			objectOut.close()
//			return baos.toByteArray()
//		} catch (e: Exception) {
////			e.printStackTrace()
//			return null
//		}
//	}

//	private fun <T> ByteArray.decompress(): T? {
//		try {
//			val bais = ByteArrayInputStream(this)
//			val gzipIn = GZIPInputStream(bais)
//			val objectIn = ObjectInputStream(gzipIn)
//			val myObj1: T = objectIn.readObject() as T
//			objectIn.close()
//			return myObj1
//		} catch (e: Exception) {
////			e.printStackTrace()
//			return null
//		}
//	}

	inner class GDriveSyncInatorBinder : Binder() {
		val service: GDriveSyncInatorService
			get() = this@GDriveSyncInatorService
	}

	companion object {

		sealed class LockStatus() {
			object Available : LockStatus()
			data class LockExpired(val fileIdList: List<String>) : LockStatus()
			object Locked : LockStatus()
			data class Error(val exception: Exception? = null) : LockStatus()
		}

		sealed class UploadResult {
			data class Success(val fileId: String) : UploadResult()
			object TooManyRetries : UploadResult()
			object ParentNotFound : UploadResult()
			data class UnknownError(val exception: Exception) : UploadResult()
		}

		sealed class UploadAttachmentResult {
			data class Success(val fileId: String, val parentFileId: String) : UploadAttachmentResult()
			object TooManyRetries : UploadAttachmentResult()
			object ParentNotFound : UploadAttachmentResult()
			data class UnknownError(val exception: Exception? = null) : UploadAttachmentResult()
		}

		sealed class DownloadResult {
			data class Success(val fileId: String, val fileContent: ByteArray) : DownloadResult()
			object FileNotFound : DownloadResult()
			object TooManyRetries : DownloadResult()
			data class UnknownError(val exception: Exception? = null) : DownloadResult()
		}

		sealed class DeleteResult {
			object Success : DeleteResult()
			object TooManyRetries : DeleteResult()
			object NetworkError : DeleteResult()
			data class UnknownError(val exception: Exception) : DeleteResult()
		}

		sealed class RetrieveFile {
			data class Success(val fileId: String) : RetrieveFile()
			object TooManyRetries : RetrieveFile()
			object ParentNotFound : RetrieveFile()
			data class UnknownError(val exception: Exception? = null) : RetrieveFile()
		}

		/**
		 * Possible values
		 * 	*	[Success]
		 * 	*	[TooManyRetries]
		 * 	*	[ParentNotFound]
		 * 	*	[UnknownError]
		 */
		sealed class ListFiles {
			data class Success(val fileList: List<File>) : ListFiles()
			object TooManyRetries : ListFiles()
			object ParentNotFound : ListFiles()
			data class UnknownError(val exception: Exception? = null) : ListFiles()
		}

		sealed class ScanAttachmentsResult {
			data class Success(val attachmentMap: Map<File, List<File>>) : ScanAttachmentsResult()
			data class UnknownError(val exception: Exception? = null) : ScanAttachmentsResult()
		}

		@Serializable
		data class Metadata(
			val modifiedTime: Long,
			val fileId: String
		)

		sealed class MetadataResult {
			data class Success(val currentObjectMap: Map<RealmUUID, Metadata>) : MetadataResult()
			data class UnknownError(val exception: Exception) : MetadataResult()
		}
	}
}

class GDriveSyncServiceConnectionManager(
	private val context: Context,
	private val onBound: (GDriveSyncInatorService) -> Unit
) : ServiceConnection {
	var service: GDriveSyncInatorService? = null
	private var attemptingToBind = false
	private var bound = false

	init {
		bindToService()
	}

	private fun bindToService() {
		if (!attemptingToBind) {
			attemptingToBind = true
			Intent(context, GDriveSyncInatorService::class.java).let { intent ->
				intent.putExtra("syncProvider", "Google Drive")
				context.startService(intent)
				context.bindService(intent, this, Context.BIND_AUTO_CREATE)
			}
		}
	}

	override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
		attemptingToBind = false
		bound = true
		(iBinder as GDriveSyncInatorService.GDriveSyncInatorBinder).service.let {
			service = it
			onBound(it)
		}
	}

	override fun onServiceDisconnected(componentName: ComponentName) {
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
