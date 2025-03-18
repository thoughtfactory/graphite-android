package com.syncodec.graphite.di.modelObjectBox.encryptable

import com.syncodec.graphite.di.modelObjectBox.Decryptable
import com.syncodec.graphite.utils.alice2.Alice2
import io.objectbox.converter.PropertyConverter


data class EncryptedString(val data: String) : Decryptable<String> {

    override fun decrypt(alice2: Alice2, default: String?): String? = try {
        alice2.decrypt(data)
    } catch (e: Exception) {
        e.printStackTrace()
        default
    }

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromString(string: String, alice2: Alice2) : EncryptedString? {
            val encryptedString = alice2.encrypt(data = string)?.toHexString(HexFormat.UpperCase) ?: return null
            return EncryptedString(data = encryptedString)
        }
    }
}

class EncryptedStringConverter : PropertyConverter<EncryptedString?, String?> {
    override fun convertToEntityProperty(databaseValue: String?): EncryptedString? = databaseValue?.let { EncryptedString(it) }
    override fun convertToDatabaseValue(entityProperty: EncryptedString?): String? = entityProperty?.data
}

