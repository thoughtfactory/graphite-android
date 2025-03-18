package com.syncodec.graphite.di.modelObjectBox.encryptable

import com.syncodec.graphite.di.modelObjectBox.Decryptable
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.utils.alice2.Alice2
import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.json.Json


data class EncryptedBucketItemData(val data: String) : Decryptable<BucketItemData> {

    private val json = Json

    override fun decrypt(alice2: Alice2, default: BucketItemData?): BucketItemData? = try {
        json.decodeFromString(alice2.decrypt(data) ?: "")
    } catch (e: Exception) {
        e.printStackTrace()
        default
    }

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromBucketItemData(bucketItemData: BucketItemData?, alice2: Alice2): EncryptedBucketItemData? {
            bucketItemData ?: return null
            val encryptedBucketItemData = alice2.encrypt(Json.encodeToString(bucketItemData))?.toHexString(HexFormat.UpperCase) ?: return null
            return EncryptedBucketItemData(data = encryptedBucketItemData)
        }
    }
}

class EncryptedBucketItemDataConverter : PropertyConverter<EncryptedBucketItemData?, String?> {
    override fun convertToEntityProperty(databaseValue: String?): EncryptedBucketItemData? = databaseValue?.let { EncryptedBucketItemData(it) }
    override fun convertToDatabaseValue(entityProperty: EncryptedBucketItemData?): String? = entityProperty?.data
}

