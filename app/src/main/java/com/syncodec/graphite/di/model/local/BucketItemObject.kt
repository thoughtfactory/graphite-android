package com.syncodec.graphite.di.model.local

import androidx.annotation.Keep
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.cloud.dropbox.DropboxObjectMetadata
import com.syncodec.graphite.di.model.local.ext.Syncable
import com.syncodec.graphite.utils.toDbxHashString
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.time.Instant


enum class BucketItemState {
	ALPHA,
	BETA,
	GAMMA
}

@Keep
class BucketItemObject() : RealmObject, Syncable {

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
	var bucketItemDataJson: String? = null

	inline fun <reified T : BucketItemData> getBucketItemData(): T? {
		val json = Json { ignoreUnknownKeys = true }
		return try {
			this.bucketItemDataJson?.let { json.decodeFromString<T>(it) }
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			null
		}
	}

	fun setBucketItemData(bucketItemData: BucketItemData?) {
		val json = Json { ignoreUnknownKeys = true }
		this.bucketItemDataJson = try {
			json.encodeToString(bucketItemData)
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
			null
		}
	}

	fun updateModifyTimestamp() {
		this.modifiedTimestamp = Instant.now().toEpochMilli()
	}

	fun getState(): Int = BucketItemState.entries.find { it.name == this.state }?.ordinal ?: 0

	fun setState(stateInt: Int) {
		this.state = BucketItemState.entries.getOrNull(stateInt)?.name ?: BucketItemState.ALPHA.name
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
			this.bucketItemDataJson = this@BucketItemObject.bucketItemDataJson
		}
	}

	constructor(byteArray: ByteArray) : this() {

		val jsonObject = JSONObject(byteArray.decodeToString())

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
		this.bucketItemDataJson = jsonObject.optString("bucketItemDataJson").let { if (it.isNullOrEmpty() || it == "null") null else it }
	}

	override fun toCloudSnapshot(): String {
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
		jsonObject.put("bucketItemDataJson", this.bucketItemDataJson)

		return jsonObject.toString()
	}

	override fun toObjectMetadata(): DropboxObjectMetadata = DropboxObjectMetadata(
		modifiedTimestamp = this.modifiedTimestamp,
		hash = this.toCloudSnapshot().toDbxHashString(),
		isDeleted = false,
	)

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
		result = 31 * result + (bucketItemDataJson?.hashCode() ?: 0)
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as BucketItemObject

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
		if (bucketItemDataJson != other.bucketItemDataJson) return false

		return true
	}

}
