package com.syncodec.graphite.di.repository

import android.content.Context
import android.net.Uri
import com.syncodec.graphite.utils.copyInputStreamToOutputStream
import com.syncodec.graphite.utils.getFileName
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.File
import java.io.InputStream


class AttachmentRepository {

	private lateinit var attachmentDir : File
	private lateinit var context : Context

	fun initRepository(context : Context) {
		this.context = context
		attachmentDir = File("${context.filesDir.path}/data/attachment")
		this::attachmentDir.isInitialized
	}

	fun putAttachment(parentId : RealmUUID, file : File) {
		val fileName = "${RealmUUID.random()}_${file.name.split(".").lastOrNull()?.let { ".$it" }}"
		val newFile = File(context.attachmentDir(parentId, true), fileName).also { it.createNewFile() }
		file.copyTo(newFile, true)
	}

	fun putAttachment(parentId : RealmUUID, name : String, byteArray : ByteArray) {
		val newFile = File(context.attachmentDir(parentId, true), name).also { it.createNewFile() }
		newFile.writeBytes(byteArray)
	}

	fun putAttachment(parentId : RealmUUID, uriList : List<Uri>) {
		uriList.forEach { uri ->
			val fileName = "${RealmUUID.random()}_${uri.getFileName(context)}"
			val inputStream = context.contentResolver.openInputStream(uri)?.also { inputStream ->
				val file = File(context.attachmentDir(parentId, true), fileName).also { it.createNewFile() }
				file.outputStream().use { outputStream -> copyInputStreamToOutputStream(inputStream, outputStream) }
			}
			inputStream?.close()
		}
	}

	fun putAttachment(parentId : RealmUUID, zipFile : ZipFile, attachmentList : List<ZipArchiveEntry>) {
		attachmentList.forEach { zipArchiveEntry ->
			val inputStream = zipFile.getInputStream(zipArchiveEntry).also { inputStream ->
				val fileName = "${RealmUUID.random()}.${zipArchiveEntry.name.split(".").last()}"
				val file = File(context.attachmentDir(parentId, true), fileName).also { it.createNewFile() }
				file.outputStream().use { outputStream -> copyInputStreamToOutputStream(inputStream, outputStream) }
			}
			inputStream.close()
		}
	}

	fun doFileExist(parentId : RealmUUID, fileName : String) : Boolean {
		return File(context.attachmentDirPath(parentId = parentId), fileName).exists()
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

	fun getAttachmentDir() : File {
		return attachmentDir
	}

	fun getNoteAttachmentDir(parentId : RealmUUID) : File {
		return File("${attachmentDir.path}/$parentId")
	}

	fun getAttachment(parentId : RealmUUID, name : String) : File? {
		return try {
			val file = File("${attachmentDir.path}/$parentId/$name")
			if (file.exists()) file else null
		} catch (e : Exception) {
			null
		}
	}

	fun getAttachmentFromNote(parentId : RealmUUID) : List<File> {
		return getNoteAttachmentDir(parentId).listFiles()?.toList() ?: listOf()
	}

	fun haveAttachment(parentId : RealmUUID) : Boolean {
		return getNoteAttachmentDir(parentId).listFiles()?.isNotEmpty() ?: false
	}

	fun importAttachmentFromGraphite(file : File) {
		try {
			if (file.isDirectory) {
				file.listFiles()?.forEach {
					val parentId = try {
						RealmUUID.from(it.name)
					} catch (e : Exception) {
						return@forEach
					}
					val noteAttachmentDir = File("${attachmentDir.path}/$parentId")
					if (! noteAttachmentDir.exists()) noteAttachmentDir.mkdirs()
					it.listFiles()?.forEach { attachment -> putAttachment(parentId, attachment) }
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

	fun delete(parentId : RealmUUID) {
		CoroutineScope(Dispatchers.IO).launch {
			getNoteAttachmentDir(parentId = parentId).deleteRecursively()
		}
	}

	fun delete(parentId : RealmUUID, name:String) {
		try {
			val file = File("${attachmentDir.path}/$parentId/$name")
			if (file.exists()) file.delete()
		} catch (e : Exception) {

		}
	}

	fun deleteSuspended(parentId : RealmUUID, name:String) {
		CoroutineScope(Dispatchers.IO).launch {
			delete(parentId, name)
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
		fun Context.attachmentDirPath(parentId : RealmUUID) = "${this.filesDir.path}/data/attachment/$parentId"
		fun Context.attachmentDir(parentId : RealmUUID, mkdir : Boolean = false) = File(attachmentDirPath(parentId = parentId)).also { if (mkdir) it.mkdirs() }

		fun Context.getAttachmentCountFromNoteId(parentId : RealmUUID) : Int {
			val attachmentDir = File(attachmentDirPath(parentId = parentId))
			return if (attachmentDir.exists()) attachmentDir.listFiles()?.size ?: 0
			else 0
		}
	}
}
