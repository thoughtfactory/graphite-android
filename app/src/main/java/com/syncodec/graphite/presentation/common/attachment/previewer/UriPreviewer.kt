package com.syncodec.graphite.presentation.common.attachment.previewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.request.ImageRequest
import com.syncodec.graphite.utils.getFileName
import com.syncodec.graphite.utils.subType
import com.syncodec.graphite.utils.type
import com.syncodec.graphite.utils.uriUtil.UriUtil.createTemporaryCopy


object UriPreviewer {

	//	Uses 25% of max available memory at runtime
	private val maxCacheSize = Runtime.getRuntime().maxMemory() * 0.25

	private val filePreviewDataCache: MutableMap<String, PreviewData> = mutableMapOf()
	private var cacheSize: Pair<Int, Long> = Pair(0, 0)

	suspend fun getPreview(uri: Uri, context: Context): PreviewData {
		val cachedPreviewData = filePreviewDataCache[uri.toString()]

		if (cachedPreviewData != null) {
			Log.d("npr71", "getPreview : using cached data")
			return cachedPreviewData
		} else {
			Log.d("npr71", "getPreview : cache not found")
			val type = uri.type(context)
			val newPreviewData = when (type) {
				"image" -> getImageOrVideoUriPreview(uri = uri, context = context)
				"video" -> getImageOrVideoUriPreview(uri = uri, context = context)
				"application" -> getApplicationUriPreview(uri = uri, context = context)
				else -> getImageOrVideoUriPreview(uri = uri, context = context)
			}

			cachePreviewData(uri = uri, previewData = newPreviewData)
			return newPreviewData
		}
	}

	private suspend fun getImageOrVideoUriPreview(uri: Uri, context: Context): PreviewData {
		val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
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
				.data(uri)
				.allowHardware(true)
				.build()

			val imageResult = imageLoader.execute(imageRequest)

			val type = uri.type(context)
			val newPreviewData = when (type) {
				"image" -> imageResult.drawable?.let { PreviewData.Image(drawable = it, name1 = uri.getFileName(context = context), extension2 = extension) } ?: PreviewData.Unknown(
					name1 = uri.getFileName(context = context),
					extension2 = extension
				)

				"video" -> imageResult.drawable?.let { PreviewData.Video(drawable = it, uri.getFileName(context = context), extension2 = extension) } ?: PreviewData.Unknown(name1 = uri.getFileName(context = context), extension2 = extension)
				else -> PreviewData.Unknown(name1 = uri.getFileName(context = context), extension2 = extension)
			}

			return newPreviewData
		} catch (_: Exception) {
			return PreviewData.Unknown(name1 = uri.getFileName(context = context), extension2 = extension)
		}
	}

	private fun getApplicationUriPreview(uri: Uri, context: Context): PreviewData {
		Log.d("npr71", "getApplicationFilePreview")
		val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
		val subType = uri.subType(context)
		return when (subType) {
			"pdf" -> getPdfUriPreview(uri = uri, context = context)
			else -> PreviewData.Unknown(name1 = uri.getFileName(context = context), extension2 = extension)
		}
	}

	private fun getPdfUriPreview(uri: Uri, context: Context): PreviewData {
		Log.d("npr71", "getPdfFilePreview")
		val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
		val file = uri.createTemporaryCopy(context = context)
		return try {
			val pdfRenderer = PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
			val page = pdfRenderer.openPage(0)
			val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
			page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
			page.close()
			pdfRenderer.close()
			PreviewData.Pdf(name1 = uri.getFileName(context = context), bitmap = bitmap)
		} catch (_: Exception) {
			PreviewData.Unknown(name1 = uri.getFileName(context = context), extension2 = extension)
		}
	}

	private fun cachePreviewData(uri: Uri, previewData: PreviewData) {
		if (calculateCacheSize() < maxCacheSize) {
			Log.d("npr71", "cachePreviewData : caching attachment")
			when (previewData) {
				is PreviewData.Image -> filePreviewDataCache[uri.toString()] = previewData
				is PreviewData.Video -> filePreviewDataCache[uri.toString()] = previewData
				is PreviewData.Pdf -> filePreviewDataCache[uri.toString()] = previewData
				else -> filePreviewDataCache[uri.toString()] = previewData
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
