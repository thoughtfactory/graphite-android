package com.syncodec.graphite.service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder


class ServiceConnectionManager(context : Context, service : Class<out Service?>) : ServiceConnection {
	private val context : Context
	val service : Class<out Service?>
	private var attemptingToBind = false
	private var bound = false

	init {
		this.context = context
		this.service = service
	}

	fun bindToService() {
		if (! attemptingToBind) {
			attemptingToBind = true
			context.bindService(Intent(context, service), this, Context.BIND_AUTO_CREATE)
		}
	}

	override fun onServiceConnected(componentName : ComponentName, iBinder : IBinder) {
		attemptingToBind = false
		bound = true
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
