package com.syncodec.graphite.di.modelObjectBox.customObject

import android.util.Log
import com.syncodec.graphite.di.modelObjectBox.serializer.LocalDateTimeSerializer
import com.syncodec.graphite.di.modelObjectBox.serializer.TimezoneSerializer
import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.util.TimeZone


@Serializable
@InternalSerializationApi
data class Timestamp(
//    @Serializable(LocalDateTimeSerializer::class) val localDateTime: LocalDateTime = LocalDateTime.now(),
    @Serializable(TimezoneSerializer::class) val timeZone: TimeZone = TimeZone.getDefault()
)

@OptIn(InternalSerializationApi::class)
class TimestampConverter : PropertyConverter<Timestamp, String> {
    private val json = Json

    @OptIn(InternalSerializationApi::class)
    override fun convertToEntityProperty(databaseValue: String): Timestamp? {
        Log.d("npr71", "timestamp : $databaseValue")
        return json.decodeFromString(databaseValue)
    }

    @OptIn(InternalSerializationApi::class)
    override fun convertToDatabaseValue(entityProperty: Timestamp): String? {
        Log.d("npr71", "timestamp : $entityProperty")
        return json.encodeToString(entityProperty)
    }
}
