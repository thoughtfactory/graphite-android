package com.syncodec.graphite.di.model.local

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Deprecated("Use html instead")
typealias KitKatContent = Content

@Deprecated("Use html instead")
@Keep
@Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
data class Content(
	val type : String? = null,
	val attrs : Attrs? = null,
	val marks : List<Mark>? = null,
	val content : List<Content>? = null,
	val text : String? = null,
) {
	fun toJsonString() : String? {
		val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		return objectMapper.writeValueAsString(this)
	}

	override fun toString() : String {
		return content?.let {
			it.joinToString(separator = "") { it.toString() }
		} ?: text ?: ""
	}

	fun toTxt() : String {
		// new line when type is paragraph or heading or blockquote or hardBreak
		val newLine = when (type) {
			"paragraph", "heading", "blockquote", "hardBreak" -> "\n"
			else -> ""
		}
		content?.let {
			return it.joinToString(separator = "") { it.toTxt() } + newLine
		} ?: return text ?: ""
	}

	fun isContentEmpty() : Boolean {
		if (content.isNullOrEmpty()) {
			return text.isNullOrEmpty()
		} else {
			content.forEach { if (!it.isContentEmpty()) return false }
			return true
		}
	}

	override fun hashCode(): Int {
		var result = type?.hashCode() ?: 0
		result = 31 * result + (attrs?.hashCode() ?: 0)
		result = 31 * result + (marks?.hashCode() ?: 0)
		result = 31 * result + (content?.hashCode() ?: 0)
		result = 31 * result + (text?.hashCode() ?: 0)
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Content

		if (type != other.type) return false
		if (attrs != other.attrs) return false
		if (marks != other.marks) return false
		if (content != other.content) return false
		return text == other.text
	}

	companion object {
		fun fromString(jsonString: String?) : KitKatContent?{
			return try {
				jsonString?.let { Json.decodeFromString<KitKatContent>(it) }
			}catch (_ : Exception) {
				null
			}
		}
	}
}

@Keep
@Serializable
data class Attrs(
	val textAlign : String? = null,
	val checked : Boolean? = null,
	val start : Int? = null,
) {
	override fun hashCode(): Int {
		var result = textAlign?.hashCode() ?: 0
		result = 31 * result + (checked?.hashCode() ?: 0)
		result = 31 * result + (start ?: 0)
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Attrs

		if (textAlign != other.textAlign) return false
		if (checked != other.checked) return false
		return start == other.start
	}

	companion object {
		enum class TextAlignType(val value : String) {
			Left("left"),
			Center("center"),
			Right("right"),
			Justify("justify"),
		}

		enum class CheckedType(val value : Boolean) {
			True(true),
			False(false),
		}
	}
}

@Keep
@Serializable
data class Mark(
	val type : String? = null,
) {
	override fun hashCode(): Int {
		return type?.hashCode() ?: 0
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Mark

		return type == other.type
	}
}
