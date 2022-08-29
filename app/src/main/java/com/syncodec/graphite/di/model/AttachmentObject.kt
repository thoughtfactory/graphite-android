package com.syncodec.graphite.di.model

import androidx.room.PrimaryKey
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmObject


class AttachmentObject: RealmObject {
	@PrimaryKey var id: ObjectId = ObjectId.create()

	var createdTimestamp: Long = System.currentTimeMillis()
	var name: String = ""
	var extension: String? = null
	var mimeType: String? = null
	var isSaved: Boolean = false
	var isFavourite: Boolean = false
	var isLocked: Boolean = false

	fun getTypeString(): String? {
		return mimeType?.split("/")?.getOrNull(0)
	}

	fun getSubTypeString(): String? {
		return mimeType?.split("/")?.getOrNull(1)
	}

	fun getType(): Type {
		return getTypeString()?.let {
			when(it) {
				"image" -> Type.IMAGE
				"video" -> Type.VIDEO
				"audio" -> Type.AUDIO
				else -> Type.UNKNOWN
			}
		} ?: Type.UNKNOWN
	}

	fun isRenderable(): Boolean {
		return getTypeString() == "image" || getTypeString() == "video" || getTypeString() == "audio" || getSubTypeString() == "pdf"
	}

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + name.hashCode()
		result = 31 * result + (extension?.hashCode() ?: 0)
		result = 31 * result + (mimeType?.hashCode() ?: 0)
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is AttachmentObject) return false

		if (id != other.id) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (name != other.name) return false
		if (extension != other.extension) return false
		if (mimeType != other.mimeType) return false

		return true
	}

	fun clone(): AttachmentObject = AttachmentObject().apply {
		this.id = this@AttachmentObject.id
		this.createdTimestamp = this@AttachmentObject.createdTimestamp
		this.name = this@AttachmentObject.name
		this.extension = this@AttachmentObject.extension
		this.mimeType = this@AttachmentObject.mimeType
		this.isSaved = this@AttachmentObject.isSaved
	}

	companion object {
		enum class Type {
			IMAGE,
			VIDEO,
			AUDIO,
			UNKNOWN
		}
	}
}


//@Entity
//data class AttachmentObject(
//	@Id var id: Long = 0,
//	@Unique var uId: String = generatePrimaryKey(),
//	var name: String = "",
//	var extension: String? = null,
//	var mimeType: String? = null,
//) {
//
//	lateinit var parentNote: ToOne<NoteObject>
//
//
//	override fun hashCode(): Int {
//		var result = id.hashCode()
//		result = 31 * result + name.hashCode()
//		result = 31 * result + (extension?.hashCode() ?: 0)
//		result = 31 * result + (mimeType?.hashCode() ?: 0)
//
//		return result
//	}
//
//	override fun equals(other: Any?): Boolean {
//		if (this === other) return true
//		if (other !is AttachmentObject) return false
//
//		if (id != other.id) return false
//		if (name != other.name) return false
//		if (extension != other.extension) return false
//		if (mimeType != other.mimeType) return false
//		return true
//	}
//}
