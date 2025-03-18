package com.syncodec.graphite.di.modelObjectBox

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
import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany


@Entity
data class BucketBox(
    @Id
    var id: Long = 0,

    @Convert(converter = EncryptedUuidConverter::class, dbType = String::class) var uuid: EncryptedUuid? = null,

    @Convert(converter = EncryptedZonedDateTimeConverter::class, dbType = String::class) var createdTimestamp: EncryptedZonedDateTime? = null,
    @Convert(converter = EncryptedZonedDateTimeConverter::class, dbType = String::class) var modifiedTimestamp: EncryptedZonedDateTime? = null,

    @Convert(converter = EncryptedStringConverter::class, dbType = String::class) var title: EncryptedString? = null,
    @Convert(converter = EncryptedStringConverter::class, dbType = String::class) var description: EncryptedString? = null,

    @Convert(converter = EncryptedBucketTypeConverter::class, dbType = String::class) var bucketType: EncryptedBucketType? = null,

    @Convert(converter = EncryptedLongListConverter::class, dbType = String::class) var sortedIdList: EncryptedLongList? = null,

    @Convert(converter = EncryptedBooleanConverter::class, dbType = String::class) var isFavourite: EncryptedBoolean? = null,
    @Convert(converter = EncryptedBooleanConverter::class, dbType = String::class) var isLocked: EncryptedBoolean? = null,
) {
    @Backlink(to = "parent")
    lateinit var childList: ToMany<BucketItemBox>

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BucketBox) return false

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

