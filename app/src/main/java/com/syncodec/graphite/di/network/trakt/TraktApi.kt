package com.syncodec.graphite.di.network.trakt

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import coil3.Bitmap
import coil3.ImageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemShow
import com.syncodec.graphite.di.network.NetworkResponse
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


class TraktApi(private val context: Context) {

    private val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            classDiscriminator = "klass"
        }
    }

    private val cache = Cache(directory = context.cacheDir, maxSize = 50 * 1024 * 1024)

    private val client = OkHttpClient.Builder()
        .cache(cache = cache)
        .build()

    fun searchForShow(
        query: String,
        showType: BucketItemShow.ShowType,
        page: Int,
        callback: (networkResponse: NetworkResponse<List<TraktShowSearchResult>>) -> Unit
    ) {
        callback(NetworkResponse.Loading)

        try {

            val httpUrl = HttpUrl.Builder()
                .scheme(scheme = "https")
                .host(host = "api.trakt.tv")
                .addPathSegment(pathSegment = "search")
                .addPathSegment(pathSegment = if (showType == BucketItemShow.ShowType.Movie) "movie" else "show")
                .addQueryParameter(name = "query", value = query)
                .addQueryParameter(name = "extended", value = "images")
                .addQueryParameter(name = "limit", value = "9")
                .addQueryParameter(name = "page", value = "$page")
//                .addQueryParameter(name = "offset", value = "0")
                .build()

            Log.d(TAG, httpUrl.toUrl().toString())

            val request = Request.Builder()
                .addHeader(name = "Content-Type", value = "application/json")
                .addHeader(name = "trakt-api-version", value = "2")
                .addHeader(name = "trakt-api-key", value = "23069a6f5a678d96fb2035b0ffced3afa492ac9011d0e9e954e6f2043b35de7f")
                .url(url = httpUrl)
                .build()

            val response = client
                .newCall(request = request)
                .execute()

            response.body.toString()

            client
                .newCall(request = request)
                .enqueue(
                    responseCallback = object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                            callback(NetworkResponse.Error(exception = e))
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val traktShowSearchResultLists: List<TraktShowSearchResult> = if (showType == BucketItemShow.ShowType.Movie) json.decodeFromString<List<TraktShowSearchResult.TraktMovieSearchResult>>(string = response.body.string())
                            else json.decodeFromString<List<TraktShowSearchResult.TraktSeriesSearchResult>>(string = response.body.string())
                            callback(NetworkResponse.Success(data = traktShowSearchResultLists))
                        }
                    }
                )

        } catch (e: Exception) {
            e.printStackTrace()
            callback(NetworkResponse.Error(exception = e))
        }

    }

    @WorkerThread
    suspend fun getMovieSummary(
        traktId: Int,
        callback: (networkResponse: NetworkResponse<TraktMovieData>) -> Unit
    ) {
        callback(NetworkResponse.Loading)

        try {

//            https://api.trakt.tv/movies/219729?extended=full

            val httpUrl = HttpUrl.Builder()
                .scheme(scheme = "https")
                .host(host = "api.trakt.tv")
                .addPathSegment(pathSegment = "movies")
                .addPathSegment(pathSegment = "$traktId")
                .addQueryParameter(name = "extended", value = "full")
                .build()

            Log.d(TAG, httpUrl.toUrl().toString())

            val request = Request.Builder()
                .addHeader(name = "Content-Type", value = "application/json")
                .addHeader(name = "trakt-api-version", value = "2")
                .addHeader(name = "trakt-api-key", value = "23069a6f5a678d96fb2035b0ffced3afa492ac9011d0e9e954e6f2043b35de7f")
                .url(url = httpUrl)
                .build()

            val response = client
                .newCall(request = request)
                .execute()

            response.body.toString()

            client
                .newCall(request = request)
                .enqueue(
                    responseCallback = object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                            callback(NetworkResponse.Error(exception = e))
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val traktMovieData: TraktMovieData = json.decodeFromString(string = response.body.string())
                            callback(NetworkResponse.Success(data = traktMovieData))
                        }
                    }
                )
        } catch (e: Exception) {
            e.printStackTrace()
            callback(NetworkResponse.Error(exception = e))
        }
    }

    @WorkerThread
    suspend fun getSeriesSummary(
        traktId: Int,
        callback: (networkResponse: NetworkResponse<TraktSeriesData>) -> Unit
    ) {
        callback(NetworkResponse.Loading)

        try {

//            https://api.trakt.tv/movies/219729?extended=full

            val httpUrl = HttpUrl.Builder()
                .scheme(scheme = "https")
                .host(host = "api.trakt.tv")
                .addPathSegment(pathSegment = "shows")
                .addPathSegment(pathSegment = "$traktId")
                .addQueryParameter(name = "extended", value = "full")
                .build()

            Log.d(TAG, httpUrl.toUrl().toString())

            val request = Request.Builder()
                .addHeader(name = "Content-Type", value = "application/json")
                .addHeader(name = "trakt-api-version", value = "2")
                .addHeader(name = "trakt-api-key", value = "23069a6f5a678d96fb2035b0ffced3afa492ac9011d0e9e954e6f2043b35de7f")
                .url(url = httpUrl)
                .build()

            val response = client
                .newCall(request = request)
                .execute()

            response.body.toString()

            client
                .newCall(request = request)
                .enqueue(
                    responseCallback = object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                            callback(NetworkResponse.Error(exception = e))
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val traktSeriesData: TraktSeriesData = json.decodeFromString(string = response.body.string())
                            callback(NetworkResponse.Success(data = traktSeriesData))
                        }
                    }
                )
        } catch (e: Exception) {
            e.printStackTrace()
            callback(NetworkResponse.Error(exception = e))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @WorkerThread
    suspend fun getShowCoverImage(
        posterPath: String,
        callback: (networkResponse: NetworkResponse<Bitmap>) -> Unit
    ) {
        callback(NetworkResponse.Loading)

//        https://covers.openlibrary.org/b/id/8153054-M.jpg

        try {
            val imageRequest = ImageRequest
                .Builder(context = context)
                .data(data = "https://$posterPath")
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
        const val TAG = "TraktApi"
    }
}
