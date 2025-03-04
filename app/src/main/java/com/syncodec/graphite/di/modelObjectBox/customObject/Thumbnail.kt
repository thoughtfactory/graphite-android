package com.syncodec.graphite.di.modelObjectBox.customObject

import androidx.compose.ui.graphics.toArgb
import com.syncodec.graphite.utils.getRandomColor
import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Serializable
@InternalSerializationApi
sealed class Thumbnail {

    @Serializable
    data class Color(val argbValue: Int) : Thumbnail() {
        companion object {
            val Random = Color(getRandomColor().toArgb())
        }
    }

    @Serializable
    data class Image(val base64String: String) : Thumbnail()

    data object None : Thumbnail()
}

@OptIn(InternalSerializationApi::class)
class ThumbnailConverter : PropertyConverter<Thumbnail, String> {
    private val json = Json

    @OptIn(InternalSerializationApi::class)
    override fun convertToEntityProperty(databaseValue: String?): Thumbnail? {
        try {
            databaseValue ?: return null
            return json.decodeFromString(string = databaseValue)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    @OptIn(InternalSerializationApi::class)
    override fun convertToDatabaseValue(entityProperty: Thumbnail?): String? {
        try {
            entityProperty ?: return null
            return json.encodeToString(value = entityProperty)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}

