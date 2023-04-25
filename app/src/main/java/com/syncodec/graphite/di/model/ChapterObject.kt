package com.syncodec.graphite.di.model

import androidx.annotation.Keep
import androidx.compose.ui.graphics.toArgb
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.syncodec.graphite.utils.getRandomColor
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey
import org.json.JSONObject
import kotlin.random.Random


@Keep
@JsonIgnoreProperties(value = ["io_realm_kotlin_objectReference"], ignoreUnknown = true)
class ChapterObject() : RealmObject {
	constructor(jsonObject : JSONObject) : this() {
		this.id = jsonObject.optString("id").let { if (it.isNullOrEmpty() || it == "null") RealmUUID.random() else RealmUUID.from(it) }
		this.createdTimestamp = jsonObject.optLong("createdTimestamp", System.currentTimeMillis())
		this.modifiedTimestamp = jsonObject.optLong("modifiedTimestamp", System.currentTimeMillis())
		this.title = jsonObject.optString("title").let { if (it.isNullOrEmpty() || it == "null") null else it }
		this.description = jsonObject.optString("description").let { if (it.isNullOrEmpty() || it == "null") null else it }
		this.color = jsonObject.optInt("color")
		this.thumbnail = jsonObject.optString("thumbnail")
		if (this.thumbnail == "null") {
			this.thumbnail = null
			if (this.color == null || this.color == 0) this.color = getRandomColor().toArgb()
		}
		this.isFavourite = jsonObject.optBoolean("isFavourite", false)
		this.isLocked = jsonObject.optBoolean("isLocked", false)
		this.parentId = jsonObject.optString("parentId").let { if (it.isNullOrEmpty() || it == "null") null else RealmUUID.from(it) }
	}

	@PrimaryKey
	var id : RealmUUID = RealmUUID.random()

	var createdTimestamp : Long = System.currentTimeMillis()
	var modifiedTimestamp : Long = System.currentTimeMillis()
	var title : String? = null
	var description : String? = null
	var color : Int? = getRandomColor().toArgb()
	var thumbnail : String? = null
	var isFavourite : Boolean = false
	var isLocked : Boolean = false

	var parentId : RealmUUID? = null

	fun toLite() : ChapterObjectLite {
		return ChapterObjectLite(
			id = this.id,
			createdTimestamp = this.createdTimestamp,
			modifiedTimestamp = this.modifiedTimestamp,
			title = this.title,
			description = this.description,
			color = this.color,
			isFavourite = this.isFavourite,
			isLocked = this.isLocked,
			parentId = this.parentId
		)
	}

	fun clone() = ChapterObject().apply {
		this.id = this@ChapterObject.id
		this.createdTimestamp = this@ChapterObject.createdTimestamp
		this.modifiedTimestamp = this@ChapterObject.modifiedTimestamp
		this.title = this@ChapterObject.title
		this.description = this@ChapterObject.description
		this.color = this@ChapterObject.color
		this.thumbnail = this@ChapterObject.thumbnail
		this.isFavourite = this@ChapterObject.isFavourite
		this.isLocked = this@ChapterObject.isLocked
		this.parentId = this@ChapterObject.parentId
	}

	fun toCloudSnapshot() : String {
		val jsonObject = JSONObject()
		jsonObject.put("id", this.id.toString())
		jsonObject.put("createdTimestamp", this.createdTimestamp)
		jsonObject.put("modifiedTimestamp", this.modifiedTimestamp)
		jsonObject.put("title", this.title)
		jsonObject.put("description", this.description)
		jsonObject.put("color", this.color)
		jsonObject.put("thumbnail", this.thumbnail)
		jsonObject.put("isFavourite", this.isFavourite)
		jsonObject.put("isLocked", this.isLocked)
		jsonObject.put("parentId", this.parentId?.toString())

		return jsonObject.toString()
	}

	override fun hashCode() : Int {
		var result = id.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + title.hashCode()
		result = 31 * result + (description?.hashCode() ?: 0)
		result = 31 * result + (color ?: 0)
		result = 31 * result + (thumbnail?.hashCode() ?: 0)
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isLocked.hashCode()
		result = 31 * result + (parentId?.hashCode() ?: 0)
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is ChapterObject) return false

		if (id != other.id) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false
		if (title != other.title) return false
		if (description != other.description) return false
		if (color != other.color) return false
		if (isFavourite != other.isFavourite) return false
		if (isLocked != other.isLocked) return false
		if (parentId != other.parentId) return false

		return true
	}

	companion object {
		fun fromCloudSnapshot(snapshot : ByteArray) : ChapterObject? {
			return try {
				ChapterObject(JSONObject(String(snapshot, Charsets.UTF_8)))
			} catch (e : Exception) {
				null
			}
		}
	}
}

@Keep
data class ChapterObjectLite(
	val id : RealmUUID,
	val createdTimestamp : Long,
	val modifiedTimestamp : Long,
	val title : String?,
	val description : String?,
	val color : Int?,
	val isFavourite : Boolean,
	val isLocked : Boolean,
	val parentId : RealmUUID?,
) {
	override fun hashCode() : Int {
		var result = id.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + title.hashCode()
		result = 31 * result + (description?.hashCode() ?: 0)
		result = 31 * result + (color ?: 0)
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isLocked.hashCode()
		result = 31 * result + (parentId?.hashCode() ?: 0)
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is ChapterObjectLite) return false

		if (id != other.id) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false
		if (title != other.title) return false
		if (description != other.description) return false
		if (color != other.color) return false
		if (isFavourite != other.isFavourite) return false
		if (isLocked != other.isLocked) return false
		if (parentId != other.parentId) return false

		return true
	}

	companion object {
		fun getRandomInstance() : ChapterObjectLite {
			return ChapterObjectLite(
				id = RealmUUID.random(),
				createdTimestamp = System.currentTimeMillis() + Random.nextLong(),
				modifiedTimestamp = System.currentTimeMillis() + Random.nextLong(),
				title = RealmUUID.random().toString(),
				description = RealmUUID.random().toString(),
				color = getRandomColor().toArgb(),
				isFavourite = Random.nextBoolean(),
				isLocked = Random.nextBoolean(),
				parentId = RealmUUID.random(),
			)
		}
	}
}
