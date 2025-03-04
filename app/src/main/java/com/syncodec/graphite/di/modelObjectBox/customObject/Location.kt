package com.syncodec.graphite.di.modelObjectBox.customObject

import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
@InternalSerializationApi
data class Location(
    val address: String? = null,
    val coordinate: Coordinate? = null
)

@Serializable
@InternalSerializationApi
data class Coordinate(
    val latitude: Double,
    val longitude: Double
)

@OptIn(InternalSerializationApi::class)
class LocationConverter : PropertyConverter<Location, String> {
    private val json = Json

    @OptIn(InternalSerializationApi::class)
    override fun convertToEntityProperty(databaseValue: String): Location? {
        return json.decodeFromString(databaseValue)
    }

    @OptIn(InternalSerializationApi::class)
    override fun convertToDatabaseValue(entityProperty: Location): String? {
        return json.encodeToString(entityProperty)
    }
}

