package com.syncodec.graphite.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import io.realm.kotlin.types.RealmUUID
import java.io.*
import java.util.zip.ZipFile


enum class AttachmentType {
	IMAGE,
	VIDEO,
	AUDIO,
	UNKNOWN
}

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

fun Context.getAttachmentCountFromNoteId(noteId : RealmUUID) : Int {
	val attachmentDirPath = "${filesDir.path}/data/attachment"
	val attachmentDir = File("$attachmentDirPath/$noteId")
	return if (attachmentDir.exists()) attachmentDir.listFiles()?.size ?: 0
	else 0
}


fun File.extension() : String? {
	return try {
		MimeTypeMap.getFileExtensionFromUrl(this.name)
	} catch (e: Exception) {
		null
	}
}

fun File.mimeType() : String? {
	return try {
		MimeTypeMap.getSingleton().getMimeTypeFromExtension(this.extension())
	} catch (e: Exception) {
		null
	}
}

fun File.type() : String? {
	return try {
		mimeType()?.split("/")?.get(0)
	} catch (e: Exception) {
		null
	}
}

fun File.subType() : String? {
	return try {
		mimeType()?.split("/")?.get(1)
	} catch (e: Exception) {
		null
	}
}

@Throws(IOException::class)
fun createTempAttachmentFile(context : Context, id : String): File {
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

fun Context.copyToCache(file : File): File {
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

fun File.share(context : Context) {
	try {
		val sharingIntent = Intent(Intent.ACTION_SEND)

		sharingIntent.type = mimeType() ?: "*/*"

		FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", this).let {
			sharingIntent.putExtra(Intent.EXTRA_STREAM, it)

			Intent.createChooser(sharingIntent, "Share using").apply {
				addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
				context.startActivity(this)
			}
		}
	} catch (e : Exception) {
		e.printStackTrace()
		Toast.makeText(context, "Error sharing file", Toast.LENGTH_SHORT).show()
	}
}

fun File.viewFile(context : Context) {
	try {
		Intent(Intent.ACTION_VIEW, FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", this)).apply {
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

			context.startActivity(this)
		}
	} catch (e : ActivityNotFoundException) {
		Toast.makeText(context, "No application found to open this attachment", Toast.LENGTH_SHORT).show()
	} catch (e : Exception) {
		e.printStackTrace()
		Toast.makeText(context, "Error viewing file", Toast.LENGTH_SHORT).show()
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
	e.printStackTrace()
}

fun copyInputStreamToOutputStream(inputStream: InputStream, outputStream: OutputStream) =
	try {
		val buf = ByteArray(1024)
		var len: Int
		while (inputStream.read(buf).also { len = it } > 0) {
			outputStream.write(buf, 0, len)
		}
		outputStream.close()
		inputStream.close()
	} catch (e: Exception) {
		e.printStackTrace()
	}

fun Context.getFileFromUri(uri: Uri?): File? {
	if (uri == null) return null
	val tempFile = File.createTempFile("${System.currentTimeMillis()}", null)
	val inputStream = contentResolver.openInputStream(uri) ?: return null
	val outputStream = tempFile.outputStream()

	copyInputStreamToOutputStream(inputStream, outputStream)
	return tempFile
}

fun copyDirectory(srcDir: File, destDir: File) {
	if (!destDir.exists()) {
		destDir.mkdirs()
	}
	srcDir.listFiles()?.forEach { file ->
		if (file.isDirectory) {
			copyDirectory(file, File(destDir, file.name))
		} else {
			val destFile = File(destDir, file.name)
			file.inputStream().use { input ->
				destFile.outputStream().use { output ->
					input.copyTo(output)
				}
			}
		}
	}
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

fun extractZipFile(inputFile : ZipFile, destination : File) {
	inputFile.entries().iterator().forEach {
		val entry = it
		val entryFile = File(destination, entry.name)
		entryFile.parentFile?.mkdirs()
		entryFile.delete()
		entryFile.createNewFile()
		val entryInputStream = inputFile.getInputStream(entry)
		entryInputStream.copyTo(entryFile.outputStream())
		entryInputStream.close()
	}
}
