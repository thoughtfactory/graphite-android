package com.syncodec.graphite.miscellaneous

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

fun View.loadBitmapFromView() : Bitmap? {
	val b = Bitmap.createBitmap(layoutParams.width, layoutParams.height, Bitmap.Config.ARGB_8888)
	val c = Canvas(b)
	layout(left, top, right, bottom)
	draw(c)
	return b
}

class GraphicUtils {
	companion object {
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

		fun Bitmap.saveBitmap(file: File) {
			try {
				FileOutputStream(file).use { out ->
					compress(Bitmap.CompressFormat.PNG, 100, out)
				}
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}

		fun Bitmap.toByteArray(): ByteArray {
			val stream = ByteArrayOutputStream()
			compress(Bitmap.CompressFormat.PNG, 100, stream)
			val byteArray: ByteArray = stream.toByteArray()
			recycle()
			return byteArray
		}
	}
}
