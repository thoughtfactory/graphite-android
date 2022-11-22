package com.syncodec.graphite.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.*


fun Context.getFileName(uri: Uri): String? {
	try {
		var result: String? = null
		if (uri.getScheme().equals("content")) {
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
