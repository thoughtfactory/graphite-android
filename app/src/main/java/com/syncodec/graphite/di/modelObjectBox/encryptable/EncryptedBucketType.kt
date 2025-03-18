package com.syncodec.graphite.di.modelObjectBox.encryptable

import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.di.modelObjectBox.Decryptable
import com.syncodec.graphite.utils.alice2.Alice2
import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.json.Json


data class EncryptedBucketType(val data: String) : Decryptable<BucketBox.BucketType> {

    val json = Json

    override fun decrypt(alice2: Alice2, default: BucketBox.BucketType?): BucketBox.BucketType? = try {
        val decryptedDataRaw = alice2.decrypt(data)
        BucketBox.BucketType.entries.find { it.id == decryptedDataRaw } ?: BucketBox.BucketType.Unknown
    } catch (e: Exception) {
        e.printStackTrace()
        default
    }

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromBucketType(bucketType: BucketBox.BucketType, alice2: Alice2): EncryptedBucketType? {
            val encryptedBucketType = alice2.encrypt(bucketType.id)?.toHexString(HexFormat.UpperCase) ?: return null
            return EncryptedBucketType(data = encryptedBucketType)
        }
    }
}

class EncryptedBucketTypeConverter : PropertyConverter<EncryptedBucketType?, String?> {
    override fun convertToEntityProperty(databaseValue: String?): EncryptedBucketType? = databaseValue?.let { EncryptedBucketType(it) }
    override fun convertToDatabaseValue(entityProperty: EncryptedBucketType?): String? = entityProperty?.data
}

