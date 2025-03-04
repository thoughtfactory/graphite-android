package com.syncodec.graphite.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import coil3.ImageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap


object FileUtil {

    fun Uri.toByteArray(context: Context): ByteArray? {
        try {
            var byteArray: ByteArray? = null
            context.contentResolver.openInputStream(this)?.use { byteArray = it.readBytes() }
            return byteArray
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun ByteArray.toBitmap( context: Context): Bitmap? {
        try {
            val imageRequest = ImageRequest
                .Builder(context = context)
                .data(data = this)
                .build()

            val imageLoader = ImageLoader(context = context)
                .enqueue(request = imageRequest)

            val imageResult = imageLoader
                .job
                .await()

            return when (imageResult) {
                is ErrorResult -> {
                    imageResult.throwable.printStackTrace()
                    null
                }
                is SuccessResult -> imageResult.image.toBitmap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    const val TAG = "FileUtil"
}
