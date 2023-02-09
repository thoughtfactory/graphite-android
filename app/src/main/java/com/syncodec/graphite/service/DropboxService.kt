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
import com.dropbox.core.InvalidAccessTokenException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.SearchOptions
import com.dropbox.core.v2.files.WriteMode
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.repository.RealmUUIDDeserializer
import com.syncodec.graphite.di.repository.RealmUUIDKeyDeserializer
import com.syncodec.graphite.di.repository.RealmUUIDSerializer
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import com.syncodec.graphite.di.sync.dropbox.DBox
import com.syncodec.graphite.di.sync.dropbox.DropboxResponse
import com.syncodec.graphite.utils.dbxHash
import com.syncodec.graphite.utils.sha256
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.Date
import kotlin.reflect.KFunction1
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
		)
	}.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)


	private var serviceLooper : Looper? = null
	private var serviceHandler : ServiceHandler? = null

	private var dropboxServiceBinder : IBinder? = DropboxServiceBinder()

	//	val presenter : Presenter by inject()
	val repository : KoinRepository by inject()
	val dBox : DBox by inject()

	val dropboxSyncStatus = MutableStateFlow<DropboxSyncStatus>(DropboxSyncStatus.Init)

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
		val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
		chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
		getSystemService(NotificationManager::class.java).createNotificationChannel(chan)


		val notification : Notification = Notification.Builder(this, channelId)
			.setContentTitle("Synchronizing")
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

	fun initSync() {
		if (dropboxSyncStatus.value == DropboxSyncStatus.Init) {
			Log.i("npr71", "DropboxService: startSync")
			Toast.makeText(this, "startSync", Toast.LENGTH_SHORT).show()
			dropboxSyncStatus.tryEmit(DropboxSyncStatus.Loading)

			val config = DbxRequestConfig("Graphite")
			dBox.getAccessToken {
				if (it is DropboxResponse.Success<*>) {
					DbxClientV2(config, it.result as String).let {
						dropboxSyncStatus.tryEmit(DropboxSyncStatus.Connected)
						lifecycleScope.launch(Dispatchers.IO) {
							it.checkLock()
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

	fun forceSync() {
		if (dropboxSyncStatus.value !is DropboxSyncStatus.Loading && dropboxSyncStatus.value !is DropboxSyncStatus.Connected && dropboxSyncStatus.value !is DropboxSyncStatus.Syncing) {
			Log.i("npr71", "DropboxService: startSync")
			Toast.makeText(this, "startSync", Toast.LENGTH_SHORT).show()
			dropboxSyncStatus.tryEmit(DropboxSyncStatus.Loading)

			val config = DbxRequestConfig("Graphite")
			dBox.getAccessToken {
				if (it is DropboxResponse.Success<*>) {
					DbxClientV2(config, it.result as String).let {
						dropboxSyncStatus.tryEmit(DropboxSyncStatus.Connected)
						lifecycleScope.launch(Dispatchers.IO) {
							it.checkLock()
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

	@WorkerThread
	private suspend fun DbxClientV2.checkLock() {
		val searchOption = SearchOptions
			.newBuilder()
			.withFilenameOnly(true)
			.withFileExtensions(listOf("lock"))
			.build()

		try {
			files()
				.searchV2Builder("graphite")
				.withOptions(searchOption)
				.start()
				.matches
				.isEmpty()
				.let { isEmpty ->
					if (isEmpty) {
						lockDrive()
						sync()
						unlockDrive()
					} else {
						dropboxSyncStatus.tryEmit(DropboxSyncStatus.DriveLocked)
					}
				}
		} catch (e : InvalidAccessTokenException) {
			dropboxSyncStatus.tryEmit(DropboxSyncStatus.SyncError)
		} catch (e : Exception) {
			dropboxSyncStatus.tryEmit(DropboxSyncStatus.SyncError)
			e.printStackTrace()
			Log.i("npr71", "DropboxService: ${e.message}")
		}
	}

	@WorkerThread
	fun DbxClientV2.lockDrive() {
		Log.i("npr71", "DropboxService: lockDrive")
		val currentTimestamp = "${System.currentTimeMillis()}".toByteArray()

		files()
			.upload("/sync/graphite.lock")
			.uploadAndFinish(currentTimestamp.inputStream()) {
				if (it == currentTimestamp.size.toLong()) {
					Log.i("npr71", "DropboxService: lockDrive: upload success")
					dropboxSyncStatus.tryEmit(DropboxSyncStatus.Syncing(0, 0))
				} else {
					Log.i("npr71", "DropboxService: lockDrive: upload failed")
				}
			}
	}

	@WorkerThread
	fun DbxClientV2.unlockDrive() {
		Log.i("npr71", "DropboxService: unlockDrive")
		files()
			.deleteV2("/sync/graphite.lock")
			.let {
				Log.i("npr71", "DropboxService: unlockDrive: delete success")
				dropboxSyncStatus.tryEmit(DropboxSyncStatus.Init)
			}
	}

	@WorkerThread
	fun DbxClientV2.sync() {
		downloadMetadata().let { dropboxMetadata ->

			Log.i("npr71", "DropboxService: sync: dropboxMetadata: $dropboxMetadata")

			val localChapterList = repository.getAllChapter().map { it.clone() }
			val localNoteList = repository.getAllNote().map { it.clone() }

			syncObjects(
				localObjectList = localChapterList,
				cloudObjectMetadata = dropboxMetadata?.chapterMetadata ?: mapOf(),
				objectPath = "chapter",
				id = ChapterObject::id,
				modifiedTimestamp = ChapterObject::modifiedTimestamp,
				toCloudSnapshot = ChapterObject::toCloudSnapshot
			) {
				it.forEach { (chapterObject, operation) ->
					if (operation == Operation.Create || operation == Operation.Update) {
						ChapterObject(chapterObject).apply { repository.putChapter(this, false) }
					} else if (operation == Operation.Delete) {
//						repository.deleteChapterSuspended(chapterObject)
					}
				}
			}

			syncObjects(
				localObjectList = localNoteList,
				cloudObjectMetadata = dropboxMetadata?.noteMetadata ?: mapOf(),
				objectPath = "note",
				id = NoteObject::id,
				modifiedTimestamp = NoteObject::modifiedTimestamp,
				toCloudSnapshot = NoteObject::toCloudSnapshot
			) {
				it.forEach { (noteObject, operation) ->
					if (operation == Operation.Create || operation == Operation.Update) {
						NoteObject(noteObject).apply { repository.putNote(this, false) }
					} else if (operation == Operation.Delete) {
						//						repository.deleteNoteSuspended(noteObject)
					}
				}
			}

			updateMetadata()
		}
	}

	private fun DbxClientV2.downloadMetadata() : DropboxMetadata? {

		val searchOption = SearchOptions
			.newBuilder()
			.withFilenameOnly(true)
			.withFileExtensions(listOf("json"))
			.withPath("/sync")
			.build()

		files()
			.searchV2Builder("metadata")
			.withOptions(searchOption)
			.start()
			.matches
			.isEmpty()
			.let { isEmpty ->
				if (isEmpty) {
					Log.i("npr71", "DropboxService: downloadMetadata: metadata not found")
					return null
				} else {
					files()
						.downloadBuilder("/sync/metadata.json")
						.start()
						.inputStream
						.readBytes()
						.let {
							return try {
								objectMapper.readValue<DropboxMetadata>(it)
							} catch (e : Exception) {
								e.printStackTrace()
								null
							}
						}
				}
			}
	}

	@WorkerThread
	private inline fun <reified T> DbxClientV2.syncObjects(
		localObjectList : List<T>,
		cloudObjectMetadata : Map<RealmUUID, DropboxInnerMetadata>,
		objectPath : String,
		id : KMutableProperty1<T, RealmUUID>,
		modifiedTimestamp : KMutableProperty1<T, Long>,
		toCloudSnapshot : KFunction1<T, String>,
		updateLocalData : (Map<ByteArray, Operation>) -> Unit = {},
	) {
		val (toUpSyncObjectList, toDownSyncObjectList) = countUnSyncedObjects(
			cloudObjectMetadata = cloudObjectMetadata,
			localObjectList = localObjectList,
			id = id,
			modifiedTimestamp = modifiedTimestamp,
			toCloudSnapshot = toCloudSnapshot
		)

		dropboxSyncStatus.tryEmit(DropboxSyncStatus.Syncing(toUpSyncObjectList.size, toDownSyncObjectList.size))

		val downloadedObjectList = mutableMapOf<ByteArray, Operation>()

		toDownSyncObjectList.toList().forEachIndexed { index, (chapterId, operation) ->
			downloadedObjectList[downSyncObject(objectPath, chapterId.toString())] = operation
			dropboxSyncStatus.tryEmit(DropboxSyncStatus.Syncing(toUpSyncObjectList.size, toDownSyncObjectList.size - index))
		}

		updateLocalData(downloadedObjectList)

		toUpSyncObjectList.forEachIndexed { index, objectId ->
			upSyncObject(objectPath, objectId.toString(), toCloudSnapshot(localObjectList.first { id.get(it) == objectId }).encodeToByteArray())
			dropboxSyncStatus.tryEmit(DropboxSyncStatus.Syncing(toUpSyncObjectList.size - index, toDownSyncObjectList.size))
		}
	}

	@WorkerThread
	private fun DbxClientV2.downSyncObject(objectPath : String, objectId : String) = files()
		.downloadBuilder("/sync/$objectPath/$objectId.json")
		.start()
		.inputStream
		.readBytes()

	@WorkerThread
	private fun DbxClientV2.upSyncObject(objectPath : String, objectId : String, byteArray : ByteArray) {
		val inputStream = byteArray.inputStream()
		files()
			.uploadBuilder("/sync/$objectPath/$objectId.json")
			.withMode(WriteMode.OVERWRITE)
			.withStrictConflict(false)
			.withAutorename(false)
			.start()
			.uploadAndFinish(inputStream) { inputStream.close() }
	}

	@WorkerThread
	private fun <T> countUnSyncedObjects(
		cloudObjectMetadata : Map<RealmUUID, DropboxInnerMetadata>,
		localObjectList : List<T>,
		id : KMutableProperty1<T, RealmUUID>,
		modifiedTimestamp : KMutableProperty1<T, Long>,
		toCloudSnapshot : KFunction1<T, String>,
	) : Pair<List<RealmUUID>, Map<RealmUUID, Operation>> {
		val toUpSyncObjectList = mutableListOf<RealmUUID>()
		val toDownSyncObjectList = mutableMapOf<RealmUUID, Operation>()

		val localObjectIdList = localObjectList.map { id.get(it) }
		val cloudObjectIdList = cloudObjectMetadata.keys

		localObjectList.forEach { localChapterObject ->
			val localObjectId = id.get(localChapterObject)
			val localModifiedTimestamp = modifiedTimestamp.get(localChapterObject)

			if (localObjectId !in cloudObjectIdList) {
				toUpSyncObjectList.add(localObjectId)
				Log.i("npr71", "$localObjectId not in cloudObjectIdList")
			} else {
				val remoteHash = cloudObjectMetadata[localObjectId]?.hash
				val localHash = objectMapper
					.writeValueAsString(localChapterObject)
					.toByteArray()
					.dbxHash()
					.joinToString("") { java.lang.String.format("%02x", it) }

				if (
					localHash != remoteHash &&
					localModifiedTimestamp > (cloudObjectMetadata[localObjectId]?.modifiedTimestamp ?: 0)
				) {
					toUpSyncObjectList.add(localObjectId)
					Log.i("npr71", "$localObjectId localHash != remoteHash && localModifiedTimestamp > cloudModifiedTimestamp")
				}
			}
		}

		cloudObjectMetadata.forEach { (cloudObjectId, dbxInnerData) ->
			if (cloudObjectId !in localObjectIdList) toDownSyncObjectList[cloudObjectId] = Operation.Create
			else {
				val remoteHash = dbxInnerData.hash
				val localHash = toCloudSnapshot(localObjectList.first { id.get(it) == cloudObjectId })
				if (
					localHash != remoteHash &&
					dbxInnerData.modifiedTimestamp > modifiedTimestamp.get(localObjectList.first { id.get(it) == cloudObjectId })
				) {
					toDownSyncObjectList[cloudObjectId] = Operation.Update
					Log.i("npr71", "$cloudObjectId localHash != remoteHash && cloudModifiedTimestamp > localModifiedTimestamp")
				}
			}
		}

		return Pair(toUpSyncObjectList, toDownSyncObjectList)
	}

	private fun DbxClientV2.updateMetadata() {

		val localChapterList = repository.getAllChapter().map { it.clone() }
		val localNoteList = repository.getAllNote().map { it.clone() }

		val dropboxMetadata = DropboxMetadata()

		localChapterList
			.associate {
				Pair(it.id, DropboxInnerMetadata(it.modifiedTimestamp, it.toCloudSnapshot().sha256(), false))
			}.let { dropboxMetadata.chapterMetadata.putAll(it) }

		localNoteList
			.associate {
				Pair(it.id, DropboxInnerMetadata(it.modifiedTimestamp, it.toCloudSnapshot().sha256(), false))
			}.let { dropboxMetadata.noteMetadata.putAll(it) }

		objectMapper.writeValueAsString(dropboxMetadata).let {
			files()
				.uploadBuilder("/sync/metadata.json")
				.withClientModified(Date(System.currentTimeMillis()))
				.withMode(WriteMode.OVERWRITE)
				.withStrictConflict(false)
				.withAutorename(false)
				.start()
				.uploadAndFinish(it.encodeToByteArray().inputStream())
		}
	}

	inner class DropboxServiceBinder : Binder() {
		val service : DropboxService
			get() = this@DropboxService
	}

	companion object {
		class DropboxMetadata {
			@JsonDeserialize(keyUsing = RealmUUIDKeyDeserializer::class)
			val chapterMetadata : MutableMap<RealmUUID, DropboxInnerMetadata> = mutableMapOf()

			@JsonDeserialize(keyUsing = RealmUUIDKeyDeserializer::class)
			val noteMetadata : MutableMap<RealmUUID, DropboxInnerMetadata> = mutableMapOf()
		}

		data class DropboxInnerMetadata(
			val modifiedTimestamp : Long,
			val hash : String,
			val isDeleted : Boolean
		)

		sealed class DropboxSyncStatus {
			object Init : DropboxSyncStatus()
			object SyncNotConfigured : DropboxSyncStatus()
			object SyncDisabled : DropboxSyncStatus()
			object NoInternet : DropboxSyncStatus()
			object NotLoggedIn : DropboxSyncStatus()
			object Loading : DropboxSyncStatus()
			object Connected : DropboxSyncStatus()
			class Syncing(val toUpSyncCount : Int, val toDownSyncCount : Int) : DropboxSyncStatus()
			object SyncError : DropboxSyncStatus()
			object DriveLocked : DropboxSyncStatus()
		}

		enum class Operation {
			Create,
			Update,
			Delete
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
