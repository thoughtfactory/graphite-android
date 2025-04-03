package com.syncodec.graphite.di.modelObjectBox.encryptable

import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import com.syncodec.graphite.di.modelObjectBox.Decryptable
import com.syncodec.graphite.utils.alice2.Alice2
import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.json.Json


data class EncryptedBucketType(val data: String) : Decryptable<BucketBoxEncrypted.BucketType> {

    val json = Json

    override fun decrypt(alice2: Alice2, default: BucketBoxEncrypted.BucketType?): BucketBoxEncrypted.BucketType? = try {
        val decryptedDataRaw = alice2.decrypt(data)
        BucketBoxEncrypted.BucketType.entries.find { it.id == decryptedDataRaw } ?: BucketBoxEncrypted.BucketType.Unknown
    } catch (e: Exception) {
        e.printStackTrace()
        default
    }

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromBucketType(bucketType: BucketBoxEncrypted.BucketType, alice2: Alice2): EncryptedBucketType? {
            val encryptedBucketType = alice2.encrypt(bucketType.id)?.toHexString(HexFormat.UpperCase) ?: return null
            return EncryptedBucketType(data = encryptedBucketType)
        }
    }
}

fun BucketBoxEncrypted.BucketType.encrypt(alice2: Alice2) = EncryptedBucketType.fromBucketType(bucketType = this, alice2 = alice2)

class EncryptedBucketTypeConverter : PropertyConverter<EncryptedBucketType?, String?> {
    override fun convertToEntityProperty(databaseValue: String?): EncryptedBucketType? = databaseValue?.let { EncryptedBucketType(it) }
    override fun convertToDatabaseValue(entityProperty: EncryptedBucketType?): String? = entityProperty?.data
}

