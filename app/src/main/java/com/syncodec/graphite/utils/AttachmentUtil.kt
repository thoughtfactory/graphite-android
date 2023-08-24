package com.syncodec.graphite.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import java.io.File


object AttachmentUtil {

	suspend fun getPreview(context: Context, file: File): Bitmap? {
		return try {
			val loader = ImageLoader(context)
			val request = ImageRequest.Builder(context)
				.data(file)
				.allowHardware(true) // Disable hardware bitmaps.
				.build()

			val imageResult = loader.execute(request)

			if (imageResult is SuccessResult) (imageResult.drawable as BitmapDrawable).bitmap
			else null
		} catch (_ : Exception) {
			null
		}
	}
}

