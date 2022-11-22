package com.syncodec.graphite.di.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule


class AttachmentObject {
	var name : String = ""
	var extension : String? = null
	var mimeType : String? = null

	@JsonIgnore
	fun getTypeString() : String? {
		return mimeType?.split("/")?.getOrNull(0)
	}

	@JsonIgnore
	fun getSubTypeString() : String? {
		return mimeType?.split("/")?.getOrNull(1)
	}

	@JsonIgnore
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

	@JsonIgnore
	fun serialize() : String? {
		return try {
			val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			return objectMapper.writeValueAsString(this)
		} catch (e: Exception) {
			null
		}
	}

	override fun hashCode() : Int {
		var result = name.hashCode()
		result = 31 * result + (extension?.hashCode() ?: 0)
		result = 31 * result + (mimeType?.hashCode() ?: 0)
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is AttachmentObject) return false

		if (name != other.name) return false
		if (extension != other.extension) return false
		if (mimeType != other.mimeType) return false

		return true
	}

	@JsonIgnore
	fun clone() : AttachmentObject = AttachmentObject().apply {
		this.name = this@AttachmentObject.name
		this.extension = this@AttachmentObject.extension
		this.mimeType = this@AttachmentObject.mimeType
	}

	@JsonIgnore
	fun toSnapshot() = AttachmentSnapshot(
		name = name,
		extension = extension,
		mimeType = mimeType,
		data = null
	)

	companion object {
		enum class Type {
			IMAGE,
			VIDEO,
			AUDIO,
			UNKNOWN
		}

		fun deserialize(json : String) : AttachmentObject? {
			return try {
				val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				objectMapper.readValue(json, AttachmentObject::class.java)
			} catch (e : Exception) {
				null
			}
		}
	}
}

data class AttachmentSnapshot(
	val name : String,
	val extension : String?,
	val mimeType : String?,
	var data: ByteArray?
)
