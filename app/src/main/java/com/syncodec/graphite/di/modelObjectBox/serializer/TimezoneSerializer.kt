package com.syncodec.graphite.di.modelObjectBox.serializer

import android.util.Log
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import java.util.TimeZone


object TimezoneSerializer : KSerializer<TimeZone> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName = "timezoneId", kind = PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: TimeZone) {
        Log.d("npr71", "TimezoneSerializer : ${value.displayName}")
        encoder.encodeString(value.displayName)
        encoder.encodeStructure {
            descriptor.serialName 
        }
    }

    override fun deserialize(decoder: Decoder): TimeZone {
        Log.d("npr71", "TimezoneSerializer : ${decoder.decodeString()}")
        return TimeZone.getTimeZone(decoder.decodeString())
    }
}

