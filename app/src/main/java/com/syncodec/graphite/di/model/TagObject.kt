package com.syncodec.graphite.di.model

import androidx.annotation.Keep
import androidx.compose.ui.graphics.toArgb
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.syncodec.graphite.utils.getRandomColor
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey
import org.json.JSONObject


@Keep
@JsonIgnoreProperties(value = ["io_realm_kotlin_objectReference"], ignoreUnknown = true)
class TagObject() : RealmObject {
	constructor(jsonObject : JSONObject) : this() {
		this.id = jsonObject.optString("id").let { if (it.isNullOrEmpty() || it == "null") RealmUUID.random() else RealmUUID.from(it) }
		this.tag = jsonObject.optString("tag").let { if (it.isNullOrEmpty() || it == "null") this.id.toString() else it }
		this.color = jsonObject.optInt("color").let { if (it == 0) getRandomColor().toArgb() else it }
		this.modifiedTimestamp = jsonObject.optLong("userTimestamp", System.currentTimeMillis())
		jsonObject.optJSONArray("objectIdList")?.let { jsonArray ->
			for (i in 0 until jsonArray.length()) {
				try {
					RealmUUID.from(jsonArray.optString(i)).let { realmUUID ->
						this.objectIdList.add(realmUUID)
					}
				} catch (e : Exception) {
					e.printStackTrace()
				}
			}
		}
	}

	@PrimaryKey
	var id : RealmUUID = RealmUUID.random()

	var tag : String = ""
	var color : Int = getRandomColor().toArgb()

	var modifiedTimestamp : Long = System.currentTimeMillis()

	var objectIdList : RealmList<RealmUUID> = realmListOf()

	fun toLite() : TagObjectLite {
		return TagObjectLite(
			id = id,
			tag = tag,
			color = color
		)
	}

	fun clone() : TagObject = TagObject().apply {
		this.id = this@TagObject.id
		this.tag = this@TagObject.tag
		this.color = this@TagObject.color
		this.objectIdList = this@TagObject.objectIdList.toRealmList()
	}

	fun toCloudSnapshot() : String {
		val jsonObject = JSONObject()

		jsonObject.put("id", id.toString())
		jsonObject.put("tag", tag)
		jsonObject.put("color", color)
		jsonObject.put("modifiedTimestamp", modifiedTimestamp)
		objectIdList.map { it.toString() }.let { jsonObject.put("objectIdList", it) }

		return jsonObject.toString()
	}

	override fun hashCode() : Int {
		var result = id.hashCode()
		result = 31 * result + tag.hashCode()
		result = 31 * result + color
		result = 31 * result + objectIdList.hashCode()
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is TagObject) return false

		if (id != other.id) return false
		if (tag != other.tag) return false
		if (color != other.color) return false
		if (objectIdList != other.objectIdList) return false

		return true
	}

	companion object {
		fun fromCloudSnapshot(snapshot : ByteArray) : TagObject? {
			return try {
				TagObject(JSONObject(String(snapshot, Charsets.UTF_8)))
			} catch (e : Exception) {
				null
			}
		}
		fun getRandomInstance() = TagObject().apply {
			tag = "Tag ${System.currentTimeMillis()}"
			color = getRandomColor().toArgb()
		}
	}
}

@Keep
data class TagObjectLite(
	val id : RealmUUID,
	val tag : String,
	val color : Int
)
