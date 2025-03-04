package com.syncodec.graphite.di.modelObjectBox.converter

import com.syncodec.graphite.di.modelObjectBox.serializer.ZonedDateTimeSerializer
import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import java.time.ZonedDateTime


@OptIn(InternalSerializationApi::class)
class ZonedDateTimeConverter : PropertyConverter<ZonedDateTime, String> {
    private val json = Json

    @OptIn(InternalSerializationApi::class)
    override fun convertToEntityProperty(databaseValue: String?): ZonedDateTime? {
        try {
            databaseValue ?: return null
            return json.decodeFromString(deserializer = ZonedDateTimeSerializer, string = databaseValue)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    @OptIn(InternalSerializationApi::class)
    override fun convertToDatabaseValue(entityProperty: ZonedDateTime?): String? {
        try {
            entityProperty ?: return null
            return json.encodeToString(serializer = ZonedDateTimeSerializer, value = entityProperty)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}

