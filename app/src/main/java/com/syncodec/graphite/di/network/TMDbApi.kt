package com.syncodec.graphite.di.network

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.syncodec.graphite.utils.TMDbKey
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.Serializable
import java.net.URLEncoder

enum class ShowType {
	MOVIE,
	TV
}

class TMDbApi {
	private val client = OkHttpClient.Builder().build()

	fun searchForMovieTitle(title: String, onResponse: (Response?) -> Unit) {
		val url = "https://api.themoviedb.org/3/search/movie?api_key=${TMDbKey}&language=en-US&query=${URLEncoder.encode(title, "utf-8")}"
		val request = Request.Builder()
			.url(url)
			.build()

		onResponse(client.newCall(request).execute())
	}

	fun retrieveMoviePoster(posterPath: String?, onResponse: (Response?) -> Unit) {
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
			val url = "https://api.themoviedb.org/3/movie/$id?api_key=${TMDbKey}&language=en-US&query="
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
	var adult: Boolean,
	@JsonProperty("backdrop_path")
	var backdropPath: String?,
	@JsonProperty("genre_ids")
	var genreIds: List<Int?>?,
	@JsonProperty("id")
	var id: String?,
	@JsonProperty("original_language")
	var originalLanguage: String?,
	@JsonProperty("original_title")
	var originalTitle: String?,
	@JsonProperty("overview")
	var overview: String?,
	@JsonProperty("popularity")
	var popularity: Double?,
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
	@JsonProperty("video")
	var video: Boolean,
	@JsonProperty("vote_average")
	var voteAverage: Double?,
	@JsonProperty("vote_count")
	var voteCount: Int?
) : Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class TvData(
	@JsonProperty("adult")
	var adult: Boolean,
	@JsonProperty("backdrop_path")
	var backdropPath: String?,
	@JsonProperty("genre_ids")
	var genreIds: List<Int?>?,
	@JsonProperty("id")
	var id: String?,
	@JsonProperty("original_language")
	var originalLanguage: String?,
	@JsonProperty("original_title")
	var originalTitle: String?,
	@JsonProperty("overview")
	var overview: String?,
	@JsonProperty("popularity")
	var popularity: Double?,
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
	@JsonProperty("video")
	var video: Boolean,
	@JsonProperty("vote_average")
	var voteAverage: Double?,
	@JsonProperty("vote_count")
	var voteCount: Int?
) : Serializable
