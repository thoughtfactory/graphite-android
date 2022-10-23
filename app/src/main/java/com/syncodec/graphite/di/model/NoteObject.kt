package com.syncodec.graphite.di.model

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.Base64


class NoteObject: RealmObject {
	@PrimaryKey var id: ObjectId = ObjectId.create()

	var createdTimestamp: Long = System.currentTimeMillis()
	var modifiedTimestamp: Long = System.currentTimeMillis()
	var userTimestamp: Long = System.currentTimeMillis()
	var title: String? = null
	var color: Int? = null
	var latLng: String? = null
	var address: String? = null
	var contentThumbnail: String? = null
	var content: String? = null
	var thumbnail: String? = null
	var thumbnailType: String? = null
	var isFavourite: Boolean = false
	var isLocked: Boolean = false

	var attachmentList: RealmList<AttachmentObject> = realmListOf()

	var parentChapterId: ObjectId? = null

	fun setLatLng(latLng : LatLng?) {
		val objectMapper = jsonMapper { addModule(kotlinModule()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) }
		this.latLng = objectMapper.writeValueAsString(latLng)
	}

	fun getLatLng(): LatLng? {
		try {
			if (latLng == null) return null
			val objectMapper = jsonMapper { addModule(kotlinModule()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) }
			return objectMapper.readValue(latLng, LatLng::class.java)
		} catch (e: Exception) {
			e.printStackTrace()
			return null
		}
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
		result = 31 * result + (contentThumbnail?.hashCode() ?: 0)
		result = 31 * result + (content?.hashCode() ?: 0)
		result = 31 * result + (thumbnail?.hashCode() ?: 0)
		result = 31 * result + (thumbnailType?.hashCode() ?: 0)
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isLocked.hashCode()
		result = 31 * result + attachmentList.hashCode()
		result = 31 * result + (parentChapterId?.hashCode() ?: 0)
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
		if (contentThumbnail != other.contentThumbnail) return false
		if (content != other.content) return false
		if (thumbnail != other.thumbnail) return false
		if (thumbnailType != other.thumbnailType) return false
		if (isFavourite != other.isFavourite) return false
		if (isLocked != other.isLocked) return false
		if (attachmentList != other.attachmentList) return false
		if (parentChapterId != other.parentChapterId) return false

		return true
	}

	fun toRaw(): NoteObjectRaw = NoteObjectRaw(
		id = id.toString(),
		createdTimestamp = createdTimestamp,
		modifiedTimestamp = modifiedTimestamp,
		userTimestamp = userTimestamp,
		title = title,
		color = color,
		latLng = getLatLng(),
		address = address,
		contentThumbnail = contentThumbnail,
		content = content,
		thumbnail = thumbnail,
		thumbnailType = thumbnailType,
		isFavourite = isFavourite,
		isLocked = isLocked,
		attachmentList = attachmentList.map { it.id },
		parentChapterId = parentChapterId?.toString()
	)


	fun clone(): NoteObject {
		return NoteObject().apply {
//			id = this@NoteObject.id

			createdTimestamp = this@NoteObject.createdTimestamp
			modifiedTimestamp = this@NoteObject.modifiedTimestamp
			userTimestamp = this@NoteObject.userTimestamp
			title = this@NoteObject.title
			color = this@NoteObject.color
			latLng = this@NoteObject.latLng
			address = this@NoteObject.address
			contentThumbnail = this@NoteObject.contentThumbnail
			content = this@NoteObject.content
			thumbnail = this@NoteObject.thumbnail
			thumbnailType = this@NoteObject.thumbnailType
			isFavourite = this@NoteObject.isFavourite
			isLocked = this@NoteObject.isLocked

			attachmentList = this@NoteObject.attachmentList

			parentChapterId = this@NoteObject.parentChapterId
		}
	}

	fun toLite(): NoteObjectLite {
		return NoteObjectLite(
			id = this.id,
			createdTimestamp = this.createdTimestamp,
			modifiedTimestamp = this.modifiedTimestamp,
			userTimestamp = this.userTimestamp,
			title = this.title,
			color = this.color,
			latLng = try { this.getLatLng() } catch (e: Exception) { null },
			address = this.address,
			contentThumbnail = this.contentThumbnail,
			thumbnail = this.thumbnail?.let { try { Base64.getDecoder().decode(it) } catch (e: Exception) { e.printStackTrace(); null } },
			thumbnailType = this.thumbnailType,
			attachmentCount = this.attachmentList.size,
			isFavourite = this.isFavourite,
			isLocked = this.isLocked
		)
	}


	companion object {
		fun getInstance(): NoteObject {
			return NoteObject().apply {
				this.createdTimestamp = System.currentTimeMillis()
				this.modifiedTimestamp = System.currentTimeMillis()
				this.userTimestamp = System.currentTimeMillis()

				this.title = "New Note"
				this.color = 0
				setLatLng(LatLng(0.0, 0.0))
				this.address = "New Address"
				this.contentThumbnail = "New Content Thumbnail"
				this.thumbnailType = "New Thumbnail Type"
				this.isFavourite = false
				this.isLocked = false
			}
		}
	}
}

data class NoteObjectRaw(
	val id: String,
	val createdTimestamp: Long,
	val modifiedTimestamp: Long,
	val userTimestamp: Long,
	val title: String?,
	val color: Int?,
	val latLng: LatLng?,
	val address: String?,
	val contentThumbnail: String?,
	val content: String?,
	val thumbnail: String?,
	val thumbnailType: String?,
	val isFavourite: Boolean,
	val isLocked: Boolean,
	val attachmentList: List<ObjectId>,
	val parentChapterId: String?
) {
	override fun hashCode() : Int {
		var result = id.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + userTimestamp.hashCode()
		result = 31 * result + (title?.hashCode() ?: 0)
		result = 31 * result + (color ?: 0)
		result = 31 * result + (latLng?.hashCode() ?: 0)
		result = 31 * result + (address?.hashCode() ?: 0)
		result = 31 * result + (contentThumbnail?.hashCode() ?: 0)
		result = 31 * result + (content?.hashCode() ?: 0)
		result = 31 * result + (thumbnail?.hashCode() ?: 0)
		result = 31 * result + (thumbnailType?.hashCode() ?: 0)
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isLocked.hashCode()
		result = 31 * result + attachmentList.hashCode()
		result = 31 * result + (parentChapterId?.hashCode() ?: 0)
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is NoteObjectRaw) return false

		if (id != other.id) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false
		if (userTimestamp != other.userTimestamp) return false
		if (title != other.title) return false
		if (color != other.color) return false
		if (latLng != other.latLng) return false
		if (address != other.address) return false
		if (contentThumbnail != other.contentThumbnail) return false
		if (content != other.content) return false
		if (thumbnail != other.thumbnail) return false
		if (thumbnailType != other.thumbnailType) return false
		if (isFavourite != other.isFavourite) return false
		if (isLocked != other.isLocked) return false
		if (attachmentList != other.attachmentList) return false
		if (parentChapterId != other.parentChapterId) return false

		return true
	}
}


data class NoteObjectLite(
	val id: ObjectId,
	val createdTimestamp: Long,
	val modifiedTimestamp: Long,
	val userTimestamp: Long,
	val title: String?,
	val color: Int?,
	val latLng: LatLng?,
	val address: String?,
	val contentThumbnail: String?,
	val thumbnail: ByteArray? = null,
	val thumbnailType: String? = null,
	val attachmentCount: Int,
	val isFavourite: Boolean,
	val isLocked: Boolean
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is NoteObjectLite) return false

		if (id != other.id) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false
		if (userTimestamp != other.userTimestamp) return false
		if (title != other.title) return false
		if (color != other.color) return false
		if (latLng != other.latLng) return false
		if (address != other.address) return false
		if (contentThumbnail != other.contentThumbnail) return false
		if (thumbnail != null) {
			if (other.thumbnail == null) return false
			if (!thumbnail.contentEquals(other.thumbnail)) return false
		} else if (other.thumbnail != null) return false
		if (thumbnailType != other.thumbnailType) return false
		if (isFavourite != other.isFavourite) return false
		if (isLocked != other.isLocked) return false

		return true
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
		result = 31 * result + (contentThumbnail?.hashCode() ?: 0)
		result = 31 * result + (thumbnail?.contentHashCode() ?: 0)
		result = 31 * result + (thumbnailType?.hashCode() ?: 0)
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isLocked.hashCode()
		return result
	}
}


data class LatLng(
	var latitude: Double? = null,
	var longitude: Double? = null
) {
	fun toGLatLng(): com.google.android.gms.maps.model.LatLng? {
		return if (latitude == null || longitude == null) null
		else com.google.android.gms.maps.model.LatLng(latitude!!, longitude!!)
	}
}
