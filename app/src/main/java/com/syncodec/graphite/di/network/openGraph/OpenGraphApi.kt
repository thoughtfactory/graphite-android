package com.syncodec.graphite.di.network.openGraph

import android.content.Context
import android.util.Log
import com.kedia.ogparser.OpenGraphCallback
import com.kedia.ogparser.OpenGraphParser
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.di.network.NetworkResponse
import com.syncodec.graphite.di.network.openLibrary.OpenLibraryTitleSearchResult2
import okhttp3.Cache
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException


class OpenGraphApi(private val context: Context) {

    private val cache = Cache(directory = context.cacheDir, maxSize = 50 * 1024 * 1024)

    private val client = OkHttpClient.Builder()
        .cache(cache = cache)
        .build()

    fun getLinkDataPreview(
        url: String,
        callback: (networkResponse: NetworkResponse<LinkData>) -> Unit
    ) {
        callback(NetworkResponse.Loading)

        val openGraphParser = OpenGraphParser(
            listener = object : OpenGraphCallback {
                override fun onError(error: String) {
                    callback(NetworkResponse.Error(message = error))
                }

                override fun onPostResponse(openGraphResult: OpenGraphResult) {
                    val linkData = LinkData(
                        title = openGraphResult.title,
                        description = openGraphResult.description,
                        url = openGraphResult.url,
                        imagePath = openGraphResult.image,
                        imageBase64 = null,
                        siteName = openGraphResult.siteName,
                        type = openGraphResult.title,
                    )

                    callback(NetworkResponse.Success(data = linkData))
                }
            }
        )

        openGraphParser.parse(url = url)
    }

    suspend fun getLinkImage(
        url: String,
    ) : String?  {
        try {
            val httpUrl = url.toHttpUrlOrNull()

            val request = Request.Builder()
                .url(url = httpUrl ?: return null)
                .build()

            val response = client
                .newCall(request = request)
                .execute()

            return response.body.toString()

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
