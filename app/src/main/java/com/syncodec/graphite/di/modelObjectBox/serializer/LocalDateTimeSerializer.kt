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
import java.time.LocalDateTime


object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("date") {
        element("year", PrimitiveSerialDescriptor("year", PrimitiveKind.INT))
        element("month", PrimitiveSerialDescriptor("month", PrimitiveKind.INT))
        element("day", PrimitiveSerialDescriptor("day", PrimitiveKind.INT))
        element("hour", PrimitiveSerialDescriptor("hour", PrimitiveKind.INT))
        element("minute", PrimitiveSerialDescriptor("minute", PrimitiveKind.INT))
        element("second", PrimitiveSerialDescriptor("second", PrimitiveKind.INT))
        element("nanosecond", PrimitiveSerialDescriptor("nanosecond", PrimitiveKind.INT))
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.year)
            encodeIntElement(descriptor, 1, value.monthValue)
            encodeIntElement(descriptor, 2, value.dayOfMonth)
            encodeIntElement(descriptor, 3, value.hour)
            encodeIntElement(descriptor, 4, value.minute)
            encodeIntElement(descriptor, 5, value.second)
            encodeIntElement(descriptor, 6, value.nano)
        }
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        var year: Int? = null
        var month: Int? = null
        var day: Int? = null
        var hour: Int? = null
        var minute: Int? = null
        var second: Int? = null
        var nanoSecond: Int? = null

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
                        CompositeDecoder.DECODE_DONE -> break
                        else -> error("Unexpected index: $index")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return LocalDateTime.of(year!!, month!!, day!!, hour!!, minute!!, second!!, nanoSecond!!)
    }
}

