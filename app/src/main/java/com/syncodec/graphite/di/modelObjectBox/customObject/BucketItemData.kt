package com.syncodec.graphite.di.modelObjectBox.customObject

import android.content.Context
import com.syncodec.graphite.di.modelObjectBox.encryptable.EncryptedBucketItemData
import com.syncodec.graphite.di.network.openGraph.LinkData
import com.syncodec.graphite.di.network.trakt.TraktIDs
import com.syncodec.graphite.utils.alice2.Alice2
import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
sealed class BucketItemData {

    abstract val state: State
    open fun thumbnail(context: Context): Thumbnail? = null

    fun nextState(): State = when (state) {
        State.Alpha -> State.Beta
        State.Beta -> State.Gamma
        State.Gamma -> State.Alpha
    }

    fun copyWithState(newState: State) = when (this) {
        is BucketItemTodo -> copy(state = newState)
        is BucketItemBook.OpenLibrary -> copy(state = newState)
        is BucketItemBook.Custom -> copy(state = newState)
        is BucketItemShow.TraktMovie -> copy(state = newState)
        is BucketItemShow.TraktSeries -> copy(state = newState)
        is BucketItemLink -> copy(state = newState)
        is BucketItemLocation -> copy(state = newState)
    }

    fun itemTitle() = when (this) {
        is BucketItemTodo -> this.title
        is BucketItemBook.OpenLibrary -> this.title
        is BucketItemBook.Custom -> this.title
        is BucketItemShow.TraktMovie -> this.title
        is BucketItemShow.TraktSeries -> this.title
        is BucketItemLink -> null
        is BucketItemLocation -> this.title
    }

    fun encrypt(alice2: Alice2) = EncryptedBucketItemData.fromBucketItemData(this, alice2)

    @Serializable
    enum class State { Alpha, Beta, Gamma }

    companion object {

        @Serializable
        sealed class Thumbnail {

            @Serializable
            data object File : Thumbnail()

            @Serializable
            data class Base64(val data: String) : Thumbnail()
        }
    }
}

@Serializable
data class BucketItemTodo(
    val title: String,
    override val state: State,
) : BucketItemData()

@Serializable
sealed class BucketItemBook : BucketItemData() {

    abstract fun bookTitle(): String?
    abstract fun bookDescription(): String?
    abstract fun primaryAuthor(): String?
    abstract fun allBookAuthor(): List<String>
    abstract fun bookPublicationYear(): Int?
    abstract fun bookNumberOfPages(): Int?
    abstract fun toCustom(): Custom

    @Serializable
    data class OpenLibrary(
        override val state: State = State.Alpha,
        val key: String? = null,
        val title: String? = null,
        val description: String? = null,
        val coverI: Int? = null,
        val authorList: List<String> = listOf(),
        val firstPublishYear: Int? = null,
        val numberOfPages: Int? = null,
        val thumbnail: BucketItemData.Companion.Thumbnail.Base64? = null
    ) : BucketItemBook() {
        override fun bookTitle(): String? = title
        override fun bookDescription(): String? = description
        override fun primaryAuthor(): String? = authorList.firstOrNull()
        override fun allBookAuthor(): List<String> = authorList
        override fun bookPublicationYear(): Int? = firstPublishYear
        override fun bookNumberOfPages(): Int? = numberOfPages
        override fun thumbnail(context: Context): BucketItemData.Companion.Thumbnail? = thumbnail
        override fun toCustom(): Custom = Custom(
            state = state,
            key = key,
            title = title,
            description = description,
            authorList = authorList,
            firstPublishYear = firstPublishYear,
            numberOfPages = numberOfPages,
            thumbnail = thumbnail,
        )
    }

    @Serializable
    data class Custom(
        override val state: State = State.Alpha,
        val key: String? = null,
        val title: String? = null,
        val description: String? = null,
        val authorList: List<String> = listOf(),
        val firstPublishYear: Int? = null,
        val numberOfPages: Int? = null,
        val thumbnail: BucketItemData.Companion.Thumbnail.Base64? = null
    ) : BucketItemBook() {
        override fun bookTitle(): String? = title
        override fun bookDescription(): String? = description
        override fun primaryAuthor(): String? = authorList.firstOrNull()
        override fun allBookAuthor(): List<String> = authorList
        override fun bookPublicationYear(): Int? = firstPublishYear
        override fun bookNumberOfPages(): Int? = numberOfPages
        override fun thumbnail(context: Context): BucketItemData.Companion.Thumbnail? = thumbnail

        override fun toCustom(): Custom = this
    }
}

@Serializable
sealed class BucketItemShow : BucketItemData() {

    enum class ShowType { Movie, Series }

    open fun releaseYear(): Int? = null

    @Serializable
    data class TraktMovie(
        override val state: State = State.Alpha,
        val title: String? = null,
        val year: Int? = null,
        val ids: TraktIDs? = null,
        val tagline: String? = null,
        val overview: String? = null,
        val released: String? = null,
        val runtime: Int? = null,
        val country: String? = null,
        val trailer: String? = null,
        val homepage: String? = null,
        val genres: List<String> = listOf(),
        val thumbnail: BucketItemData.Companion.Thumbnail? = null
    ) : BucketItemShow() {
        override fun releaseYear(): Int? = year
        override fun thumbnail(context: Context): BucketItemData.Companion.Thumbnail? = thumbnail
    }

    @Serializable
    data class TraktSeries(
        override val state: State = State.Alpha,
        val title: String? = null,
        val year: Int? = null,
        val ids: TraktIDs? = null,
        val tagline: String? = null,
        val overview: String? = null,
        val firstAired: String? = null,
        val runtime: Int? = null,
        val certification: String? = null,
        val country: String? = null,
        val trailer: String? = null,
        val homepage: String? = null,
        val genres: List<String> = listOf(),
        val thumbnail: BucketItemData.Companion.Thumbnail? = null
    ) : BucketItemShow() {
        override fun releaseYear(): Int? = year
        override fun thumbnail(context: Context): BucketItemData.Companion.Thumbnail? = thumbnail
    }
}

@Serializable
data class BucketItemLink(
    override val state: State,
    val linkData: LinkData
) : BucketItemData()

@Serializable
data class BucketItemLocation(
    val title: String,
    override val state: State,
) : BucketItemData()


@OptIn(InternalSerializationApi::class)
class BucketItemDataConverter : PropertyConverter<BucketItemData, String> {
    private val json = Json

    @OptIn(InternalSerializationApi::class)
    override fun convertToEntityProperty(databaseValue: String?): BucketItemData? {
        try {
            databaseValue ?: return null
            return json.decodeFromString(deserializer = BucketItemData.serializer(), string = databaseValue)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    @OptIn(InternalSerializationApi::class)
    override fun convertToDatabaseValue(entityProperty: BucketItemData?): String? {
        try {
            entityProperty ?: return null
            return json.encodeToString(serializer = BucketItemData.serializer(), value = entityProperty)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
