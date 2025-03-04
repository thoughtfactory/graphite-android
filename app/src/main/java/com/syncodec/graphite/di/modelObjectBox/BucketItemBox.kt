package com.syncodec.graphite.di.modelObjectBox

import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.converter.ZonedDateTimeConverter
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemDataConverter
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemLink
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemTodo
import com.syncodec.graphite.di.network.openGraph.LinkData
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import kotlinx.serialization.InternalSerializationApi
import java.time.ZonedDateTime
import kotlin.random.Random


@OptIn(InternalSerializationApi::class)
@Entity
data class BucketItemBox(
    @Id var id: Long = 0,

    @Convert(converter = ZonedDateTimeConverter::class, dbType = String::class) var createdTimestamp: ZonedDateTime? = ZonedDateTime.now(),
    @Convert(converter = ZonedDateTimeConverter::class, dbType = String::class) var modifiedTimestamp: ZonedDateTime? = ZonedDateTime.now(),

    @Convert(converter = BucketItemDataConverter::class, dbType = String::class) var bucketItemData: BucketItemData? = null,

    var isFavourite: Boolean = false,
    var isLocked: Boolean = false,
) {
    lateinit var parent: ToOne<BucketBox>

    companion object {
        val randomTodo
            get() = BucketItemBox(bucketItemData = BucketItemTodo(title = "title_${Random.nextInt(10)}", state = BucketItemData.State.Alpha))

        val randomLink
            get() = BucketItemBox(bucketItemData = BucketItemLink(state = BucketItemData.State.Alpha, linkData = LinkData(title = "title_${Random.nextInt(10)}")))
    }
}


fun BucketBox.BucketType.bucketTypeToAlphaText(): Int = when (this) {
    BucketBox.BucketType.Todo -> R.string.to_do
    BucketBox.BucketType.Book -> R.string.to_read
    BucketBox.BucketType.Show -> R.string.to_watch
    BucketBox.BucketType.Link -> R.string.to_do
    BucketBox.BucketType.Location -> R.string.to_visit
    BucketBox.BucketType.Unknown -> R.string.to_do
}

fun BucketBox.BucketType.bucketTypeToBetaText(): Int = when (this) {
    BucketBox.BucketType.Todo -> R.string.doing
    BucketBox.BucketType.Book -> R.string.reading
    BucketBox.BucketType.Show -> R.string.watching
    BucketBox.BucketType.Link -> R.string.doing
    BucketBox.BucketType.Location -> R.string.visiting
    BucketBox.BucketType.Unknown -> R.string.doing
}

fun BucketBox.BucketType.bucketTypeToGammaText(): Int = when (this) {
    BucketBox.BucketType.Todo -> R.string.done
    BucketBox.BucketType.Book -> R.string.read
    BucketBox.BucketType.Show -> R.string.watched
    BucketBox.BucketType.Link -> R.string.done
    BucketBox.BucketType.Location -> R.string.visited
    BucketBox.BucketType.Unknown -> R.string.done
}


fun BucketBox.BucketType.bucketTypeToBetaIcon(): Int = when (this) {
    BucketBox.BucketType.Todo -> R.drawable.ic_fa_todo
    BucketBox.BucketType.Book -> R.drawable.ic_fa_book_open
    BucketBox.BucketType.Show -> R.drawable.ic_fa_todo
    BucketBox.BucketType.Link -> R.drawable.ic_fa_todo
    BucketBox.BucketType.Location -> R.drawable.ic_fa_todo
    BucketBox.BucketType.Unknown -> R.drawable.ic_fa_todo
}

fun BucketBox.BucketType.bucketTypeToBetaSelectedIcon(): Int = when (this) {
    BucketBox.BucketType.Todo -> R.drawable.ic_fa_todo_duotone
    BucketBox.BucketType.Book -> R.drawable.ic_fa_book_open_duotone
    BucketBox.BucketType.Show -> R.drawable.ic_fa_todo
    BucketBox.BucketType.Link -> R.drawable.ic_fa_todo
    BucketBox.BucketType.Location -> R.drawable.ic_fa_todo
    BucketBox.BucketType.Unknown -> R.drawable.ic_fa_todo
}

