package com.syncodec.graphite.service

import android.app.Service
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
import android.util.Log
import android.widget.Toast
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.SearchOptions
import com.dropbox.core.v2.files.WriteMode
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.graphite.di.model.ChapterSnapshot
import com.syncodec.graphite.di.model.NoteSnapshot
import com.syncodec.graphite.di.repository.RealmUUIDDeserializer
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.di.sync.DbxToken
import com.syncodec.graphite.di.sync.DropboxApi
import com.syncodec.graphite.di.sync.DropboxInnerMetadata
import com.syncodec.graphite.di.sync.DropboxMetadata
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.alice.AliceRequestResult
import com.syncodec.graphite.utils.alice.getSecretData
import com.syncodec.graphite.utils.dbxHash
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject


enum class DropboxSyncStatus {
	INIT,
	SYNC_DISABLED,
	NO_INTERNET,
	NOT_LOGGED_IN,
	CONNECTED,
	SYNCING,
	SYNC_ERROR,
	DRIVE_LOCKED
}

@AndroidEntryPoint
class DropboxSyncService : Service() {

	private val objectMapper = jsonMapper {
		addModule(
			kotlinModule().addDeserializer(
				RealmUUID::class.java,
				RealmUUIDDeserializer()
			)
		)
	}.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	private var isBound = false

	private var serviceLooper : Looper? = null
	private var serviceHandler : ServiceHandler? = null

	private val binder = LocalBinder()

	inner class LocalBinder : Binder() {
		fun getService() : DropboxSyncService = this@DropboxSyncService
	}

	override fun onBind(intent: Intent): IBinder {
		isBound = true
		Log.d("npr71", "onBind")
		return binder
	}

	override fun onUnbind(intent : Intent?) : Boolean {
		isBound = false
		Log.i("npr71", "onUnbind")
		return super.onUnbind(intent)
	}

	private val dropboxApi = DropboxApi()
	private var dbxToken : DbxToken? = null
	var dropboxSyncStatus = MutableStateFlow(DropboxSyncStatus.INIT)

	init {
		CoroutineScope(Dispatchers.Default).launch {
			dropboxSyncStatus.collect {
				Log.i("npr71", "dropboxSyncStatus = $it")
			}
		}
	}

	@Inject
	lateinit var repository2 : Repository2

	private inner class ServiceHandler(looper : Looper) : Handler(looper) {

		override fun handleMessage(msg : Message) {
			repository2
				.repositoryState
				.value
				.let {
					if (it == RepositoryState.SUCCESS && (dropboxSyncStatus.value == DropboxSyncStatus.INIT || dropboxSyncStatus.value == DropboxSyncStatus.SYNC_ERROR) ) {
						checkSyncPermission {
							if (it) {
								connectWithDropbox().let { _dbxClientV2 ->
									_dbxClientV2?.let { dbxClientV2 ->
										dropboxSyncStatus.tryEmit(DropboxSyncStatus.CONNECTED)
										dbxClientV2.lock()
									}
								}
							} else dropboxSyncStatus.tryEmit(DropboxSyncStatus.SYNC_DISABLED)
						}
					}
				}
		}
	}

	override fun onCreate() {
		super.onCreate()
		HandlerThread("ServiceStartArguments", 10).apply {
			start()
			serviceLooper = looper
			serviceHandler = ServiceHandler(looper)
		}

		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
		CoroutineScope(Dispatchers.Default).launch {
			while (true) {
				delay(5000)
				if (isBound) {
					serviceHandler?.sendEmptyMessage(0)
				} else  {
					serviceLooper?.quit()
					stopSelf()
					break
				}
			}
		}
	}

	override fun onDestroy() {
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
	}

	fun onSync() {
		Log.i("npr71", "onSync")
		serviceHandler?.sendEmptyMessage(0)
	}

	private fun checkSyncPermission(isSyncEnabled : (Boolean) -> Unit) {
		val dataStoreInstance = DataStoreInstance(this)
		CoroutineScope(Dispatchers.IO).launch {
			dataStoreInstance.getIsSyncEnabled().collect {
				isSyncEnabled(it)
				this.cancel()
			}
		}
	}

	private fun connectWithDropbox() : DbxClientV2? {
		val config = DbxRequestConfig("Graphite")
		getSecretData("dropbox_refresh_token").let {
			if (it.result == AliceRequestResult.KEY_NOT_FOUND)  null else it.data?.decodeToString()
		}?.let {
			dbxToken = dropboxApi.getAccessToken(it)
			return DbxClientV2(config, dbxToken?.accessToken)
		} ?: run {
			dropboxSyncStatus.tryEmit(DropboxSyncStatus.NOT_LOGGED_IN)
			return null
		}
	}

	private fun DbxClientV2.lock() {
		val searchOption = SearchOptions
			.newBuilder()
			.withFilenameOnly(true)
			.withFileExtensions(listOf("lock"))
			.build()

		files()
			.searchV2Builder("graphite")
			.withOptions(searchOption)
			.start()
			.matches
			.isEmpty()
			.let {
				if (it) {
					val currentTimestamp = "${System.currentTimeMillis()}".toByteArray()

					files()
						.upload("/sync/graphite.lock")
						.uploadAndFinish(currentTimestamp.inputStream()) {
							if (it == currentTimestamp.size.toLong()) {
								CoroutineScope(Dispatchers.IO).launch {
									upSync()
								}
							}
						}
				} else {
					dropboxSyncStatus.tryEmit(DropboxSyncStatus.DRIVE_LOCKED)
				}
			}
	}

	private suspend fun DbxClientV2.unlock(retryCount : Int = 47) {
		try {
			files()
				.deleteV2("/sync/graphite.lock")
				.apply {
					dropboxSyncStatus.tryEmit(DropboxSyncStatus.INIT)
				}
		} catch (e : Exception) {
			if (retryCount > 0 && dropboxSyncStatus.value != DropboxSyncStatus.INIT) {
				dropboxSyncStatus.tryEmit(DropboxSyncStatus.SYNC_ERROR)
				delay(1000)
				unlock(retryCount - 1)
			}
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
			.let {
				if (it) {
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
								null
							}
						}
				}
			}
	}

	private fun DbxClientV2.updateMetadata() {
		val dropboxMetadata = DropboxMetadata()

		repository2
			.getAllNote()
			.associate {
				Pair(
					it.id.toString(),
					DropboxInnerMetadata(
						modifiedTimestamp = it.modifiedTimestamp,
						hash = objectMapper.writeValueAsString(it.toSnapshot()).toByteArray().dbxHash()
							.joinToString("") { java.lang.String.format("%02x", it) },
						isDeleted = false
					)
				)
			}
			.let { dropboxMetadata.noteMetadata.putAll(it) }

		repository2
			.getAllChapter()
			.associate {
				Pair(
					it.id.toString(),
					DropboxInnerMetadata(
						modifiedTimestamp = it.modifiedTimestamp,
						hash = objectMapper.writeValueAsString(it.toSnapshot()).toByteArray().dbxHash()
							.joinToString("") { java.lang.String.format("%02x", it) },
						isDeleted = false
					)
				)
			}
			.let { dropboxMetadata.chapterMetadata.putAll(it) }

		objectMapper.writeValueAsString(dropboxMetadata).let {
			files()
				.uploadBuilder("/sync/metadata.json")
				.withClientModified(Date(System.currentTimeMillis()))
				.withMode(WriteMode.OVERWRITE)
				.withStrictConflict(false)
				.withAutorename(false)
				.start()
				.uploadAndFinish(it.toByteArray().inputStream())
		}
	}

	private suspend fun DbxClientV2.upSync() {
		val dropboxMetadata = downloadMetadata()
		dropboxSyncStatus.tryEmit(DropboxSyncStatus.SYNCING)
		syncChapters(dropboxMetadata)
		syncNotes(dropboxMetadata)
		updateMetadata()
		unlock()
	}

	private suspend fun DbxClientV2.syncChapters(dropboxMetadata : DropboxMetadata?) {
		suspend fun downSyncChapter(chapterId:String) = files()
			.downloadBuilder("/sync/chapters/${chapterId}.json")
			.start()
			.inputStream
			.readBytes()
			.let {
				objectMapper
					.readValue<ChapterSnapshot>(it)
					.toObject()
			}
			.let {
				repository2.putChapter(it.parentId, it) { _, _ -> }
			}

		val chapterIdList:MutableList<String> = mutableListOf()

		repository2
			.getAllChapter()
			.forEach {
				chapterIdList.add(it.id.toString())

				val chapter = it.toSnapshot()
				val chapterJson = objectMapper.writeValueAsString(chapter)
				val chapterJsonByteArray = chapterJson.toByteArray()

				val localModifiedTimestamp = it.modifiedTimestamp

				fun upSyncChapter() = files()
					.uploadBuilder("/sync/chapters/${it.id}.json")
					.withClientModified(Date(localModifiedTimestamp))
					.withMode(WriteMode.OVERWRITE)
					.withStrictConflict(false)
					.withAutorename(false)
					.start()
					.uploadAndFinish(chapterJsonByteArray.inputStream())

				(dropboxMetadata?.chapterMetadata?.keys?.contains(chapter.id) == true).let { existsOnCloud ->
					if (existsOnCloud) {
						val localHash = chapterJsonByteArray.dbxHash().joinToString("") { java.lang.String.format("%02x", it) }
						val remoteHash = dropboxMetadata?.chapterMetadata?.get(chapter.id)?.hash

						val remoteModifiedTimestamp = dropboxMetadata?.chapterMetadata?.get(chapter.id)?.modifiedTimestamp ?: 0

						if (localHash != remoteHash) {
							if (localModifiedTimestamp > remoteModifiedTimestamp) upSyncChapter() else downSyncChapter(it.id.toString())
						}

					} else upSyncChapter()
				}
			}

		dropboxMetadata?.chapterMetadata?.forEach { (chapterId, dbxInnerData) ->
			if (chapterId !in chapterIdList) {
				if (dbxInnerData.isDeleted) repository2.delete(listOf(RealmUUID.Companion.from(chapterId)))
				else downSyncChapter(chapterId)
			}
		}
	}

	private suspend fun DbxClientV2.syncNotes(dropboxMetadata : DropboxMetadata?) {
		suspend fun downSyncNote(noteId:String) = files()
			.downloadBuilder("/sync/notes/${noteId}.json")
			.start()
			.inputStream
			.readBytes()
			.let {
				objectMapper
					.readValue<NoteSnapshot>(it)
					.toObject()
			}
			.let {
				repository2.putNote(it) { _, _ -> }
			}

		val noteIdList:MutableList<String> = mutableListOf()

		repository2
			.getAllNote()
			.forEach {
				noteIdList.add(it.id.toString())

				val note = it.toSnapshot()
				val noteJson = objectMapper.writeValueAsString(note)
				val noteJsonByteArray = noteJson.toByteArray()

				val localModifiedTimestamp = it.modifiedTimestamp

				fun upSyncNote() = files()
					.uploadBuilder("/sync/notes/${it.id}.json")
					.withClientModified(Date(localModifiedTimestamp))
					.withMode(WriteMode.OVERWRITE)
					.withStrictConflict(false)
					.withAutorename(false)
					.start()
					.uploadAndFinish(noteJsonByteArray.inputStream())

				(dropboxMetadata?.noteMetadata?.keys?.contains(note.id) == true).let { existsOnCloud ->
					if (existsOnCloud) {
						val localHash = noteJsonByteArray.dbxHash().joinToString("") { java.lang.String.format("%02x", it) }
						val remoteHash = dropboxMetadata?.noteMetadata?.get(note.id)?.hash

						val remoteModifiedTimestamp = dropboxMetadata?.noteMetadata?.get(note.id)?.modifiedTimestamp ?: 0

						if (localHash != remoteHash) {
							if (localModifiedTimestamp > remoteModifiedTimestamp) upSyncNote() else downSyncNote(it.id.toString())
						}

					} else upSyncNote()
				}
			}

		dropboxMetadata?.noteMetadata?.forEach { (noteId, dbxInnerData) ->
			if (noteId !in noteIdList) {
				if (dbxInnerData.isDeleted) repository2.deleteNote(RealmUUID.Companion.from(noteId)) { _, _ -> }
				else downSyncNote(noteId)
			}
		}
	}
}

class DropboxSyncServiceConnectionManager(val context : Context, val onBound:(DropboxSyncService?) -> Unit) : ServiceConnection {
	var dropboxSyncService : DropboxSyncService? = null
	private var attemptingToBind = false
	private var bound = false

	fun bindToService() {
		if (! attemptingToBind) {
			attemptingToBind = true
			context.bindService(Intent(context, DropboxSyncService::class.java), this, Context.BIND_AUTO_CREATE)
		}
	}

	override fun onServiceConnected(componentName : ComponentName, iBinder : IBinder) {
		attemptingToBind = false
		bound = true

		val binder = iBinder as DropboxSyncService.LocalBinder
		dropboxSyncService = binder.getService()
		onBound(dropboxSyncService)
	}

	override fun onServiceDisconnected(componentName : ComponentName) {
		bound = false
	}

	fun unbindFromService() {
		attemptingToBind = false
		if (bound) {
			context.unbindService(this)
			bound = false
		}
	}
}
