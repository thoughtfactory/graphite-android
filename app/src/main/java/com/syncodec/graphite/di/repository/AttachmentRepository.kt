package com.syncodec.graphite.di.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.syncodec.graphite.utils.copyInputStreamToOutputStream
import com.syncodec.graphite.utils.getFileName
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.File

class AttachmentRepository {

	private lateinit var attachmentDir : File
	private lateinit var context : Context

	fun initRepository(context : Context) {
		this.context = context
		attachmentDir = File("${context.filesDir.path}/data/attachment")
		this::attachmentDir.isInitialized
	}

	fun putAttachment(noteId : RealmUUID, file : File) {
		val fileName = "${RealmUUID.random()}${file.name.split(".").lastOrNull()?.let { ".$it" }}"
		val newFile = File(context.attachmentDir(noteId, true), fileName).also { it.createNewFile() }
		file.copyTo(newFile, true)
	}

	fun putAttachment(noteId : RealmUUID, uriList : List<Uri>) {
		uriList.forEach { uri ->
			val fileName = uri.getFileName(context) ?: RealmUUID.random().toString()
			val newFileName = getFileName(noteId, fileName)
			val inputStream = context.contentResolver.openInputStream(uri)?.also { inputStream ->
				val file = File(context.attachmentDir(noteId, true), newFileName).also { it.createNewFile() }
				file.outputStream().use { outputStream -> copyInputStreamToOutputStream(inputStream, outputStream) }
			}
			inputStream?.close()
		}
	}

	fun putAttachment(noteId : RealmUUID, zipFile : ZipFile, attachmentList : List<ZipArchiveEntry>) {
		attachmentList.forEach { zipArchiveEntry ->
			val inputStream = zipFile.getInputStream(zipArchiveEntry).also { inputStream ->
				val fileName = "${RealmUUID.random()}.${zipArchiveEntry.name.split(".").last()}"
				val file = File(context.attachmentDir(noteId, true), fileName).also { it.createNewFile() }
				file.outputStream().use { outputStream -> copyInputStreamToOutputStream(inputStream, outputStream) }
			}
			inputStream.close()
		}
	}

	fun doFileExist(noteId : RealmUUID, fileName : String) : Boolean {
		return File(context.attachmentDirPath(noteId = noteId), fileName).exists()
	}

	fun checkIfFilesAreSame(file1 : File, file2 : File) : Boolean {
		return file1.readBytes().contentEquals(file2.readBytes())
	}

	fun checkIfFilesAreSame(file1 : File, file2 : Uri) : Boolean {
		val inputStream = context.contentResolver.openInputStream(file2)?.also { inputStream ->
			val areFilesSame = file1.readBytes().contentEquals(inputStream.readBytes())
			inputStream.close()
			return areFilesSame
		}
		return false
	}

	fun getFileName(noteId : RealmUUID, fileName : String) : String {
		var newFileName = fileName
		var i = 1
		if (! doFileExist(noteId, newFileName)) return newFileName
		val extension = newFileName.split(".").lastOrNull()
		val onlyFileName = newFileName.split(".").dropLast(1).joinToString(".")
		while (true) {
			val newFileNameWithNumber = "$onlyFileName ($i).$extension"
			if (! doFileExist(noteId, newFileNameWithNumber)) {
				newFileName = newFileNameWithNumber
				break
			} else i ++
		}
		return newFileName
	}

	fun getAttachmentDir() : File {
		return attachmentDir
	}

	fun getNoteAttachmentDir(noteId : RealmUUID) : File {
		return File("${attachmentDir.path}/$noteId")
	}

	fun getAttachmentFromNote(noteId : RealmUUID) : List<File> {
		return getNoteAttachmentDir(noteId).listFiles()?.toList() ?: listOf()
	}

	fun haveAttachment(noteId : RealmUUID) : Boolean {
		return getNoteAttachmentDir(noteId).listFiles()?.isNotEmpty() ?: false
	}

	fun importAttachmentFromGraphite(file : File) {
		try {
			if (file.isDirectory) {
				file.listFiles()?.forEach {
					val noteId = try {
						RealmUUID.from(it.name)
					} catch (e : Exception) {
						return@forEach
					}
					val noteAttachmentDir = File("${attachmentDir.path}/$noteId")
					if (! noteAttachmentDir.exists()) noteAttachmentDir.mkdirs()
					it.listFiles()?.forEach { attachment -> putAttachment(noteId, attachment) }
				}
			}
		} catch (e : Exception) {
		}
	}

	fun delete(attachmentList : List<File>) {
		CoroutineScope(Dispatchers.IO).launch {
			attachmentList.forEach {
				try {
					if (it.exists()) it.delete()
				} catch (e : Exception) {
				}
			}
		}
	}

	fun delete(noteId : RealmUUID) {
		CoroutineScope(Dispatchers.IO).launch {
			getNoteAttachmentDir(noteId = noteId).deleteRecursively()
		}
	}

	fun deleteAll() {
		CoroutineScope(Dispatchers.IO).launch {
			attachmentDir.listFiles()?.forEach {
				try {
					if (it.exists()) it.deleteRecursively()
				} catch (e : Exception) {
				}
			}
		}
	}

	companion object {
		fun Context.attachmentDirPath() = "${this.filesDir.path}/data/attachment"
		fun Context.attachmentDirPath(noteId : RealmUUID) = "${this.filesDir.path}/data/attachment/$noteId"
		fun Context.attachmentDir(noteId : RealmUUID, mkdir : Boolean = false) = File(attachmentDirPath(noteId = noteId)).also { if (mkdir) it.mkdirs() }

		fun Context.getAttachmentCountFromNoteId(noteId : RealmUUID) : Int {
			val attachmentDir = File(attachmentDirPath(noteId = noteId))
			return if (attachmentDir.exists()) attachmentDir.listFiles()?.size ?: 0
			else 0
		}
	}
}
