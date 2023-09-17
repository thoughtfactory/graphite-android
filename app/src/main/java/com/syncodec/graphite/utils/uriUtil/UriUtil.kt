package com.syncodec.graphite.utils.uriUtil

import android.content.Context
import android.net.Uri
import com.syncodec.graphite.utils.copyInputStreamToOutputStream
import com.syncodec.graphite.utils.createTempAttachmentFile
import io.realm.kotlin.types.RealmUUID
import java.io.File


object UriUtil {

	private val uriFileCache : MutableMap<String, File> = mutableMapOf()

	fun Uri.createTemporaryCopy(context: Context): File {

		val cachedFile = uriFileCache[this.toString()]
		if (cachedFile != null) {
			return cachedFile
		} else {
			val fileName = RealmUUID.random().toString()
			val outputFile = createTempAttachmentFile(context = context, id = fileName)

			val inputStream = context.contentResolver.openInputStream(this)
			val outputStream = outputFile.outputStream()

			inputStream?.let { inputStream1 ->
				copyInputStreamToOutputStream(inputStream1, outputStream)
			}
			outputStream.close()
			inputStream?.close()

			cacheUri(uri = this, file = outputFile)

			return outputFile
		}
	}

	private fun cacheUri(uri: Uri, file: File) {
		uriFileCache[uri.toString()] = file
	}
}
