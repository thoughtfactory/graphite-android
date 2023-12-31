package com.syncodec.graphite.di.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.syncodec.graphite.BuildConfig
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request


class Network(context: Context) {

	private val client = OkHttpClient.Builder()
		.cache(Cache(context.cacheDir, 50 * 1024 * 1024))
		.build()

	/**
	 * @param url: The url to be requested
	 * @param useHttps: Whether to use https or not
	 * @return: Decoded bitmap from the response body
	 */
	fun retrieveImage(url: String?, useHttps: Boolean = false): Bitmap? {
		return try {
			if (url == null) null
			else {
				val request = Request.Builder()
					.url(if (useHttps) url.replace("http://", "https://") else url)
					.cacheControl(okhttp3.CacheControl.Builder().build())
					.build()

				val response = client.newCall(request).execute()
				response.body.byteStream().use { BitmapFactory.decodeStream(it) } ?: null
			}
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			null
		}
	}
}

sealed class NetworkRequest<out T> {
	data object Init : NetworkRequest<Nothing>()
	data object Loading : NetworkRequest<Nothing>()
	data class Success<T>(val data: T) : NetworkRequest<T>()
	data class Error(val exception: Exception? = null) : NetworkRequest<Nothing>()
}
