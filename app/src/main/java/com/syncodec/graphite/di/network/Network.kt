package com.syncodec.graphite.di.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.OkHttpClient
import okhttp3.Request

class Network {

	private val client = OkHttpClient
		.Builder()
		.build()

	fun retrieveImage(url: String?, useHttps: Boolean = false, onResponse: (Bitmap?) -> Unit) {
		if (url == null) {
			onResponse(null)
		} else {
			val request = Request.Builder()
				.url(if (useHttps) url.replace("http://", "https://") else url)
				.build()

			client.newCall(request).execute().use { response ->
				if (response.isSuccessful) {
					onResponse(response.body?.byteStream()?.use { BitmapFactory.decodeStream(it) })
				} else {
					onResponse(null)
				}
			}
		}
	}
}
