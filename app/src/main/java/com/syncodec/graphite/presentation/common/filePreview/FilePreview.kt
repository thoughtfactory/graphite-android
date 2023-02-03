package com.syncodec.graphite.presentation.common.filePreview

import android.R.attr.text
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.annotation.WorkerThread
import com.syncodec.graphite.R
import com.syncodec.graphite.utils.subType
import com.syncodec.graphite.utils.type
import java.io.File


class FilePreview {
	companion object {
		@WorkerThread
		fun File.preview(context : Context) : Pair<Bitmap?, Int?> {

			return when (type()) {
				"image" -> Pair(previewImage(), null)
				"video" -> Pair(previewVideo(context = context), R.drawable.ic_play_button)
				"audio" -> Pair(previewAudio(context = context), R.drawable.ic_play_button)
				"text" -> Pair(previewText(context = context), null)
				"application" -> Pair(previewApplication(context = context), null)
				else -> Pair(null, null)
			}
		}

		private fun File.previewImage() : Bitmap? {
			return when (subType()) {
				"jpeg" -> BitmapFactory.decodeStream(inputStream())
				"png" -> BitmapFactory.decodeStream(inputStream())
				"gif" -> BitmapFactory.decodeStream(inputStream())
				"webp" -> BitmapFactory.decodeStream(inputStream())
				"heic" -> BitmapFactory.decodeStream(inputStream())
				else -> BitmapFactory.decodeStream(inputStream())
			}
		}

		private fun File.previewVideo(context : Context) : Bitmap? {
			return when (subType()) {
				"mp4" -> {
					val retriever = MediaMetadataRetriever()
					retriever.setDataSource(this.absolutePath)
					val bitmap = retriever.frameAtTime
					retriever.release()

					bitmap
				}

				"webm" -> null
				"ogg" -> null
				else -> null
			}
		}

		private fun File.previewAudio(context : Context) : Bitmap? {
			when (subType()) {
				"mp3" -> {
					val mediaMetadataRetriever = MediaMetadataRetriever()
					val uri = Uri.fromFile(this)
					mediaMetadataRetriever.setDataSource(context, uri)
					val bitmap =
						mediaMetadataRetriever.embeddedPicture?.let { BitmapFactory.decodeByteArray(it, 0, it.size).copy(Bitmap.Config.ARGB_8888, true )}
					mediaMetadataRetriever.release()
					return bitmap
				}

				"mpeg" -> {
					val mediaMetadataRetriever = MediaMetadataRetriever()
					val uri = Uri.fromFile(this)
					mediaMetadataRetriever.setDataSource(context, uri)
					val bitmap =
						mediaMetadataRetriever.embeddedPicture?.let { BitmapFactory.decodeByteArray(it, 0, it.size).copy(Bitmap.Config.ARGB_8888, true) }
					mediaMetadataRetriever.release()
					return bitmap
				}

				"ogg" -> return null
				"wav" -> return null
				else -> return null
			}
		}

		private fun File.previewText(context : Context) : Bitmap? {
			return when (subType()) {
				"plain" -> previewTextPlain(context)
				else -> null
			}
		}

		private fun File.previewApplication(context : Context) : Bitmap? {
			return when (subType()) {
				"pdf" -> previewPdf(context)
				else -> null
			}
		}

		private fun File.previewPdf(context : Context) : Bitmap? {
			val pdfRenderer = PdfRenderer(ParcelFileDescriptor.open(this, ParcelFileDescriptor.MODE_READ_ONLY))
			val page = pdfRenderer.openPage(0)
			val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
			page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
			page.close()
			pdfRenderer.close()
			return bitmap
		}

		private fun File.previewTextPlain(context : Context) : Bitmap? = null
		fun File.icon() : Int {
			return when (type()) {
				"image" -> R.drawable.ic_file
				"video" -> R.drawable.ic_file
				"audio" -> R.drawable.ic_file
				"application" -> when (subType()) {
					"pdf" -> R.drawable.ic_file
					"zip" -> R.drawable.ic_file
					"rar" -> R.drawable.ic_file
					"7z" -> R.drawable.ic_file
					"tar" -> R.drawable.ic_file
					"gz" -> R.drawable.ic_file
					"bz2" -> R.drawable.ic_file
					"doc" -> R.drawable.ic_file
					"docx" -> R.drawable.ic_file
					"xls" -> R.drawable.ic_file
					"xlsx" -> R.drawable.ic_file
					"ppt" -> R.drawable.ic_file
					"pptx" -> R.drawable.ic_file
					"txt" -> R.drawable.ic_file
					"rtf" -> R.drawable.ic_file
					"html" -> R.drawable.ic_file
					"htm" -> R.drawable.ic_file
					"xml" -> R.drawable.ic_file
					"json" -> R.drawable.ic_file
					"csv" -> R.drawable.ic_file
					"apk" -> R.drawable.ic_file
					"exe" -> R.drawable.ic_file
					"iso" -> R.drawable.ic_file
					"msi" -> R.drawable.ic_file
					"jar" -> R.drawable.ic_file
					"js" -> R.drawable.ic_file
					"css" -> R.drawable.ic_file
					"php" -> R.drawable.ic_file
					"py" -> R.drawable.ic_file
					"rb" -> R.drawable.ic_file
					"sh" -> R.drawable.ic_file
					"bat" -> R.drawable.ic_file
					"psd" -> R.drawable.ic_file
					"ai" -> R.drawable.ic_file
					"eps" -> R.drawable.ic_file
					"indd" -> R.drawable.ic_file
					"ttf" -> R.drawable.ic_file
					"otf" -> R.drawable.ic_file
					"woff" -> R.drawable.ic_file
					"woff2" -> R.drawable.ic_file
					"eot" -> R.drawable.ic_file
					"svg" -> R.drawable.ic_file
					else -> R.drawable.ic_file
				}

				else -> R.drawable.ic_file
			}
		}

	}
}
