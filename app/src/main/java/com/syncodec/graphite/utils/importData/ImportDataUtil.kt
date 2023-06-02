package com.syncodec.graphite.utils.importData

import android.content.Context
import android.net.Uri
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel


class ImportDataUtil {
	companion object {
		fun Context.validateGraphiteExportFile(uri: Uri): SevenZFile? {
			val inputStream = contentResolver.openInputStream(uri) ?: return null
			val seekableByteChannel = SeekableInMemoryByteChannel(inputStream.readBytes())
			val importFile = SevenZFile(seekableByteChannel)
			inputStream.close()
			return importFile
		}

		fun Context.validateJourneyFile(uri: Uri): ZipFile? {
			val inputStream = contentResolver.openInputStream(uri) ?: return null
			val seekableByteChannel = SeekableInMemoryByteChannel(inputStream.readBytes())
			val importFile = ZipFile(seekableByteChannel)
			inputStream.close()
			return importFile
		}

		fun Context.validateGoogleKeepFile(uri: Uri): ZipFile? {
			val inputStream = contentResolver.openInputStream(uri) ?: return null
			val seekableByteChannel = SeekableInMemoryByteChannel(inputStream.readBytes())
			val importFile = ZipFile(seekableByteChannel)
			inputStream.close()
			return importFile
		}
	}
}
