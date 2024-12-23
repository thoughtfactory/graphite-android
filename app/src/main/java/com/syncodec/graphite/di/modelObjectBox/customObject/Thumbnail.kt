package com.syncodec.graphite.di.modelObjectBox.customObject

import io.objectbox.annotation.Entity
import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Serializable
@InternalSerializationApi
sealed class Thumbnail {

    @Serializable
    data class Color(val value: Int) : Thumbnail()

    @Serializable
    data class Image(val base64String: String) : Thumbnail()
}

@OptIn(InternalSerializationApi::class)
class ThumbnailConverter : PropertyConverter<Thumbnail, String> {
    private val json = Json

    @OptIn(InternalSerializationApi::class)
    override fun convertToEntityProperty(databaseValue: String): Thumbnail? {
        return json.decodeFromString(databaseValue)
    }

    @OptIn(InternalSerializationApi::class)
    override fun convertToDatabaseValue(entityProperty: Thumbnail): String? {
        return json.encodeToString(entityProperty)
    }
}

