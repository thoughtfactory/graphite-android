package com.syncodec.graphite.service

//import android.app.Service
//import android.content.Intent
//import android.os.Handler
//import android.os.HandlerThread
//import android.os.IBinder
//import android.os.Looper
//import android.os.Message
//import android.util.Log
//import android.widget.Toast
//import androidx.compose.runtime.mutableStateOf
//import com.fasterxml.jackson.databind.DeserializationFeature
//import com.fasterxml.jackson.module.kotlin.jsonMapper
//import com.fasterxml.jackson.module.kotlin.kotlinModule
//import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
//import com.google.api.client.http.ByteArrayContent
//import com.google.api.client.http.javanet.NetHttpTransport
//import com.google.api.client.json.gson.GsonFactory
//import com.google.api.client.util.DateTime
//import com.google.api.services.drive.Drive
//import com.google.api.services.drive.DriveScopes
//import com.google.api.services.drive.model.File
//import com.google.api.services.drive.model.FileList
//import com.google.firebase.auth.FirebaseAuth
//import com.syncodec.graphite.di.model.NoteSnapshot
//import com.syncodec.graphite.di.repository.RealmUUIDDeserializer
//import com.syncodec.graphite.di.repository.Repository2
//import com.syncodec.graphite.utils.DataStoreInstance
//import dagger.hilt.android.AndroidEntryPoint
//import io.realm.kotlin.types.RealmUUID
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.cancel
//import kotlinx.coroutines.launch
//import java.io.ByteArrayOutputStream
//import javax.inject.Inject


//enum class GoogleDriveSyncStatus {
//	INIT,
//	SYNC_DISABLED,
//	NO_INTERNET,
//	UP_SYNC,
//	DOWN_SYNC,
//	SYNC_ERROR,
//	DRIVE_LOCKED
//}

//@AndroidEntryPoint
//class GoogleDriveSyncService : Service() {
//
//	private val objectMapper = jsonMapper {
//		addModule(
//			kotlinModule().addDeserializer(
//				RealmUUID::class.java,
//				RealmUUIDDeserializer()
//			)
//		)
//	}.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//
//	private var serviceLooper : Looper? = null
//	private var serviceHandler : ServiceHandler? = null
//
//	private var googleDriveSyncStatus = mutableStateOf(GoogleDriveSyncStatus.INIT)
//
//	@Inject
//	lateinit var repository2 : Repository2
//
//	// Handler that receives messages from the thread
//	private inner class ServiceHandler(looper : Looper) : Handler(looper) {
//
//		override fun handleMessage(msg : Message) {
//			// Normally we would do some work here, like download a file.
//			// For our sample, we just sleep for 5 seconds.
//
//			checkSyncPermission {
//				if (it) {
//					val drive = connectWithDrive()
//					drive?.let {
//						CoroutineScope(Dispatchers.IO).launch {
//							var lockFileId : String? = null
//							it.lock(
//								onSuccess = {
//									lockFileId = it
//									Log.i("npr71", "drive lock : success")
//								},
//								onFailure = {
//									it.printStackTrace()
//									Log.i("npr71", "drive lock : failure")
//								}
//							)
//
//							googleDriveSyncStatus.value = GoogleDriveSyncStatus.UP_SYNC
//							it.upSync()
//
//							googleDriveSyncStatus.value = GoogleDriveSyncStatus.DOWN_SYNC
//							it.downSync()
//
//							it.unlock(
//								fileId = lockFileId,
//								onSuccess = {
//									Log.i("npr71", "drive unlock : success")
//								},
//								onFailure = {
//									Log.i("npr71", "drive unlock : failure")
//								}
//							)
//						}
//
//					} ?: run {
//						googleDriveSyncStatus.value = GoogleDriveSyncStatus.DRIVE_LOCKED
//						CoroutineScope(Dispatchers.Main).launch {
//							Toast.makeText(applicationContext, "Error connecting to Google Drive", Toast.LENGTH_SHORT).show()
//						}
//					}
//				} else {
//					googleDriveSyncStatus.value = GoogleDriveSyncStatus.SYNC_DISABLED
//				}
//			}
//
//			// Stop the service using the startId, so that we don't stop
//			// the service in the middle of handling another job
//			stopSelf(msg.arg1)
//		}
//	}
//
//	override fun onCreate() {
//		super.onCreate()
//		// Start up the thread running the service.  Note that we create a
//		// separate thread because the service normally runs in the process's
//		// main thread, which we don't want to block.  We also make it
//		// background priority so CPU-intensive work will not disrupt our UI.
//		HandlerThread("ServiceStartArguments", 10).apply {
//			start()
//
//			// Get the HandlerThread's Looper and use it for our Handler
//			serviceLooper = looper
//			serviceHandler = ServiceHandler(looper)
//		}
//	}
//
//	override fun onStartCommand(intent : Intent, flags : Int, startId : Int) : Int {
//		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()
//
//		// For each start request, send a message to start a job and deliver the
//		// start ID so we know which request we're stopping when we finish the job
//		serviceHandler?.obtainMessage()?.also { msg ->
//			msg.arg1 = startId
//			serviceHandler?.sendMessage(msg)
//		}
//
//		// If we get killed, after returning from here, restart
//		return START_STICKY
//	}
//
//	override fun onBind(intent : Intent) : IBinder? {
//		// We don't provide binding, so return null
//		return null
//	}
//
//	override fun onDestroy() {
//		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
//	}
//
//	private fun checkSyncPermission(isSyncEnabled : (Boolean) -> Unit) {
//		val dataStoreInstance = DataStoreInstance(this)
//		CoroutineScope(Dispatchers.Default).launch {
//			dataStoreInstance.getIsSyncEnabled().collect {
//				isSyncEnabled(it)
//				this.cancel()
//			}
//		}
//	}
//
//	private fun connectWithDrive() : Drive? {
//		try {
//			val auth = FirebaseAuth.getInstance()
//			Log.i("npr71", "auth : ${auth.currentUser?.email}")
//			return Drive
//				.Builder(
//					NetHttpTransport(),
//					GsonFactory.getDefaultInstance(),
//					GoogleAccountCredential
//						.usingOAuth2(this, listOf(DriveScopes.DRIVE_APPDATA))
//						.setSelectedAccountName(auth.currentUser !!.email)
//				)
//				.setApplicationName("Graphite")
//				.build()
//		} catch (e : Exception) {
//			e.printStackTrace()
//			return null
//		}
//	}
//
//	private suspend fun Drive.lock(
//		onSuccess : (String) -> Unit,
//		onFailure : (Exception) -> Unit
//	) {
//		searchFile(query = "name = 'graphite.lock'").let { fileList ->
//			if (fileList.isEmpty()) {
//				createFile(
//					fileName = "graphite.lock",
//					onSuccess = { onSuccess(it.id) },
//					onFailure = onFailure
//				)
//			} else {
//				CoroutineScope(Dispatchers.Main).launch {
//					Toast.makeText(applicationContext, "Currently syncing on another device", Toast.LENGTH_SHORT).show()
//				}
//
//				onFailure(Exception("Drive is locked"))
//			}
//		}
//	}
//
//	private fun Drive.unlock(
//		fileId : String?,
//		onSuccess : () -> Unit,
//		onFailure : (Exception) -> Unit
//	) {
//		fileId?.let {
//			deleteFile(
//				fileId = it,
//				onSuccess = onSuccess,
//				onFailure = onFailure
//			)
//		} ?: run {
////			TODO()
//		}
//	}
//
//	private suspend fun Drive.upSync() {
//		upSyncNotes()
//	}
//
//	private suspend fun Drive.downSync() {
//		downSyncNotes()
//	}
//
//	private suspend fun Drive.upSyncNotes() {
//		repository2
//			.getAllNote()
//			.filter { it.googleDriveId == null }
//			.forEach { noteObject ->
//				objectMapper.writeValueAsString(noteObject.toSnapshot())
//					.let {
//						createFile(
//							fileName = "${noteObject.id}.json",
//							fileContent = it.toByteArray(),
//							fileModifiedTime = noteObject.modifiedTimestamp,
//							onSuccess = {
//								noteObject
//									.clone()
//									.apply {
//										this.id = noteObject.id
//										googleDriveId = it.id
//										repository2.putNote(
//											noteObject = this,
//											callback = { _, _ -> }
//										)
//									}
//							},
//							onFailure = {
//								it.printStackTrace()
//								Log.i("npr71", "drive upSyncNotes : failure")
//							}
//						)
//					}
//			}
//	}
//
//	private suspend fun Drive.downSyncNotes() {
//		val baseObject = repository2.getBaseObject()
//		baseObject?.let {
//			val lastSyncedTimestamp = it.lastSyncedTimestamp
//			searchFile(query = "modifiedTime > '${DateTime(lastSyncedTimestamp)}'")
//				.forEach { file ->
//					downloadFile(
//						fileId = file.id,
//						onSuccess = {
//							objectMapper.readValue(it, NoteSnapshot::class.java)
//								.let { noteSnapshot ->
//									noteSnapshot
//										.toObject()
//										.apply {
//											repository2.putNote(
//												noteObject = this,
//												callback = { _, _ -> }
//											)
//										}
//								}
//						},
//						onFailure = {}
//					)
//				}
//		}
//	}
//
//	private fun Drive.searchFile(query : String) : List<File> {
//		val files : MutableList<File> = mutableListOf()
//		var pageToken : String? = null
//		do {
//			val result : FileList = files()
//				.list()
//				.setQ(query)
//				.setSpaces("appDataFolder")
//				.setFields("nextPageToken, files(id, name, createdTime, modifiedTime)")
//				.setPageToken(pageToken)
//				.execute()
//			files.addAll(result.files)
//			pageToken = result.nextPageToken
//		} while (pageToken != null)
//		return files
//	}
//
//	private suspend fun Drive.createFile(
//		fileName : String,
//		fileContent : ByteArray? = null,
//		fileModifiedTime : Long = System.currentTimeMillis(),
//		onSuccess : suspend (File) -> Unit,
//		onFailure : (Exception) -> Unit
//	) {
//		try {
//			val fileMetadata = File()
//			fileMetadata.name = fileName
//			fileMetadata.parents = listOf("appDataFolder")
//			fileMetadata.modifiedTime = DateTime(fileModifiedTime)
//
//			val file = if (fileContent == null) files().create(fileMetadata).setFields("id").execute()
//			else files().create(fileMetadata, ByteArrayContent("application/json", fileContent)).setFields("id").execute()
//			onSuccess(file)
//		} catch (e : Exception) {
//			onFailure(e)
//		}
//	}
//
//	private suspend fun Drive.downloadFile(
//		fileId : String,
//		onSuccess : suspend (ByteArray) -> Unit,
//		onFailure : (Exception) -> Unit
//	) {
//		try {
//			val outputStream = ByteArrayOutputStream()
//			files().get(fileId).executeMediaAndDownloadTo(outputStream)
//			onSuccess(outputStream.toByteArray())
//		} catch (e : Exception) {
//			onFailure(e)
//		}
//	}
//
//	private fun Drive.deleteFile(
//		fileId : String,
//		onSuccess : () -> Unit,
//		onFailure : (Exception) -> Unit
//	) {
//		try {
//			files()
//				.delete(fileId)
//				.execute()
//			onSuccess()
//		} catch (e : Exception) {
//			onFailure(e)
//		}
//	}
//}
