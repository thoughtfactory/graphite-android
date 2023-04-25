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
import com.syncodec.graphite.di.model.BucketItemObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.Serializable
import java.net.URLEncoder


object OpenLibraryApi {
	private val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	private val client = OkHttpClient.Builder().build()

	enum class OpenLibraryApiRequestType {
		QUERY,
		TITLE,
		ISBN
	}

	/**
	 * Search for a book by title on OpenLibrary
	 * @author pushpull
	 * @since 2.2.0
	 * @param query Query to search for
	 * @param requestType Type of request QUERY, TITLE, ISBN
	 * @param onResponse Callback for the result in form of OpenLibraryTitleSearchResult
	 */
	fun searchForBook(query : String, requestType : OpenLibraryApiRequestType, onResponse : (ApiResult<OpenLibraryTitleSearchResult>) -> Unit) {
		onResponse(ApiResult.Loading(null, true))
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
				onResponse(ApiResult.Success(openLibraryTitleSearchResult))
			}
		} catch (e : Exception) {
			onResponse(ApiResult.Error(e.message ?: "Unknown error"))
		}
	}

	fun retrieveBookCover(coverI : String?, onResponse : (Bitmap?) -> Unit) {
		try {
			if (coverI == null) onResponse(null)
			else {
				val url = "https://covers.openlibrary.org/b/id/${coverI}-M.jpg"

				val request = Request.Builder()
					.url(url)
					.build()

				try {
					client.newCall(request).execute().body.byteStream().let { inputStream ->
						onResponse(BitmapFactory.decodeStream(inputStream))
						inputStream.close()
					}
				} catch (e : Exception) {
					onResponse(null)
				}
			}
		} catch (e : Exception) {
			onResponse(null)
		}
	}

	fun retrieveBookCover(coverI : String?) : Bitmap? {
		try {
			if (coverI == null) return null
			else {
				val url = "https://covers.openlibrary.org/b/id/${coverI}-M.jpg"

				val request = Request.Builder()
					.url(url)
					.build()

				try {
					client.newCall(request).execute().body.byteStream().let { inputStream ->
						val bitmap = BitmapFactory.decodeStream(inputStream)
						inputStream.close()
						return bitmap
					}
				} catch (e : Exception) {
					return null
				}
			}
		} catch (e : Exception) {
			return null
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
	val docs : List<BucketItemObject.Companion.BucketItemData.BookData?>?
)
