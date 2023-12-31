package com.syncodec.graphite.di.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.Keep
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.model.local.BucketItemData
import com.syncodec.graphite.di.network.OpenLibraryApi.OpenLibraryApiRequestType.ISBN
import com.syncodec.graphite.di.network.OpenLibraryApi.OpenLibraryApiRequestType.QUERY
import com.syncodec.graphite.di.network.OpenLibraryApi.OpenLibraryApiRequestType.TITLE
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
import okio.IOException
import org.json.JSONObject


class OpenLibraryApi(context: Context) {
	private val json = Json { ignoreUnknownKeys = true }

	private val bookKeyCoverMap: MutableMap<Int, Bitmap> = mutableMapOf()
	private val bookKeyDataMap: MutableMap<String?, BucketItemData.BookData.OpenLibraryBookData> = mutableMapOf()
	private val bookKeyDescriptionMap: MutableMap<String?, String> = mutableMapOf()

	private val client = OkHttpClient.Builder()
		.cache(Cache(context.cacheDir,  50 * 1024 * 1024))
		.build()

	/**
	 * [QUERY] Search for a book by openLibrary query
	 *
	 * [TITLE] Search for a book by title
	 *
	 * [ISBN] Search for a book by ISBN
	 */
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
	 * @param requestType Type of request [OpenLibraryApiRequestType]
	 * @param callback Callback for the result in form of OpenLibraryTitleSearchResult
	 * @sample https://openlibrary.org/search.json?title=the%20book%20thief&fields=key,title,author_name,cover_i,first_publish_year,number_of_pages_median&limit=9&offset=0
	 */
	fun searchForBook(query: String, requestType: OpenLibraryApiRequestType = OpenLibraryApiRequestType.TITLE, callback: (NetworkRequest<OpenLibraryTitleSearchResult>) -> Unit) {
		callback(NetworkRequest.Loading)
		try {

			val httpUrl = HttpUrl.Builder()
				.scheme("https")
				.host("openlibrary.org")
				.addPathSegment("search.json")
				.addQueryParameter("title", query)
				.addQueryParameter("fields", "key,title,author_name,cover_i,first_publish_year,number_of_pages_median")
				.addQueryParameter("limit", "9")
				.addQueryParameter("offset", "0")
				.build()

			val request = Request.Builder()
				.url(httpUrl)
				.build()

			client.newCall(request).enqueue(
				responseCallback = object  : Callback {
					override fun onFailure(call: Call, e: IOException) {
						if (BuildConfig.DEBUG) e.printStackTrace()
						callback(NetworkRequest.Error(e))
					}

					override fun onResponse(call: Call, response: Response) {
						try {
							val openLibraryTitleSearchResult : OpenLibraryTitleSearchResult = json.decodeFromString(response.body.string())
							openLibraryTitleSearchResult.docs?.filterNotNull()?.associateBy { it.key }?.let { bookKeyDataMap.putAll(it) }
							callback(NetworkRequest.Success(openLibraryTitleSearchResult))
						} catch (e: Exception) {
							if (BuildConfig.DEBUG) e.printStackTrace()
							callback(NetworkRequest.Error(e))
						}
					}
				}
			)
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			callback(NetworkRequest.Error(e))
		}
	}

	fun getBookDataFromCache(bookKey: String?): BucketItemData.BookData.OpenLibraryBookData? = bookKeyDataMap[bookKey]

	fun retrieveBookCover(coverI: Int?): Bitmap? {
		return bookKeyCoverMap[coverI] ?: try {
			if (coverI == null) return null
			else {
				val httpUrl = HttpUrl.Builder()
					.scheme("https")
					.host("covers.openlibrary.org")
					.addPathSegment("b")
					.addPathSegment("id")
					.addPathSegment("${coverI}-M.jpg")
					.build()

				val request = Request.Builder()
					.url(httpUrl)
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

	fun retrieveDescriptionFromKey(bookKey: String?): String? = bookKeyDescriptionMap[bookKey]
		?: try {
			val httpUrl = HttpUrl.Builder()
				.scheme("https")
				.host("openlibrary.org")
				.addPathSegment("$bookKey.json")
				.build()

			val request = Request.Builder()
				.url(httpUrl)
				.build()

			client.newCall(request).execute().body.let {
				val jsonObject = JSONObject(it.string())
				val description = jsonObject.optJSONObject("description")?.optString("value")
				description?.let { bookKeyDescriptionMap[bookKey] = it }
				description
			}
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			null
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
@Serializable
data class OpenLibraryTitleSearchResult(
	@SerialName("numFound") val numFound: Int?,
	@SerialName("start") val start: Int?,
	@SerialName("docs") val docs: List<BucketItemData.BookData.OpenLibraryBookData?>? = null,
)
