package com.syncodec.graphite.di.repository

import android.content.Context
import android.net.Uri
import com.syncodec.graphite.service.syncService.DropboxService
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

	fun putAttachment(parentId : RealmUUID, file : File, keepName : Boolean = false) {
		val fileName = if (keepName) file.name else "${RealmUUID.random()}_${file.name.split(".").lastOrNull()?.let { ".$it" }}"
		val newFile = File(context.attachmentDir(parentId, true), fileName).also { it.createNewFile() }
		file.copyTo(newFile, true)
	}

	fun putAttachment(parentId : RealmUUID, fileName : String, byteArray : ByteArray) {
		val newFile = File(context.attachmentDir(parentId, true), fileName).also { it.createNewFile() }
		newFile.writeBytes(byteArray)
	}

	fun putAttachment(parentId : RealmUUID, uriList : List<Uri>, keepName : Boolean = false) {
		uriList.forEach { uri ->
			val fileName = if (keepName) context.getFileName(uri) else "${RealmUUID.random()}_${uri.getFileName(context)}"
			val inputStream = context.contentResolver.openInputStream(uri)?.also { inputStream ->
				val file = File(context.attachmentDir(parentId, true), fileName).also { it.createNewFile() }
				file.outputStream().use { outputStream -> copyInputStreamToOutputStream(inputStream, outputStream) }
			}
			inputStream?.close()
		}
	}

	fun putAttachment(parentId : RealmUUID, zipFile : ZipFile, attachmentList : List<ZipArchiveEntry>, keepName : Boolean = false) {
		attachmentList.forEach { zipArchiveEntry ->
			val inputStream = zipFile.getInputStream(zipArchiveEntry).also { inputStream ->
				val fileName = if (keepName) zipArchiveEntry.name else "${RealmUUID.random()}.${zipArchiveEntry.name.split(".").last()}"
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
					it.listFiles()?.forEach { attachment -> putAttachment(parentId = parentId, file = attachment, keepName = true) }
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

	fun delete(parentId : RealmUUID, fileName : String) {
		try {
			val file = File("${attachmentDir.path}/$parentId/$fileName")
			if (file.exists()) file.delete()
		} catch (e : Exception) {

		}
	}

	fun deleteSuspended(parentId : RealmUUID, name : String) {
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

	fun getAttachmentMetadataMap() : Map<RealmUUID, List<DropboxService.Companion.AttachmentMetadata>> {
		val attachmentMetadataMap = mutableMapOf<RealmUUID, List<DropboxService.Companion.AttachmentMetadata>>()
		attachmentDir.listFiles()?.forEach { noteAttachmentDir ->
			val noteId = try {
				RealmUUID.from(noteAttachmentDir.name)
			} catch (e : Exception) {
				return@forEach
			}
			val attachmentMetadataList = noteAttachmentDir.listFiles()?.map { attachmentFile ->
				DropboxService.Companion.AttachmentMetadata(
					fileName = attachmentFile.name,
					parentId = noteId,
					isDeleted = false
				)
			} ?: listOf()
			attachmentMetadataMap[noteId] = attachmentMetadataList
		}
		return attachmentMetadataMap
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
