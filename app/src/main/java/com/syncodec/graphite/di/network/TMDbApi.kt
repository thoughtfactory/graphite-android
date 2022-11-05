package com.syncodec.graphite.di.network

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.syncodec.graphite.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.Serializable
import java.net.URLEncoder

enum class ShowType {
	MOVIE,
	TV
}

object TMDbApi {
	val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	private val client = OkHttpClient.Builder().build()

	fun searchForMovieTitle(title: String, onResponse: (TMDbMovieSearchResult?) -> Unit) {
		val url = "https://api.themoviedb.org/3/search/movie?api_key=${BuildConfig.TMDB_API_KEY}&language=en-US&query=${URLEncoder.encode(title, "utf-8")}"
		val request = Request.Builder()
			.url(url)
			.build()

		try {
			val response = client.newCall(request).execute()
			val tmDbMovieSearchResult = objectMapper.readValue(response.body?.string(), TMDbMovieSearchResult::class.java)
			onResponse(tmDbMovieSearchResult)
		} catch (e: Exception) {
			e.printStackTrace()
			onResponse(null)
		}
	}

	fun searchForTvTitle(title: String, onResponse: (TMDbTvSearchResult?) -> Unit) {
		val url = "https://api.themoviedb.org/3/search/tv?api_key=${BuildConfig.TMDB_API_KEY}&language=en-US&query=${URLEncoder.encode(title, "utf-8")}"
		val request = Request.Builder()
			.url(url)
			.build()

		try {
			val response = client.newCall(request).execute()
			val tmDbTvSearchResult = objectMapper.readValue(response.body?.string(), TMDbTvSearchResult::class.java)
			onResponse(tmDbTvSearchResult)
		} catch (e: Exception) {
			e.printStackTrace()
			onResponse(null)
		}
	}

	fun retrieveShowPoster(posterPath: String?, onResponse: (Response?) -> Unit) {
		if (posterPath == null) onResponse(null)
		else {
			val url = "https://image.tmdb.org/t/p/w500$posterPath"

			val request = Request.Builder()
				.url(url)
				.build()

			onResponse(client.newCall(request).execute())
		}
	}

	fun retrieveMovieDataFromId(id: String?, onResponse: (Response?) -> Unit) {
		if (id == null) onResponse(null)
		else {
			val url = "https://api.themoviedb.org/3/movie/$id?api_key=${BuildConfig.TMDB_API_KEY}&language=en-US&query="
			val request = Request.Builder()
				.url(url)
				.build()

			onResponse(client.newCall(request).execute())
		}
	}

	fun retrieveTvDataFromId(id: String?, onResponse: (Response?) -> Unit) {
		if (id == null) onResponse(null)
		else {
			val url = "https://api.themoviedb.org/3/tv/$id?api_key=${BuildConfig.TMDB_API_KEY}&language=en-US&query="
			val request = Request.Builder()
				.url(url)
				.build()

			onResponse(client.newCall(request).execute())
		}
	}
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TMDbMovieSearchResult(
	@JsonProperty("page")
	val page: Int?,
	@JsonProperty("results")
	val results: List<MovieData?>?,
	@JsonProperty("total_pages")
	val totalPages: Int?,
	@JsonProperty("total_results")
	val totalResults: Int?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TMDbTvSearchResult(
	@JsonProperty("page")
	val page: Int?,
	@JsonProperty("results")
	val results: List<TvData?>?,
	@JsonProperty("total_pages")
	val totalPages: Int?,
	@JsonProperty("total_results")
	val totalResults: Int?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MovieData(
	@JsonProperty("adult")
	var adult: Boolean?,
	@JsonProperty("genres")
	var genres: List<Genre?>?,
	@JsonProperty("homepage")
	var homepage : String?,
	@JsonProperty("id")
	var id: String?,
	@JsonProperty("imdb_id")
	var imdbId: String?,
	@JsonProperty("original_language")
	var originalLanguage: String?,
	@JsonProperty("original_title")
	var originalTitle: String?,
	@JsonProperty("overview")
	var overview: String?,
	@JsonProperty("poster_path")
	var posterPath: String?,
	@JsonProperty("release_date")
	var releaseDate: String?,
	@JsonProperty("runtime")
	var runtime: Int?,
	@JsonProperty("tagline")
	var tagline: String?,
	@JsonProperty("title")
	var title: String?,
) : Serializable {
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class TvData(
	@JsonProperty("adult")
	var adult: Boolean?,
	@JsonProperty("first_air_date")
	var firstAirDate: String?,
	@JsonProperty("homepage")
	var homepage: String?,
	@JsonProperty("genres")
	var genres: List<Genre?>?,
	@JsonProperty("id")
	var id: String?,
	@JsonProperty("name")
	var name: String?,
	@JsonProperty("number_of_episodes")
	var numberOfEpisodes: Int?,
	@JsonProperty("number_of_seasons")
	var numberOfSeasons: Int?,
	@JsonProperty("original_language")
	var originalLanguage: String?,
	@JsonProperty("original_name")
	var originalName: String?,
	@JsonProperty("overview")
	var overview: String?,
	@JsonProperty("poster_path")
	var posterPath: String?,
	@JsonProperty("tagline")
	var tagline: String?,
) : Serializable {
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class Genre(
	@JsonProperty("id")
	var id: Int?,
	@JsonProperty("name")
	var name: String?
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

data class ShowData(
	var type: ShowType?,
	var tvData: TvData? = null,
	var movieData : MovieData? = null
) {
	fun toJsonString(): String {
		return try {
			val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			objectMapper.writeValueAsString(this)
		} catch (e: Exception) {
			e.printStackTrace()
			"null"
		}
	}

	override fun hashCode() : Int {
		var result = type?.hashCode() ?: 0
		result = 31 * result + (tvData?.hashCode() ?: 0)
		result = 31 * result + (movieData?.hashCode() ?: 0)
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is ShowData) return false

		if (type != other.type) return false
		if (tvData != other.tvData) return false
		if (movieData != other.movieData) return false

		return true
	}


}
