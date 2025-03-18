package com.syncodec.graphite.di.modelObjectBox.encryptable

import com.syncodec.graphite.di.modelObjectBox.Decryptable
import com.syncodec.graphite.di.modelObjectBox.serializer.ZonedDateTimeSerializer
import com.syncodec.graphite.utils.alice2.Alice2
import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.json.Json
import java.time.ZonedDateTime


data class EncryptedZonedDateTime(val data: String) : Decryptable<ZonedDateTime> {

    val json = Json

    override fun decrypt(alice2: Alice2, default: ZonedDateTime?): ZonedDateTime? = try {
        json.decodeFromString(ZonedDateTimeSerializer, alice2.decrypt(data) ?: "")
    } catch (e: Exception) {
        e.printStackTrace()
        default
    }

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromZonedTimeStamp(zonedDateTime: ZonedDateTime, alice2: Alice2) : EncryptedZonedDateTime? {
            val encryptedZonedDateTime = alice2.encrypt(Json.encodeToString(ZonedDateTimeSerializer, zonedDateTime))?.toHexString(HexFormat.UpperCase) ?: return null
            return EncryptedZonedDateTime(data = encryptedZonedDateTime)
        }
    }
}

fun ZonedDateTime.encrypt(alice2: Alice2) = EncryptedZonedDateTime.fromZonedTimeStamp(zonedDateTime = this, alice2 = alice2)

class EncryptedZonedDateTimeConverter : PropertyConverter<EncryptedZonedDateTime?, String?> {
    override fun convertToEntityProperty(databaseValue: String?): EncryptedZonedDateTime? = databaseValue?.let { EncryptedZonedDateTime(it) }
    override fun convertToDatabaseValue(entityProperty: EncryptedZonedDateTime?): String? = entityProperty?.data
}
