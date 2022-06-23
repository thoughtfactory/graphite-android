package com.syncodec.graphite.database.bucketItem

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.gms.maps.model.LatLng
import com.syncodec.graphite.miscellaneous.base64stringToBitmap
import com.syncodec.graphite.miscellaneous.bitmapToBase64String
import java.io.IOException

@JsonSerialize(using = BucketItemDbEntrySerializer::class)
@JsonDeserialize(using = BucketItemDbEntryDeserializer::class)
@Entity(tableName = "bucket_item_table")
data class BucketItemDbEntry(
	@PrimaryKey(autoGenerate = false)
	@ColumnInfo(name = "key")
	val key: String,

	@ColumnInfo(name = "bucket_key")
	val bucketKey: String,

	@ColumnInfo(name = "bucket_item_type")
	val bucketItemType: BucketItemType,

	@ColumnInfo(name = "created_timestamp")
	var createdTimestamp: Long = -1
) {
	@ColumnInfo(name = "modified_timestamp")
	var modifiedTimestamp: Long = -1

	@ColumnInfo(name = "title")
	var title: String? = null

	@ColumnInfo(name = "thumbnail", typeAffinity = ColumnInfo.BLOB)
	var thumbnail: Bitmap? = null

	@ColumnInfo(name = "state")
	var state: BucketItemState = BucketItemState.ALPHA

	@ColumnInfo(name = "latlng")
	var latLng: LatLng? = null

	@ColumnInfo(name = "address")
	var address: String? = null

	@ColumnInfo(name = "data", typeAffinity = ColumnInfo.BLOB)
	var data: BucketItem? = null

	@ColumnInfo(name = "is_favourite")
	var isFavourite: Boolean = false

	@ColumnInfo(name = "is_archived")
	var isArchived: Boolean = false

	@ColumnInfo(name = "is_locked")
	var isLocked: Boolean = false

	@get:JsonIgnore
	@ColumnInfo(name = "g_drive_file_id")
	var gDriveFileId: String? = null

	@ColumnInfo(name = "hash")
	var hash: Long? = null

	override fun hashCode(): Int {
		var result = key.hashCode()
		result = 31 * result + bucketKey.hashCode()
		result = 31 * result + bucketItemType.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + (title?.hashCode() ?: 0)
		result = 31 * result + (thumbnail?.hashCode() ?: 0)
		result = 31 * result + state.hashCode()
		result = 31 * result + (latLng?.hashCode() ?: 0)
		result = 31 * result + (address?.hashCode() ?: 0)
		result = 31 * result + (data?.hashCode() ?: 0)
		result = 31 * result + (gDriveFileId?.hashCode() ?: 0)
		result = 31 * result + (hash?.hashCode() ?: 0)
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as BucketItemDbEntry

		if (key != other.key) return false
		if (bucketKey != other.bucketKey) return false
		if (bucketItemType != other.bucketItemType) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false
		if (title != other.title) return false
		if (thumbnail != other.thumbnail) return false
		if (state != other.state) return false
		if (latLng != other.latLng) return false
		if (address != other.address) return false
		if (data != other.data) return false
		if (gDriveFileId != other.gDriveFileId) return false
		if (hash != other.hash) return false

		return true
	}

}

enum class BucketItemType {
	TODO,
	BOOK,
	SHOW,
	LINK
}

enum class BucketItemState {
	ALPHA,
	BETA,
	GAMMA
}


private class BucketItemDbEntrySerializer @JvmOverloads constructor(t: Class<BucketItemDbEntry?>? = null) :
	StdSerializer<BucketItemDbEntry>(t) {
	@Throws(IOException::class, JsonProcessingException::class)
	override fun serialize(
		value: BucketItemDbEntry, gen: JsonGenerator, provider: SerializerProvider
	) {
		val objectMapper: ObjectMapper = jsonMapper { addModule(kotlinModule()) }

		gen.writeStartObject()
		gen.writeStringField("key", value.key)
		gen.writeStringField("bucketKey", value.bucketKey)
		gen.writeStringField("bucketItemType", value.bucketItemType.name)
		gen.writeNumberField("createdTimestamp", value.createdTimestamp)
		gen.writeNumberField("modifiedTimestamp", value.modifiedTimestamp)
		gen.writeStringField("title", value.title)
		gen.writeStringField("thumbnail", value.thumbnail?.bitmapToBase64String())
		gen.writeStringField("state", value.state.name)
		gen.writeObjectField("latLng", value.latLng)
		gen.writeStringField("address", value.address)
		gen.writeStringField("data", objectMapper.writeValueAsString(value.data))
		gen.writeBooleanField("isFavourite", value.isFavourite)
		gen.writeBooleanField("isArchived", value.isArchived)
		gen.writeBooleanField("isLocked", value.isLocked)

		gen.writeEndObject()
	}
}

private class BucketItemDbEntryDeserializer @JvmOverloads constructor(t: Class<BucketItemDbEntry?>? = null) :
	StdDeserializer<BucketItemDbEntry>(t) {

	@Throws(IOException::class, JsonProcessingException::class)
	override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): BucketItemDbEntry {
		val objectMapper: ObjectMapper = jsonMapper { addModule(kotlinModule()) }

		val node: JsonNode = parser.codec.readTree(parser)

		val key = node["key"].asText()
		val bucketKey = node["bucketKey"].asText()
		val bucketItemType = BucketItemType.valueOf(node["bucketItemType"].asText())
		val createdTimestamp = node["createdTimestamp"].asLong()
		val modifiedTimestamp = node["modifiedTimestamp"].asLong()
		val title = node["title"].asText()
		val thumbnail = node["thumbnail"].asText().base64stringToBitmap()
		val state = BucketItemState.valueOf(node["state"].asText())
		val latLngNode = node["latLng"] as JsonNode
		val latitude =
			if (latLngNode !is NullNode) latLngNode["latitude"].asDouble() else NullNode.getInstance()
		val longitude =
			if (latLngNode !is NullNode) latLngNode["longitude"].asDouble() else NullNode.getInstance()
		val latLng =
			if (latitude is Double && longitude is Double) LatLng(latitude, longitude) else null
		val address = node["address"].asText()
		val data = objectMapper.readValue<BucketItem>(node["data"].asText())
		val isFavourite = node["isFavourite"].asBoolean(false)
		val isArchived = node["isArchived"].asBoolean(false)
		val isLocked = node["isLocked"].asBoolean(false)

		return BucketItemDbEntry(
			key = key,
			bucketKey = bucketKey,
			bucketItemType = bucketItemType,
			createdTimestamp = createdTimestamp
		) .apply {
			this.modifiedTimestamp = modifiedTimestamp
			this.title = title
			this.thumbnail = thumbnail
			this.state = state
			this.latLng = latLng
			this.address = address
			this.data = data
			this.isFavourite = isFavourite
			this.isArchived = isArchived
			this.isLocked = isLocked
		}
	}
}
