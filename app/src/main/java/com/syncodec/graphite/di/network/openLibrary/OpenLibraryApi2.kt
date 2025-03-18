package com.syncodec.graphite.di.network.openLibrary

import android.content.Context
import android.util.Log
import androidx.annotation.IntRange
import androidx.annotation.WorkerThread
import coil3.Bitmap
import coil3.ImageLoader
import coil3.decode.ImageSource
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
import coil3.toBitmap
import com.syncodec.graphite.di.network.NetworkResponse
import com.syncodec.graphite.utils.encodeBase64
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException


class OpenLibraryApi2(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val cache = Cache(directory = context.cacheDir, maxSize = 50 * 1024 * 1024)

    private val client = OkHttpClient.Builder()
        .cache(cache = cache)
        .build()


    @WorkerThread
    fun searchForBook(
        query: String,
        @IntRange(from = 1) page: Int = 1,
        callback: (networkResponse: NetworkResponse<OpenLibraryTitleSearchResult2>) -> Unit
    ) {
        callback(NetworkResponse.Loading)
        val _page = maxOf(page, 1)

        try {
//            curl -X 'GET' \
//            'https://openlibrary.org/search.json?q=the%20book%20thief' \
//            -H 'accept: application/json'

//            https://openlibrary.org/search.json?q=the%20book%20thief

            val httpUrl = HttpUrl.Builder()
                .scheme(scheme = "https")
                .host(host = "openlibrary.org")
                .addPathSegment(pathSegment = "search.json")
                .addQueryParameter(name = "q", value = query)
                .addQueryParameter(name = "fields", value = "key,title,author_name,cover_i,first_publish_year,number_of_pages_median")
                .addQueryParameter(name = "limit", value = "9")
                .addQueryParameter(name = "page", value = "$_page")
//                .addQueryParameter(name = "offset", value = "0")
                .build()

            val request = Request.Builder()
                .addHeader(name = "Content-Type", value = "application/json")
                .url(url = httpUrl)
                .build()

            client
                .newCall(request = request)
                .enqueue(
                    responseCallback = object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                            callback(NetworkResponse.Error(exception = e))
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val openLibraryTitleSearchResult: OpenLibraryTitleSearchResult2 = json.decodeFromString(string = response.body.string())
                            callback(NetworkResponse.Success(data = openLibraryTitleSearchResult))
                        }
                    }
                )

        } catch (e: Exception) {
            e.printStackTrace()
            callback(NetworkResponse.Error(exception = e))
        }
    }

    @WorkerThread
    fun getBookData(
        bookKey: String,
        callback: (networkResponse: NetworkResponse<OLBookData>) -> Unit
    ) {
        callback(NetworkResponse.Loading)

        try {
//            curl -X 'GET' \
//            'https://openlibrary.org/books/OL5819456W' \
//            -H 'accept: application/json'

//            https://openlibrary.org/books/OL5819456W

            val httpUrl = HttpUrl.Builder()
                .scheme(scheme = "https")
                .host(host = "openlibrary.org")
                .addPathSegment(pathSegment = "books")
                .addPathSegment(pathSegment = bookKey.split("/")[2])
//                .addQueryParameter(name = "offset", value = "0")
                .build()

            val request = Request.Builder()
                .addHeader(name = "accept", value = "application/json")
                .url(url = httpUrl)
                .build()

            Log.d(TAG, httpUrl.toString())

            client
                .newCall(request = request)
                .enqueue(
                    responseCallback = object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                            callback(NetworkResponse.Error(exception = e))
                        }

                        override fun onResponse(call: Call, response: Response) {
                            try {
                                val body = response.body.string()
                                val olBookData: OLBookData = json.decodeFromString(string = body)
                                callback(NetworkResponse.Success(data = olBookData))
                            } catch (e: Exception) {
                                e.printStackTrace()
                                callback(NetworkResponse.Error(exception = e))
                            }
                        }
                    }
                )

        } catch (e: Exception) {
            e.printStackTrace()
            callback(NetworkResponse.Error(exception = e))
        }
    }

    @WorkerThread
    suspend fun getBookCoverImage(
        coverI: Int,
        callback: (networkResponse: NetworkResponse<Bitmap>) -> Unit
    ) {
        callback(NetworkResponse.Loading)

//        https://covers.openlibrary.org/b/id/8153054-M.jpg

        try {
            val imageRequest = ImageRequest
                .Builder(context = context)
                .data(data = "https://covers.openlibrary.org/b/id/$coverI-L.jpg")
                .build()

            val imageLoader = ImageLoader(context = context)
                .enqueue(request = imageRequest)

            val imageResult = imageLoader
                .job
                .await()

            when (imageResult) {
                is ErrorResult -> Unit
                is SuccessResult -> callback(NetworkResponse.Success(data = imageResult.image.toBitmap()))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callback(NetworkResponse.Error(exception = e))
        }
    }

    companion object {
        const val TAG = "OpenLibraryApi2"
    }
}
