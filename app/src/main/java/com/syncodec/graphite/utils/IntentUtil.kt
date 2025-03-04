package com.syncodec.graphite.utils

import android.content.Context
import android.content.Intent
import com.syncodec.graphite.di.network.openLibrary.OLBookSearchResult
import com.syncodec.graphite.di.network.trakt.TraktShowSearchResult
import com.syncodec.graphite.presentation.bucket2.BucketActivity2
import com.syncodec.graphite.presentation.bucketItem2.BucketItemActivity2
import kotlinx.serialization.json.Json


object IntentUtil {

    val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            classDiscriminator = "klass"
        }
    }

    enum class IntentKey {
        BucketId,
        BucketItemId
    }

    enum class IntentData {
        BucketItemActivityData
    }

    fun launchBucketActivity(context: Context, bucketId: Long) {
        Intent(context, BucketActivity2::class.java).apply {
            putExtra(IntentKey.BucketId.name, bucketId)
            context.startActivity(this)
        }
    }

    fun launchBucketItemActivity(context: Context, bucketItemActivityData: BucketItemActivityData) {
        Intent(context, BucketItemActivity2::class.java).apply {
            putExtra(IntentData.BucketItemActivityData.name, json.encodeToString(value = bucketItemActivityData))
            context.startActivity(this)
        }
    }

    fun launchBookBucketItemActivity(context: Context, bucketId: Long) {

    }


    @kotlinx.serialization.Serializable
    sealed class BucketItemActivityData {

        abstract val parentId: Long

        @kotlinx.serialization.Serializable
        sealed class NewItem : BucketItemActivityData() {
            @kotlinx.serialization.Serializable
            data class BookItem(override val parentId: Long, val olBookSearchResult: OLBookSearchResult) : NewItem()

            @kotlinx.serialization.Serializable
            data class ShowItem(override val parentId: Long, val traktShowSearchResult: TraktShowSearchResult) : NewItem()
        }

        @kotlinx.serialization.Serializable
        data class LocalItem(override val parentId: Long, val bucketItemId: Long) : BucketItemActivityData()
    }
}
