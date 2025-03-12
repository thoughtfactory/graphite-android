package com.syncodec.graphite.di.modelObjectBox.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import java.time.ZoneId
import java.time.ZonedDateTime


object ZonedDateTimeSerializer : KSerializer<ZonedDateTime?> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("date") {
        element("year", PrimitiveSerialDescriptor("year", PrimitiveKind.INT))
        element("month", PrimitiveSerialDescriptor("month", PrimitiveKind.INT))
        element("day", PrimitiveSerialDescriptor("day", PrimitiveKind.INT))
        element("hour", PrimitiveSerialDescriptor("hour", PrimitiveKind.INT))
        element("minute", PrimitiveSerialDescriptor("minute", PrimitiveKind.INT))
        element("second", PrimitiveSerialDescriptor("second", PrimitiveKind.INT))
        element("nanosecond", PrimitiveSerialDescriptor("nanosecond", PrimitiveKind.INT))
        element("zoneId", PrimitiveSerialDescriptor("zoneId", PrimitiveKind.STRING))
    }

    override fun serialize(encoder: Encoder, value: ZonedDateTime?) {
        value ?: return
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.year)
            encodeIntElement(descriptor, 1, value.monthValue)
            encodeIntElement(descriptor, 2, value.dayOfMonth)
            encodeIntElement(descriptor, 3, value.hour)
            encodeIntElement(descriptor, 4, value.minute)
            encodeIntElement(descriptor, 5, value.second)
            encodeIntElement(descriptor, 6, value.nano)
            encodeStringElement(descriptor, 7, value.zone.id)
        }
    }

    override fun deserialize(decoder: Decoder): ZonedDateTime? {
        var year: Int? = null
        var month: Int? = null
        var day: Int? = null
        var hour: Int? = null
        var minute: Int? = null
        var second: Int? = null
        var nanoSecond: Int? = null
        var zoneId: String? = null

        try {
            decoder.decodeStructure(descriptor) {
                while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        0 -> year = decodeIntElement(descriptor, 0)
                        1 -> month = decodeIntElement(descriptor, 1)
                        2 -> day = decodeIntElement(descriptor, 2)
                        3 -> hour = decodeIntElement(descriptor, 3)
                        4 -> minute = decodeIntElement(descriptor, 4)
                        5 -> second = decodeIntElement(descriptor, 5)
                        6 -> nanoSecond = decodeIntElement(descriptor, 6)
                        7 -> zoneId = decodeStringElement(descriptor, 7)
                        CompositeDecoder.DECODE_DONE -> break
                        else -> error("Unexpected index: $index")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            return ZonedDateTime.of(year ?: ZonedDateTime.now().year, month ?: 1, day ?: 1, hour ?: 0, minute ?: 0, second ?: 0, nanoSecond ?: 0, ZoneId.of(zoneId))
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}

