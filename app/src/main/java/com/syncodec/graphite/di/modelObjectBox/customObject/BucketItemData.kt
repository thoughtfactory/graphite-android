package com.syncodec.graphite.di.modelObjectBox.customObject

import android.content.Context
import com.syncodec.graphite.di.network.openGraph.LinkData
import com.syncodec.graphite.di.network.trakt.TraktIDs
import com.syncodec.graphite.utils.alice2.Alice2
import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
@Serializable
sealed class BucketItemData {

    open fun thumbnail(context: Context): Thumbnail? = null

    fun itemTitle() = when (this) {
        is BucketItemTodo -> this.title
        is BucketItemBook.OpenLibrary -> this.title
        is BucketItemBook.Custom -> this.title
        is BucketItemShow.TraktMovie -> this.title
        is BucketItemShow.TraktSeries -> this.title
        is BucketItemLink -> null
        is BucketItemLocation -> this.title
    }

    companion object {

        @Serializable
        sealed class Thumbnail {

            @Serializable
            sealed class File : Thumbnail() {
                abstract val fileName: String

                abstract fun getFile(context: Context): java.io.File?
                abstract fun getAndDecryptFile(context: Context, alice2: Alice2): ByteArray?

                @Serializable
                data class CachedFile(override val fileName: String = Uuid.random().toString()) : File() {
                    override fun getFile(context: Context): java.io.File? {
                        try {
                            val cacheDir = context.cacheDir
                            val thumbnailDir = java.io.File(cacheDir, "thumbnail")
                            thumbnailDir.mkdirs()
                            val cachedFile = java.io.File(thumbnailDir, fileName)
                            cachedFile.createNewFile()
                            return cachedFile
                        } catch (e: Exception) {
                            e.printStackTrace()
                            return null
                        }
                    }

                    override fun getAndDecryptFile(context: Context, alice2: Alice2): ByteArray? {
                        val file = getFile(context)
                        try {
                            val encryptedByteArray = file?.readBytes()
                            return alice2.decrypt(encryptedByteArray)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            return null
                        }
                    }
                }

                @Serializable
                data class PersistentFile(override val fileName: String = Uuid.random().toString()) : File() {
                    override fun getFile(context: Context): java.io.File? {
                        try {
                            val dataDir = context.dataDir
                            val thumbnailDir = java.io.File(dataDir, "thumbnail")
                            thumbnailDir.mkdirs()
                            val cachedFile = java.io.File(thumbnailDir, fileName)
                            cachedFile.createNewFile()
                            return cachedFile
                        } catch (e: Exception) {
                            e.printStackTrace()
                            return null
                        }
                    }

                    override fun getAndDecryptFile(context: Context, alice2: Alice2): ByteArray? {
                        val file = getFile(context)
                        try {
                            val encryptedByteArray = file?.readBytes()
                            return alice2.decrypt(encryptedByteArray)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            return null
                        }
                    }
                }
            }

            @Serializable
            data class Base64(val data: String) : Thumbnail()
        }
    }
}

@Serializable
data class BucketItemTodo(
    val title: String,
    val description: String? = null
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

    abstract fun getUrl(): String?

    @Serializable
    data class OpenLibrary(
        val key: String? = null,
        val title: String? = null,
        val description: String? = null,
        val coverI: Int? = null,
        val authorList: List<String> = listOf(),
        val firstPublishYear: Int? = null,
        val numberOfPages: Int? = null,
        val thumbnail: BucketItemData.Companion.Thumbnail.File? = null
    ) : BucketItemBook() {
        override fun bookTitle(): String? = title
        override fun bookDescription(): String? = description
        override fun primaryAuthor(): String? = authorList.firstOrNull()
        override fun allBookAuthor(): List<String> = authorList
        override fun bookPublicationYear(): Int? = firstPublishYear
        override fun bookNumberOfPages(): Int? = numberOfPages
        override fun thumbnail(context: Context): BucketItemData.Companion.Thumbnail? = thumbnail
        override fun toCustom(): Custom = Custom(
            key = key,
            title = title,
            description = description,
            authorList = authorList,
            firstPublishYear = firstPublishYear,
            numberOfPages = numberOfPages,
            thumbnail = thumbnail,
        )

        override fun getUrl() = key?.let { "https://openlibrary.org$it" }
    }

    @Serializable
    data class Custom(
        val key: String? = null,
        val title: String? = null,
        val description: String? = null,
        val authorList: List<String> = listOf(),
        val firstPublishYear: Int? = null,
        val numberOfPages: Int? = null,
        val thumbnail: BucketItemData.Companion.Thumbnail.File? = null
    ) : BucketItemBook() {
        override fun bookTitle(): String? = title
        override fun bookDescription(): String? = description
        override fun primaryAuthor(): String? = authorList.firstOrNull()
        override fun allBookAuthor(): List<String> = authorList
        override fun bookPublicationYear(): Int? = firstPublishYear
        override fun bookNumberOfPages(): Int? = numberOfPages
        override fun thumbnail(context: Context): BucketItemData.Companion.Thumbnail? = thumbnail

        override fun toCustom(): Custom = this

        override fun getUrl() = null
    }
}

@Serializable
sealed class BucketItemShow : BucketItemData() {

    enum class ShowType { Movie, Series }

    abstract fun showTitle(): String?
    abstract fun showOverview(): String?
    abstract fun showTagline(): String?
    abstract fun releaseYear(): Int?

    abstract fun getUrl(): String?

    @Serializable
    data class TraktMovie(
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
        override fun showTitle(): String? = title
        override fun showOverview(): String? = overview
        override fun showTagline(): String? = tagline
        override fun releaseYear(): Int? = year
        override fun thumbnail(context: Context): BucketItemData.Companion.Thumbnail? = thumbnail

        override fun getUrl() = ids?.trakt?.let { "https://trakt.tv/movies/$it" }
    }

    @Serializable
    data class TraktSeries(
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
        override fun showTitle(): String? = title
        override fun showOverview(): String? = overview
        override fun showTagline(): String? = tagline
        override fun releaseYear(): Int? = year
        override fun thumbnail(context: Context): BucketItemData.Companion.Thumbnail? = thumbnail

        override fun getUrl() = ids?.trakt?.let { "https://trakt.tv/shows/$it" }
    }
}

@Serializable
data class BucketItemLink(
    val linkData: LinkData
) : BucketItemData()

@Serializable
data class BucketItemLocation(
    val title: String,
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
