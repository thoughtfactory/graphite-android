package com.syncodec.graphite.presentation.bucketItem2.utils

import android.content.Context
import android.content.Intent
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemBook
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemLink
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemLocation
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemShow
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemTodo
import com.syncodec.graphite.utils.IntentUtil


object BucketItemUtils {

    fun toMarkdown(bucketItemBoxDecrypted: BucketItemBoxDecrypted): String = when (bucketItemBoxDecrypted.bucketItemData) {
        is BucketItemTodo -> bucketItemBoxDecrypted.bucketItemData.toMarkdown()
        is BucketItemBook -> TODO()
        is BucketItemShow -> TODO()
        is BucketItemLink -> TODO()
        is BucketItemLocation -> TODO()
        else -> TODO()
    }


    private fun BucketItemTodo.toMarkdown(): String = ""

    fun toSharableString(bucketItemBoxDecrypted: BucketItemBoxDecrypted): String {
        return when (bucketItemBoxDecrypted.bucketItemData) {
            is BucketItemTodo -> bucketItemBoxDecrypted.bucketItemData.toSharableString(state = bucketItemBoxDecrypted.state)
            is BucketItemBook -> bucketItemBoxDecrypted.bucketItemData.toSharableString(state = bucketItemBoxDecrypted.state)
            is BucketItemShow -> bucketItemBoxDecrypted.bucketItemData.toSharableString(state = bucketItemBoxDecrypted.state)
            is BucketItemLink -> TODO()
            is BucketItemLocation -> TODO()
            else -> TODO()
        }
    }


    fun toSharableString(bucketItemBoxDecryptedList: List<BucketItemBoxDecrypted>): String {
        var str = ""
        bucketItemBoxDecryptedList.forEach { bucketItemBoxDecrypted ->
            when (bucketItemBoxDecrypted.bucketItemData) {
                is BucketItemTodo -> str += bucketItemBoxDecrypted.bucketItemData.toSharableString(state = bucketItemBoxDecrypted.state)
                is BucketItemBook -> str += (bucketItemBoxDecrypted.bucketItemData.toSharableString(state = bucketItemBoxDecrypted.state) + "\n\n")
                is BucketItemShow -> str += (bucketItemBoxDecrypted.bucketItemData.toSharableString(state = bucketItemBoxDecrypted.state) + "\n\n")
                is BucketItemLink -> TODO()
                is BucketItemLocation -> TODO()
                else -> TODO()
            }
        }

        return str
    }

    private fun BucketItemTodo.toSharableString(state: BucketItemBoxDecrypted.State?): String {
        val checkbox = when (state) {
            BucketItemBoxDecrypted.State.Alpha -> "[ ]"
            BucketItemBoxDecrypted.State.Beta -> "[-]"
            BucketItemBoxDecrypted.State.Gamma -> "[x]"
            null -> "[ ]"
        }
        val str = "$checkbox ${this.title}\n${this.description}${if (this.description == null) "\n" else "\n\n"}"

        return str
    }

    private fun BucketItemBook.toSharableString(state: BucketItemBoxDecrypted.State?): String {

        val title = bookTitle() ?: ""
        val isRead = when (state) {
            BucketItemBoxDecrypted.State.Alpha -> "[ ]"
            BucketItemBoxDecrypted.State.Beta -> "[-]"
            BucketItemBoxDecrypted.State.Gamma -> "[✓]"
            else -> "[ ]"
        }
        val url = (this as? BucketItemBook.OpenLibrary)?.getUrl()

        var text = "$isRead $title"
        if (url != null) text += "\n$url"

        return text
    }

    private fun BucketItemShow.toSharableString(state: BucketItemBoxDecrypted.State?): String {

        val title = showTitle() ?: ""
        val isRead = when (state) {
            BucketItemBoxDecrypted.State.Alpha -> "[ ]"
            BucketItemBoxDecrypted.State.Beta -> "[-]"
            BucketItemBoxDecrypted.State.Gamma -> "[✓]"
            else -> "[ ]"
        }
        val url = getUrl()

        var text = "$isRead $title"
        if (url != null) text += "\n$url"

        return text
    }

}
