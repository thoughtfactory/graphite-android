package com.syncodec.graphite.di.network

import android.graphics.Bitmap
import android.webkit.URLUtil
import androidx.lifecycle.viewModelScope
import com.kedia.ogparser.CacheProvider
import com.kedia.ogparser.OpenGraphCallback
import com.kedia.ogparser.OpenGraphParser
import com.kedia.ogparser.OpenGraphResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


sealed class OpenGraphResponse {
	object Loading : OpenGraphResponse()
	data class Success(val openGraphResult: OpenGraphResult, val bitmap: Bitmap? = null) : OpenGraphResponse() {
		override fun hashCode(): Int {
			var result = openGraphResult.hashCode()
			result = 31 * result + (bitmap?.hashCode() ?: 0)
			return result
		}

		override fun equals(other: Any?): Boolean {
			if (this === other) return true
			if (javaClass != other?.javaClass) return false

			other as Success

			if (openGraphResult != other.openGraphResult) return false
			return bitmap == other.bitmap
		}
	}
	object InvalidUrl : OpenGraphResponse()
	data class Error(val message: String) : OpenGraphResponse()

	override fun hashCode(): Int {
		return javaClass.hashCode()
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		return javaClass == other?.javaClass
	}
}

object OpenGraphApi {
	private val urlOpenGraphResultMap: MutableMap<String, OpenGraphResult> = mutableMapOf()
	private val urlBitmapMap: MutableMap<String, Bitmap> = mutableMapOf()

	private val getBitmapCoroutineScopeMap: MutableMap<String?, CoroutineScope> = mutableMapOf()

	fun getData(url: String, onResponse: (OpenGraphResponse) -> Unit) {
		onResponse(OpenGraphResponse.Loading)
		val isUrlValid = URLUtil.isValidUrl(url)
		if (isUrlValid) {
			val openGraphParser = OpenGraphParser(
				listener = object : OpenGraphCallback {
					override fun onError(error: String) {
						onResponse(OpenGraphResponse.Error(error))
					}

					override fun onPostResponse(openGraphResult: OpenGraphResult) {
						onResponse(OpenGraphResponse.Success(openGraphResult = openGraphResult))
						CoroutineScope(Dispatchers.IO).launch {
							getBitmapCoroutineScopeMap[url]?.cancel()
							getBitmapCoroutineScopeMap[url] = this
							urlBitmapMap[url]?.let {
								onResponse(OpenGraphResponse.Success(openGraphResult = openGraphResult, bitmap = it))
							} ?: Network.retrieveImage(openGraphResult.image) { bitmap ->
								bitmap?.let { urlBitmapMap[url] = it }
								onResponse(OpenGraphResponse.Success(openGraphResult = openGraphResult, bitmap = bitmap))
							}
						}
					}
				},
				cacheProvider = object : CacheProvider {
					override suspend fun getOpenGraphResult(url: String): OpenGraphResult? {
						return this@OpenGraphApi.urlOpenGraphResultMap[url]
					}

					override suspend fun setOpenGraphResult(openGraphResult: OpenGraphResult, url: String) {
						this@OpenGraphApi.urlOpenGraphResultMap[url] = openGraphResult
					}
				}
			)

			openGraphParser.parse(url)
		} else {
			onResponse(OpenGraphResponse.InvalidUrl)
		}
	}
}
