package com.syncodec.momento.miscellaneous

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.syncodec.momento.BuildConfig
import java.io.*

class FileUtils {
	companion object {
		@Throws(IOException::class)
		fun createTempFile(primaryKey: String, mimeType: String?): File = File.createTempFile("attachment_", "_$primaryKey")

		@Throws(IOException::class)
		fun createTempFileToExpose(context: Context, primaryKey: String, mimeType: String): Uri = FileProvider.getUriForFile(
			context,
			"${BuildConfig.APPLICATION_ID}.provider",
			createTempFile(primaryKey = primaryKey, mimeType = mimeType)
		)


		fun copyInputStreamToOutputStream(inputStream: FileInputStream, outputStream: FileOutputStream) = try {
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

		fun copyInputStreamToOutputStream(inputStream: InputStream, outputStream: FileOutputStream) = try {
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

		fun copyInputStreamToOutputStream(inputStream: InputStream, outputStream: OutputStream) = try {
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
	}
}
