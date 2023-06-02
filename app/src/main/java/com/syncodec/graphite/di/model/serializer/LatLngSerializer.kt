package com.syncodec.graphite.di.model.serializer

import com.syncodec.graphite.di.model.LatLng
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure


@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = LatLng::class)
object LatLngSerializer : KSerializer<LatLng?> {
	override val descriptor: SerialDescriptor = buildClassSerialDescriptor("LatLng") {
		element("latitude", Double.serializer().descriptor)
		element("longitude", Double.serializer().descriptor)
	}

	override fun serialize(encoder: Encoder, value: LatLng?) {
		try {
			if (value == null) {
				encoder.encodeNull()
			} else {
				encoder.encodeStructure(descriptor) {
					encodeDoubleElement(descriptor, 0, value.latitude)
					encodeDoubleElement(descriptor, 1, value.longitude)
				}
			}
		} catch (e: Exception) {
			encoder.encodeNull()
		}
	}

	override fun deserialize(decoder: Decoder): LatLng? {
		return try {
			decoder.decodeStructure(descriptor) {
				var latitude: Double? = null
				var longitude: Double? = null
				loop@ while (true) {
					when (val index = decodeElementIndex(descriptor)) {
						CompositeDecoder.DECODE_DONE -> break@loop
						0 -> latitude = decodeDoubleElement(descriptor, 0)
						1 -> longitude = decodeDoubleElement(descriptor, 1)
						else -> error("Unexpected index: $index")
					}
				}
				if (latitude == null || longitude == null) null
				else LatLng(latitude, longitude)
			}
		} catch (e: Exception) {
			null
		}
	}
}
