package com.syncodec.graphite.di.network

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.Serializable
import java.net.URLEncoder


enum class ApiStatus {
	LOADING,
	SUCCESS,
	ERROR,
}  // for your case might be simplify to use only sealed class

sealed class ApiResult<out T>(val status : ApiStatus, val data : T?, val message : String?) {

	data class Loading<out R>(val _data : R?, val isLoading : Boolean) : ApiResult<R>(
		status = ApiStatus.LOADING,
		data = _data,
		message = null
	)

	data class Success<out R>(val _data : R?) : ApiResult<R>(
		status = ApiStatus.SUCCESS,
		data = _data,
		message = null
	)

	data class Error(val exception : String) : ApiResult<Nothing>(
		status = ApiStatus.ERROR,
		data = null,
		message = exception
	)
}


object OpenLibraryApi {
	private val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	private val client = OkHttpClient.Builder().build()

	enum class OpenLibraryApiRequestType {
		QUERY,
		TITLE,
		ISBN
	}

	fun searchForBook(query : String, requestType : OpenLibraryApiRequestType, onResponse : (OpenLibraryTitleSearchResult?) -> Unit) {
		try {
			val url = when (requestType) {
				OpenLibraryApiRequestType.QUERY -> "https://openlibrary.org/search.json?q="
					.plus(URLEncoder.encode(query, "utf-8"))
					.plus("&fields=key,title,author_name,cover_i,first_publish_year,number_of_pages_median&limit=9&offset=0")

				OpenLibraryApiRequestType.TITLE -> "https://openlibrary.org/search.json?title="
					.plus(URLEncoder.encode(query, "utf-8"))
					.plus("&fields=key,title,author_name,cover_i,first_publish_year,number_of_pages_median&limit=9&offset=0")

				OpenLibraryApiRequestType.ISBN -> "https://openlibrary.org/api/books?bibkeys=ISBN:$query&jscmd=details&format=json"
			}
			url.let {
				val request = Request.Builder()
					.url(it)
					.build()

				val response = client.newCall(request).execute()
				val openLibraryTitleSearchResult = objectMapper.readValue(response.body?.string(), OpenLibraryTitleSearchResult::class.java)
				onResponse(openLibraryTitleSearchResult)
			}
		} catch (e : Exception) {
			onResponse(null)
		}
	}

	fun retrieveBookCover(coverI : String?, onResponse : (Response?) -> Unit) {
		try {
			if (coverI == null) onResponse(null)
			else {
				val url = "https://covers.openlibrary.org/b/id/${coverI}-M.jpg"

				val request = Request.Builder()
					.url(url)
					.build()

				onResponse(client.newCall(request).execute())
			}
		} catch (e : Exception) {
			onResponse(null)
		}
	}

	fun retrieveDescriptionFromKey(key : String?, onResponse : (String?) -> Unit) {
		if (key == null) onResponse(null)
		else {
			try {
				val url = "https://openlibrary.org/$key.json"

				val request = Request.Builder()
					.url(url)
					.build()

				client.newCall(request).execute().body?.let {
					val jsonObject = JSONObject(it.string())
					jsonObject.optJSONObject("description")?.optString("value")?.let { onResponse(it) } ?: onResponse(null)
				} ?: onResponse(null)
			} catch (e : Exception) {
//				e.printStackTrace()
				onResponse(null)
			}
		}
	}
}

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenLibraryTitleSearchResult(
	@JsonProperty("numFound")
	val numFound : Int?,
	@JsonProperty("start")
	val start : Int?,
	@JsonProperty("docs")
	val docs : List<BookData?>?
)

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class BookData(
	@JsonProperty("key")
	var key : String?,
	@JsonProperty("title")
	var title : String?,
	@JsonProperty("cover_i")
	var coverI : String?,    // Url for cover
	@JsonProperty("author_name")
	var authorList : List<String?>?,
	@JsonProperty("first_publish_year")
	var firstPublishYear : String?,
	@JsonProperty("number_of_pages_median")
	var numberOfPages : Int?,
	@JsonProperty("description")
	var description : String?
) : Serializable {

	fun toJsonString() : String {
		return try {
			val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			objectMapper.writeValueAsString(this)
		} catch (e : Exception) {
//			e.printStackTrace()
			"null"
		}
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is BookData) return false

		if (key != other.key) return false
		if (title != other.title) return false
		if (coverI != other.coverI) return false
		if (authorList != other.authorList) return false
		if (firstPublishYear != other.firstPublishYear) return false
		if (numberOfPages != other.numberOfPages) return false
		if (description != other.description) return false

		return true
	}

	override fun hashCode() : Int {
		var result = key?.hashCode() ?: 0
		result = 31 * result + (title?.hashCode() ?: 0)
		result = 31 * result + (coverI?.hashCode() ?: 0)
		result = 31 * result + (authorList?.hashCode() ?: 0)
		result = 31 * result + (firstPublishYear?.hashCode() ?: 0)
		result = 31 * result + (numberOfPages ?: 0)
		result = 31 * result + (description?.hashCode() ?: 0)
		return result
	}
}
