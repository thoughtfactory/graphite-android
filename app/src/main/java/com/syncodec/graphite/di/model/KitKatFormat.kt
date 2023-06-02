package com.syncodec.graphite.di.model

import androidx.annotation.Keep
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import kotlinx.serialization.Serializable

typealias KitKatContent = Content
@Keep
@Serializable
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
}

@Keep
@Serializable
data class Attrs(
	val textAlign : String? = null,
	val checked : Boolean? = null,
	val start : Int? = null,
) {
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
)
