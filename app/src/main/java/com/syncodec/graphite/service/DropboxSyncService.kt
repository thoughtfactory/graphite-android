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
import kotlin.system.exitProcess


enum class DropboxSyncStatus {
	INIT,
	SYNC_NOT_CONFIGURED,
	SYNC_DISABLED,
	NO_INTERNET,
	NOT_LOGGED_IN,
	CONNECTED,
	SYNCING,
	SYNC_ERROR,
	DRIVE_LOCKED
}

@AndroidEntryPoint
class DropboxSyncService : Service(), DropboxApi {

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

	private var dbxToken : DbxToken? = null
	var dropboxSyncStatus = MutableStateFlow(DropboxSyncStatus.INIT)

	init {
		Thread.setDefaultUncaughtExceptionHandler { paramThread, paramThrowable -> //Catch your exception
			// Without System.exit() this will not work.
			serviceLooper?.quitSafely()
			CoroutineScope(Dispatchers.Main).launch {
				dropboxSyncStatus.emit(DropboxSyncStatus.SYNC_ERROR)
			}
			restartSyncer()
			Log.i("npr71", "DropboxSyncService: Uncaught exception: ${paramThrowable.message}")
		}

//		TODO: remove this
		CoroutineScope(Dispatchers.Default).launch {
			dropboxSyncStatus.collect {
				Log.i("npr71", "dropboxSyncStatus = $it")
			}
		}
	}

	@Inject
	lateinit var repository2 : Repository2

	override fun onCreate() {
		super.onCreate()
		startSyncer()
	}

	override fun onDestroy() {
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
	}

	fun startSyncer() {
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

	fun restartSyncer() {
		HandlerThread("ServiceStartArguments", 10).apply {
			start()
			serviceLooper = looper
			serviceHandler = ServiceHandler(looper)
		}

		Toast.makeText(this, "service restarting", Toast.LENGTH_SHORT).show()
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

	private inner class ServiceHandler(looper : Looper) : Handler(looper) {
		override fun handleMessage(msg : Message) {
//			repository2
//				.repositoryState
//				.value
//				.let {
//					if (it == RepositoryState.SUCCESS && (dropboxSyncStatus.value == DropboxSyncStatus.INIT || dropboxSyncStatus.value == DropboxSyncStatus.SYNC_ERROR) ) {
//						checkSyncPermission {
//							if (it) {
////								connectWithDropbox().let { _dbxClientV2 ->
////									_dbxClientV2?.let { dbxClientV2 ->
////										dropboxSyncStatus.tryEmit(DropboxSyncStatus.CONNECTED)
////										dbxClientV2.tryLock()
////									}
////								}
//							} else dropboxSyncStatus.tryEmit(DropboxSyncStatus.SYNC_DISABLED)
//						}
//					}
//				}
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

	fun unbindFromServiceAndStopService() {
		unbindFromService()
		context.stopService(Intent(context, DropboxSyncService::class.java))
	}
}
