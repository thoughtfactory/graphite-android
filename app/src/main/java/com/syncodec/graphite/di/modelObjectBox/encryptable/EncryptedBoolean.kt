package com.syncodec.graphite.di.modelObjectBox.encryptable

import com.syncodec.graphite.di.modelObjectBox.Decryptable
import com.syncodec.graphite.utils.alice2.Alice2
import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.json.Json


data class EncryptedBoolean(val data: String) : Decryptable<Boolean> {

    private val json = Json

    override fun decrypt(alice2: Alice2, default: Boolean?): Boolean? = try {
        json.decodeFromString<Boolean>(alice2.decrypt(data) ?: "")
    } catch (e: Exception) {
        e.printStackTrace()
        default
    }

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromBoolean(boolean: Boolean, alice2: Alice2): EncryptedBoolean? {
            val encryptedBoolean = alice2.encrypt(Json.encodeToString(boolean))?.toHexString(HexFormat.UpperCase) ?: return null
            return EncryptedBoolean(data = encryptedBoolean)
        }
    }
}

fun Boolean.encrypt(alice2: Alice2) = EncryptedBoolean.fromBoolean(boolean = this, alice2 = alice2)

class EncryptedBooleanConverter : PropertyConverter<EncryptedBoolean?, String?> {
    override fun convertToEntityProperty(databaseValue: String?): EncryptedBoolean? = databaseValue?.let { EncryptedBoolean(it) }
    override fun convertToDatabaseValue(entityProperty: EncryptedBoolean?): String? = entityProperty?.data
}

