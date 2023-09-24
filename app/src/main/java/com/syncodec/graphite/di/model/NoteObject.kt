package com.syncodec.graphite.di.model

import android.graphics.Color
import androidx.annotation.Keep
import androidx.compose.ui.graphics.toArgb
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.syncodec.graphite.di.model.serializer.LatLngSerializer
import com.syncodec.graphite.di.model.serializer.RealmUUIDNullableSerializer
import com.syncodec.graphite.di.model.serializer.RealmUUIDSerializer
import com.syncodec.graphite.di.repository.cache.NoteCache
import com.syncodec.graphite.utils.getRandomColor
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.File
import java.time.Instant
import kotlin.random.Random


@Keep
@JsonIgnoreProperties(value = ["io_realm_kotlin_objectReference"], ignoreUnknown = true)
@Serializable
class NoteObject() : RealmObject {
	constructor(jsonObject: JSONObject) : this() {
		this.id = jsonObject.optString("id").let { if (it.isNullOrEmpty() || it == "null") RealmUUID.random() else RealmUUID.from(it) }
		this.createdTimestamp = jsonObject.optLong("createdTimestamp", Instant.now().toEpochMilli())
		this.modifiedTimestamp = jsonObject.optLong("modifiedTimestamp", Instant.now().toEpochMilli())
		this.userTimestamp = jsonObject.optLong("userTimestamp", Instant.now().toEpochMilli())
		this.title = jsonObject.optString("title").let { if (it.isNullOrEmpty() || it == "null") null else it }
		this.color = jsonObject.optInt("color", getRandomColor().toArgb())
		val latLngObject = jsonObject.optJSONObject("latLng")
		if (latLngObject != null) {
			val lat = latLngObject.optDouble("latitude")
			val lng = latLngObject.optDouble("longitude")
			this.setLatLng(LatLng(lat, lng))
		}
		this.address = jsonObject.optString("address").let { if (it.isNullOrEmpty() || it == "null") null else it }
		this.content = jsonObject.optString("content").let { if (it.isNullOrEmpty() || it == "null") null else it }
		this.content2 = jsonObject.optString("content2").let { if (it.isNullOrEmpty() || it == "null") null else it }
		this.isFavourite = jsonObject.optBoolean("isFavourite", false)
		this.isLocked = jsonObject.optBoolean("isLocked", false)
		this.parentId = jsonObject.optString("parentId").let { if (it.isNullOrEmpty() || it == "null") null else RealmUUID.from(it) }
	}

	@Serializable(with = RealmUUIDSerializer::class)
	@PrimaryKey
	var id: RealmUUID = RealmUUID.random()

	var createdTimestamp: Long = Instant.now().toEpochMilli()
	var modifiedTimestamp: Long = Instant.now().toEpochMilli()
	var userTimestamp: Long = Instant.now().toEpochMilli()
	var title: String? = null
	var color: Int? = null
	var latLng: String? = null
	var address: String? = null
	var content: String? = null
	var content2 : String? = null
	var isFavourite: Boolean = false
	var isLocked: Boolean = false

	@Serializable(with = RealmUUIDNullableSerializer::class)
	var parentId: RealmUUID? = null

	fun updateModifyTimestamp() {
		this.modifiedTimestamp = Instant.now().toEpochMilli()
	}

	fun setLatLng(latLng: LatLng?) {
		try {
			latLng?.let {
				val latitude = it.latitude
				val longitude = it.longitude
				if ((latitude >= -90) && (latitude <= 90) && (longitude >= -180) && (longitude <= 180)) {
					val objectMapper = jsonMapper { addModule(kotlinModule()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) }
					this.latLng = objectMapper.writeValueAsString(it)
				} else this.latLng = null
			}
		} catch (e: Exception) {
			this.latLng = null
		}
	}

	fun getLatLng(): LatLng? {
		try {
			if (latLng == null) return null
			val objectMapper = jsonMapper { addModule(kotlinModule()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) }
			return objectMapper.readValue(latLng, LatLng::class.java)
		} catch (e: Exception) {
//			e.printStackTrace()
			return null
		}
	}

	fun getContentString(): String {
		val json = Json { ignoreUnknownKeys = true }
		val kitKatContent = json.decodeFromString<KitKatContent>(content ?: "")
		return kitKatContent.toTxt()
	}

	override fun toString(): String = this.id.toString()

	fun toLite(): NoteObjectLite {
		return NoteObjectLite(
			id = this.id,
			parentId = this.parentId,
			createdTimestamp = this.createdTimestamp,
			modifiedTimestamp = this.modifiedTimestamp,
			userTimestamp = this.userTimestamp,
			title = this.title,
			color = this.color,
			latLng = try {
				this.getLatLng()
			} catch (e: Exception) {
				null
			},
			address = this.address,
			contentThumbnail = NoteCache.getOrSetCache(noteObject = this),
			isFavourite = this.isFavourite,
			isLocked = this.isLocked,
		)
	}

	fun clone(): NoteObject = NoteObject().apply {
		this.id = this@NoteObject.id
		this.createdTimestamp = this@NoteObject.createdTimestamp
		this.modifiedTimestamp = this@NoteObject.modifiedTimestamp
		this.userTimestamp = this@NoteObject.userTimestamp
		this.title = this@NoteObject.title
		this.color = this@NoteObject.color
		this.latLng = this@NoteObject.latLng
		this.address = this@NoteObject.address
		this.content = this@NoteObject.content
		this.content2 = this@NoteObject.content2
		this.isFavourite = this@NoteObject.isFavourite
		this.isLocked = this@NoteObject.isLocked
		this.parentId = this@NoteObject.parentId
	}

	fun toCloudSnapshot(): String {
		val jsonObject = JSONObject()
		jsonObject.put("id", this.id.toString())
		jsonObject.put("createdTimestamp", this.createdTimestamp)
		jsonObject.put("modifiedTimestamp", this.modifiedTimestamp)
		jsonObject.put("userTimestamp", this.userTimestamp)
		jsonObject.put("title", this.title)
		jsonObject.put("latLng", this.latLng)
		jsonObject.put("address", this.address)
		jsonObject.put("content", this.content)
		jsonObject.put("isFavourite", this.isFavourite)
		jsonObject.put("isLocked", this.isLocked)
		jsonObject.put("parentId", this.parentId?.toString())

		return jsonObject.toString()
	}

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + userTimestamp.hashCode()
		result = 31 * result + (title?.hashCode() ?: 0)
		result = 31 * result + (color ?: 0)
		result = 31 * result + (latLng?.hashCode() ?: 0)
		result = 31 * result + (address?.hashCode() ?: 0)
		result = 31 * result + (content?.hashCode() ?: 0)
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isLocked.hashCode()
		result = 31 * result + parentId.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is NoteObject) return false

		if (id != other.id) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false
		if (userTimestamp != other.userTimestamp) return false
		if (title != other.title) return false
		if (color != other.color) return false
		if (latLng != other.latLng) return false
		if (address != other.address) return false
		if (content != other.content) return false
		if (isFavourite != other.isFavourite) return false
		if (isLocked != other.isLocked) return false
		if (parentId != other.parentId) return false

		return true
	}


	companion object {
		fun fromCloudSnapshot(snapshot: ByteArray): NoteObject? {
			return try {
				val json = Json { ignoreUnknownKeys = true }
				val jsonObject = JSONObject(String(snapshot))
				val kitKatContent = json.decodeFromString<KitKatContent>(jsonObject.getString("content")).toString()
				jsonObject.put("contentThumbnail", kitKatContent.substring(0, minOf(256, kitKatContent.length)))
				NoteObject(jsonObject)
			} catch (e: Exception) {
				null
			}
		}

		fun getRandomInstance(): NoteObject {
			return NoteObject().apply {
				this.createdTimestamp = Instant.now().toEpochMilli()
				this.modifiedTimestamp = Instant.now().toEpochMilli()
				this.userTimestamp = Instant.now().toEpochMilli()

				this.title = "Random Title ${Random.nextInt()}"
				this.color = Color.argb(255, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
				setLatLng(LatLng(0.0, 0.0))
				this.address = "Random Address ${Random.nextInt()}"
				this.isFavourite = Random.nextBoolean()
				this.isLocked = Random.nextBoolean()
				this.parentId = RealmUUID.random()
			}
		}
	}
}

@Keep
data class NoteObjectLite(
	val id: RealmUUID,
	val parentId: RealmUUID?,
	val createdTimestamp: Long,
	val modifiedTimestamp: Long,
	val userTimestamp: Long,
	val title: String?,
	val color: Int?,
	val latLng: LatLng?,
	val address: String?,
	val contentThumbnail: String?,
	val isFavourite: Boolean,
	val isLocked: Boolean,
	val tagList: List<TagObjectLite> = listOf(),
	val attachmentList: List<File> = listOf(),
	val overWritable: Boolean = true,
	val deletable: Boolean = true,
	val localOnly: Boolean = false
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is NoteObjectLite) return false

		if (id != other.id) return false
		if (parentId != other.parentId) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false
		if (userTimestamp != other.userTimestamp) return false
		if (title != other.title) return false
		if (color != other.color) return false
		if (latLng != other.latLng) return false
		if (address != other.address) return false
		if (contentThumbnail != other.contentThumbnail) return false
		if (isFavourite != other.isFavourite) return false
		if (isLocked != other.isLocked) return false
		if (overWritable != other.overWritable) return false
		if (deletable != other.deletable) return false
		if (localOnly != other.localOnly) return false

		return true
	}

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + (parentId?.hashCode() ?: 0)
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + userTimestamp.hashCode()
		result = 31 * result + (title?.hashCode() ?: 0)
		result = 31 * result + (color ?: 0)
		result = 31 * result + (latLng?.hashCode() ?: 0)
		result = 31 * result + (address?.hashCode() ?: 0)
		result = 31 * result + (contentThumbnail?.hashCode() ?: 0)
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isLocked.hashCode()
		result = 31 * result + overWritable.hashCode()
		result = 31 * result + deletable.hashCode()
		result = 31 * result + localOnly.hashCode()
		return result
	}
}

@Keep
@Serializable(with = LatLngSerializer::class)
data class LatLng(
	var latitude: Double,
	var longitude: Double
) {
	fun toGLatLng(): com.google.android.gms.maps.model.LatLng {
		return com.google.android.gms.maps.model.LatLng(latitude, longitude)
	}

	override fun toString(): String = "Lat : $latitude, Lng : $longitude"
}
