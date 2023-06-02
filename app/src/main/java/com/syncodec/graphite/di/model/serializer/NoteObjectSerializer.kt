package com.syncodec.graphite.di.model.serializer

import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObject
import io.realm.kotlin.types.RealmUUID
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure


@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = NoteObject::class)
object NoteObjectSerializer : KSerializer<NoteObject> {
	override val descriptor: SerialDescriptor = buildClassSerialDescriptor("NoteObject") {
		element("id", RealmUUIDSerializer.descriptor)
		element("createdTimestamp", PrimitiveSerialDescriptor("createdTimestamp", PrimitiveKind.LONG))
		element("modifiedTimestamp", PrimitiveSerialDescriptor("modifiedTimestamp", PrimitiveKind.LONG))
		element("userTimestamp", PrimitiveSerialDescriptor("userTimestamp", PrimitiveKind.LONG))
		element("title", PrimitiveSerialDescriptor("title", PrimitiveKind.STRING))
		element("color", PrimitiveSerialDescriptor("color", PrimitiveKind.INT))
		element("latLng", buildClassSerialDescriptor("latLng") {
			element("latitude", PrimitiveSerialDescriptor("latitude", PrimitiveKind.DOUBLE))
			element("longitude", PrimitiveSerialDescriptor("longitude", PrimitiveKind.DOUBLE))
		})
		element("address", PrimitiveSerialDescriptor("address", PrimitiveKind.STRING))
		element("content", PrimitiveSerialDescriptor("content", PrimitiveKind.STRING))
		element("isFavourite", PrimitiveSerialDescriptor("isFavourite", PrimitiveKind.BOOLEAN))
		element("isLocked", PrimitiveSerialDescriptor("isLocked", PrimitiveKind.BOOLEAN))
		element("parentId", RealmUUIDNullableSerializer.descriptor)
	}

	override fun serialize(encoder: Encoder, value: NoteObject) {
		encoder.encodeStructure(descriptor) {
			encodeSerializableElement(descriptor, 0, RealmUUIDSerializer, value.id)
			encodeLongElement(descriptor, 1, value.createdTimestamp)
			encodeLongElement(descriptor, 2, value.modifiedTimestamp)
			encodeLongElement(descriptor, 3, value.userTimestamp)
			encodeStringElement(descriptor, 4, value.title ?: "")
			encodeIntElement(descriptor, 5, value.color ?: 0)
			encodeSerializableElement(descriptor, 6, LatLngSerializer, value.getLatLng())
			encodeStringElement(descriptor, 7, value.address ?: "")
			encodeStringElement(descriptor, 8, value.content ?: "")
			encodeBooleanElement(descriptor, 9, value.isFavourite)
			encodeBooleanElement(descriptor, 10, value.isLocked)
			encodeSerializableElement(descriptor, 11, RealmUUIDNullableSerializer, value.parentId )
		}
	}

	override fun deserialize(decoder: Decoder): NoteObject {
		return decoder.decodeStructure(descriptor) {
			var id: RealmUUID = RealmUUID.random()
			var createdTimestamp: Long? = null
			var modifiedTimestamp: Long? = null
			var userTimestamp: Long? = null
			var title: String? = null
			var color: Int? = null
			var latLng: LatLng? = null
			var address: String? = null
			var content: String? = null
			var isFavourite: Boolean? = null
			var isLocked: Boolean? = null
			var parentId: RealmUUID? = null
			loop@ while (true) {
				when (val index = decodeElementIndex(descriptor)) {
					CompositeDecoder.DECODE_DONE -> break@loop
					0 -> id = decodeSerializableElement(descriptor, 0, RealmUUIDSerializer)
					1 -> createdTimestamp = decodeLongElement(descriptor, 1)
					2 -> modifiedTimestamp = decodeLongElement(descriptor, 2)
					3 -> userTimestamp = decodeLongElement(descriptor, 3)
					4 -> title = decodeStringElement(descriptor, 4)
					5 -> color = decodeIntElement(descriptor, 5)
					6 -> latLng = decodeSerializableElement(descriptor, 6, LatLngSerializer)
					7 -> address = decodeStringElement(descriptor, 7)
					8 -> content = decodeStringElement(descriptor, 8)
					9 -> isFavourite = decodeBooleanElement(descriptor, 9)
					10 -> isLocked = decodeBooleanElement(descriptor, 10)
					11 -> parentId = decodeSerializableElement(descriptor, 11, RealmUUIDNullableSerializer)
					else -> error("Unexpected index: $index")
				}
			}
			NoteObject().apply {
				this.id = id
				createdTimestamp?.let { this.createdTimestamp = it }
				modifiedTimestamp?.let { this.modifiedTimestamp = it }
				userTimestamp?.let { this.userTimestamp = it }
				this.title = title
				this.color = color
				setLatLng(latLng)
				this.address = address
				this.content = content
				this.isFavourite = isFavourite ?: false
				this.isLocked = isLocked ?: false
				this.parentId = parentId
				this.contentThumbnail = getContentString().take(256)
			}
		}
	}
}
