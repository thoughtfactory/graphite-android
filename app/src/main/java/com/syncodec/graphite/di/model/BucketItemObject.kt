package com.syncodec.graphite.di.model

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.di.network.MovieData
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.di.network.TvData
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.Serializable
import java.time.Instant


enum class BucketItemState {
	ALPHA,
	BETA,
	GAMMA
}

@Keep
@JsonIgnoreProperties(value = ["io_realm_kotlin_objectReference"], ignoreUnknown = true)
class BucketItemObject() : RealmObject {
	constructor(jsonObject: JSONObject) : this() {
		this.id = jsonObject.optString("id").let { if (it.isNullOrEmpty() || it == "null") RealmUUID.random() else RealmUUID.from(it) }
		this.createdTimestamp = jsonObject.getLong("createdTimestamp")
		this.modifiedTimestamp = jsonObject.getLong("modifiedTimestamp")
		this.bucketType = jsonObject.optString("bucketType").let { if (it.isNullOrEmpty() || it == "null") BucketType.UNKNOWN.name else it }
		this.title = jsonObject.optString("title").let { if (it.isNullOrEmpty() || it == "null") null else it }
		this.state = jsonObject.optString("state").let { if (it.isNullOrEmpty() || it == "null") BucketItemState.ALPHA.name else it }
		this.thumbnail = jsonObject.optString("thumbnail").let { if (it.isNullOrEmpty() || it == "null") null else it }
		this.isFavourite = jsonObject.optBoolean("isFavourite", false)
		this.isLocked = jsonObject.optBoolean("isLocked", false)
		this.parentId = jsonObject.optString("parentId").let { if (it.isNullOrEmpty() || it == "null") null else RealmUUID.from(it) }
		this.key = jsonObject.optString("key").let { if (it.isNullOrEmpty() || it == "null") null else it }
		this.data = jsonObject.optString("data").let { if (it.isNullOrEmpty() || it == "null") null else it }
	}

	@PrimaryKey
	var id: RealmUUID = RealmUUID.random()

	var createdTimestamp: Long = Instant.now().toEpochMilli()
	var modifiedTimestamp: Long = Instant.now().toEpochMilli()
	var bucketType: String = BucketType.UNKNOWN.name
	var title: String? = null
	var state: String = BucketItemState.ALPHA.name
	var thumbnail: String? = null
	var isFavourite: Boolean = false
	var isLocked: Boolean = false
	var parentId: RealmUUID? = null

	var key: String? = null
	var data: String? = null

	var googleDriveId: String? = null


	fun getData(): BucketItemData? {
		val json = Json { ignoreUnknownKeys = true }
		return try {
			this.data?.let { json.decodeFromString<BucketItemData.ShowData?>(it) }
		} catch (e: Exception) {
//			e.printStackTrace()
			null
		}
	}

	fun getOpenGraphResult(): OpenGraphResult? {
		return try {
			val objectMapper = jsonMapper { addModule(kotlinModule()) }
			objectMapper.readValue(data, OpenGraphResult::class.java)
		} catch (e: Exception) {
//			e.printStackTrace()
			null
		}
	}

	fun putOpenGraphResult(openGraphResult: OpenGraphResult) {
		this.data = jsonMapper { addModule(kotlinModule()) }.writeValueAsString(openGraphResult)
	}

	fun getState(): Int = BucketItemState.values().find { it.name == this.state }?.ordinal ?: 0
	fun setState(stateInt : Int) {
		this.state = BucketItemState.values().getOrNull(stateInt)?.name ?: BucketItemState.ALPHA.name
	}

	fun clone(): BucketItemObject {
		return BucketItemObject().apply {
			this.id = this@BucketItemObject.id
			this.createdTimestamp = this@BucketItemObject.createdTimestamp
			this.modifiedTimestamp = this@BucketItemObject.modifiedTimestamp
			this.bucketType = this@BucketItemObject.bucketType
			this.title = this@BucketItemObject.title
			this.state = this@BucketItemObject.state
			this.thumbnail = this@BucketItemObject.thumbnail
			this.isFavourite = this@BucketItemObject.isFavourite
			this.isLocked = this@BucketItemObject.isLocked
			this.parentId = this@BucketItemObject.parentId
			this.key = this@BucketItemObject.key
			this.data = this@BucketItemObject.data
		}
	}

	fun toCloudSnapshot(): String {
		val jsonObject = JSONObject()
		jsonObject.put("id", this.id.toString())
		jsonObject.put("createdTimestamp", this.createdTimestamp)
		jsonObject.put("modifiedTimestamp", this.modifiedTimestamp)
		jsonObject.put("bucketType", this.bucketType)
		jsonObject.put("title", this.title)
		jsonObject.put("state", this.state)
		jsonObject.put("isFavourite", this.isFavourite)
		jsonObject.put("isLocked", this.isLocked)
		jsonObject.put("parentId", this.parentId?.toString())
		jsonObject.put("key", this.key)
		jsonObject.put("data", this.data)

		return jsonObject.toString()
	}

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + bucketType.hashCode()
		result = 31 * result + (title?.hashCode() ?: 0)
		result = 31 * result + state.hashCode()
		result = 31 * result + (thumbnail?.hashCode() ?: 0)
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isLocked.hashCode()
		result = 31 * result + (parentId?.hashCode() ?: 0)
		result = 31 * result + (key?.hashCode() ?: 0)
		result = 31 * result + (data?.hashCode() ?: 0)
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is BucketItemObject) return false

		if (id != other.id) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false
		if (bucketType != other.bucketType) return false
		if (title != other.title) return false
		if (state != other.state) return false
		if (thumbnail != other.thumbnail) return false
		if (isFavourite != other.isFavourite) return false
		if (isLocked != other.isLocked) return false
		if (parentId != other.parentId) return false
		if (key != other.key) return false
		if (data != other.data) return false

		return true
	}

	companion object {
		fun fromCloudSnapshot(snapshot: ByteArray): BucketItemObject? {
			return try {
				BucketItemObject(JSONObject(String(snapshot, Charsets.UTF_8)))
			} catch (e: Exception) {
				null
			}
		}

		@kotlinx.serialization.Serializable
		@Keep
		@JsonIgnoreProperties(ignoreUnknown = true)
		sealed class BucketItemData : Serializable {

			@kotlinx.serialization.Serializable
			@Keep
			@JsonIgnoreProperties(ignoreUnknown = true)
			data class BookData(
				@JsonProperty("key")
				@SerialName("key")
				var key: String? = null,
				@JsonProperty("title")
				@SerialName("title")
				var title: String? = null,
				@JsonProperty("cover_i")
				@SerialName("cover_i")
				var coverI: String? = null,    // Url for cover
				@JsonProperty("author_name")
				@SerialName("author_name")
				var authorList: List<String?>? = null,
				@JsonProperty("first_publish_year")
				@SerialName("first_publish_year")
				var firstPublishYear: String? = null,
				@JsonProperty("number_of_pages_median")
				@SerialName("number_of_pages_median")
				var numberOfPages: Int? = null,
				@JsonProperty("description")
				@SerialName("description")
				var description: String? = null,
			) : BucketItemData() {
				constructor(jsonString: String?) : this(null, null, null, null, null, null, null) {
					if (jsonString != null) {
						try {
							val bookData: BookData = Json.decodeFromString(jsonString)
							this.key = bookData.key
							this.title = bookData.title
							this.coverI = bookData.coverI
							this.authorList = bookData.authorList
							this.firstPublishYear = bookData.firstPublishYear
							this.numberOfPages = bookData.numberOfPages
							this.description = bookData.description
						} catch (e: Exception) {
//							e.printStackTrace()
						}
					}
				}

				fun toJsonString(): String {
					return try {
//						val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//						objectMapper.writeValueAsString(this)
						Json.encodeToString(this)
					} catch (e: Exception) {
			            e.printStackTrace()
						"null"
					}
				}

				override fun toString(): String {
					return "title : $title\nkey : $key\ndesc : $description"
				}

				override fun equals(other: Any?): Boolean {
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

				override fun hashCode(): Int {
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

			@kotlinx.serialization.Serializable
			@Keep
			data class ShowData(
				@JsonProperty("type")
				@SerialName("type")
				var type: ShowType? = null,
				@JsonProperty("tvData")
				@SerialName("tvData")
				var tvData: TvData? = null,
				@JsonProperty("movieData")
				@SerialName("movieData")
				var movieData: MovieData? = null,
			) : BucketItemData() {
				constructor(jsonString: String?) : this(null, null, null) {
					if (jsonString != null) {
						try {
							val showData: ShowData = Json.decodeFromString(jsonString)
							this.type = showData.type
							this.tvData = showData.tvData
							this.movieData = showData.movieData
						} catch (e: Exception) {

						}
					}
				}

				fun posterPath(): String? {
					return when (type) {
						ShowType.TV -> tvData?.posterPath
						ShowType.MOVIE -> movieData?.posterPath
						else -> null
					}
				}

				fun toJsonString(): String {
					return try {
						Json.encodeToString(this)
					} catch (e: Exception) {
//			            e.printStackTrace()
						"null"
					}
				}

				override fun hashCode(): Int {
					var result = type?.hashCode() ?: 0
					result = 31 * result + (tvData?.hashCode() ?: 0)
					result = 31 * result + (movieData?.hashCode() ?: 0)
					return result
				}

				override fun equals(other: Any?): Boolean {
					if (this === other) return true
					if (other !is ShowData) return false

					if (type != other.type) return false
					if (tvData != other.tvData) return false
					if (movieData != other.movieData) return false

					return true
				}
			}

			@Keep
			data class OpenGraphResult(
				var title: String? = null,
				var description: String? = null,
				var url: String? = null,
				var image: String? = null,
				var siteName: String? = null,
				var type: String? = null
			) : BucketItemData()
		}
	}
}
