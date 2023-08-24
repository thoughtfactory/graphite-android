package com.syncodec.graphite.di.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.syncodec.graphite.di.model.BucketItemObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder


sealed class OpenLibraryResponse {
	object Loading : OpenLibraryResponse()
	data class Success(val data: OpenLibraryTitleSearchResult) : OpenLibraryResponse()
	data class Error(val message: String = "Unknown error") : OpenLibraryResponse()
}

object OpenLibraryApi {
	private val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	private val bookKeyCoverMap: MutableMap<String, Bitmap> = mutableMapOf()
	private val bookKeyDataMap: MutableMap<String?, BucketItemObject.Companion.BucketItemData.BookData> = mutableMapOf()
	private val bookKeyDescriptionMap: MutableMap<String?, String> = mutableMapOf()

	private val client = OkHttpClient.Builder().build()

	enum class OpenLibraryApiRequestType {
		QUERY,
		TITLE,
		ISBN
	}

	private var searchForBookCoroutine: CoroutineScope? = null

	/**
	 * Search for a book by title on OpenLibrary
	 * @author pushpull
	 * @since 2.2.0
	 * @param query Query to search for
	 * @param requestType Type of request QUERY, TITLE, ISBN
	 * @param onResponse Callback for the result in form of OpenLibraryTitleSearchResult
	 */
	fun searchForBook(query: String, requestType: OpenLibraryApiRequestType = OpenLibraryApiRequestType.TITLE, onResponse: (OpenLibraryResponse) -> Unit) {
		CoroutineScope(Dispatchers.IO).launch {
			searchForBookCoroutine?.cancel()
			searchForBookCoroutine = this
			onResponse(OpenLibraryResponse.Loading)
			try {
				val url = "https://openlibrary.org/search.json?title=${URLEncoder.encode(query, "utf-8")}&fields=key,title,author_name,cover_i,first_publish_year,number_of_pages_median&limit=9&offset=0"
				val request = Request.Builder()
					.url(url)
					.build()

				val response = client.newCall(request).execute()
				val openLibraryTitleSearchResult = objectMapper.readValue(response.body?.string(), OpenLibraryTitleSearchResult::class.java)
				openLibraryTitleSearchResult.docs?.filterNotNull()?.associateBy { it.key }?.let { bookKeyDataMap.putAll(it) }
				onResponse(OpenLibraryResponse.Success(openLibraryTitleSearchResult))
			} catch (e: Exception) {
				onResponse(OpenLibraryResponse.Error())
			}
		}
	}

	fun getBookDataFromCache(bookKey: String?): BucketItemObject.Companion.BucketItemData.BookData? = bookKeyDataMap[bookKey]

	fun retrieveBookCover(coverI: String?): Bitmap? {
		return bookKeyCoverMap[coverI] ?: try {
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
						bookKeyCoverMap[coverI] = bitmap
						bitmap
					}
				} catch (e: Exception) {
					null
				}
			}
		} catch (e: Exception) {
			return null
		}
	}

	fun retrieveDescriptionFromKey(bookKey: String?): String? {
		val cachedDescription = bookKeyDescriptionMap[bookKey]
		return if (cachedDescription == null) {
			try {
				val url = "https://openlibrary.org/$bookKey.json"

				val request = Request.Builder()
					.url(url)
					.build()

				client.newCall(request).execute().body.let {
					val jsonObject = JSONObject(it.string())
					val description = jsonObject.optJSONObject("description")?.optString("value")
					Log.d("npr71", "retrieveDescriptionFromKey : network : $description")
					description?.let { bookKeyDescriptionMap[bookKey] = it }
					description
				}
			} catch (_: Exception) {
				null
			}
		} else cachedDescription
	}


	/**
	 * [Keep an eye at this commit](https://github.com/internetarchive/openlibrary/blob/abd73aa37ea27b4e7d70f521bfd1e30b7dc1dc6e/openlibrary/plugins/worksearch/schemes/works.py#L113-L132)
	 * [Latest commit](https://github.com/internetarchive/openlibrary/blob/master/openlibrary/plugins/worksearch/schemes/works.py)
	 */
	enum class Sort(val sortName: String) {
		Editions(sortName = "editions"),
		Old(sortName = "old"),
		New(sortName = "new"),
		Title(sortName = "title"),
		Scans(sortName = "scans"),
//		'title': 'title_sort asc',
//		'scans': 'ia_count desc',
//		# Classifications
//		'lcc_sort': 'lcc_sort asc',
//		'lcc_sort asc': 'lcc_sort asc',
//		'lcc_sort desc': 'lcc_sort desc',
//		'ddc_sort': 'ddc_sort asc',
//		'ddc_sort asc': 'ddc_sort asc',
//		'ddc_sort desc': 'ddc_sort desc',
//		# Random
//		'random': 'random_1 asc',
//		'random asc': 'random_1 asc',
//		'random desc': 'random_1 desc',
//		'random.hourly': lambda: f'random_{datetime.now():%Y%m%dT%H} asc',
//		'random.daily': lambda: f'random_{datetime.now():%Y%m%d} asc',
	}
}

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenLibraryTitleSearchResult(
	@JsonProperty("numFound")
	val numFound: Int?,
	@JsonProperty("start")
	val start: Int?,
	@JsonProperty("docs")
	val docs: List<BucketItemObject.Companion.BucketItemData.BookData?>?
)
