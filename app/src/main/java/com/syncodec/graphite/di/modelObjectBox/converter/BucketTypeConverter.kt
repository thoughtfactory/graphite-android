package com.syncodec.graphite.di.modelObjectBox.converter

import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.InternalSerializationApi


@OptIn(InternalSerializationApi::class)
class BucketTypeConverter : PropertyConverter<BucketBoxEncrypted.BucketType, String> {

    @OptIn(InternalSerializationApi::class)
    override fun convertToEntityProperty(databaseValue: String?): BucketBoxEncrypted.BucketType {
        val bucketType = BucketBoxEncrypted.BucketType.entries.find { it.id == databaseValue }
        return bucketType ?: BucketBoxEncrypted.BucketType.Unknown
    }

    @OptIn(InternalSerializationApi::class)
    override fun convertToDatabaseValue(entityProperty: BucketBoxEncrypted.BucketType?): String? {
        return entityProperty?.id
    }
}

