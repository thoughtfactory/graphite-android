package com.syncodec.graphite.di.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.utils.alice.Alice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.Serializable
import java.net.URLEncoder


@kotlinx.serialization.Serializable
enum class ShowType {
	@SerialName(value = "MOVIE")
	MOVIE,
	@SerialName(value = "TV")
	TV,
}

object TMDbApi {
	val objectMapper : ObjectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	private val client = OkHttpClient.Builder().build()

	private val tmdbApi = Alice.decrypt(BuildConfig.TMDB_API_KEY, "lt3(3x4R7M^107!&4E74Z%*o8cp2i7y@") ?: ""

	fun searchForMovieTitle(title : String, onResponse : (ApiResult<TMDbMovieSearchResult>) -> Unit) {
		val url = "https://api.themoviedb.org/3/search/movie?api_key=${tmdbApi}&language=en-US&query=${URLEncoder.encode(title, "utf-8")}"
		val request = Request.Builder()
			.url(url)
			.build()

		try {
			val response = client.newCall(request).execute()
			val tmDbMovieSearchResult = objectMapper.readValue(response.body?.string(), TMDbMovieSearchResult::class.java)
			onResponse(ApiResult.Success(tmDbMovieSearchResult))
		} catch (e : Exception) {
			onResponse(ApiResult.Error(e.message ?: "Unknown error"))
		}
	}

	fun searchForTvTitle(title : String, onResponse : (ApiResult<TMDbTvSearchResult>) -> Unit) {
		val url = "https://api.themoviedb.org/3/search/tv?api_key=${tmdbApi}&language=en-US&query=${URLEncoder.encode(title, "utf-8")}"
		val request = Request.Builder()
			.url(url)
			.build()

		try {
			val response = client.newCall(request).execute()
			val tmDbTvSearchResult = objectMapper.readValue(response.body?.string(), TMDbTvSearchResult::class.java)
			onResponse(ApiResult.Success(tmDbTvSearchResult))
		} catch (e : Exception) {
			onResponse(ApiResult.Error(e.message ?: "Unknown error"))
		}
	}

	fun retrieveShowPoster(posterPath : String?, onResponse : (Bitmap?) -> Unit) {
		if (posterPath == null) onResponse(null)
		else {
			try {
				val url = "https://image.tmdb.org/t/p/w500$posterPath"

				val request = Request.Builder()
					.url(url)
					.build()

				try {
					client.newCall(request).execute().body.byteStream().let { inputStream ->
						val bitmap = BitmapFactory.decodeStream(inputStream)
						onResponse(bitmap)
					}
				} catch (e : Exception) {
					onResponse(null)
				}
			} catch (e : Exception) {
				onResponse(null)
			}
		}
	}

	fun retrieveShowPoster(posterPath : String?) : Bitmap? {
		if (posterPath == null) return null
		else {
			try {
				val url = "https://image.tmdb.org/t/p/w500$posterPath"

				val request = Request.Builder()
					.url(url)
					.build()

				try {
					client.newCall(request).execute().body.byteStream().let { inputStream ->
						val bitmap = BitmapFactory.decodeStream(inputStream)
						return bitmap
					}
				} catch (e : Exception) {
					return null
				}
			} catch (e : Exception) {
				return null
			}
		}
	}

	fun retrieveMovieDataFromId(id : String?, onResponse : (Response?) -> Unit) {
		if (id == null) onResponse(null)
		else {
			val url = "https://api.themoviedb.org/3/movie/$id?api_key=${tmdbApi}&language=en-US&query="
			val request = Request.Builder()
				.url(url)
				.build()

			onResponse(client.newCall(request).execute())
		}
	}

	fun retrieveTvDataFromId(id : String?, onResponse : (Response?) -> Unit) {
		if (id == null) onResponse(null)
		else {
			val url = "https://api.themoviedb.org/3/tv/$id?api_key=${tmdbApi}&language=en-US&query="
			val request = Request.Builder()
				.url(url)
				.build()

			onResponse(client.newCall(request).execute())
		}
	}
}

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class TMDbMovieSearchResult(
	@JsonProperty("page")
	val page : Int?,
	@JsonProperty("results")
	val results : List<MovieData?>?,
	@JsonProperty("total_pages")
	val totalPages : Int?,
	@JsonProperty("total_results")
	val totalResults : Int?
)

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class TMDbTvSearchResult(
	@JsonProperty("page")
	val page : Int?,
	@JsonProperty("results")
	val results : List<TvData?>?,
	@JsonProperty("total_pages")
	val totalPages : Int?,
	@JsonProperty("total_results")
	val totalResults : Int?
)

@kotlinx.serialization.Serializable
@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class MovieData(
	@JsonProperty("adult")
	@SerialName("adult")
	var adult : Boolean? = null,
	@JsonProperty("genres")
	@SerialName("genres")
	var genres : List<Genre?>? = null,
	@JsonProperty("homepage")
	@SerialName("homepage")
	var homepage : String? = null,
	@JsonProperty("id")
	@SerialName("id")
	var id : String? = null,
	@JsonProperty("imdb_id")
	@SerialName("imdb_id")
	var imdbId : String? = null,
	@JsonProperty("original_language")
	@SerialName("original_language")
	var originalLanguage : String? = null,
	@JsonProperty("original_title")
	@SerialName("original_title")
	var originalTitle : String? = null,
	@JsonProperty("overview")
	@SerialName("overview")
	var overview : String? = null,
	@JsonProperty("poster_path")
	@SerialName("poster_path")
	var posterPath : String? = null,
	@JsonProperty("release_date")
	@SerialName("release_date")
	var releaseDate : String? = null,
	@JsonProperty("runtime")
	@SerialName("runtime")
	var runtime : Int? = null,
	@JsonProperty("tagline")
	@SerialName("tagline")
	var tagline : String? = null,
	@JsonProperty("title")
	@SerialName("title")
	var title : String? = null,
) : Serializable {
	constructor(jsonString : String?) : this(null, null, null, null, null, null, null, null, null, null, null, null, null) {
		if (jsonString != null) {
			try {
				val objectMapper : ObjectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				val movieData = objectMapper.readValue(jsonString, MovieData::class.java)
				this.adult = movieData.adult
				this.genres = movieData.genres
				this.homepage = movieData.homepage
				this.id = movieData.id
				this.imdbId = movieData.imdbId
				this.originalLanguage = movieData.originalLanguage
				this.originalTitle = movieData.originalTitle
				this.overview = movieData.overview
				this.posterPath = movieData.posterPath
				this.releaseDate = movieData.releaseDate
				this.runtime = movieData.runtime
				this.tagline = movieData.tagline
				this.title = movieData.title
			} catch (e : Exception) {

			}
		}
	}

	fun toJsonString() : String {
		return try {
			val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			objectMapper.writeValueAsString(this)
		} catch (e : Exception) {
//			e.printStackTrace()
			"null"
		}
	}

	override fun hashCode() : Int {
		var result = adult?.hashCode() ?: 0
		result = 31 * result + (genres?.hashCode() ?: 0)
		result = 31 * result + (homepage?.hashCode() ?: 0)
		result = 31 * result + (id?.hashCode() ?: 0)
		result = 31 * result + (imdbId?.hashCode() ?: 0)
		result = 31 * result + (originalLanguage?.hashCode() ?: 0)
		result = 31 * result + (originalTitle?.hashCode() ?: 0)
		result = 31 * result + (overview?.hashCode() ?: 0)
		result = 31 * result + (posterPath?.hashCode() ?: 0)
		result = 31 * result + (releaseDate?.hashCode() ?: 0)
		result = 31 * result + (runtime ?: 0)
		result = 31 * result + (tagline?.hashCode() ?: 0)
		result = 31 * result + (title?.hashCode() ?: 0)
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is MovieData) return false

		if (adult != other.adult) return false
		if (genres != other.genres) return false
		if (homepage != other.homepage) return false
		if (id != other.id) return false
		if (imdbId != other.imdbId) return false
		if (originalLanguage != other.originalLanguage) return false
		if (originalTitle != other.originalTitle) return false
		if (overview != other.overview) return false
		if (posterPath != other.posterPath) return false
		if (releaseDate != other.releaseDate) return false
		if (runtime != other.runtime) return false
		if (tagline != other.tagline) return false
		if (title != other.title) return false

		return true
	}
}

@kotlinx.serialization.Serializable
@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class TvData(
	@JsonProperty("adult")
	@SerialName("adult")
	var adult : Boolean? = null,
	@JsonProperty("first_air_date")
	@SerialName("first_air_date")
	var firstAirDate : String? = null,
	@JsonProperty("homepage")
	@SerialName("homepage")
	var homepage : String? = null,
	@JsonProperty("genres")
	@SerialName("genres")
	var genres : List<Genre?>? = null,
	@JsonProperty("id")
	@SerialName("id")
	var id : String? = null,
	@JsonProperty("name")
	@SerialName("name")
	var name : String? = null,
	@JsonProperty("number_of_episodes")
	@SerialName("number_of_episodes")
	var numberOfEpisodes : Int? = null,
	@JsonProperty("number_of_seasons")
	@SerialName("number_of_seasons")
	var numberOfSeasons : Int? = null,
	@JsonProperty("original_language")
	@SerialName("original_language")
	var originalLanguage : String? = null,
	@JsonProperty("original_name")
	@SerialName("original_name")
	var originalName : String? = null,
	@JsonProperty("overview")
	@SerialName("overview")
	var overview : String? = null,
	@JsonProperty("poster_path")
	@SerialName("poster_path")
	var posterPath : String? = null,
	@JsonProperty("tagline")
	@SerialName("tagline")
	var tagline : String? = null,
) : Serializable {
	constructor(jsonString : String?) : this(null, null, null, null, null, null, null, null, null, null, null, null, null) {
		if (jsonString != null) {
			try {
				val objectMapper : ObjectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				val tvData = objectMapper.readValue(jsonString, TvData::class.java)
				this.adult = tvData.adult
				this.firstAirDate = tvData.firstAirDate
				this.homepage = tvData.homepage
				this.genres = tvData.genres
				this.id = tvData.id
				this.name = tvData.name
				this.numberOfEpisodes = tvData.numberOfEpisodes
				this.numberOfSeasons = tvData.numberOfSeasons
				this.originalLanguage = tvData.originalLanguage
				this.originalName = tvData.originalName
				this.overview = tvData.overview
				this.posterPath = tvData.posterPath
				this.tagline = tvData.tagline
			} catch (e : Exception) {

			}
		}
	}

	fun toJsonString() : String {
		return try {
			val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			objectMapper.writeValueAsString(this)
		} catch (e : Exception) {
//			e.printStackTrace()
			"null"
		}
	}

	override fun hashCode() : Int {
		var result = adult?.hashCode() ?: 0
		result = 31 * result + (firstAirDate?.hashCode() ?: 0)
		result = 31 * result + (homepage?.hashCode() ?: 0)
		result = 31 * result + (genres?.hashCode() ?: 0)
		result = 31 * result + (id?.hashCode() ?: 0)
		result = 31 * result + (name?.hashCode() ?: 0)
		result = 31 * result + (numberOfEpisodes ?: 0)
		result = 31 * result + (numberOfSeasons ?: 0)
		result = 31 * result + (originalLanguage?.hashCode() ?: 0)
		result = 31 * result + (originalName?.hashCode() ?: 0)
		result = 31 * result + (overview?.hashCode() ?: 0)
		result = 31 * result + (posterPath?.hashCode() ?: 0)
		result = 31 * result + (tagline?.hashCode() ?: 0)
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is TvData) return false

		if (adult != other.adult) return false
		if (firstAirDate != other.firstAirDate) return false
		if (homepage != other.homepage) return false
		if (genres != other.genres) return false
		if (id != other.id) return false
		if (name != other.name) return false
		if (numberOfEpisodes != other.numberOfEpisodes) return false
		if (numberOfSeasons != other.numberOfSeasons) return false
		if (originalLanguage != other.originalLanguage) return false
		if (originalName != other.originalName) return false
		if (overview != other.overview) return false
		if (posterPath != other.posterPath) return false
		if (tagline != other.tagline) return false

		return true
	}
}

@kotlinx.serialization.Serializable
@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class Genre(
	@JsonProperty("id")
	@SerialName("id")
	var id : Int? = null,
	@JsonProperty("name")
	@SerialName("name")
	var name : String? = null,
) : Serializable {
	override fun hashCode() : Int {
		var result = id ?: 0
		result = 31 * result + (name?.hashCode() ?: 0)
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is Genre) return false

		if (id != other.id) return false
		if (name != other.name) return false

		return true
	}
}
