package com.syncodec.graphite.di.modelObjectBox.converter

import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.di.modelObjectBox.customObject.Thumbnail
import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@OptIn(InternalSerializationApi::class)
class BucketTypeConverter : PropertyConverter<BucketBox.BucketType, String> {

    @OptIn(InternalSerializationApi::class)
    override fun convertToEntityProperty(databaseValue: String?): BucketBox.BucketType {
        val bucketType = BucketBox.BucketType.entries.find { it.id == databaseValue }
        return bucketType ?: BucketBox.BucketType.Unknown
    }

    @OptIn(InternalSerializationApi::class)
    override fun convertToDatabaseValue(entityProperty: BucketBox.BucketType?): String? {
        return entityProperty?.id
    }
}

