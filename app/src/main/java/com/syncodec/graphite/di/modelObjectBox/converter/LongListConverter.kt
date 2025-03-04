package com.syncodec.graphite.di.modelObjectBox.converter

import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json


@OptIn(InternalSerializationApi::class)
class LongListConverter : PropertyConverter<List<Long>, String> {

    val json = Json

    @OptIn(InternalSerializationApi::class)
    override fun convertToEntityProperty(databaseValue: String?): List<Long> {
        try {
            databaseValue ?: return emptyList()
            return json.decodeFromString(string = databaseValue)
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    @OptIn(InternalSerializationApi::class)
    override fun convertToDatabaseValue(entityProperty: List<Long>?): String? {
        try {
            return json.encodeToString(value = entityProperty)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}

