package com.syncodec.graphite.presentation.note.composable.buildingBlock.renderAttachment


import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.io.File


@Composable
fun RenderPdf(
	type : String?,
	name : String?,
	file : File?
) {
	var bitmap by remember { mutableStateOf<Bitmap?>(null) }

	LaunchedEffect(key1 = null) {
		try {
			PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
				.use {
					try {
						val page = it.openPage(0)
						bitmap = page?.let { Bitmap.createBitmap(it.width, it.height, Bitmap.Config.ARGB_8888) }
						page?.render(bitmap !!, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
						page?.close()
						it.close()
					} catch (e : Exception) {
						e.printStackTrace()
					}
				}
		} catch (e : Exception) {
			e.printStackTrace()
		}
	}

	Crossfade(targetState = bitmap) {
		if (it == null) {
			RenderGeneric(type = type, name = name)
		} else {
			Image(
				bitmap = it.asImageBitmap(),
				modifier = Modifier.fillMaxSize(),
				contentDescription = name,
				contentScale = ContentScale.Crop,
			)
		}
	}
}
