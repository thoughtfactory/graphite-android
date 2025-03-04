package com.syncodec.graphite.di.modelObjectBox

import com.syncodec.graphite.di.modelObjectBox.converter.BucketTypeConverter
import com.syncodec.graphite.di.modelObjectBox.converter.LongListConverter
import com.syncodec.graphite.di.modelObjectBox.converter.ZonedDateTimeConverter
import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany
import java.time.ZonedDateTime


@Entity
data class BucketBox (
    @Id var id: Long = 0,

    @Convert(converter = ZonedDateTimeConverter::class, dbType = String::class) var createdTimestamp: ZonedDateTime? = ZonedDateTime.now(),
    @Convert(converter = ZonedDateTimeConverter::class, dbType = String::class) var modifiedTimestamp: ZonedDateTime? = ZonedDateTime.now(),

    var title: String? = null,
    var description: String? = null,

    @Convert(converter = BucketTypeConverter::class, dbType = String::class) var bucketType: BucketType = BucketType.Unknown,

    @Convert(converter = LongListConverter::class, dbType = String::class) var sortedIdList: List<Long> = listOf(),

    var isFavourite: Boolean = false,
    var isLocked: Boolean = false,
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

}
