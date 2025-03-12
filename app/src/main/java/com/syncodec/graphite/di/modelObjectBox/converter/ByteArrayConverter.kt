package com.syncodec.graphite.di.modelObjectBox.converter

import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlin.ByteArray


@OptIn(InternalSerializationApi::class)
class ByteArrayConverter : PropertyConverter<ByteArray, String> {
    @OptIn(InternalSerializationApi::class, ExperimentalStdlibApi::class)
    override fun convertToEntityProperty(databaseValue: String?): ByteArray {
        return try {
            databaseValue?.hexToByteArray(format = HexFormat.UpperCase) ?: ByteArray(1)
        } catch (e: Exception) {
            e.printStackTrace()
            ByteArray(1)
        }
    }

    @OptIn(InternalSerializationApi::class, ExperimentalStdlibApi::class)
    override fun convertToDatabaseValue(entityProperty: ByteArray): String? {
        return try {
            entityProperty.toHexString(format = HexFormat.UpperCase)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

