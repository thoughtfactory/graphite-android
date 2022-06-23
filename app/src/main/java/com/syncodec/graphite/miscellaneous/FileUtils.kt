package com.syncodec.graphite.miscellaneous

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.database.chapter.ChapterDbEntry
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileUtils {
	companion object {
		val objectMapper: ObjectMapper = jsonMapper { addModule(kotlinModule()) }

		@Throws(IOException::class)
		fun createTempFile(key: String, extension: String?): File =
			File.createTempFile(
				"attachment_",
				"_$key${if (extension != null) ".$extension" else ""}"
			)

		@Throws(IOException::class)
		fun createTempFileToExpose(
			context: Context,
			key: String,
			extension: String?
		): Uri = FileProvider.getUriForFile(
			context,
			"com.syncodec.fileprovider",
			createTempFile(key = key, extension = extension)
		)

		fun copyInputStreamToOutputStream(
			inputStream: FileInputStream,
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

		fun Context.getFileName(uri: Uri): String? {
			var result: String? = null
			if (uri.scheme == "content") {
				val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
				cursor.use { cursor ->
					if (cursor != null && cursor.moveToFirst()) {
						result =
							cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
					}
				}
			}
			if (result == null) {
				result = uri.path
				val cut = result!!.lastIndexOf('/')
				if (cut != -1) {
					result = result!!.substring(cut + 1)
				}
			}
			return result
		}

		fun Context.getFileExtension(uri: Uri): String? {
			return getFileName(uri = uri)?.split(".")?.lastOrNull()
		}

		fun zipFolder(inputFolderPath: String, outZipPath: String) {
			try {
				val fos = FileOutputStream(outZipPath)
				val zos = ZipOutputStream(fos)
				val srcFile = File(inputFolderPath)
				val files = srcFile.listFiles()
				Log.d("", "Zip directory: " + srcFile.name)
				files?.forEach {
					Log.d("", "Adding file: " + it.name)
					val buffer = ByteArray(1024)
					val fis = FileInputStream(it)
					zos.putNextEntry(ZipEntry(it.name))
					var length: Int
					while (fis.read(buffer).also { length = it } > 0) {
						zos.write(buffer, 0, length)
					}
					zos.closeEntry()
					fis.close()
				}
				zos.close()
			} catch (ioe: IOException) {
				ioe.printStackTrace()
			}
		}

		fun DocumentFile.readAsString(context: Context): String {
			val inputStream =
				context.contentResolver.openInputStream(uri)

			val r = BufferedReader(InputStreamReader(inputStream))
			val data: StringBuilder = StringBuilder()
			var line: String?
			while (r.readLine().also { line = it } != null) {
				data.append(line).append('\n')
			}

			return data.toString()
		}

		inline fun <reified T : Any> DocumentFile.readAsObject(context: Context): T {
			val inputStream = context.contentResolver.openInputStream(uri)

			val r = BufferedReader(InputStreamReader(inputStream))
			val data: StringBuilder = StringBuilder()
			var line: String?
			while (r.readLine().also { line = it } != null) {
				data.append(line).append('\n')
			}

			return objectMapper.readValue(data.toString(), T::class.java)
		}
	}
}
