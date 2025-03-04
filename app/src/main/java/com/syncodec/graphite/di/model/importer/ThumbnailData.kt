package com.syncodec.graphite.di.model.importer

import android.graphics.BitmapFactory
import coil3.decode.BitmapFactoryDecoder
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.encodeBase64


sealed class ThumbnailData {

    open fun getAsBitmap(): android.graphics.Bitmap? = null
    open fun getAsBase64(): String? = null

    data class Bitmap(val data: android.graphics.Bitmap) : ThumbnailData() {
        override fun getAsBitmap(): android.graphics.Bitmap? = this.data
        override fun getAsBase64(): String? = data.encodeBase64()
    }

    data class Base64(val data: String) : ThumbnailData() {
        override fun getAsBitmap(): android.graphics.Bitmap? = try {
            data.decodeBase64ToBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        override fun getAsBase64(): String? = data
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
    }
}
