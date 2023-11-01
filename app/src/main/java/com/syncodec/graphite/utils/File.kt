package com.syncodec.graphite.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import java.io.*
import java.time.Instant
import java.util.zip.ZipFile


fun Context.getFileName(uri: Uri): String? {
	try {
		var result: String? = null
		if (uri.scheme.equals("content")) {
			contentResolver.query(uri, null, null, null, null).use { cursor ->
				if (cursor != null && cursor.moveToFirst()) {
					cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).let {
						if (it >= 0) result = cursor.getString(it)
					}
				}
			}
		}
		if (result == null) {
			result = uri.path
			val cut = result?.lastIndexOf('/')
			if (cut != null && cut != -1) {
				result = result?.substring(cut + 1)
			}
		}
		return result
	} catch (e: Exception) {
		return null
	}
}

fun Uri.mimeType(context: Context): String? {
	return context.contentResolver.getType(this)
}

fun Uri.type(context: Context): String? {
	return try {
		mimeType(context)?.split("/")?.get(0)
	} catch (e: Exception) {
		null
	}
}

fun Uri.subType(context: Context): String? {
	return try {
		mimeType(context)?.split("/")?.get(1)
	} catch (e: Exception) {
		null
	}
}

fun File.extension(): String? {
	return try {
		val ext = MimeTypeMap.getFileExtensionFromUrl(this.name)
		if (ext.isNullOrEmpty()) this.name.split(".").lastOrNull()
		else ext
	} catch (e: Exception) {
		null
	}
}

fun File.mimeType(): String? {
	return try {
		MimeTypeMap.getSingleton().getMimeTypeFromExtension(this.extension())
	} catch (e: Exception) {
		null
	}
}

fun File.type(): String? {
	return try {
		mimeType()?.split("/")?.get(0)
	} catch (e: Exception) {
		null
	}
}

fun File.subType(): String? {
	return try {
		mimeType()?.split("/")?.get(1)
	} catch (e: Exception) {
		null
	}
}

@Throws(IOException::class)
fun createTempAttachmentFile(context: Context, id: String): File {
	val directory = File(context.cacheDir, "attachment")
	directory.mkdirs()
	return File.createTempFile("attachment_", "_$id", directory)
}

@Throws(IOException::class)
fun createTempAttachmentFileToExpose(
	context: Context,
	name: String,
): Pair<Uri, File> {
	val file = createTempAttachmentFile(context, name)
	val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
	return Pair(uri, file)
}

fun Context.copyToCache(file: File): File {
	val directory = File(cacheDir, "tmp")
	directory.mkdirs()
	val cacheFile = File(directory, file.name)
	cacheFile.delete()
	val inputStream = file.inputStream()
	val outputStream = cacheFile.outputStream()
	inputStream.copyTo(outputStream)
	inputStream.close()
	outputStream.close()

	return cacheFile
}

fun File.share(context: Context) {
	try {
		val sharingIntent = Intent(Intent.ACTION_SEND)

		sharingIntent.type = mimeType() ?: "*/*"
		sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

		FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", this).let {
			sharingIntent.putExtra(Intent.EXTRA_STREAM, it)

			Intent.createChooser(sharingIntent, "Share using").apply {
				addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
				context.startActivity(this)
			}
		}
	} catch (e: Exception) {
//		e.printStackTrace()
		Toast.makeText(context, "Error sharing file", Toast.LENGTH_SHORT).show()
	}
}

fun Collection<File>.share(context: Context) {
	if (this.isEmpty()) {
		Toast.makeText(context, "No files to share", Toast.LENGTH_SHORT).show()
		return
	}
	try {
		val sharingIntent = Intent(Intent.ACTION_SEND_MULTIPLE)

		sharingIntent.type = "*/*"

		val uris = ArrayList<Uri>()
		for (file in this) {
			uris.add(FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file))
		}

		sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)

		Intent.createChooser(sharingIntent, "Share using").apply {
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
			context.startActivity(this)
		}
	} catch (e: Exception) {
		if (BuildConfig.DEBUG) e.printStackTrace()
		Toast.makeText(context, "Error sharing file", Toast.LENGTH_SHORT).show()
	}
}

fun File.viewExternally(context: Context) {
	try {
		Intent(Intent.ACTION_VIEW).apply {
			setDataAndType(FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", this@viewExternally), mimeType() ?: "*/*")
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

			context.startActivity(this)
		}
	} catch (e: ActivityNotFoundException) {
		Toast.makeText(context, "No application found to open this attachment", Toast.LENGTH_SHORT).show()
	} catch (e: Exception) {
//		e.printStackTrace()
		Toast.makeText(context, "Error viewing file", Toast.LENGTH_SHORT).show()
	}
}

fun Uri.viewExternally(context: Context, fromThirdParty: Boolean) {
	if (fromThirdParty) {
		try {
			Intent(Intent.ACTION_VIEW).apply {
//				putExtra(Intent.EXTRA_STREAM, this@viewExternally)
//				setDataAndType(this@viewExternally, this@viewExternally.mimeType(context) ?: "*/*")
				setData(this@viewExternally)
//				addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

				context.startActivity(this)
			}
		} catch (e: ActivityNotFoundException) {
			Toast.makeText(context, "No application found to open this attachment", Toast.LENGTH_SHORT).show()
		} catch (e: Exception) {
//		e.printStackTrace()
			Toast.makeText(context, "Error viewing file", Toast.LENGTH_SHORT).show()
		}
	} else {
		try {
			Intent(Intent.ACTION_VIEW).apply {
				setDataAndType(this@viewExternally, this@viewExternally.mimeType(context) ?: "*/*")
				addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

				context.startActivity(this)
			}
		} catch (e: ActivityNotFoundException) {
			Toast.makeText(context, "No application found to open this attachment", Toast.LENGTH_SHORT).show()
		} catch (e: Exception) {
//		e.printStackTrace()
			Toast.makeText(context, "Error viewing file", Toast.LENGTH_SHORT).show()
		}
	}
}

fun copyInputStreamToOutputStream(
	inputStream: InputStream,
	outputStream: FileOutputStream
) = try {
	val buf = ByteArray(1024)
	var len: Int
	while (inputStream.read(buf).also { len = it } > 0) {
		outputStream.write(buf, 0, len)
	}
	outputStream.close()
	inputStream.close()
} catch (e: Exception) {
//	e.printStackTrace()
}


fun copyInDirectory(srcDir: File, destDir: File) {
	srcDir.listFiles()?.forEach { file ->
		if (file.isDirectory) {
			copyInDirectory(file, File(destDir, file.name))
		} else {
			val destFile = File(destDir, file.name)
			file.inputStream().use { input ->
				destFile.parentFile?.mkdirs()
				destFile.outputStream().use { output ->
					copyInputStreamToOutputStream(input, output)
				}
			}
		}
	}
}

fun Uri.getFileName(context: Context): String? {
	var result: String? = null
	if (this.scheme == "content") {
		val cursor: Cursor? = context.contentResolver.query(this, null, null, null, null)
		cursor?.use { kursor ->
			if (kursor.moveToFirst() && kursor.columnCount > 0 && kursor.columnNames.contains(OpenableColumns.DISPLAY_NAME)) {
				cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).let { if (it != -1) result = kursor.getString(it) }
			}
		}
	}
	if (result == null) {
		result = this.path
		result?.lastIndexOf('/')?.let { if (it != -1) result = result?.substring(it + 1) }
	}
	return result
}

fun Uri.icon(context: Context): Int = mimeSubTypeIconMap.getOrDefault(context.contentResolver.getType(this), R.drawable.ic_file)
