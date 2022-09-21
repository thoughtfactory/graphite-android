package com.syncodec.graphite.di.network

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.Serializable
import java.net.URLEncoder


enum class ApiStatus {
	LOADING,
	SUCCESS,
	ERROR,
}  // for your case might be simplify to use only sealed class

sealed class ApiResult<out T>(val status: ApiStatus, val data: T?, val message: String?) {

	data class Loading<out R>(val _data: R?, val isLoading: Boolean) : ApiResult<R>(
		status = ApiStatus.LOADING,
		data = _data,
		message = null
	)

	data class Success<out R>(val _data: R?) : ApiResult<R>(
		status = ApiStatus.SUCCESS,
		data = _data,
		message = null
	)

	data class Error(val exception: String) : ApiResult<Nothing>(
		status = ApiStatus.ERROR,
		data = null,
		message = exception
	)
}


class OpenLibraryApi {
	private val client = OkHttpClient.Builder().build()

	fun searchForTitle(title: String, onResponse: (Response?) -> Unit, ) {
		val url = "https://openlibrary.org/search.json?title=${URLEncoder.encode(title, "utf-8")}&fields=key,title,author_name,cover_i,first_publish_year&limit=10&offset=0"

		val request = Request.Builder()
			.url(url)
			.build()

		onResponse(client.newCall(request).execute())
	}

	fun retrieveBookCover(coverI: String?, onResponse: (Response?) -> Unit) {
		if (coverI == null) onResponse(null)
		else {
			val url = "https://covers.openlibrary.org/b/id/${coverI}-M.jpg"

			val request = Request.Builder()
				.url(url)
				.build()

			onResponse(client.newCall(request).execute())
		}
	}

	fun retrieveDataFromKey(key: String?, onResponse: (Response?) -> Unit) {
		if (key == null) onResponse(null)
		else {
			val url = "https://openlibrary.org/$key.json"
			val request = Request.Builder()
				.url(url)
				.build()

			onResponse(client.newCall(request).execute())
		}
	}
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenLibraryTitleSearchResult(
	@JsonProperty("numFound")
	val numFound: Int?,
	@JsonProperty("start")
	val start: Int?,
	@JsonProperty("docs")
	val docs: List<BookData>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BookData(
	@JsonProperty("key")
	var key: String?,
	@JsonProperty("title")
	var title: String?,
	@JsonProperty("cover_i")
	var coverI: String?,    // Url for cover
	@JsonProperty("author_name")
	var authorList: List<String?>?,
	@JsonProperty("first_publish_year")
	var firstPublishedYear: String?,
	@JsonProperty("description")
	var description: String?
) : Serializable {
	override fun hashCode(): Int {
		var result = key?.hashCode() ?: 0
		result = 31 * result + (title?.hashCode() ?: 0)
		result = 31 * result + (coverI?.hashCode() ?: 0)
		result = 31 * result + (authorList?.hashCode() ?: 0)
		result = 31 * result + (firstPublishedYear?.hashCode() ?: 0)
		result = 31 * result + (description?.hashCode() ?: 0)
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is BookData) return false

		if (key != other.key) return false
		if (title != other.title) return false
		if (coverI != other.coverI) return false
		if (authorList != other.authorList) return false
		if (firstPublishedYear != other.firstPublishedYear) return false
		if (description != other.description) return false

		return true
	}
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class BookDataDescription(
	@JsonProperty("type")
	var type: String?,
	@JsonProperty("value")
	var value: String?
) {
	override fun hashCode(): Int {
		var result = type?.hashCode() ?: 0
		result = 31 * result + (value?.hashCode() ?: 0)
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is BookDataDescription) return false

		if (type != other.type) return false
		if (value != other.value) return false

		return true
	}
}
