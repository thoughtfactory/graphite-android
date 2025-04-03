package com.syncodec.graphite.di.model.importer

import android.content.Context
import android.graphics.BitmapFactory
import coil3.decode.BitmapFactoryDecoder
import com.syncodec.graphite.di.modelObjectBox.customObject.BucketItemData
import com.syncodec.graphite.utils.alice2.Alice2
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.encodeBase64
import com.syncodec.graphite.utils.toByteArray


sealed class ThumbnailData {

    open fun getAsBitmap(): android.graphics.Bitmap? = null
    open fun getAsBase64(): String? = null
    open fun getAsByteArray(): ByteArray? = null

    data class Bitmap(val data: android.graphics.Bitmap) : ThumbnailData() {
        override fun getAsBitmap(): android.graphics.Bitmap? = this.data
        override fun getAsBase64(): String? = data.encodeBase64()
        override fun getAsByteArray(): ByteArray? = data.toByteArray()
    }

    data class Base64(val data: String) : ThumbnailData() {
        override fun getAsBitmap(): android.graphics.Bitmap? = try {
            data.decodeBase64ToBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        override fun getAsBase64(): String? = data

        override fun getAsByteArray(): ByteArray? = data.encodeToByteArray()
    }

    data class File(val data: java.io.File) : ThumbnailData() {
        override fun getAsBitmap(): android.graphics.Bitmap? = try {
            BitmapFactory.decodeFile(data.path)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        override fun getAsBase64(): String? {
            return super.getAsBase64()
        }

        override fun getAsByteArray(): ByteArray? = data.readBytes()
    }

    data class EncryptedFile(val file: BucketItemData.Companion.Thumbnail.File) : ThumbnailData() {
//        fun getFile(context: Context): java.io.File? {
//            try {
//                val cacheDir = context.cacheDir
//                val thumbnailDir = java.io.File(cacheDir, "thumbnail")
//                thumbnailDir.mkdirs()
//                val cachedFile = java.io.File(thumbnailDir, fileName)
//                cachedFile.createNewFile()
//                return cachedFile
//            } catch (e: Exception) {
//                e.printStackTrace()
//                return null
//            }
//        }

        fun getAndDecryptFile(context: Context, alice2: Alice2): ByteArray? {
            return file.getAndDecryptFile(context,alice2)
//            val file = getFile(context)
//            try {
//                val encryptedByteArray = file?.readBytes()
//                return alice2.decrypt(encryptedByteArray)
//            } catch (e: Exception) {
//                e.printStackTrace()
//                return null
//            }
        }
    }
}
