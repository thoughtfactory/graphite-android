package com.syncodec.graphite.service.syncInator

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.dropbox.core.v2.files.Metadata
import com.dropbox.core.v2.files.TagObject
import com.google.common.collect.Maps
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.cloud.dropbox.DropboxApi
import com.syncodec.graphite.di.cloud.dropbox.DropboxConnector
import com.syncodec.graphite.di.cloud.dropbox.DropboxOperation
import com.syncodec.graphite.di.cloud.dropbox.DropboxOperation.ActiveOperationResult.DeleteFileResult
import com.syncodec.graphite.di.cloud.dropbox.DropboxOperation.ActiveOperationResult.UploadFileResult
import com.syncodec.graphite.di.cloud.dropbox.DropboxOperation.PassiveOperationResult.DownloadFileResult
import com.syncodec.graphite.di.cloud.dropbox.DropboxOperation.PassiveOperationResult.ListFolderResult
import com.syncodec.graphite.di.model.local.BucketItemObject
import com.syncodec.graphite.di.model.local.BucketObject
import com.syncodec.graphite.di.model.local.ChapterObject
import com.syncodec.graphite.di.model.local.NoteObject
import com.syncodec.graphite.di.model.local.Syncable
import com.syncodec.graphite.service.syncInator.SyncInatorService.Companion.Operation
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.TypedRealmObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Locale
import kotlin.reflect.KFunction1
import kotlin.reflect.KMutableProperty1


class DyncInator : SyncInatorService() {

	private val logTransfer = BuildConfig.DEBUG
	private val syncChapter = true
	private val syncNote = true
	private val syncBucket = true
	private val syncBucketItem = true
	private val syncTag = true
	private val syncAttachment = true

	inner class DyncInatorBinder : Binder() {
		val service: DyncInator
			get() = this@DyncInator
	}

	private var dyncInatorBinder: IBinder? = DyncInatorBinder()

	private val json = Json {
		ignoreUnknownKeys = true
		encodeDefaults = true
	}

	val dropBoxConnector: DropboxConnector by inject()
	private var dropboxApi: DropboxApi? = null

	private var syncJob: Job? = null
	private var locker: Locker? = null

	override fun onCreate() {
		super.onCreate()
		Log.d("rits", "DropboxSyncInatorService.onCreate: ")

		lifecycleScope.launch(Dispatchers.IO) {
			super.syncStat.tryEmit(SyncStat.Connecting)
			dropboxApi = dropBoxConnector.connect()
			when {
				dropboxApi == null -> super.syncStat.tryEmit(SyncStat.NotConnected)
				isAutoSyncEnabled.first() -> {
					super.syncStat.tryEmit(SyncStat.Idle(isAutoSyncEnabled = true))
					syncNow()
				}

				else -> {
					super.syncStat.tryEmit(SyncStat.Idle(isAutoSyncEnabled = false))
					syncNow()
				}
			}
			if (dropboxApi != null && isAutoSyncEnabled.first()) syncNow()
		}
	}

	override fun onBind(intent: Intent): IBinder? {
		super.onBind(intent)
		return dyncInatorBinder
	}

	override fun syncNow() {
		syncJob?.cancel()
		syncJob = lifecycleScope.launch(Dispatchers.IO) {
			while (true) {
				super.syncStat.value.let {
					Log.d("rits", "DropboxSyncInatorService.syncNow: $it")
					if (it is SyncStat.Idle && super.isAutoSyncEnabled.value) dropboxApi?.tryAndHoldLock()
				}
				delay(10_000)
			}
		}
	}

	override fun forceSync() {

	}

	private fun onUnknownError(exception: Exception? = null) {
		syncJob?.cancel()
		super.syncStat.tryEmit(SyncStat.Error.UnknownError(message = exception?.message))
	}

	private fun onNetworkError() {
		syncJob?.cancel()
		super.syncStat.tryEmit(SyncStat.Error.NetworkError)
	}

	private fun onCredentialsError() {
		syncJob?.cancel()
		super.syncStat.tryEmit(SyncStat.Error.CredentialsError)
	}

	private suspend fun DropboxApi.tryAndHoldLock(forced: Boolean = false) {
		when (checkIfLocked()) {
			true -> Log.d("rits", "DropboxSyncInatorService.tryAndHoldLock: locked")
			false -> withConnection(
				onError = {
					syncJob?.cancel()
					super.syncStat.tryEmit(SyncStat.Error.UnknownError())
				}
			) {
				lockDropbox()
				sync()
				delay(5_000)
				unlockDropbox()
			}

			null -> Log.d("rits", "DropboxSyncInatorService.tryAndHoldLock: error")
		}
	}

	private suspend fun DropboxApi.checkIfLocked(): Boolean? {
		Log.d("rits", "DropboxSyncInatorService.checkIfLocked: ")
		return withConnection(onError = ::onUnknownError) {
			val lockerDownloadFileResult = downloadFile(DropboxApi.Companion.Path.Lock.path)
			Log.d("rits", "DropboxSyncInatorService.checkIfLocked: ${lockerDownloadFileResult::class.simpleName}")
			when (lockerDownloadFileResult) {
				is DownloadFileResult.Success -> return@withConnection true
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

	private suspend fun DropboxApi.lockDropbox() {
		Log.d("rits", "DropboxSyncInatorService.lockDropbox: ")
		withConnection(onError = ::onUnknownError) {
			locker = Locker(sessionId = RealmUUID.random().toString(), timestamp = Instant.now().toEpochMilli())
			val lockerString = json.encodeToString(locker)
			Log.d("rits", "DropboxSyncInatorService.lockDropbox: $lockerString")
			when (val uploadFileResult = uploadFile(DropboxApi.Companion.Path.Lock.path, lockerString.encodeToByteArray())) {
				is UploadFileResult.Success -> return@withConnection
				is UploadFileResult.Error.UploadError -> onUnknownError(uploadFileResult.exception)
				is UploadFileResult.Error.CredentialsError -> onCredentialsError()
				is UploadFileResult.Error.NetworkError -> onNetworkError()
				is UploadFileResult.Error.UnknownError -> onUnknownError(uploadFileResult.exception)
			}
		}
	}

	private suspend fun DropboxApi.unlockDropbox() {
		Log.d("rits", "DropboxSyncInatorService.unlockDropbox: ")
		withConnection(onError = ::onUnknownError) {
			val downloadFileResult = downloadFile(path = DropboxApi.Companion.Path.Lock.path)
			Log.d("rits", "DropboxSyncInatorService.unlockDropbox: ${downloadFileResult::class.simpleName}")
			when (downloadFileResult) {
				is DownloadFileResult.Success -> {
					val remoteLocker: Locker = json.decodeFromString(downloadFileResult.byteArray.decodeToString())
					if (remoteLocker.sessionId == locker?.sessionId) {
						val deleteFileResult = deleteFile(path = DropboxApi.Companion.Path.Lock.path)
						Log.d("rits", "DropboxSyncInatorService.unlockDropbox: ${deleteFileResult::class.simpleName}")
						when (deleteFileResult) {
							is DeleteFileResult.Success -> return@withConnection
							is DeleteFileResult.Error.DeleteError -> return@withConnection
							is DeleteFileResult.Error.CredentialsError -> onCredentialsError()
							is DeleteFileResult.Error.NetworkError -> onNetworkError()
							is DeleteFileResult.Error.UnknownError -> onUnknownError(deleteFileResult.exception)
						}
					}
				}

				is DownloadFileResult.FileNotFound -> {
					Log.d("rits", "DropboxSyncInatorService.unlockDropbox: not found")
				}

				is DownloadFileResult.Error.DownloadError -> return@withConnection
				is DownloadFileResult.Error.CredentialsError -> onCredentialsError()
				is DownloadFileResult.Error.NetworkError -> onNetworkError()
				is DownloadFileResult.Error.UnknownError -> onUnknownError(downloadFileResult.exception)
			}
		}
	}

	private suspend fun DropboxApi.sync() {
		Log.d("rits", "DropboxSyncInatorService.sync: ")

		val chapterMetadataMap = syncRealm<ChapterObject>(rectify = false)
		Log.d("rits", "DropboxSyncInatorService.sync: chapterMetadataMap: ${chapterMetadataMap.size}")
		chapterMetadataMap.forEach { t, u ->
			Log.d("rits", "DropboxSyncInatorService.sync: $t, $u")
		}
	}

	private suspend inline fun <reified T : TypedRealmObject> DropboxApi.syncRealm(rectify: Boolean): Map<RealmUUID, ObjectMetadata> {

		Log.d("rits", "DropboxSyncInatorService.realm: syncing ${T::class.simpleName}")

		val remoteMetadataMap = getRemoteMetadata(path = DropboxApi.Companion.Path.Chapter.path, rectify = rectify)
		val localMetadataMap = getLocalMetadata<ChapterObject>(idGetter = ChapterObject::id, toObjectMetadata = ChapterObject::toObjectMetaData)

		Log.d("rits", "DropboxSyncInatorService.syncRealm: remoteMetadataMap: ${remoteMetadataMap.size}")
		Log.d("rits", "DropboxSyncInatorService.syncRealm: localMetadataMap: ${localMetadataMap.size}")

		val localRemoteDifference = Maps.difference(localMetadataMap, remoteMetadataMap)

//		Contains deleted as well as existing entries
		val localOnlyEntries = localRemoteDifference.entriesOnlyOnLeft()
		val remoteOnlyEntries = localRemoteDifference.entriesOnlyOnRight()
		val differentEntries = localRemoteDifference.entriesDiffering()

		Log.d("rits", "DropboxSyncInatorService.syncRealm: localOnlyEntries: ${localOnlyEntries.size}")
		Log.d("rits", "DropboxSyncInatorService.syncRealm: remoteOnlyEntries: ${remoteOnlyEntries.size}")
		Log.d("rits", "DropboxSyncInatorService.syncRealm: differentEntries: ${differentEntries.size}")

		val operationMap: MutableMap<RealmUUID, Operation> = mutableMapOf()

//		Local deleted entries
		operationMap.putAll(
			localOnlyEntries
				.filter { it.value.isDeleted }
				.mapValues { Operation.UpSync.Delete(realmUUID = it.key, path = "${DropboxApi.Companion.Path.Chapter.path}/${it.key}.json", modifiedTimestamp = it.value.modifiedTimestamp) }
		)
//		Local new entries
		repository
			?.getObjectFromId<ChapterObject>(idList = localOnlyEntries.filter { !it.value.isDeleted }.keys.toList(), includeLocked = true)
			?.associate { it.id to Operation.UpSync.Upsert(realmUUID = it.id, path = "${DropboxApi.Companion.Path.Chapter.path}/${it.id}.json", modifiedTimestamp = it.modifiedTimestamp) }
			?.let { operationMap.putAll(it) }

//		Remote only entries
		operationMap.putAll(
			remoteOnlyEntries.mapValues {
				if (it.value.isDeleted) Operation.DownSync.Delete(realmUUID = it.key)
				else Operation.DownSync.Upsert(realmUUID = it.key, path = "${DropboxApi.Companion.Path.Chapter.path}/${it.key}.json")
			}
		)

		differentEntries.forEach { (id, diff) ->
			val localObject = diff.leftValue()
			val remoteObject = diff.rightValue()

			Log.d("rits", "local : ${localObject.hash} : remote : ${remoteObject.hash} : ${localObject.modifiedTimestamp} : ${remoteObject.modifiedTimestamp}")

			operationMap[id] = when {
				localObject.hash == remoteObject.hash -> Operation.NoOp.Skip(realmUUID = id, path = "${DropboxApi.Companion.Path.Chapter.path}/${id}.json")

				//"upSync_delete"
				localObject.modifiedTimestamp > remoteObject.modifiedTimestamp && localObject.isDeleted ->
					Operation.UpSync.Delete(realmUUID = id, path = "${DropboxApi.Companion.Path.Chapter.path}/$id.json", modifiedTimestamp = localObject.modifiedTimestamp)

//					"upSync_upsert"
				localObject.modifiedTimestamp > remoteObject.modifiedTimestamp && !localObject.isDeleted ->
					Operation.UpSync.Upsert(realmUUID = id, path = "${DropboxApi.Companion.Path.Chapter.path}/$id.json", modifiedTimestamp = localObject.modifiedTimestamp)

//					"downSync_delete"
				localObject.modifiedTimestamp < remoteObject.modifiedTimestamp && remoteObject.isDeleted ->
					Operation.DownSync.Delete(realmUUID = id)

//					"downSync_upsert"
				localObject.modifiedTimestamp < remoteObject.modifiedTimestamp && !remoteObject.isDeleted ->
					Operation.DownSync.Upsert(realmUUID = id, path = "${DropboxApi.Companion.Path.Chapter.path}/$id.json")

//					"downSync_upsert"
				localObject.modifiedTimestamp == remoteObject.modifiedTimestamp && localObject.isDeleted != remoteObject.isDeleted && localObject.isDeleted ->
					Operation.DownSync.Upsert(realmUUID = id, path = "${DropboxApi.Companion.Path.Chapter.path}/$id.json")

//					"upSync_upsert"
				localObject.modifiedTimestamp == remoteObject.modifiedTimestamp && localObject.isDeleted != remoteObject.isDeleted && remoteObject.isDeleted ->
					Operation.UpSync.Upsert(realmUUID = id, path = "${DropboxApi.Companion.Path.Chapter.path}/$id.json", modifiedTimestamp = localObject.modifiedTimestamp)

				else -> Operation.NoOp.Skip(realmUUID = id, path = "${DropboxApi.Companion.Path.Chapter.path}/${id}.json")
			}
		}

		val newMetadataMap: MutableMap<RealmUUID, ObjectMetadata> = mutableMapOf()

		val upSyncOperationResultList = upSync<T>(upSyncOperationMap = operationMap.filterValues { it is Operation.UpSync } as Map<RealmUUID, Operation.UpSync>)
		val downSyncOperationResultMap = downSync<T>(downSyncOperationMap = operationMap.filterValues { it is Operation.DownSync } as Map<RealmUUID, Operation.DownSync>)

		Log.d("rits", "DropboxSyncInatorService.syncRealm: upSyncOperationResultList: ${upSyncOperationResultList.size}")
		Log.d("rits", "DropboxSyncInatorService.syncRealm: downSyncOperationResultMap: ${downSyncOperationResultMap.size}")

//		Entries in common
		newMetadataMap.putAll(localRemoteDifference.entriesInCommon())

//		Upsyced entries
		upSyncOperationResultList.forEach { (id, upSyncActiveOperationResult) ->
			val operation = upSyncActiveOperationResult.first
			when (val operationResult = upSyncActiveOperationResult.second) {
				is UploadFileResult.Success -> newMetadataMap[id] = ObjectMetadata.fromMetadata(operationResult.metadata)
				is DeleteFileResult.Success -> newMetadataMap[id] = ObjectMetadata(modifiedTimestamp = operation.modifiedTimestamp, hash = "", isDeleted = true)
				is DeleteFileResult.Error.DeleteError -> newMetadataMap[id] = ObjectMetadata(modifiedTimestamp = operation.modifiedTimestamp, hash = "", isDeleted = false)
				is UploadFileResult.Error -> if (BuildConfig.DEBUG) operationResult.exception?.printStackTrace()
				else -> Unit
			}
		}

//		Downsynced entries
		downSyncOperationResultMap.forEach { (id, downSyncOperationResult) ->
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
		differentEntries.filterValues { it is Operation.NoOp }.mapValues { it.value.rightValue() }.let { newMetadataMap.putAll(it) }

		return newMetadataMap + operationMap.filterValues { it is Operation.NoOp }.mapValues { localMetadataMap[it.key]!! }
	}

	private inline fun <reified T : TypedRealmObject> getLocalMetadata(
		idGetter: KMutableProperty1<T, RealmUUID>,
		toObjectMetadata: KFunction1<T, ObjectMetadata>,
	): Map<RealmUUID, ObjectMetadata> {
		return (repository?.getDeletedObjectOfType<T>()?.mapValues { it.value.toObjectMetadata() } ?: mapOf()) + (repository?.getAllObjectOfType<T>(includeLocked = true)?.associate { idGetter(it) to toObjectMetadata(it) } ?: mapOf())
	}

	private suspend fun DropboxApi.getRemoteMetadata(path: String, rectify: Boolean): Map<RealmUUID, ObjectMetadata> = withConnection {
		when (val remoteMetadataDownloadResult = downloadFile(path = "$path/metadata.json")) {
			is DownloadFileResult.Success -> {
				return@withConnection if (rectify) getFullRemoteMetadata(path = path)
				else json.decodeFromString<Map<RealmUUID, ObjectMetadata>>(remoteMetadataDownloadResult.byteArray.decodeToString())
			}

			is DownloadFileResult.FileNotFound -> return@withConnection getFullRemoteMetadata(path = path)
			is DownloadFileResult.Error.DownloadError -> onUnknownError(remoteMetadataDownloadResult.exception)
			is DownloadFileResult.Error.CredentialsError -> onCredentialsError()
			is DownloadFileResult.Error.NetworkError -> onNetworkError()
			is DownloadFileResult.Error.UnknownError -> onUnknownError(remoteMetadataDownloadResult.exception)
		}
		return@withConnection mapOf()
	} ?: mapOf()

	/**
	 * Scan all the files in remote directory and return the metadata map
	 */
	private suspend fun DropboxApi.getFullRemoteMetadata(path: String): Map<RealmUUID, ObjectMetadata> = withConnection {
		val metadataMap: MutableMap<RealmUUID, ObjectMetadata> = mutableMapOf()
		when (val listFolderResult = listFolder(path = path)) {
			is ListFolderResult.Success -> {
				listFolderResult.metadataList.forEach { metadata ->
					val realmUUID = RealmUUID.from(metadata.name.substringBeforeLast("."))
					metadataMap[realmUUID] = ObjectMetadata.fromMetadata(metadata)
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

	private suspend inline fun <reified T : TypedRealmObject> DropboxApi.upSync(upSyncOperationMap: Map<RealmUUID, Operation.UpSync>): Map<RealmUUID, Pair<Operation.UpSync, DropboxOperation.ActiveOperationResult>> {
		val upSyncOperationResultMap: MutableMap<RealmUUID, Pair<Operation.UpSync, DropboxOperation.ActiveOperationResult>> = mutableMapOf()
		upSyncOperationMap.forEach { (id, upSyncOperation) ->
			val operationResult = withConnection {
				when (upSyncOperation) {
					is Operation.UpSync.Upsert -> {
						val syncable = repository?.getObjectFromId<T>(id = upSyncOperation.realmUUID) as? Syncable
						return@withConnection syncable?.toCloudSnapshot()?.let { uploadFile(path = upSyncOperation.path, byteArray = it.encodeToByteArray(), modifiedTimestamp = upSyncOperation.modifiedTimestamp) }
							?: UploadFileResult.Error.UnknownError()
					}

					is Operation.UpSync.Delete -> {
						return@withConnection deleteFile(path = upSyncOperation.path)
					}
				}
			}

			operationResult?.let { upSyncOperationResultMap[upSyncOperation.realmUUID] = Pair(upSyncOperation, it) }
		}

		return upSyncOperationResultMap
	}

	private suspend inline fun <reified T : TypedRealmObject> DropboxApi.downSync(downSyncOperationMap: Map<RealmUUID, Operation.DownSync>): Map<RealmUUID, Pair<Operation.DownSync, Boolean>> {
		val downSyncOperationResultMap: MutableMap<RealmUUID, Pair<Operation.DownSync, Boolean>> = mutableMapOf()
		downSyncOperationMap.forEach { (id, downSyncOperation) ->
			when (downSyncOperation) {
				is Operation.DownSync.Upsert -> withConnection {
					val downloadFileResult = downloadFile(path = downSyncOperation.path)
					if (downloadFileResult is DownloadFileResult.Success) {
						val jsonObject = JSONObject(downloadFileResult.byteArray.decodeToString())
						try {
							when (T::class) {
								ChapterObject::class -> {
									val chapterObject = ChapterObject(jsonObject = jsonObject)
									repository?.putChapter(chapterObject = chapterObject, modifyTimestampAuto = false)
								}

								NoteObject::class -> {
									val noteObject = NoteObject(jsonObject = jsonObject)
									repository?.putNote(noteObject = noteObject, modifyTimestampAuto = false)
								}

								BucketObject::class -> {
									val bucketObject = BucketObject(jsonObject = jsonObject)
									repository?.putBucket(bucketObject = bucketObject, modifyTimestampAuto = false)
								}

								BucketItemObject::class -> {
									val bucketItemObject = BucketItemObject(jsonObject = jsonObject)
									repository?.putBucketItem(bucketItemObject = bucketItemObject, modifyTimestampAuto = false)
								}

								TagObject::class -> {
									val tagObject = com.syncodec.graphite.di.model.local.TagObject(jsonObject = jsonObject)
									repository?.putTag(tagObject = tagObject, modifyTimestampAuto = false)
								}
							}
							downSyncOperationResultMap[id] = Pair(downSyncOperation, true)
						} catch (e: Exception) {
							if (BuildConfig.DEBUG) e.printStackTrace()
							downSyncOperationResultMap[id] = Pair(downSyncOperation, false)
						}
					}
				}

				is Operation.DownSync.Delete -> {
					repository?.delete(id = id, keepHistory = true)
					downSyncOperationResultMap[id] = Pair(downSyncOperation, true)
				}
			}
		}

		return downSyncOperationResultMap
	}

	private fun mergeMetadata(
		remoteMetadataMap: Map<RealmUUID, ObjectMetadata>,
		localMetadataMap: Map<RealmUUID, ObjectMetadata>,
	) {

	}

	companion object {
		@Serializable
		data class Locker(
			val sessionId: String,
			val timestamp: Long,
		)

		@Serializable
		data class ObjectMetadata(
			val modifiedTimestamp: Long,
			val hash: String,
			val isDeleted: Boolean
		) {
			companion object {
				fun fromMetadata(metadata: Metadata): ObjectMetadata {
//					 SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)

					val metadataJsonObject = JSONObject(metadata.toStringMultiline())
					val timestamp = try {
						SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).parse(metadataJsonObject.optString("client_modified"))?.time ?: 0
					} catch (e: Exception) {
						if (BuildConfig.DEBUG) e.printStackTrace()
						0
					}
					val hash = metadataJsonObject.optString("content_hash")

					return ObjectMetadata(modifiedTimestamp = timestamp, hash = hash, isDeleted = false)
				}
			}
		}
	}
}

class DropboxSyncServiceConnectionManager(private val context: Context, private val onBound: (DyncInator) -> Unit) : ServiceConnection {
	var service: DyncInator? = null
	private var attemptingToBind = false
	private var bound = false

	init {
		bindToService()
	}

	private fun bindToService() {
		if (!attemptingToBind) {
			attemptingToBind = true
			Intent(context, DyncInator::class.java).let { intent ->
				intent.putExtra("syncProvider", "Dropbox")
				context.startService(intent)
				context.bindService(intent, this, Context.BIND_AUTO_CREATE)
			}
		}
	}

	override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
		attemptingToBind = false
		bound = true
		(iBinder as DyncInator.DyncInatorBinder).service.let {
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
