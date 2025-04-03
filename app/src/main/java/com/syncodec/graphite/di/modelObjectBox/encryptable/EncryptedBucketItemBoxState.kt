package com.syncodec.graphite.di.modelObjectBox.encryptable

import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.Decryptable
import com.syncodec.graphite.utils.alice2.Alice2
import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.json.Json


data class EncryptedBucketItemBoxState(val data: String) : Decryptable<BucketItemBoxDecrypted.State> {

    private val json = Json

    override fun decrypt(alice2: Alice2, default: BucketItemBoxDecrypted.State?): BucketItemBoxDecrypted.State? = try {
        json.decodeFromString(alice2.decrypt(data) ?: "")
    } catch (e: Exception) {
        e.printStackTrace()
        default
    }

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromState(state: BucketItemBoxDecrypted.State, alice2: Alice2): EncryptedBucketItemBoxState? {
            val encryptedBucketItemBoxState = alice2.encrypt(Json.encodeToString(state))?.toHexString(HexFormat.UpperCase) ?: return null
            return EncryptedBucketItemBoxState(data = encryptedBucketItemBoxState)
        }
    }
}

fun BucketItemBoxDecrypted.State.encrypt(alice2: Alice2) = EncryptedBucketItemBoxState.fromState(state = this, alice2 = alice2)

class EncryptedBucketItemBoxStateConverter : PropertyConverter<EncryptedBucketItemBoxState?, String?> {
    override fun convertToEntityProperty(databaseValue: String?): EncryptedBucketItemBoxState? = databaseValue?.let { EncryptedBucketItemBoxState(it) }
    override fun convertToDatabaseValue(entityProperty: EncryptedBucketItemBoxState?): String? = entityProperty?.data
}

