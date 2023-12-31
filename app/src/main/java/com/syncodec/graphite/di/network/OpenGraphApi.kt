package com.syncodec.graphite.di.network

import android.content.Context
import android.webkit.URLUtil
import com.kedia.ogparser.CacheProvider
import com.kedia.ogparser.OpenGraphCallback
import com.kedia.ogparser.OpenGraphParser
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.di.model.local.BucketItemData


class OpenGraphApi(context: Context) {

	private val urlOpenGraphResultMap: MutableMap<String, OpenGraphResult> = mutableMapOf()

	fun getLinkData(url: String, callback: (NetworkRequest<BucketItemData.LinkData>) -> Unit) {
		callback(NetworkRequest.Loading)
		val isUrlValid = URLUtil.isValidUrl(url)
		if (isUrlValid) {
			val openGraphParser = OpenGraphParser(
				listener = object : OpenGraphCallback {
					override fun onError(error: String) = callback(NetworkRequest.Error(Exception(error)))

					override fun onPostResponse(openGraphResult: OpenGraphResult) {
						val linkData = BucketItemData.LinkData(
							key = openGraphResult.url,
							title = openGraphResult.title,
							description = openGraphResult.description,
							url = openGraphResult.url,
							siteName = openGraphResult.siteName,
							type = openGraphResult.type,
							imagePath = openGraphResult.image,
						)
						callback(NetworkRequest.Success(linkData))
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
			callback(NetworkRequest.Error(Exception("Invalid URL")))
		}
	}
}
