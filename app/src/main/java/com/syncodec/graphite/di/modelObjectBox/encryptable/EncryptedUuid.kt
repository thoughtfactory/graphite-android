package com.syncodec.graphite.di.modelObjectBox.encryptable

import com.syncodec.graphite.di.modelObjectBox.Decryptable
import com.syncodec.graphite.utils.alice2.Alice2
import io.objectbox.converter.PropertyConverter
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
data class EncryptedUuid(val data: String) : Decryptable<Uuid> {

    override fun decrypt(alice2: Alice2, default: Uuid?): Uuid? = try {
        alice2.decrypt(data)?.let { Uuid.parseHex(it) } ?: Uuid.random()
    } catch (e: Exception) {
        e.printStackTrace()
        default
    }

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun fromUuid(uuid: Uuid, alice2: Alice2) : EncryptedUuid? {
            val encryptedUuidString = alice2.encrypt(data = uuid.toHexString())?.toHexString(HexFormat.UpperCase) ?: return null
            return EncryptedUuid(data = encryptedUuidString)
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
fun Uuid.encrypt(alice2: Alice2) = EncryptedUuid.fromUuid(uuid = this, alice2 = alice2)

class EncryptedUuidConverter : PropertyConverter<EncryptedUuid?, String?> {
    override fun convertToEntityProperty(databaseValue: String?): EncryptedUuid? = databaseValue?.let { EncryptedUuid(it) }
    override fun convertToDatabaseValue(entityProperty: EncryptedUuid?): String? = entityProperty?.data
}

