package com.syncodec.graphite.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.glance.appwidget.action.actionRunCallback
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WatchdogService : Service() {

	private var isBound = false

	private var serviceLooper : Looper? = null
	private var serviceHandler : ServiceHandler? = null

	private val binder = LocalBinder()

	inner class LocalBinder : Binder() {
		fun getService() : WatchdogService = this@WatchdogService
	}

	override fun onBind(intent : Intent) : IBinder {
		isBound = true
		Log.d("npr71", "onBind")
		return binder
	}

	override fun onUnbind(intent : Intent?) : Boolean {
		isBound = false
		Log.i("npr71", "onUnbind")
		return super.onUnbind(intent)
	}

	override fun onCreate() {
		super.onCreate()
		Log.i("npr71", "watchdog : onCreate")

		moveToForeground()

		HandlerThread("ServiceStartArguments", 10).apply {
			start()
			serviceLooper = looper
			serviceHandler = ServiceHandler(looper)
		}

		CoroutineScope(Dispatchers.Default).launch {
			serviceHandler?.sendEmptyMessage(0)

			while (true) {
				if (MainActivity.isInStack) serviceHandler?.sendEmptyMessage(0) else break
//				TODO: Delay for 5 seconds
				delay(5000)
			}
		}
	}

	override fun onDestroy() {
		Log.i("npr71", "watchdog : onDestroy")
		dropboxServiceConnection.unbindFromServiceAndStopService()
		super.onDestroy()
	}

	private fun moveToForeground() {
		val channelId = "WatchdogService"
		val channelName = "Watchdog Service"
		val notificationId = 71

		val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
		notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
		val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		service.createNotificationChannel(notificationChannel)

		val notification : Notification = Notification
			.Builder(this, channelId)
			.setContentTitle("Watchdog")
			.setContentText("Watchdog is running")
			.setSmallIcon(R.mipmap.ic_launcher_foreground)
			.setTicker("Watchdog is running")
			.build()
		startForeground(notificationId, notification)
	}

	private inner class ServiceHandler(looper : Looper) : Handler(looper) {
		override fun handleMessage(msg : Message) {
			Log.i("npr71", "watchdog : handleMessage")
			startSyncService()
			stopSelf()
		}
	}

	var dropboxSyncService : DropboxSyncService? = null
	private val dropboxServiceConnection = DropboxSyncServiceConnectionManager(this) {
		dropboxSyncService = it
		it?.let {
			Log.i("npr71", "dropboxSyncService is not null")
//			CoroutineScope(Dispatchers.Main).launch {
//				it.dropboxSyncStatus.collect {
////					syncStatus.value = it
//				}
//			}
		}
	}

	private fun startSyncService() {
		Intent(this.applicationContext, DropboxSyncService::class.java).apply {
			dropboxServiceConnection.bindToService()
		}
	}
}

class WatchdogServiceConnectionManager(val context : Context, val onBound : (WatchdogService?) -> Unit) : ServiceConnection {
	var watchdogSyncService : WatchdogService? = null
	private var attemptingToBind = false
	private var bound = false

	fun bindToService() {
		if (! attemptingToBind) {
			attemptingToBind = true
			context.bindService(Intent(context, WatchdogService::class.java), this, Context.BIND_AUTO_CREATE)

			CoroutineScope(Dispatchers.Default).launch {
				while(true) {
					if (!MainActivity.isInStack) {
						unbindFromService()
						break
					}
					delay(5000)
				}
			}
		}
	}

	override fun onServiceConnected(componentName : ComponentName, iBinder : IBinder) {
		attemptingToBind = false
		bound = true

		val binder = iBinder as WatchdogService.LocalBinder
		watchdogSyncService = binder.getService()
		onBound(watchdogSyncService)
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
