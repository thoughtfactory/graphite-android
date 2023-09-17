package com.syncodec.graphite.presentation.common.attachment.previewer

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.syncodec.graphite.utils.MimeType


sealed class PreviewData(val name: String? = null, val mimeType: MimeType = MimeType.APPLICATION, val extension: String? = null) {
	data class Image(val drawable: Drawable, val name1: String?, val extension2: String?) : PreviewData(name = name1, mimeType = MimeType.IMAGE, extension = extension2)
	data class Video(val drawable: Drawable, val name1: String?, val extension2: String?) : PreviewData(name = name1, mimeType = MimeType.VIDEO, extension = extension2)
	data class Audio(val totalLength: Int, val name1: String?) : PreviewData(name = name1, mimeType = MimeType.AUDIO)
	data class Text(val text: String, val name1: String?) : PreviewData(name = name1, mimeType = MimeType.TEXT)
	data class Pdf(val bitmap: Bitmap, val name1: String?) : PreviewData(name = name1, mimeType = MimeType.PDF, extension = "pdf")
	data class Unknown(val name1: String?, val extension2: String?) : PreviewData(name = name1, extension = extension2)
	data object Init : PreviewData()
	data object Loading : PreviewData()
}

