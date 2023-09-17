package com.syncodec.graphite.presentation.common.attachment.previewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.request.ImageRequest
import com.syncodec.graphite.utils.extension
import com.syncodec.graphite.utils.subType
import com.syncodec.graphite.utils.type
import java.io.File


object FilePreviewer {

	//	Uses 25% of max available memory at runtime
	private val maxCacheSize = Runtime.getRuntime().maxMemory() * 0.25

	private val filePreviewDataCache: MutableMap<String, PreviewData> = mutableMapOf()
	private var cacheSize: Pair<Int, Long> = Pair(0, 0)

	suspend fun getPreview(file: File, context: Context, cache : Boolean = true): PreviewData {
		if (cache) {
			val cachedPreviewData = filePreviewDataCache[file.name]

			if (cachedPreviewData != null) {
				Log.d("npr71", "getPreview : using cached data")
				return cachedPreviewData
			} else {
				Log.d("npr71", "getPreview : cache not found")
				val extension = file.extension()
				val fileType = file.type()
				val newPreviewData = when (fileType) {
					"image" -> getImageOrVideoFilePreview(file = file, context = context)
					"video" -> getImageOrVideoFilePreview(file = file, context = context)
					"application" -> getApplicationFilePreview(file = file)
					else -> PreviewData.Unknown(name1 = file.name, extension2 = extension)
				}

				cachePreviewData(fileName = file.name, previewData = newPreviewData)
				return newPreviewData
			}
		} else {
			val extension = file.extension()
			val fileType = file.type()
			return when (fileType) {
				"image" -> getImageOrVideoFilePreview(file = file, context = context)
				"video" -> getImageOrVideoFilePreview(file = file, context = context)
				"application" -> getApplicationFilePreview(file = file)
				else -> PreviewData.Unknown(name1 = file.name, extension2 = extension)
			}
		}
	}

	private suspend fun getImageOrVideoFilePreview(file: File, context: Context): PreviewData {
		val extension = file.extension()
		try {
			val imageLoader = ImageLoader.Builder(context)
				.diskCache {
					DiskCache.Builder()
						.directory(context.cacheDir.resolve("image_cache"))
						.maxSizeBytes(1024 * 1024 * 128)
						.build()
				}
				.components {
					add(VideoFrameDecoder.Factory())
				}
				.build()

			val imageRequest = ImageRequest.Builder(context)
				.data(file)
				.allowHardware(true)
				.build()

			val imageResult = imageLoader.execute(imageRequest)

			val fileType = file.type()
			val newPreviewData = when (fileType) {
				"image" -> imageResult.drawable?.let { PreviewData.Image(drawable = it, name1 = file.name, extension2 = extension) } ?: PreviewData.Unknown(name1 = file.name, extension2 = extension)
				"video" -> imageResult.drawable?.let { PreviewData.Video(drawable = it, name1 = file.name, extension2 = extension) } ?: PreviewData.Unknown(name1 = file.name, extension2 = extension)
				else -> PreviewData.Unknown(name1 = file.name, extension2 = extension)
			}

			return newPreviewData
		} catch (_: Exception) {
			return PreviewData.Unknown(name1 = file.name, extension2 = extension)
		}
	}

	private fun getApplicationFilePreview(file: File): PreviewData {
		Log.d("npr71", "getApplicationFilePreview")
		val extension = file.extension()
		val fileSubType = file.subType()
		return when (fileSubType) {
			"pdf" -> getPdfFilePreview(file)
			else -> PreviewData.Unknown(name1 = file.name, extension2 = extension)
		}
	}

	private fun getPdfFilePreview(file: File): PreviewData {
		Log.d("npr71", "getPdfFilePreview")
		val extension = file.extension()
		return try {
			val pdfRenderer = PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
			val page = pdfRenderer.openPage(0)
			val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
			page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
			page.close()
			pdfRenderer.close()
			PreviewData.Pdf(name1 = file.name, bitmap = bitmap)
		} catch (_: Exception) {
			PreviewData.Unknown(name1 = file.name, extension2 = extension)
		}
	}

	private fun cachePreviewData(fileName: String, previewData: PreviewData) {
		if (calculateCacheSize() < maxCacheSize) {
			Log.d("npr71", "cachePreviewData : caching attachment")
			when (previewData) {
				is PreviewData.Image -> filePreviewDataCache[fileName] = previewData
				is PreviewData.Video -> filePreviewDataCache[fileName] = previewData
				is PreviewData.Pdf -> filePreviewDataCache[fileName] = previewData
				else -> filePreviewDataCache[fileName] = previewData
			}
		} else {
			Log.d("npr71", "cachePreviewData : cache full")
		}
	}

	private fun calculateCacheSize(): Long {
		return if (cacheSize.first == filePreviewDataCache.hashCode()) {
			Log.d("npr71", "calculateCacheSize : cachedCacheSize : $cacheSize : max : $maxCacheSize")
			cacheSize.second
		} else {
			var newCacheSize = 0L
			filePreviewDataCache.forEach { (_, previewData) ->
				when (previewData) {
					is PreviewData.Image -> newCacheSize += previewData.drawable.toBitmap().byteCount
					is PreviewData.Video -> newCacheSize += previewData.drawable.toBitmap().byteCount
					else -> Unit
				}
			}
			cacheSize = Pair(filePreviewDataCache.hashCode(), newCacheSize)
			Log.d("npr71", "calculateCacheSize : newCacheSize : $newCacheSize : max : $maxCacheSize")
			newCacheSize
		}
	}

	fun clearCache() {
		filePreviewDataCache.clear()
	}
}
