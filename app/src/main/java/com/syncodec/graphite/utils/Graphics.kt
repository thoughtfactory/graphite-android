package com.syncodec.graphite.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream


fun Bitmap.toByteArray(): ByteArray {
	val stream = ByteArrayOutputStream()
	compress(Bitmap.CompressFormat.PNG, 100, stream)
	return stream.toByteArray()
}
