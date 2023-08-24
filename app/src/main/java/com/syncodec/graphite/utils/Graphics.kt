package com.syncodec.graphite.utils

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.BitmapCompat.createScaledBitmap
import java.io.ByteArrayOutputStream


fun Bitmap.toByteArray(): ByteArray {
	val stream = ByteArrayOutputStream()
	compress(Bitmap.CompressFormat.PNG, 100, stream)
	return stream.toByteArray()
}


fun Color.isDark(): Boolean {
	val luminance = (this.red * 299 + this.green * 587 + this.blue * 114) * 0.255
	return luminance < 140
}

fun Color.getInverseBWColor(): Color {
	return if (isDark()) Color.White else Color.Black
}

fun Bitmap.scaleBitmap(maxSize: Int): Bitmap {
	val aspectRatio = this.width.toFloat() / this.height.toFloat()

	var scaled = this

	while (scaled.width * scaled.height > maxSize) {
		scaled = createScaledBitmap(
			scaled,
			(scaled.width * 0.8).toInt(),
			(scaled.height * 0.8).toInt(),
			Rect((scaled.width * 0.1).toInt(), (scaled.height * 0.1).toInt(), (scaled.width * 0.9).toInt(), (scaled.height * 0.9).toInt()),
			true
		)
	}

	return scaled
}

