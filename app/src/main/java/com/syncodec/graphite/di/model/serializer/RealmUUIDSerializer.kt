package com.syncodec.graphite.di.model.serializer

import io.realm.kotlin.types.RealmUUID
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = RealmUUID::class)
object RealmUUIDSerializer : KSerializer<RealmUUID> {
	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("RealmUUID", PrimitiveKind.STRING)

	override fun serialize(encoder: Encoder, value: RealmUUID) {
		val string = value.toString()
		encoder.encodeString(string)
	}

	override fun deserialize(decoder: Decoder): RealmUUID {
		val string = decoder.decodeString()
		return RealmUUID.from(string)
	}
}
