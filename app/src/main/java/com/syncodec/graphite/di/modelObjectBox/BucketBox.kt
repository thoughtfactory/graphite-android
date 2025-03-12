package com.syncodec.graphite.di.modelObjectBox

import android.util.Log
import com.syncodec.graphite.di.modelObjectBox.serializer.ZonedDateTimeSerializer
import com.syncodec.graphite.utils.alice2.Alice2
import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Transient
import io.objectbox.relation.ToMany
import kotlinx.serialization.json.Json
import java.time.ZonedDateTime
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class BucketBox(
    var id: Long = 0,

    var uuid: Uuid = Uuid.random(),

    var createdTimestamp: ZonedDateTime? = ZonedDateTime.now(),
    var modifiedTimestamp: ZonedDateTime? = ZonedDateTime.now(),

    var title: String? = null,
    var description: String? = null,

    var bucketType: BucketType = BucketType.Unknown,

    var sortedIdList: List<Long> = listOf(),

    var isFavourite: Boolean = false,
    var isLocked: Boolean = false,

    override var encryptedBox: BucketBoxEnc? = null
) : DecryptedBox() {

    val childList: List<BucketItemBox>
        get() = encryptedBox?.childList ?: listOf()

    @OptIn(ExperimentalStdlibApi::class)
    override fun encrypt(alice2: Alice2): EncryptedBox {
        val json = Json
        return BucketBoxEnc(
            uuid = alice2.encrypt(data = uuid.toHexString())?.toHexString(HexFormat.UpperCase),
            createdTimestamp = alice2.encrypt(json.encodeToString(ZonedDateTimeSerializer, createdTimestamp))?.toHexString(HexFormat.UpperCase),
            modifiedTimestamp = alice2.encrypt(json.encodeToString(ZonedDateTimeSerializer, modifiedTimestamp))?.toHexString(HexFormat.UpperCase),
            title = alice2.encrypt(title ?: "")?.toHexString(HexFormat.UpperCase),
            description = alice2.encrypt(description ?: "")?.toHexString(HexFormat.UpperCase),
            bucketType = alice2.encrypt(bucketType.id)?.toHexString(HexFormat.UpperCase),
            sortedIdList = alice2.encrypt(json.encodeToString(sortedIdList))?.toHexString(HexFormat.UpperCase),
            isFavourite = alice2.encrypt(json.encodeToString(isFavourite))?.toHexString(HexFormat.UpperCase),
            isLocked = alice2.encrypt(json.encodeToString(isLocked))?.toHexString(HexFormat.UpperCase),
        ).apply {
            this.id = this@BucketBox.id
        }
    }

    enum class BucketType(
        val id: String
    ) {
        Todo(id = "todo"),
        Book(id = "book"),
        Show(id = "show"),
        Link(id = "link"),
        Location(id = "location"),
        Unknown(id = "unknown")
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + isFavourite.hashCode()
        result = 31 * result + isLocked.hashCode()
        result = 31 * result + (createdTimestamp?.hashCode() ?: 0)
        result = 31 * result + (modifiedTimestamp?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + bucketType.hashCode()
        result = 31 * result + childList.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BucketBox) return false

        if (id != other.id) return false
        if (isFavourite != other.isFavourite) return false
        if (isLocked != other.isLocked) return false
        if (createdTimestamp != other.createdTimestamp) return false
        if (modifiedTimestamp != other.modifiedTimestamp) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (bucketType != other.bucketType) return false
        if (childList != other.childList) return false

        return true
    }

    companion object {
        val random
            get() = BucketBox(
                title = "title_${Random.nextLong()}",
                description = "description_${Random.nextLong()}",
            )
    }
}


@Entity
data class BucketBoxEnc(
    var uuid: String? = null,
    var createdTimestamp: String? = null,
    var modifiedTimestamp: String? = null,

    var title: String? = null,
    var description: String? = null,

    var bucketType: String? = null,

    var sortedIdList: String? = null,

    var isFavourite: String? = null,
    var isLocked: String? = null,

    @Transient override var decryptedBox: BucketBox? = null
) : EncryptedBox() {
    @Backlink(to = "parent")
    lateinit var childList: ToMany<BucketItemBox>

    @OptIn(ExperimentalUuidApi::class)
    override fun decrypt(alice2: Alice2): BucketBox {
        val json = Json

        val decUuid = try {
            alice2.decrypt(uuid)?.let { Uuid.parseHex(it) } ?: Uuid.random()
        } catch (e: Exception) {
            e.printStackTrace()
            Uuid.random()
        }
        val decCreatedTimestamp = try {
            json.decodeFromString(ZonedDateTimeSerializer, alice2.decrypt(createdTimestamp) ?: "")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        val decModifiedTimestamp = try {
            json.decodeFromString(ZonedDateTimeSerializer, alice2.decrypt(modifiedTimestamp) ?: "")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        val decTitle = alice2.decrypt(title)
        val decDescription = alice2.decrypt(description)
        val decBucketType = BucketBox.BucketType.entries.find { it.id == alice2.decrypt(bucketType) } ?: BucketBox.BucketType.Unknown
        val decSortedIdList = try {
            json.decodeFromString(alice2.decrypt(sortedIdList) ?: "[]")
        } catch (e: Exception) {
            e.printStackTrace()
            listOf<Long>()
        }
        val decIsFavourite = try {
            json.decodeFromString<Boolean>(alice2.decrypt(isFavourite) ?: "")
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        val decIsLocked = try {
            json.decodeFromString<Boolean>(alice2.decrypt(isLocked) ?: "")
        } catch (e: Exception) {
            e.printStackTrace()
            true
        }

        return BucketBox(
            id = id,
            uuid = decUuid,
            createdTimestamp = decCreatedTimestamp,
            modifiedTimestamp = decModifiedTimestamp,
            title = decTitle,
            description = decDescription,
            bucketType = decBucketType,
            sortedIdList = decSortedIdList,
            isFavourite = decIsFavourite,
            isLocked = decIsLocked,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BucketBoxEnc) return false

        if (id != other.id) return false
        if (uuid != other.uuid) return false
        if (createdTimestamp != other.createdTimestamp) return false
        if (modifiedTimestamp != other.modifiedTimestamp) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (bucketType != other.bucketType) return false
        if (sortedIdList != other.sortedIdList) return false
        if (isFavourite != other.isFavourite) return false
        if (isLocked != other.isLocked) return false
        if (childList != other.childList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uuid.hashCode()
        result = 31 * result + (createdTimestamp?.hashCode() ?: 0)
        result = 31 * result + (modifiedTimestamp?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + bucketType.hashCode()
        result = 31 * result + sortedIdList.hashCode()
        result = 31 * result + isFavourite.hashCode()
        result = 31 * result + isLocked.hashCode()
        return result
    }

}

