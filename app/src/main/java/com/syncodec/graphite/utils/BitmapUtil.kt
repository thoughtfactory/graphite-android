package com.syncodec.graphite.utils

import android.graphics.Bitmap
import android.util.Log
import java.io.OutputStream
import kotlin.math.sqrt


object BitmapUtil {

    /**
     * @param maxSize Maximum size in bytes
     */
    fun Bitmap.compress(maxSize: Int, outputStream: OutputStream) {
        val currentSize = this.width * this.height
        if (currentSize > maxSize) {
            val scalingFactor = sqrt(maxSize.toFloat() / currentSize)
            this.reconfigure((width * scalingFactor).toInt(), (height * scalingFactor).toInt(), Bitmap.Config.ARGB_8888)
        }

        this.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    }

    const val TAG = "BitmapUtil"
}
