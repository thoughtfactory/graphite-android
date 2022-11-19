package com.syncodec.graphite.di.model

import androidx.room.PrimaryKey
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID


class AttachmentObject : RealmObject {
	@PrimaryKey var id: RealmUUID = RealmUUID.random()

	var createdTimestamp : Long = System.currentTimeMillis()
	var name : String = ""
	var extension : String? = null
	var mimeType : String? = null
	var isSaved : Boolean = false
	var isFavourite : Boolean = false
	var isLocked : Boolean = false

	var parentNoteId : RealmUUID? = null

	fun getTypeString() : String? {
		return mimeType?.split("/")?.getOrNull(0)
	}

	fun getSubTypeString() : String? {
		return mimeType?.split("/")?.getOrNull(1)
	}

	fun getType() : Type {
		return getTypeString()?.let {
			when (it) {
				"image" -> Type.IMAGE
				"video" -> Type.VIDEO
				"audio" -> Type.AUDIO
				else -> Type.UNKNOWN
			}
		} ?: Type.UNKNOWN
	}

	fun isRenderable() : Boolean {
		return getTypeString() == "image" || getTypeString() == "video" || getTypeString() == "audio" || getSubTypeString() == "pdf"
	}

	override fun hashCode() : Int {
		var result = id.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + name.hashCode()
		result = 31 * result + (extension?.hashCode() ?: 0)
		result = 31 * result + (mimeType?.hashCode() ?: 0)
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is AttachmentObject) return false

		if (id != other.id) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (name != other.name) return false
		if (extension != other.extension) return false
		if (mimeType != other.mimeType) return false

		return true
	}

	fun clone() : AttachmentObject = AttachmentObject().apply {
		this.id = this@AttachmentObject.id
		this.createdTimestamp = this@AttachmentObject.createdTimestamp
		this.name = this@AttachmentObject.name
		this.extension = this@AttachmentObject.extension
		this.mimeType = this@AttachmentObject.mimeType
		this.isSaved = this@AttachmentObject.isSaved
		this.parentNoteId = this@AttachmentObject.parentNoteId
	}

	fun toSnapshot() = AttachmentSnapshot(
		id = id,
		createdTimestamp = createdTimestamp,
		name = name,
		extension = extension,
		mimeType = mimeType,
		isSaved = isSaved,
		isFavourite = isFavourite,
		isLocked = isLocked,
		parentNoteId = parentNoteId,
		data = null
	)

	companion object {
		enum class Type {
			IMAGE,
			VIDEO,
			AUDIO,
			UNKNOWN
		}
	}
}

data class AttachmentSnapshot(
	val id : RealmUUID,
	val createdTimestamp : Long,
	val name : String,
	val extension : String?,
	val mimeType : String?,
	val isSaved : Boolean,
	val isFavourite : Boolean,
	val isLocked : Boolean,
	val parentNoteId : RealmUUID?,
	var data: ByteArray?
)
