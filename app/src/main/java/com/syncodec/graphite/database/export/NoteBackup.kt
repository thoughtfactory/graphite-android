package com.syncodec.graphite.database.export

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.Keep
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.google.android.gms.maps.model.LatLng
import com.syncodec.graphite.miscellaneous.StringUtils.Companion.decrypt
import com.syncodec.graphite.miscellaneous.StringUtils.Companion.encrypt
import com.syncodec.graphite.miscellaneous.base64stringToBitmap
import com.syncodec.graphite.miscellaneous.bitmapToBase64String
import java.io.IOException


@JsonSerialize(using = NoteBackupSerializer::class)
@JsonDeserialize(using = NoteBackupDeserializer::class)
@Keep
data class NoteBackup(
	val key: String,
	val timezone: String,
	val chapterPath: List<String>,
	val notebookKey: String,
	val createdTimestamp: Long,
	val modifiedTimestamp: Long,
	val userTimestamp: Long,
	val title: String?,
	val attachmentKeyList: List<String>,
	val latLng: LatLng?,
	val address: String?,
	val mood: Int,
	val isFavourite: Boolean,
	val isArchived: Boolean,
	val isLocked: Boolean,
	val contentThumbnail: String?,
	val content: String?,
	val attachmentThumbnail: Bitmap?,
	val version: Int = 1
)

private class NoteBackupSerializer @JvmOverloads constructor(t: Class<NoteBackup?>? = null) :
	StdSerializer<NoteBackup>(t) {
	@Throws(IOException::class, JsonProcessingException::class)
	override fun serialize(
		value: NoteBackup, gen: JsonGenerator, provider: SerializerProvider
	) {
		gen.writeStartObject()
		gen.writeStringField("key", value.key)
		gen.writeStringField("timezone", value.timezone)
		gen.writeObjectField("chapterPath", value.chapterPath)
		gen.writeStringField("notebookKey", value.notebookKey)
		gen.writeNumberField("createdTimestamp", value.createdTimestamp)
		gen.writeNumberField("modifiedTimestamp", value.modifiedTimestamp)
		gen.writeNumberField("userTimestamp", value.userTimestamp)
		gen.writeStringField("title", value.title)
		gen.writeObjectField("attachmentKeyList", value.attachmentKeyList)
		gen.writeObjectField("latLng", value.latLng)
		gen.writeStringField("address", value.address)
		gen.writeNumberField("mood", value.mood)
		gen.writeBooleanField("isFavourite", value.isFavourite)
		gen.writeBooleanField("isArchived", value.isArchived)
		gen.writeBooleanField("isLocked", value.isLocked)
		gen.writeNumberField("version", value.version)
		gen.writeStringField("contentThumbnail", value.contentThumbnail?.decrypt())
		gen.writeStringField("content", value.content)
		gen.writeStringField("attachmentThumbnail", value.attachmentThumbnail?.bitmapToBase64String())

		gen.writeEndObject()
	}
}

private class NoteBackupDeserializer @JvmOverloads constructor(t: Class<NoteBackup?>? = null) :
	StdDeserializer<NoteBackup>(t) {

	@Throws(IOException::class, JsonProcessingException::class)
	override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): NoteBackup {

		val node: JsonNode = parser.codec.readTree(parser)

		val key = node["key"].asText()
		val timezone = node["timezone"].asText()
		val chapterPath: MutableList<String> = mutableListOf()
		(node["chapterPath"] as ArrayNode).forEach { chapterPath.add(it.asText()) }
		val notebookKey = node["notebookKey"].asText()
		val createdTimestamp = node["createdTimestamp"].asLong()
		val modifiedTimestamp = node["modifiedTimestamp"].asLong()
		val userTimestamp = node["userTimestamp"].asLong()
		val title = node["title"].asText()
		val attachmentKeyList: MutableList<String> = mutableListOf()
		(node["attachmentKeyList"] as ArrayNode).forEach { attachmentKeyList.add(it.asText()) }
		val latLngNode = node["latLng"] as JsonNode
		val latitude =
			if (latLngNode !is NullNode) latLngNode["latitude"].asDouble() else NullNode.getInstance()
		val longitude =
			if (latLngNode !is NullNode) latLngNode["longitude"].asDouble() else NullNode.getInstance()
		val latLng =
			if (latitude is Double && longitude is Double) LatLng(latitude, longitude) else null
		val address = node["address"].asText()
		val mood = node["mood"].asInt()
		val isFavourite = node["isFavourite"].asBoolean()
		val isArchived = node["isArchived"].asBoolean()
		val isLocked = node["isLocked"].asBoolean()
		val attachmentThumbnailBase64String = node["attachmentThumbnail"].asText()
		val attachmentThumbnail = attachmentThumbnailBase64String.base64stringToBitmap()
		val contentThumbnail = node["contentThumbnail"].asText().encrypt()
		val content = node["content"].asText()
		val version = node["version"].asInt()

		return NoteBackup(
			key = key,
			timezone = timezone,
			chapterPath = chapterPath,
			notebookKey = notebookKey,
			createdTimestamp = createdTimestamp,
			modifiedTimestamp = modifiedTimestamp,
			userTimestamp = userTimestamp,
			title = title,
			attachmentKeyList = attachmentKeyList,
			latLng = latLng,
			address = address,
			mood = mood,
			isFavourite = isFavourite,
			isArchived = isArchived,
			isLocked = isLocked,
			contentThumbnail = contentThumbnail,
			content = content,
			attachmentThumbnail = attachmentThumbnail,
			version = version,
		)
	}
}
