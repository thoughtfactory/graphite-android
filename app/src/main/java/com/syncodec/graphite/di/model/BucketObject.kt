package com.syncodec.graphite.di.model

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.syncodec.graphite.R
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey
import org.json.JSONObject
import java.time.Instant


enum class BucketType {
	TODO,
	BOOK,
	SHOW,
	LINK,
	UNKNOWN
}

val bucketTypeIconMap = mapOf(
	BucketType.TODO to R.drawable.ic_fa_bucket_todo,
	BucketType.BOOK to R.drawable.ic_fa_bucket_book,
	BucketType.SHOW to R.drawable.ic_fa_bucket_show,
	BucketType.LINK to R.drawable.ic_fa_bucket_link,
	BucketType.UNKNOWN to R.drawable.ic_fa_question,
)

@Keep
@JsonIgnoreProperties(value = ["io_realm_kotlin_objectReference"], ignoreUnknown = true)
class BucketObject() : RealmObject {
	constructor(jsonObject : JSONObject) : this() {
		this.id = jsonObject.optString("id").let { if (it.isNullOrEmpty() || it == "null") RealmUUID.random() else RealmUUID.from(it) }
		this.createdTimestamp = jsonObject.getLong("createdTimestamp")
		this.modifiedTimestamp = jsonObject.getLong("modifiedTimestamp")
		this.title = jsonObject.optString("title").let { if (it.isNullOrEmpty() || it == "null") null else it }
		this.description = jsonObject.optString("description").let { if (it.isNullOrEmpty() || it == "null") null else it }
		this.bucketType = jsonObject.optString("bucketType").let { if (it.isNullOrEmpty() || it == "null") BucketType.UNKNOWN.name else it }
		this.isFavourite = jsonObject.optBoolean("isFavourite", false)
		this.isLocked = jsonObject.optBoolean("isLocked", false)
		this.bucketItemOrderList = jsonObject.optJSONArray("bucketItemOrderList")?.let { jsonArray ->
			val realmList = realmListOf<RealmUUID>()
			for (i in 0 until jsonArray.length()) {
				realmList.add(RealmUUID.from(jsonArray.getString(i)))
			}
			realmList
		} ?: realmListOf()
	}

	@PrimaryKey
	var id : RealmUUID = RealmUUID.random()

	var createdTimestamp : Long = Instant.now().toEpochMilli()
	var modifiedTimestamp : Long = Instant.now().toEpochMilli()
	var title : String? = null
	var description : String? = null
	var bucketType : String = BucketType.UNKNOWN.name
	var isFavourite : Boolean = false
	var isLocked : Boolean = false

	var bucketItemOrderList : RealmList<RealmUUID> = realmListOf()

	var googleDriveId : String? = null

	fun clone() : BucketObject = BucketObject().apply {
		this.id = this@BucketObject.id
		this.createdTimestamp = this@BucketObject.createdTimestamp
		this.modifiedTimestamp = this@BucketObject.modifiedTimestamp
		this.title = this@BucketObject.title
		this.description = this@BucketObject.description
		this.bucketType = this@BucketObject.bucketType
		this.isFavourite = this@BucketObject.isFavourite
		this.isLocked = this@BucketObject.isLocked
		this.bucketItemOrderList = this@BucketObject.bucketItemOrderList.toRealmList()
	}

	fun toCloudSnapshot() : String {
		val jsonObject = JSONObject()
		jsonObject.put("id", this.id.toString())
		jsonObject.put("createdTimestamp", this.createdTimestamp)
		jsonObject.put("modifiedTimestamp", this.modifiedTimestamp)
		jsonObject.put("title", this.title)
		jsonObject.put("description", this.description)
		jsonObject.put("bucketType", this.bucketType)
		jsonObject.put("isFavourite", this.isFavourite)
		jsonObject.put("isLocked", this.isLocked)
		jsonObject.put("bucketItemOrderList", this.bucketItemOrderList.map { it.toString() })

		return jsonObject.toString()
	}

	override fun hashCode() : Int {
		var result = id.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + (title?.hashCode() ?: 0)
		result = 31 * result + (description?.hashCode() ?: 0)
		result = 31 * result + bucketType.hashCode()
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isLocked.hashCode()
		result = 31 * result + bucketItemOrderList.hashCode()
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is BucketObject) return false

		if (id != other.id) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false
		if (title != other.title) return false
		if (description != other.description) return false
		if (bucketType != other.bucketType) return false
		if (isFavourite != other.isFavourite) return false
		if (isLocked != other.isLocked) return false
		if (bucketItemOrderList != other.bucketItemOrderList) return false

		return true
	}

	companion object {
		fun fromCloudSnapshot(snapshot : ByteArray) : BucketObject? {
			return try {
				BucketObject(JSONObject(String(snapshot, Charsets.UTF_8)))
			} catch (e : Exception) {
				null
			}
		}
	}
}
