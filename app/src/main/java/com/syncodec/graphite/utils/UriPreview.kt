package com.syncodec.graphite.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.annotation.WorkerThread
import com.syncodec.graphite.R

class UriPreview {
	companion object {
		@WorkerThread
		fun Uri.preview(context : Context) : Pair<Bitmap?, Int?> {

			return when (type(context)) {
				"image" -> Pair(previewImage(context), null)
				"video" -> Pair(previewVideo(context = context), R.drawable.ic_play_button)
				"audio" -> Pair(previewAudio(context = context), R.drawable.ic_play_button)
				"text" -> Pair(previewText(context = context), null)
				"application" -> Pair(previewApplication(context = context), null)
				else -> Pair(null, null)
			}
		}

		private fun Uri.previewImage(context : Context) : Bitmap? {
			return when (subType(context)) {
				"jpeg" -> BitmapFactory.decodeStream(context.contentResolver.openInputStream(this))
				"png" -> BitmapFactory.decodeStream(context.contentResolver.openInputStream(this))
				"gif" -> BitmapFactory.decodeStream(context.contentResolver.openInputStream(this))
				"webp" -> BitmapFactory.decodeStream(context.contentResolver.openInputStream(this))
				"heic" -> BitmapFactory.decodeStream(context.contentResolver.openInputStream(this))
				else -> BitmapFactory.decodeStream(context.contentResolver.openInputStream(this))
			}
		}

		private fun Uri.previewVideo(context : Context) : Bitmap? {
			return when (subType(context)) {
				"mp4" -> {
					val retriever = MediaMetadataRetriever()
					retriever.setDataSource(context, this)
					val bitmap = retriever.frameAtTime
					retriever.release()

					bitmap
				}

				"webm" -> null
				"ogg" -> null
				else -> null
			}
		}

		private fun Uri.previewAudio(context : Context) : Bitmap? {
			when (subType(context)) {
				"mp3" -> {
					val mediaMetadataRetriever = MediaMetadataRetriever()
					mediaMetadataRetriever.setDataSource(context, this)
					val bitmap =
						mediaMetadataRetriever.embeddedPicture?.let { BitmapFactory.decodeByteArray(it, 0, it.size).copy(Bitmap.Config.ARGB_8888, true )}
					mediaMetadataRetriever.release()
					return bitmap
				}

				"mpeg" -> {
					val mediaMetadataRetriever = MediaMetadataRetriever()
					mediaMetadataRetriever.setDataSource(context, this)
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

		private fun Uri.previewText(context : Context) : Bitmap? {
			return when (subType(context)) {
				"plain" -> previewTextPlain(context)
				else -> null
			}
		}

		private fun Uri.previewApplication(context : Context) : Bitmap? {
			return when (subType(context)) {
				"pdf" -> previewPdf(context)
				else -> null
			}
		}

		private fun Uri.previewPdf(context : Context) : Bitmap? {
			context.contentResolver.openFileDescriptor(this, "r")?.let { fileDescriptor ->
				try {
					val pdfRenderer = PdfRenderer(fileDescriptor)
					val page = pdfRenderer.openPage(0)
					val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
					page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
					page.close()
					pdfRenderer.close()
					fileDescriptor.close()
					return bitmap
				} catch (e : Exception) {
					return null
				}
			} ?: return null
		}

		private fun Uri.previewTextPlain(context : Context) : Bitmap? = null
		fun Uri.icon(context : Context) : Int {
			return when (type(context)) {
				"image" -> R.drawable.ic_file
				"video" -> R.drawable.ic_file
				"audio" -> R.drawable.ic_file
				"application" -> when (subType(context)) {
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
