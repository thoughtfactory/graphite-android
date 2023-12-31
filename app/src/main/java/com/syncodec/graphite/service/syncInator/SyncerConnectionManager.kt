package com.syncodec.graphite.service.syncInator

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder


class SyncerConnectionManager(private val context: Context, private val onBound: (DyncInator) -> Unit) : ServiceConnection {
	var service: SyncInatorService? = null
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
