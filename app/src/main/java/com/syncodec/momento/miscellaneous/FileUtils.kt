package com.syncodec.momento.miscellaneous

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileUtils {
	companion object {
		@Throws(IOException::class)
		fun createTempFile(key: String, extension: String?): File =
			File.createTempFile("attachment_", "_$key${if (extension !=null) ".$extension" else ""}")

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
	}
}
