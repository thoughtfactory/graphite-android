package com.syncodec.graphite.di.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.model.local.BucketItemData
import com.syncodec.graphite.utils.alice.Alice
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response


class TMDbApi(context: Context) {
	private val json = Json { ignoreUnknownKeys = true }

	private val client = OkHttpClient.Builder()
		.cache(Cache(context.cacheDir, 50 * 1024 * 1024))
		.build()

	private val tmdbApiKey = Alice.decrypt(BuildConfig.TMDB_API_KEY, "lt3(3x4R7M^107!&4E74Z%*o8cp2i7y@") ?: ""

	fun searchForMovieTitle(title: String, callback: (NetworkRequest<TMDBSearchResult2<BucketItemData.ShowData.TMDbData.TMDbMovieData>>) -> Unit) {
		callback(NetworkRequest.Loading)

		val httpUrl = HttpUrl.Builder()
			.scheme("https")
			.host("api.themoviedb.org")
			.addPathSegment("3")
			.addPathSegment("search")
			.addPathSegment("movie")
			.addQueryParameter("api_key", tmdbApiKey)
			.addQueryParameter("language", "en-US")
			.addQueryParameter("query", title)
			.build()

		val request = Request.Builder()
			.url(httpUrl)
			.build()

		client.newCall(request).enqueue(
			object : Callback {
				override fun onFailure(call: Call, e: okio.IOException) {
					if (BuildConfig.DEBUG) e.printStackTrace()
					callback(NetworkRequest.Error(e))
				}

				override fun onResponse(call: Call, response: Response) {
					try {
						callback(NetworkRequest.Success(json.decodeFromString<TMDBSearchResult2<BucketItemData.ShowData.TMDbData.TMDbMovieData>>(response.body.string())))
					} catch (e: Exception) {
						if (BuildConfig.DEBUG) e.printStackTrace()
						callback(NetworkRequest.Error(e))
					}
				}
			}
		)
	}

	fun searchForTvTitle(title: String, callback: (NetworkRequest<TMDBSearchResult2<BucketItemData.ShowData.TMDbData.TMDbTvData>>) -> Unit) {
		callback(NetworkRequest.Loading)

		val httpUrl = HttpUrl.Builder()
			.scheme("https")
			.host("api.themoviedb.org")
			.addPathSegment("3")
			.addPathSegment("search")
			.addPathSegment("tv")
			.addQueryParameter("api_key", tmdbApiKey)
			.addQueryParameter("language", "en-US")
			.addQueryParameter("query", title)
			.build()

		val request = Request.Builder()
			.url(httpUrl)
			.build()

		client.newCall(request).enqueue(
			object : Callback {
				override fun onFailure(call: Call, e: okio.IOException) {
					callback(NetworkRequest.Error(e))
				}

				override fun onResponse(call: Call, response: Response) {
					try {
						callback(NetworkRequest.Success(json.decodeFromString<TMDBSearchResult2<BucketItemData.ShowData.TMDbData.TMDbTvData>>(response.body.string())))
					} catch (e: Exception) {
						if (BuildConfig.DEBUG) e.printStackTrace()
						callback(NetworkRequest.Error(e))
					}
				}
			}
		)
	}

	fun retrieveShowPoster(posterPath: String?): Bitmap? {
		val httpUrl = HttpUrl.Builder()
			.scheme("https")
			.host("image.tmdb.org")
			.addPathSegment("t")
			.addPathSegment("p")
			.addPathSegment("w500")
			.addPathSegment(posterPath ?: "")
			.build()

		val request = Request.Builder()
			.url(httpUrl)
			.build()

		return try {
			client.newCall(request).execute().body.byteStream().let { inputStream ->
				val bitmap = BitmapFactory.decodeStream(inputStream)
				inputStream.close()
				bitmap
			}
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			null
		}
	}

	fun retrieveMovieDataFromId(id: String?): BucketItemData.ShowData.TMDbData.TMDbMovieData? {
		val httpUrl = HttpUrl.Builder()
			.scheme("https")
			.host("api.themoviedb.org")
			.addPathSegment("3")
			.addPathSegment("movie")
			.addPathSegment(id ?: "")
			.addQueryParameter("api_key", tmdbApiKey)
			.addQueryParameter("language", "en-US")
			.build()

		val request = Request.Builder()
			.url(httpUrl)
			.build()

		return try {
			json.decodeFromString<BucketItemData.ShowData.TMDbData.TMDbMovieData>(client.newCall(request).execute().body.string())
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			null
		}
	}

	fun retrieveTvDataFromId(id: String?): BucketItemData.ShowData.TMDbData.TMDbTvData? {
		val httpUrl = HttpUrl.Builder()
			.scheme("https")
			.host("api.themoviedb.org")
			.addPathSegment("3")
			.addPathSegment("tv")
			.addPathSegment(id ?: "")
			.addQueryParameter("api_key", tmdbApiKey)
			.addQueryParameter("language", "en-US")
			.build()

		val request = Request.Builder()
			.url(httpUrl)
			.build()

		return try {
			json.decodeFromString<BucketItemData.ShowData.TMDbData.TMDbTvData>(client.newCall(request).execute().body.string())
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			null
		}
	}

	companion object {
		@Serializable
		data class TMDBSearchResult2<out T : BucketItemData.ShowData.TMDbData>(
			@SerialName("page") val page: Int? = null,
			@SerialName("results") val results: List<T> = listOf(),
			@SerialName("total_pages") val totalPages: Int? = null,
			@SerialName("total_results") val totalResults: Int? = null,
		)
	}
}
