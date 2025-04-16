package com.syncodec.graphite.di.modelObjectBox

import android.util.Log
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemLink
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemTodo
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedBoolean
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedBooleanConverter
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedBucketItemBoxState
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedBucketItemBoxStateConverter
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedBucketItemData
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedBucketItemDataConverter
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedUuid
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedUuidConverter
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedZonedDateTime
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedZonedDateTimeConverter
import com.syncodec.graphite.di.modelObjectBox.encryptable.encrypt
import com.syncodec.graphite.di.modelObjectBox.structureExtension.Modifiable
import com.syncodec.graphite.di.network.openGraph.LinkData
import com.syncodec.graphite.utils.alice2.Alice2
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(InternalSerializationApi::class)
@Entity
data class BucketItemBoxEncrypted(
    @Id override var id: Long = 0,

    @Convert(converter = EncryptedUuidConverter::class, dbType = String::class) var uuid: EncryptedUuid? = null,

    @Convert(converter = EncryptedZonedDateTimeConverter::class, dbType = String::class) var createdTimestamp: EncryptedZonedDateTime? = null,
    @Convert(converter = EncryptedZonedDateTimeConverter::class, dbType = String::class) var modifiedTimestamp: EncryptedZonedDateTime? = null,

    @Convert(converter = EncryptedBucketItemDataConverter::class, dbType = String::class) var bucketItemData: EncryptedBucketItemData? = null,
    @Convert(converter = EncryptedBucketItemBoxStateConverter::class, dbType = String::class) var state: EncryptedBucketItemBoxState? = null,

    @Convert(converter = EncryptedBooleanConverter::class, dbType = String::class) var isFavourite: EncryptedBoolean? = null,
    @Convert(converter = EncryptedBooleanConverter::class, dbType = String::class) override var isLocked: EncryptedBoolean? = null,
) : EncryptedBox() {
    lateinit var parent: ToOne<BucketBoxEncrypted>

    @OptIn(ExperimentalUuidApi::class)
    override fun decrypt(alice2: Alice2, default: DecryptedBox?): BucketItemBoxDecrypted? = BucketItemBoxDecrypted(
        id = id,
        uuid = uuid?.decrypt(alice2) ?: Uuid.random(),
        createdTimestamp = createdTimestamp?.decrypt(alice2),
        modifiedTimestamp = modifiedTimestamp?.decrypt(alice2),
        bucketItemData = bucketItemData?.decrypt(alice2),
        state = state?.decrypt(alice2),
        isFavourite = isFavourite?.decrypt(alice2),
        isLocked = isLocked?.decrypt(alice2),
        parent = parent.target?.decrypt(alice2)
    )

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (uuid?.hashCode() ?: 0)
        result = 31 * result + (createdTimestamp?.hashCode() ?: 0)
        result = 31 * result + (modifiedTimestamp?.hashCode() ?: 0)
        result = 31 * result + (bucketItemData?.hashCode() ?: 0)
        result = 31 * result + (state?.hashCode() ?: 0)
        result = 31 * result + (isFavourite?.hashCode() ?: 0)
        result = 31 * result + (isLocked?.hashCode() ?: 0)
        result = 31 * result + parent.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BucketItemBoxEncrypted) return false

        if (id != other.id) return false
        if (uuid != other.uuid) return false
        if (createdTimestamp != other.createdTimestamp) return false
        if (modifiedTimestamp != other.modifiedTimestamp) return false
        if (bucketItemData != other.bucketItemData) return false
        if (state != other.state) return false
        if (isFavourite != other.isFavourite) return false
        if (isLocked != other.isLocked) return false
        if (parent != other.parent) return false

        return true
    }
}

@OptIn(ExperimentalUuidApi::class)
data class BucketItemBoxDecrypted(
    override val id: Long = 0,

    val uuid: Uuid = Uuid.random(),

    val createdTimestamp: ZonedDateTime? = null,
    val modifiedTimestamp: ZonedDateTime? = null,

    val bucketItemData: BucketItemData? = null,
    val state: State? = null,

    val isFavourite: Boolean? = null,
    val isLocked: Boolean? = null,

    val parent: BucketBoxDecrypted? = null
) : DecryptedBox(), Modifiable {

    override fun encrypt(alice2: Alice2): BucketItemBoxEncrypted? = BucketItemBoxEncrypted(
        id = id,
        uuid = uuid.encrypt(alice2),
        createdTimestamp = createdTimestamp?.encrypt(alice2),
        modifiedTimestamp = modifiedTimestamp?.encrypt(alice2),
        bucketItemData = bucketItemData?.encrypt(alice2),
        state = state?.encrypt(alice2),
        isFavourite = isFavourite?.encrypt(alice2),
        isLocked = isLocked?.encrypt(alice2),
    ).apply {
        if (this@BucketItemBoxDecrypted.parent != null) this.parent.target = this@BucketItemBoxDecrypted.parent.encrypt(alice2)
    }

    override fun modifyDateTime(): BucketItemBoxDecrypted = copy(modifiedTimestamp = ZonedDateTime.now())

    fun nextState() = when (state) {
        State.Alpha -> State.Beta
        State.Beta -> State.Gamma
        State.Gamma -> State.Alpha
        else -> State.Alpha
    }

    @Serializable
    enum class State { Alpha, Beta, Gamma }

    companion object {
        val newInstance: BucketItemBoxDecrypted
            get() = BucketItemBoxDecrypted(
                uuid = Uuid.random(),
                createdTimestamp = ZonedDateTime.now(),
                modifiedTimestamp = ZonedDateTime.now(),
                state = State.Alpha,
                isFavourite = false,
                isLocked = false
            )

        val randomTodo
            get() = newInstance.copy(bucketItemData = BucketItemTodo(title = "title_${Random.nextInt(10)}"))

        val randomLink
            get() = newInstance.copy(bucketItemData = BucketItemLink(linkData = LinkData(title = "title_${Random.nextInt(10)}")))
    }
}


fun BucketBoxEncrypted.BucketType.bucketTypeToAlphaText(): Int = when (this) {
    BucketBoxEncrypted.BucketType.Todo -> R.string.to_do
    BucketBoxEncrypted.BucketType.Book -> R.string.to_read
    BucketBoxEncrypted.BucketType.Show -> R.string.to_watch
    BucketBoxEncrypted.BucketType.Link -> R.string.to_do
    BucketBoxEncrypted.BucketType.Location -> R.string.to_visit
    BucketBoxEncrypted.BucketType.Unknown -> R.string.to_do
}

fun BucketBoxEncrypted.BucketType.bucketTypeToBetaText(): Int = when (this) {
    BucketBoxEncrypted.BucketType.Todo -> R.string.doing
    BucketBoxEncrypted.BucketType.Book -> R.string.reading
    BucketBoxEncrypted.BucketType.Show -> R.string.watching
    BucketBoxEncrypted.BucketType.Link -> R.string.doing
    BucketBoxEncrypted.BucketType.Location -> R.string.visiting
    BucketBoxEncrypted.BucketType.Unknown -> R.string.doing
}

fun BucketBoxEncrypted.BucketType.bucketTypeToGammaText(): Int = when (this) {
    BucketBoxEncrypted.BucketType.Todo -> R.string.done
    BucketBoxEncrypted.BucketType.Book -> R.string.read
    BucketBoxEncrypted.BucketType.Show -> R.string.watched
    BucketBoxEncrypted.BucketType.Link -> R.string.done
    BucketBoxEncrypted.BucketType.Location -> R.string.visited
    BucketBoxEncrypted.BucketType.Unknown -> R.string.done
}


fun BucketBoxEncrypted.BucketType.bucketTypeToBetaIcon(): Int = when (this) {
    BucketBoxEncrypted.BucketType.Todo -> R.drawable.ic_fa_todo
    BucketBoxEncrypted.BucketType.Book -> R.drawable.ic_fa_book_open
    BucketBoxEncrypted.BucketType.Show -> R.drawable.ic_fa_todo
    BucketBoxEncrypted.BucketType.Link -> R.drawable.ic_fa_todo
    BucketBoxEncrypted.BucketType.Location -> R.drawable.ic_fa_todo
    BucketBoxEncrypted.BucketType.Unknown -> R.drawable.ic_fa_todo
}

fun BucketBoxEncrypted.BucketType.bucketTypeToBetaSelectedIcon(): Int = when (this) {
    BucketBoxEncrypted.BucketType.Todo -> R.drawable.ic_fa_todo_duotone
    BucketBoxEncrypted.BucketType.Book -> R.drawable.ic_fa_book_open_duotone
    BucketBoxEncrypted.BucketType.Show -> R.drawable.ic_fa_todo
    BucketBoxEncrypted.BucketType.Link -> R.drawable.ic_fa_todo
    BucketBoxEncrypted.BucketType.Location -> R.drawable.ic_fa_todo
    BucketBoxEncrypted.BucketType.Unknown -> R.drawable.ic_fa_todo
}

