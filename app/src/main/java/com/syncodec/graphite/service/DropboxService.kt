package com.syncodec.graphite.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.DeleteErrorException
import com.dropbox.core.v2.files.DownloadErrorException
import com.dropbox.core.v2.files.WriteMode
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.collect.Maps
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BaseObject
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.DeletedObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.AttachmentRepository
import com.syncodec.graphite.di.repository.RealmUUIDDeserializer
import com.syncodec.graphite.di.repository.RealmUUIDKeyDeserializer
import com.syncodec.graphite.di.repository.RealmUUIDSerializer
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import com.syncodec.graphite.di.sync.dropbox.DBox
import com.syncodec.graphite.utils.toDbxHashString
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.io.InputStream
import java.util.Date
import kotlin.reflect.KMutableProperty1


class DropboxService : LifecycleService() {

	private val objectMapper = jsonMapper {
		addModule(
			kotlinModule()
				.addSerializer(
					RealmUUID::class.java,
					RealmUUIDSerializer()
				)
				.addDeserializer(
					RealmUUID::class.java,
					RealmUUIDDeserializer()
				)
				.addKeyDeserializer(
					RealmUUID::class.java,
					RealmUUIDKeyDeserializer()
				)
		)
	}.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)


	private var serviceLooper : Looper? = null
	private var serviceHandler : ServiceHandler? = null

	private var dropboxServiceBinder : IBinder? = DropboxServiceBinder()

//	Don't make these private or lateinit
	val repository : KoinRepository by inject()
	val attachmentRepository : AttachmentRepository by inject()
	val dBox : DBox by inject()

	val dropboxSyncStatus = MutableStateFlow<DropboxSyncStatus>(DropboxSyncStatus.Init)

	init {
		lifecycleScope.launch(Dispatchers.Default) {
			dropboxSyncStatus.collect {
				Log.i("npr71", "DropboxService: dropboxSyncStatus: ${it::class.simpleName}")
			}
		}
	}

	// Handler that receives messages from the thread
	private inner class ServiceHandler(looper : Looper) : Handler(looper) {

		override fun handleMessage(msg : Message) {
//			Log.i("npr71", "DropboxService: handleMessage")
			// Normally we would do some work here, like download a file.
			// For our sample, we just sleep for 5 seconds.
			try {
				Thread.sleep(5000)
			} catch (e : InterruptedException) {
				// Restore interrupt status.
				Thread.currentThread().interrupt()
			}

			// Stop the service using the startId, so that we don't stop
			// the service in the middle of handling another job
//			stopSelf(msg.arg1)
		}
	}

	private fun moveToForeground() {
		val channelId = "dropboxSyncService"
		val channelName = "Dropbox Sync"
		val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
		chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
		getSystemService(NotificationManager::class.java).createNotificationChannel(chan)

		val notification : Notification = Notification.Builder(this, channelId)
			.setContentTitle("Graphite")
			.setContentText("Synchronizing with Dropbox")
			.setSmallIcon(R.drawable.ic_ring)
			.setOngoing(true)
			.build()

		startForeground(71, notification)
	}

	override fun onCreate() {
		super.onCreate()
		Log.i("npr71", "DropboxService: onCreate")

		moveToForeground()

		HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
			start()
			serviceLooper = looper
			serviceHandler = ServiceHandler(looper)
		}
	}

	override fun onStartCommand(intent : Intent?, flags : Int, startId : Int) : Int {
		super.onStartCommand(intent, flags, startId)
		return START_STICKY
	}

	override fun onBind(intent : Intent) : IBinder? {
		super.onBind(intent)
		Log.i("npr71", "DropboxService: onBind")
		Toast.makeText(this, "service bind", Toast.LENGTH_SHORT).show()
		return dropboxServiceBinder
	}

	override fun onUnbind(intent : Intent?) : Boolean {
		Log.i("npr71", "DropboxService: onUnbind")
		Toast.makeText(this, "service unbind", Toast.LENGTH_SHORT).show()
		return super.onUnbind(intent)
	}

	override fun onDestroy() {
		super.onDestroy()
		Log.i("npr71", "DropboxService: onDestroy")
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
	}

	var lockCoroutineScope : CoroutineScope? = null

	fun initSync() {
		Log.i("npr71", "DropboxService : fun : initSync")
		if (dropboxSyncStatus.value is DropboxSyncStatus.Init || dropboxSyncStatus.value is DropboxSyncStatus.Idle) {
			Toast.makeText(this, "startSync", Toast.LENGTH_SHORT).show()
			dropboxSyncStatus.tryEmit(DropboxSyncStatus.Loading)

			val config = DbxRequestConfig("Graphite")
			dBox.getAccessToken {
				if (it is DBox.Companion.AccessTokenResponseResponse.Success) {
					val accessToken =
						"sl.BadrlZktMta_HVrMyLAq6j1ti92zsvD-kvRZLooMNg9yK9a_Lym1V_euCoIZnZtxPkzUwrFmWjjEXiYlL8CftktmSNc6u6V7WYjllgyi8VH4UXThIpIp7dLjkd88MtUfca0IgThnXPU"
//					DbxClientV2(config, it.result as String).let {
					DbxClientV2(config, accessToken).let { dbxClientV2 ->
						dropboxSyncStatus.tryEmit(DropboxSyncStatus.Connected)
						lifecycleScope.launch(Dispatchers.IO) {
							dbxClientV2.checkLock {
								dropboxSyncStatus.tryEmit(DropboxSyncStatus.Syncing())
								dbxClientV2.lock()
								dbxClientV2.sync()
								lockCoroutineScope?.cancel()
								dbxClientV2.unlock()
							}
						}
					}
				}
			}

			lifecycleScope.launch(Dispatchers.Default) {
				while (true) {
					serviceHandler?.sendEmptyMessage(0)
					delay(5000)
				}
			}
		}
	}

	/**
	 * @return true if lock is present
	 */
	private suspend fun DbxClientV2.checkLock(callback : suspend () -> Unit) {
		try {
			files().download(Path.Lock.path)
			dropboxSyncStatus.tryEmit(DropboxSyncStatus.Locked)
		} catch (e : DownloadErrorException) {
			if (e.errorValue.pathValue.isNotFound) callback()
			else dropboxSyncStatus.tryEmit(DropboxSyncStatus.Failed)
		} catch (e : Exception) {
			dropboxSyncStatus.tryEmit(DropboxSyncStatus.Failed)
		}
	}

	private fun DbxClientV2.lock() {
		lifecycleScope.launch(Dispatchers.IO) {
			lockCoroutineScope?.cancel()
			lockCoroutineScope = this
			kotlin.run breaking@{
				while (true) {
					dropboxSyncStatus.value.let {
						when (it) {
							is DropboxSyncStatus.Connected -> {
								delay(30000)
							}

							is DropboxSyncStatus.Syncing -> {
								reLock(it.sessionId)
								delay(30000)
							}

							is DropboxSyncStatus.Idle -> {
								return@breaking
							}

							else -> return@breaking
						}
					}
				}
			}
		}
	}

	private suspend fun DbxClientV2.unlock(repeat : Int = 13) {
		for (i in 0 until repeat) {
			try {
				files().deleteV2(Path.Lock.path)
				dropboxSyncStatus.tryEmit(DropboxSyncStatus.Idle)
				break
			} catch (e : DownloadErrorException) {
				if (e.errorValue.pathValue.isNotFound) {
					dropboxSyncStatus.tryEmit(DropboxSyncStatus.Idle)
					break
				}
			} catch (e : Exception) {
			}
			delay(1300)
		}
	}

	private fun DbxClientV2.reLock(sessionId : String) {
		val timestamp = System.currentTimeMillis()
		val jsonObject = JSONObject()
		jsonObject.put("sessionId", sessionId)
		jsonObject.put("timestamp", timestamp)
		files()
			.uploadBuilder(Path.Lock.path)
			.withMode(WriteMode.OVERWRITE)
			.withClientModified(Date(timestamp))
			.uploadAndFinish(jsonObject.toString().toByteArray().inputStream())
	}

	@WorkerThread
	private fun DbxClientV2.sync() {
		val baseObject = repository.getBaseObject()
		val noteObjectList = repository.getAllNote()
		val chapterObjectList = repository.getAllChapter()
		val bucketItemObjectList = repository.getAllBucketItem()
		val bucketObjectList = repository.getAllBucket()
		val tagObjectList = repository.getAllTag()

		val localDeletedObjectIdList = baseObject?.deletedObjectSet ?: setOf()

		val chapterMetadataPath = "${Path.Root.path}/${ChapterObject::class.simpleName}/metadata.json"
		calculateDiffAndSync(
			metadataPath = chapterMetadataPath,
			localObjectList = chapterObjectList.associateBy { it.id },
			localDeletedLocalObjectList = localDeletedObjectIdList,
			modifiedTimestamp = ChapterObject::modifiedTimestamp,
		)

		val noteMetadataPath = "${Path.Root.path}/${NoteObject::class.simpleName}/metadata.json"
		calculateDiffAndSync(
			metadataPath = noteMetadataPath,
			localObjectList = noteObjectList.associateBy { it.id },
			localDeletedLocalObjectList = localDeletedObjectIdList,
			modifiedTimestamp = NoteObject::modifiedTimestamp,
		)

		val bucketMetadataPath = "${Path.Root.path}/${BucketObject::class.simpleName}/metadata.json"
		calculateDiffAndSync(
			metadataPath = bucketMetadataPath,
			localObjectList = bucketObjectList.associateBy { it.id },
			localDeletedLocalObjectList = localDeletedObjectIdList,
			modifiedTimestamp = BucketObject::modifiedTimestamp,
		)

		val bucketItemMetadataPath = "${Path.Root.path}/${BucketItemObject::class.simpleName}/metadata.json"
		calculateDiffAndSync(
			metadataPath = bucketItemMetadataPath,
			localObjectList = bucketItemObjectList.associateBy { it.id },
			localDeletedLocalObjectList = localDeletedObjectIdList,
			modifiedTimestamp = BucketItemObject::modifiedTimestamp,
		)

		val tagMetadataPath = "${Path.Root.path}/${TagObject::class.simpleName}/metadata.json"
		calculateDiffAndSync(
			metadataPath = tagMetadataPath,
			localObjectList = tagObjectList.associateBy { it.id },
			localDeletedLocalObjectList = localDeletedObjectIdList,
			modifiedTimestamp = TagObject::modifiedTimestamp,
		)

		val baseObjectMetadataPath = "${Path.Root.path}/${BaseObject::class.simpleName}/metadata.json"
		syncBaseObject(
			metadataPath = baseObjectMetadataPath,
			localBaseObject = baseObject,
		)
	}

	private inline fun <reified T> DbxClientV2.calculateDiffAndSync(
		metadataPath : String,
		localObjectList : Map<RealmUUID, T>,
		localDeletedLocalObjectList : Set<DeletedObject>,
		modifiedTimestamp : KMutableProperty1<T, Long>,
	) {
		downloadMetadata(metadataPath) { remoteObjectList ->
			val toDownSyncObjectIdList = mutableMapOf<RealmUUID, Operation>()
			val toUpSyncObjectIdList = mutableMapOf<RealmUUID, Operation>()

			val leftObjectIdList = localObjectList.mapValues {
				ObjectMetadata(
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
				leftObjectIdList[it.id] = ObjectMetadata(modifiedTimestamp = it.deletedTimestamp, hash = "", isDeleted = true)
			}
			val rightObjectIdList = remoteObjectList.toMap()
			val diff = Maps.difference(leftObjectIdList, rightObjectIdList)
			diff.entriesOnlyOnLeft().forEach { (id, objectMetadata) ->
				if (objectMetadata.isDeleted) toUpSyncObjectIdList[id] =
					localDeletedLocalObjectList.find { it.id == id }?.let { Operation.Delete(it.objectType) } ?: Operation.Delete()
				else toUpSyncObjectIdList[id] = Operation.Create
			}
			diff.entriesOnlyOnRight().forEach { (id, objectMetadata) ->
				if (objectMetadata.isDeleted) toDownSyncObjectIdList[id] = Operation.Delete()
				else toDownSyncObjectIdList[id] = Operation.Create
			}
			diff.entriesDiffering().forEach { (id, difference) ->
				val leftValue = difference.leftValue()
				val rightValue = difference.rightValue()
				if (leftValue.hash != rightValue.hash) {
					when {
						leftValue.modifiedTimestamp == rightValue.modifiedTimestamp -> null
						leftValue.modifiedTimestamp > rightValue.modifiedTimestamp && leftValue.isDeleted -> {
							localDeletedLocalObjectList.find { it.id == id }?.let { Operation.Delete(it.objectType) }?.let { toUpSyncObjectIdList[id] = it }
						}

						leftValue.modifiedTimestamp > rightValue.modifiedTimestamp -> toUpSyncObjectIdList[id] = Operation.Update
						rightValue.isDeleted -> toDownSyncObjectIdList[id] = Operation.Delete()
						else -> toDownSyncObjectIdList[id] = Operation.Update
					}
				}
			}

			toDownSyncObjectIdList.filter { it.value !is Operation.Delete }.forEach { downSync<T>(it.key, it.value) }
			toDownSyncObjectIdList.filter { it.value is Operation.Delete }.let { repository.deleteSuspended(idList = it.keys, keepHistory = false) }
			toUpSyncObjectIdList.forEach { upSync(it.key, localObjectList[it.key], it.value) }

			updateMetadata(
				localObjectList = localObjectList,
				remoteObjectList = remoteObjectList,
				deletedLocalObjectList = localDeletedLocalObjectList,
				modifiedTimestamp = modifiedTimestamp,
			)
		}
	}

	private inline fun <reified T> DbxClientV2.downSync(id : RealmUUID, operation : Operation) {
		when (operation) {
			is Operation.Create -> download("${Path.Root.path}/${T::class.simpleName}/${id}.json").let {
				when (T::class) {
					ChapterObject::class -> {
						ChapterObject.fromCloudSnapshot(it)?.let { repository.putChapter(it, modifyTimestampAuto = false) }
					}

					NoteObject::class -> {
						NoteObject.fromCloudSnapshot(it)?.let { repository.putNote(it, modifyTimestampAuto = false) }
					}

					BucketObject::class -> {
						BucketObject.fromCloudSnapshot(it)?.let { repository.putBucket(it, modifyTimestampAuto = false) }
					}

					BucketItemObject::class -> {
						BucketItemObject.fromCloudSnapshot(it)?.let { repository.putBucketItem(it, modifyTimestampAuto = false) }
					}

					else -> null
				}
			}

			is Operation.Update -> download("${Path.Root.path}/${T::class.simpleName}/${id}.json").let {
				when (T::class) {
					ChapterObject::class -> {
						ChapterObject.fromCloudSnapshot(it)?.let { repository.putChapter(it, modifyTimestampAuto = false) }
					}

					NoteObject::class -> {
						NoteObject.fromCloudSnapshot(it)?.let { repository.putNote(it, modifyTimestampAuto = false) }
					}

					BucketObject::class -> {
						BucketObject.fromCloudSnapshot(it)?.let { repository.putBucket(it, modifyTimestampAuto = false) }
					}

					BucketItemObject::class -> {
						BucketItemObject.fromCloudSnapshot(it)?.let { repository.putBucketItem(it, modifyTimestampAuto = false) }
					}

					else -> null
				}
			}

			is Operation.Delete -> repository.deleteSuspended(id = id, keepHistory = false)
		}
	}

	private inline fun <reified T> DbxClientV2.upSync(id : RealmUUID, localObject : T?, operation : Operation) {
		val cloudSnapshot = when (localObject) {
			is ChapterObject -> localObject.toCloudSnapshot()
			is NoteObject -> localObject.toCloudSnapshot()
			is BucketObject -> localObject.toCloudSnapshot()
			is BucketItemObject -> localObject.toCloudSnapshot()

			else -> null
		}

		when (operation) {
			is Operation.Create -> cloudSnapshot?.byteInputStream()?.let { upload("${Path.Root.path}/${T::class.simpleName}/${id}.json", it) }
			is Operation.Update -> cloudSnapshot?.byteInputStream()?.let { upload("${Path.Root.path}/${T::class.simpleName}/${id}.json", it) }
			is Operation.Delete -> {
				try {
					files().deleteV2("${Path.Root.path}/${operation.objectType}/${id}.json")
				} catch (e : DeleteErrorException) {
					if (e.errorValue.pathLookupValue.isNotFound) {
						// ignore
					} else {
						dropboxSyncStatus.tryEmit(DropboxSyncStatus.Failed)
					}
				} catch (e : Exception) {
					dropboxSyncStatus.tryEmit(DropboxSyncStatus.Failed)
				}
			}
		}
	}

	private fun DbxClientV2.syncBaseObject(
		metadataPath : String,
		localBaseObject : BaseObject?,
	) {
		try {
			download("${Path.Root.path}/Attachment/metadata.json").let {
				val localDeletedAttachmentSet = localBaseObject?.deletedAttachmentSet ?: setOf()
				val localSavedAttachmentList = attachmentRepository
					.getAttachmentDir()
					.listFiles()
					?.map { attachmentDir ->
						attachmentDir.listFiles()
							?.map { AttachmentMetadata(parentId = RealmUUID.Companion.from(attachmentDir.name), name = it.name, isDeleted = false) } ?: listOf()
					}?.flatten() ?: listOf()

				val remoteAttachmentSet = objectMapper.readValue(it, object : TypeReference<MutableSet<AttachmentMetadata>>() {})

//				Delete local attachment
				remoteAttachmentSet.filter { it.isDeleted }.forEach {
					attachmentRepository.delete(parentId = it.parentId, name = it.name)
				}

//				Download attachment
				remoteAttachmentSet.filter { !it.isDeleted && it !in localSavedAttachmentList}.forEach {
					download("${Path.Root.path}/Attachment/${it.parentId}/${it.name}").let { byteArray ->
						attachmentRepository.putAttachment(parentId = it.parentId, name = it.name, byteArray = byteArray)
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
					attachmentRepository.getAttachment(parentId = it.parentId, name = it.name)?.let { file ->
						upload("${Path.Root.path}/Attachment/${it.parentId}/${it.name}", file.inputStream())
					}
				}

				// Upload metadata
				remoteAttachmentSet.addAll(localSavedAttachmentList)
				localDeletedAttachmentSet.map {
					AttachmentMetadata(parentId = it.parentId, name = it.name, isDeleted = true)
				}.let { remoteAttachmentSet.addAll(it) }

				objectMapper.writeValueAsBytes(remoteAttachmentSet).inputStream().let {
					upload("${Path.Root.path}/Attachment/metadata.json", it)
				}
			}
		} catch (e : DownloadErrorException) {
			if (e.errorValue.pathValue.isNotFound) {
				val attachmentList = mutableListOf<AttachmentMetadata>()
				attachmentRepository
					.getAttachmentDir()
					.listFiles()
					?.forEach { attachmentDir ->
						attachmentDir
							.listFiles()
							?.forEach {
								upload("${Path.Root.path}/Attachment/${attachmentDir.name}/${it.name}", it.inputStream())
								AttachmentMetadata(
									parentId = RealmUUID.Companion.from(attachmentDir.name),
									name = it.name,
									isDeleted = false
								).let { attachmentList.add(it) }
							}
					}
				objectMapper.writeValueAsBytes(attachmentList).inputStream().let {
					upload("${Path.Root.path}/Attachment/metadata.json", it)
				}
			}
		} catch (e : Exception) {
			dropboxSyncStatus.tryEmit(DropboxSyncStatus.Failed)
		}
	}

	private fun DbxClientV2.downloadMetadata(path : String, response : (Map<RealmUUID, ObjectMetadata>) -> Unit) {
		try {
			return files()
				.download(path)
				.inputStream
				.readBytes()
				.let { objectMapper.readValue<Map<RealmUUID, ObjectMetadata>>(it) }
				.let { response(it) }
		} catch (e : DownloadErrorException) {
			e.printStackTrace()
			if (e.errorValue.pathValue.isNotFound) {
				response(mapOf())
			} else {
				dropboxSyncStatus.tryEmit(DropboxSyncStatus.Failed)
			}
		} catch (e : Exception) {
			dropboxSyncStatus.tryEmit(DropboxSyncStatus.Failed)
//			e.printStackTrace()
		}
	}

	@WorkerThread
	private inline fun <reified T> DbxClientV2.updateMetadata(
		localObjectList : Map<RealmUUID, T>,
		remoteObjectList : Map<RealmUUID, ObjectMetadata>,
		deletedLocalObjectList : Set<DeletedObject>,
		modifiedTimestamp : KMutableProperty1<T, Long>,
	) {
		val metadata : MutableMap<RealmUUID, ObjectMetadata> = mutableMapOf()

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
				metadata[id] = ObjectMetadata(
					hash = hash,
					modifiedTimestamp = modifiedTimestamp,
					isDeleted = false,
				)
			}
		}
		deletedLocalObjectList.forEach { deletedObject ->
			metadata[deletedObject.id] = ObjectMetadata(modifiedTimestamp = deletedObject.deletedTimestamp, hash = "", isDeleted = true)
		}

		val metadataPath = "${Path.Root.path}/${T::class.simpleName}/metadata.json"
		upload(metadataPath, objectMapper.writeValueAsBytes(metadata).inputStream())
	}

	private fun DbxClientV2.upload(path : String, inputStream : InputStream) {
		this
			.files()
			.uploadBuilder(path)
			.withMode(WriteMode.OVERWRITE)
			.uploadAndFinish(inputStream)
	}

	private fun DbxClientV2.download(path : String) : ByteArray {
		return this
			.files()
			.download(path)
			.inputStream
			.readBytes()
	}

	inner class DropboxServiceBinder : Binder() {
		val service : DropboxService
			get() = this@DropboxService
	}

	companion object {

		sealed class DropboxSyncStatus {
			object Init : DropboxSyncStatus()
			object Idle : DropboxSyncStatus()
			object Loading : DropboxSyncStatus()
			object Locked : DropboxSyncStatus()
			object Connected : DropboxSyncStatus()
			data class Syncing(val sessionId : String = RealmUUID.random().toString()) : DropboxSyncStatus()
			object Disconnected : DropboxSyncStatus()
			object Failed : DropboxSyncStatus()
		}

		enum class Path(val path : String) {
			Root("/sync"),
			DeviceMetadata("/sync/device.metadata"),
			Lock("/sync/graphite.lock"),
			NoteMetadata("/sync/graphite.metadata"),
		}

		data class DeviceMetadata(
			val deviceId : String,
			val lastSyncTimestamp : Long,
		)

		data class ObjectMetadata(
			val modifiedTimestamp : Long,
			val hash : String,
			val isDeleted : Boolean
		)

		data class AttachmentMetadata(
			val parentId : RealmUUID,
			val name : String,
			val isDeleted : Boolean,
		)

		sealed class Operation {
			object Create : Operation()
			object Update : Operation()
			class Delete(val objectType : String? = null) : Operation()
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
			context.unbindService(this)
			bound = false
		}
	}
}
