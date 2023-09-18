package com.syncodec.graphite.utils.archiveUtil

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.utils.IOUtils
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException


class ZipUtil {
	companion object {
		fun createZipFile(inputFile: File, outputFile: File) {
			when {
				inputFile.isFile -> try {
					val outputStream = outputFile.outputStream()
					val bufferedOutputStream = BufferedOutputStream(outputStream)
					val zipArchiveOutputStream = ZipArchiveOutputStream(bufferedOutputStream)
					addFileToZipStream(zipArchiveOutputStream, inputFile, "")
					zipArchiveOutputStream.close()
					bufferedOutputStream.close()
					outputStream.close()
				} catch (_: Exception) {
				}

				inputFile.isDirectory -> try {
					val outputStream = outputFile.outputStream()
					val bufferedOutputStream = BufferedOutputStream(outputStream)
					val zipArchiveOutputStream = ZipArchiveOutputStream(bufferedOutputStream)
					inputFile.listFiles()?.forEach { file ->
						addFileToZipStream(zipArchiveOutputStream, file, "")
					}
					zipArchiveOutputStream.close()
					bufferedOutputStream.close()
					outputStream.close()
				} catch (_: Exception) {
				}
			}
		}

		@Throws(IOException::class)
		private fun addFileToZipStream(zipArchiveOutputStream: ZipArchiveOutputStream, fileToZip: File, base: String) {
			val entryName = base + fileToZip.getName()
			val zipArchiveEntry = ZipArchiveEntry(fileToZip, entryName)
			zipArchiveOutputStream.putArchiveEntry(zipArchiveEntry)
			if (fileToZip.isFile()) {
				var fileInputStream: FileInputStream? = null
				try {
					fileInputStream = FileInputStream(fileToZip)
					IOUtils.copy(fileInputStream, zipArchiveOutputStream)
					zipArchiveOutputStream.closeArchiveEntry()
				} finally {
					IOUtils.closeQuietly(fileInputStream)
				}
			} else {
				zipArchiveOutputStream.closeArchiveEntry()
				fileToZip.listFiles()?.forEach {
					addFileToZipStream(zipArchiveOutputStream, it, "$entryName/")
				}
			}
		}
	}
}

