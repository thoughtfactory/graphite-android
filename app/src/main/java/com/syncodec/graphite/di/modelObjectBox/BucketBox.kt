package com.syncodec.graphite.di.modelObjectBox

import androidx.annotation.WorkerThread
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted.BucketType
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedBoolean
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedBooleanConverter
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedBucketType
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedBucketTypeConverter
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedLongList
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedLongListConverter
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedString
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedStringConverter
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedUuid
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedUuidConverter
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedZonedDateTime
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedZonedDateTimeConverter
import com.syncodec.graphite.di.modelObjectBox.encryptable.encrypt
import com.syncodec.graphite.di.modelObjectBox.structureExtension.Modifiable
import com.syncodec.graphite.utils.alice2.Alice2
import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany
import java.time.ZonedDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@Entity
data class BucketBoxEncrypted(
    @Id override var id: Long = 0,

    @Convert(converter = EncryptedUuidConverter::class, dbType = String::class) var uuid: EncryptedUuid? = null,

    @Convert(converter = EncryptedZonedDateTimeConverter::class, dbType = String::class) var createdTimestamp: EncryptedZonedDateTime? = null,
    @Convert(converter = EncryptedZonedDateTimeConverter::class, dbType = String::class) var modifiedTimestamp: EncryptedZonedDateTime? = null,

    @Convert(converter = EncryptedStringConverter::class, dbType = String::class) var title: EncryptedString? = null,
    @Convert(converter = EncryptedStringConverter::class, dbType = String::class) var description: EncryptedString? = null,

    @Convert(converter = EncryptedBucketTypeConverter::class, dbType = String::class) var bucketType: EncryptedBucketType? = null,

    @Convert(converter = EncryptedLongListConverter::class, dbType = String::class) var sortedIdList: EncryptedLongList? = null,

    @Convert(converter = EncryptedBooleanConverter::class, dbType = String::class) var isFavourite: EncryptedBoolean? = null,
    @Convert(converter = EncryptedBooleanConverter::class, dbType = String::class) override var isLocked: EncryptedBoolean? = null,
) : EncryptedBox() {
    @Backlink(to = "parent")
    lateinit var childList: ToMany<BucketItemBoxEncrypted>

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

    @OptIn(ExperimentalUuidApi::class)
    override fun decrypt(alice2: Alice2, default: DecryptedBox?): BucketBoxDecrypted? = BucketBoxDecrypted(
        id = id,
        uuid = uuid?.decrypt(alice2) ?: Uuid.random(),
        createdTimestamp = createdTimestamp?.decrypt(alice2),
        modifiedTimestamp = modifiedTimestamp?.decrypt(alice2),
        title = title?.decrypt(alice2),
        description = description?.decrypt(alice2),
        bucketType = bucketType?.decrypt(alice2),
        sortedIdList = sortedIdList?.decrypt(alice2),
        isFavourite = isFavourite?.decrypt(alice2),
        isLocked = isLocked?.decrypt(alice2),
    )

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (uuid?.hashCode() ?: 0)
        result = 31 * result + (createdTimestamp?.hashCode() ?: 0)
        result = 31 * result + (modifiedTimestamp?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (bucketType?.hashCode() ?: 0)
        result = 31 * result + (sortedIdList?.hashCode() ?: 0)
        result = 31 * result + (isFavourite?.hashCode() ?: 0)
        result = 31 * result + (isLocked?.hashCode() ?: 0)
        result = 31 * result + childList.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BucketBoxEncrypted) return false

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
}

@OptIn(ExperimentalUuidApi::class)
data class BucketBoxDecrypted(
    override val id: Long = 0,

    val uuid: Uuid = Uuid.random(),

    val createdTimestamp: ZonedDateTime? = null,
    val modifiedTimestamp: ZonedDateTime? = null,

    val title: String? = null,
    val description: String? = null,

    val bucketType: BucketType? = null,

    val sortedIdList: List<Long>? = null,

    val isFavourite: Boolean? = null,
    val isLocked: Boolean? = null,
) : DecryptedBox(), Modifiable {

    @WorkerThread
    override fun encrypt(alice2: Alice2): BucketBoxEncrypted? = BucketBoxEncrypted(
        id = id,
        uuid = uuid.encrypt(alice2),
        createdTimestamp = createdTimestamp?.encrypt(alice2),
        modifiedTimestamp = modifiedTimestamp?.encrypt(alice2),
        title = title?.encrypt(alice2),
        description = description?.encrypt(alice2),
        bucketType = bucketType?.encrypt(alice2),
        sortedIdList = sortedIdList?.encrypt(alice2),
        isFavourite = isFavourite?.encrypt(alice2),
        isLocked = isLocked?.encrypt(alice2)
    )

    override fun modifyDateTime(): BucketBoxDecrypted = copy(modifiedTimestamp = ZonedDateTime.now())

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (isFavourite?.hashCode() ?: 0)
        result = 31 * result + (isLocked?.hashCode() ?: 0)
        result = 31 * result + (uuid?.hashCode() ?: 0)
        result = 31 * result + (createdTimestamp?.hashCode() ?: 0)
        result = 31 * result + (modifiedTimestamp?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (bucketType?.hashCode() ?: 0)
        result = 31 * result + (sortedIdList?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BucketBoxDecrypted) return false

        if (id != other.id) return false
        if (isFavourite != other.isFavourite) return false
        if (isLocked != other.isLocked) return false
        if (uuid != other.uuid) return false
        if (createdTimestamp != other.createdTimestamp) return false
        if (modifiedTimestamp != other.modifiedTimestamp) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (bucketType != other.bucketType) return false
        if (sortedIdList != other.sortedIdList) return false

        return true
    }

    companion object {
        val newInstance: BucketBoxDecrypted
            get() = BucketBoxDecrypted(
                uuid = Uuid.random(),
                createdTimestamp = ZonedDateTime.now(),
                modifiedTimestamp = ZonedDateTime.now(),
                sortedIdList = listOf(),
                isFavourite = false,
                isLocked = false
            )
    }
}
