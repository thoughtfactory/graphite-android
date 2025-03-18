package com.syncodec.graphite.di.modelObjectBox.encryptable

import com.syncodec.graphite.di.modelObjectBox.Decryptable
import com.syncodec.graphite.utils.alice2.Alice2
import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.json.Json


data class EncryptedLongList(val data: String) : Decryptable<List<Long>> {

    val json = Json

    override fun decrypt(alice2: Alice2, default: List<Long>?): List<Long>? = try {
        json.decodeFromString<List<Long>>(alice2.decrypt(data) ?: "")
    } catch (e: Exception) {
        e.printStackTrace()
        default
    }

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromLongList(longList: List<Long>, alice2: Alice2) : EncryptedLongList? {
            val encryptedLongList =  alice2.encrypt(Json.encodeToString(longList))?.toHexString(HexFormat.UpperCase) ?: return null
            return EncryptedLongList(data = encryptedLongList)
        }
    }
}

fun List<Long>.encrypt(alice2: Alice2) = EncryptedLongList.fromLongList(longList = this, alice2 = alice2)

class EncryptedLongListConverter : PropertyConverter<EncryptedLongList?, String?> {
    override fun convertToEntityProperty(databaseValue: String?): EncryptedLongList? = databaseValue?.let { EncryptedLongList(it) }
    override fun convertToDatabaseValue(entityProperty: EncryptedLongList?): String? = entityProperty?.data
}

