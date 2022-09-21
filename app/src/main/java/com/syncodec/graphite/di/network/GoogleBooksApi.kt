package com.syncodec.graphite.di.network

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.syncodec.graphite.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.URLEncoder
import java.io.Serializable


class GoogleBooksApi {
	private val client = OkHttpClient
		.Builder()
		.build()

	fun searchForAll(query: String, maxResult: Int = 6, onResponse: (Response?) -> Unit ) {
		val url = "https://www.googleapis.com/books/v1/volumes?q=${URLEncoder.encode(query, "utf-8")}&maxResults=$maxResult&key=${BuildConfig.GOOGLE_BOOK_API_KEY}"

		val request = Request.Builder()
			.url(url)
			.build()

		onResponse(client.newCall(request).execute())
	}

	fun retrieveBookCover(url: String?, onResponse: (Response?) -> Unit ) {
		if (url == null) {
			onResponse(null)
		} else {
			val request = Request.Builder()
				.url(url.replace("http://", "https://"))
				.build()

			onResponse(client.newCall(request).execute())
		}
	}
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class GoogleBookSearchResult(
	@JsonProperty("kind")
	val kind: String?,
	@JsonProperty("totalItems")
	val totalItems: Int?,
	@JsonProperty("items")
	val items: List<GoogleBookData?>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GoogleBookData(
	@JsonProperty("id")
	val id: String?,
	@JsonProperty("selfLink")
	val selfLink: String?,
	@JsonProperty("volumeInfo")
	val volumeInfo: VolumeInfo?
) : Serializable {

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is GoogleBookData) return false

		if (id != other.id) return false
		if (selfLink != other.selfLink) return false
		if (volumeInfo != other.volumeInfo) return false

		return true
	}

	override fun hashCode(): Int {
		var result = id?.hashCode() ?: 0
		result = 31 * result + (selfLink?.hashCode() ?: 0)
		result = 31 * result + (volumeInfo?.hashCode() ?: 0)
		return result
	}
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class VolumeInfo(
	@JsonProperty("title")
	val title: String?,
	@JsonProperty("authors")
	val authors: List<String?>?,
	@JsonProperty("publisher")
	val publisher: String?,
	@JsonProperty("publishedDate")
	val publishedDate: String?,
	@JsonProperty("description")
	val description: String?,
	@JsonProperty("industryIdentifiers")
	val industryIdentifiers: List<IndustryIdentifier?>?,
	@JsonProperty("pageCount")
	val pageCount: Int?,
	@JsonProperty("categories")
	val categories: List<String?>?,
	@JsonProperty("imageLinks")
	val imageLinks: ImageLink?,
	@JsonProperty("language")
	val language: String?,
	@JsonProperty("previewLink")
	val previewLink: String?,
) : Serializable {

	override fun hashCode(): Int {
		var result = title?.hashCode() ?: 0
		result = 31 * result + (authors?.hashCode() ?: 0)
		result = 31 * result + (publisher?.hashCode() ?: 0)
		result = 31 * result + (publishedDate?.hashCode() ?: 0)
		result = 31 * result + (description?.hashCode() ?: 0)
		result = 31 * result + (industryIdentifiers?.hashCode() ?: 0)
		result = 31 * result + (pageCount ?: 0)
		result = 31 * result + (categories?.hashCode() ?: 0)
		result = 31 * result + (imageLinks?.hashCode() ?: 0)
		result = 31 * result + (language?.hashCode() ?: 0)
		result = 31 * result + (previewLink?.hashCode() ?: 0)
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is VolumeInfo) return false

		if (title != other.title) return false
		if (authors != other.authors) return false
		if (publisher != other.publisher) return false
		if (publishedDate != other.publishedDate) return false
		if (description != other.description) return false
		if (industryIdentifiers != other.industryIdentifiers) return false
		if (pageCount != other.pageCount) return false
		if (categories != other.categories) return false
		if (imageLinks != other.imageLinks) return false
		if (language != other.language) return false
		if (previewLink != other.previewLink) return false

		return true
	}
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class IndustryIdentifier(
	@JsonProperty("type")
	val type: String?,
	@JsonProperty("identifier")
	val identifier: String?
) : Serializable {

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is IndustryIdentifier) return false

		if (type != other.type) return false
		if (identifier != other.identifier) return false

		return true
	}

	override fun hashCode(): Int {
		var result = type?.hashCode() ?: 0
		result = 31 * result + (identifier?.hashCode() ?: 0)
		return result
	}
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ImageLink(
	@JsonProperty("smallThumbnail")
	val smallThumbnail: String?,
	@JsonProperty("thumbnail")
	val thumbnail: String?
) : Serializable {

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is ImageLink) return false

		if (smallThumbnail != other.smallThumbnail) return false
		if (thumbnail != other.thumbnail) return false

		return true
	}

	override fun hashCode(): Int {
		var result = smallThumbnail?.hashCode() ?: 0
		result = 31 * result + (thumbnail?.hashCode() ?: 0)
		return result
	}
}
