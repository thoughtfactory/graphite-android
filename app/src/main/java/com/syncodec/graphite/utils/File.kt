package com.syncodec.graphite.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.*


fun Context.getFileName(uri: Uri): String? {
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
}

@Throws(IOException::class)
fun createTempFile(id : String, extension: String?): File =
	File.createTempFile(
		"attachment_",
		"_$id${if (extension != null) ".$extension" else ""}"
	)

@Throws(IOException::class)
fun createTempFileToExpose(
	context: Context,
	key: String,
	extension: String?
): Pair<Uri, File> {
	val file = createTempFile(key, extension)
	val uri = FileProvider.getUriForFile(
		context,
		"com.syncodec.fileprovider",
		file
	)
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
