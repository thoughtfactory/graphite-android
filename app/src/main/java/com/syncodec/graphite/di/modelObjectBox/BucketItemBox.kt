package com.syncodec.graphite.di.modelObjectBox

import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemBook
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedBoolean
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedBooleanConverter
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedBucketItemData
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedBucketItemDataConverter
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedUuid
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedUuidConverter
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedZonedDateTime
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedZonedDateTimeConverter
import com.syncodec.graphite.di.modelObjectBox.encryptable.encrypt
import com.syncodec.graphite.utils.alice2.Alice2
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import java.time.ZonedDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@Entity
data class BucketItemBox(
    @Id var id: Long = 0,

    @Convert(converter = EncryptedUuidConverter::class, dbType = String::class) var uuid: EncryptedUuid? = null,

    @Convert(converter = EncryptedZonedDateTimeConverter::class, dbType = String::class) var createdTimestamp: EncryptedZonedDateTime? = null,
    @Convert(converter = EncryptedZonedDateTimeConverter::class, dbType = String::class) var modifiedTimestamp: EncryptedZonedDateTime? = null,

    @Convert(converter = EncryptedBucketItemDataConverter::class, dbType = String::class) var bucketItemData: EncryptedBucketItemData? = null,

    @Convert(converter = EncryptedBooleanConverter::class, dbType = String::class) var isFavourite: EncryptedBoolean? = null,
    @Convert(converter = EncryptedBooleanConverter::class, dbType = String::class) var isLocked: EncryptedBoolean? = null,
) {
    lateinit var parent: ToOne<BucketBox>

    @OptIn(ExperimentalUuidApi::class)
    fun decrypt(alice2: Alice2) = BucketItemBoxPlain(
        id = id,
        uuid = uuid?.decrypt(alice2),
        createdTimestamp = createdTimestamp?.decrypt(alice2),
        modifiedTimestamp = modifiedTimestamp?.decrypt(alice2),
        bucketItemData = bucketItemData?.decrypt(alice2),
        isFavourite = isFavourite?.decrypt(alice2),
        isLocked = isLocked?.decrypt(alice2)
    )

    companion object {
        val randomTodo
            get() = BucketItemBox()

        val randomLink
            get() = BucketItemBox()

        fun randomBook(alice2: Alice2) = BucketItemBox(bucketItemData = BucketItemBook.OpenLibrary(state = BucketItemData.State.Alpha).encrypt(alice2 = alice2))
    }
}

@OptIn(ExperimentalUuidApi::class)
data class BucketItemBoxPlain(
    var id: Long = 0,
    var uuid: Uuid? = null,

    var createdTimestamp: ZonedDateTime? = null,
    var modifiedTimestamp: ZonedDateTime? = null,

    var bucketItemData: BucketItemData? = null,

    var isFavourite: Boolean? = null,
    var isLocked: Boolean? = null,
) {
    fun encrypt(alice2: Alice2) = BucketItemBox(
        id = id,
        uuid = uuid?.encrypt(alice2),
        createdTimestamp = createdTimestamp?.encrypt(alice2),
        modifiedTimestamp = modifiedTimestamp?.encrypt(alice2),
        bucketItemData = bucketItemData?.encrypt(alice2),
        isFavourite = isFavourite?.encrypt(alice2),
        isLocked = isLocked?.encrypt(alice2),
    )
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

