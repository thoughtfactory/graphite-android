package com.syncodec.graphite.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.BitmapCompat.createScaledBitmap
import androidx.core.graphics.ColorUtils
import okio.ByteString.Companion.toByteString
import java.io.ByteArrayOutputStream
import kotlin.math.pow


fun Bitmap.toByteArray(): ByteArray {
	val stream = ByteArrayOutputStream()
	compress(Bitmap.CompressFormat.PNG, 100, stream)
	return stream.toByteArray()
}

fun Color.complementary(): Color {
	val hsl = FloatArray(3)
	ColorUtils.colorToHSL(this.toArgb(), hsl)
	hsl[0] = hsl[0] + 180
	return Color(ColorUtils.HSLToColor(hsl))
}

//fun Color.onForeground(): Color {
//
//}


fun calculateLuminance(rgb: ArrayList<Float>): Double {
	val r = rgb[0] / 255
	val rs = if (r <= 0.03928) r / 12.92 else ((r + 0.055) / 1.055).pow(2.4)
	val g = rgb[1] / 255
	val gs = if (g <= 0.03928) g / 12.92 else ((g + 0.055) / 1.055).pow(2.4)
	val b = rgb[2] / 255
	val bs = if (b <= 0.03928) b / 12.92 else ((b + 0.055) / 1.055).pow(2.4)
	return 0.2126f * rs + 0.7152f * gs + 0.0722f * bs
}

fun Color.getInverseBW(): String {
	val luminance = calculateLuminance(ArrayList(listOf(this.red, this.green, this.blue)))
	return if (luminance < 140) "#fff" else "#000"
}

fun Color.getInverseBWColor(): Color {
	val yiq = (this.red * 299 + this.green * 587 + this.blue * 114) * 0.255
	return if (yiq >= 128) Color.Black else Color.White
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

fun createBitmapFromView(view: View, width: Int, height: Int): Bitmap {
	view.layoutParams = LinearLayoutCompat.LayoutParams(
		LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
		LinearLayoutCompat.LayoutParams.WRAP_CONTENT
	)

	view.measure(
		View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
		View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
	)

	view.layout(0, 0, width, height)

	val canvas = Canvas()
	val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

	canvas.setBitmap(bitmap)
	view.draw(canvas)

	return bitmap
}
