package com.syncodec.graphite.di.repository

import android.content.Context
import android.net.Uri
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.model.local.AttachmentIdentity
import com.syncodec.graphite.service.syncInator.SyncInatorService
import com.syncodec.graphite.utils.copyInputStreamToOutputStream
import com.syncodec.graphite.utils.getFileName
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class AttachmentRepository(private val context: Context) {

	private val attachmentDir: File = File("${context.filesDir.path}/data/attachment")

	fun putAttachment(parentId: RealmUUID, file: File, keepName: Boolean = false) {
		val fileName = if (keepName) file.name else "${RealmUUID.random()}_${file.name.split(".").lastOrNull()?.let { ".$it" }}"
		val newFile = File(context.attachmentDir(parentId, true), fileName).also { it.createNewFile() }
		file.copyTo(newFile, true)
	}

	fun putAttachment(parentId: RealmUUID, fileName: String, byteArray: ByteArray) {
		val newFile = File(context.attachmentDir(parentId, true), fileName).also { it.createNewFile() }
		newFile.writeBytes(byteArray)
	}

	fun putAttachment(parentId: RealmUUID, uriList: List<Uri>, keepName: Boolean = false) {
		uriList.forEach { uri ->
			val fileName = if (keepName) context.getFileName(uri) else "${RealmUUID.random()}_${uri.getFileName(context)}"
			val inputStream = context.contentResolver.openInputStream(uri)?.also { inputStream ->
				val file = File(context.attachmentDir(parentId, true), fileName).also { it.createNewFile() }
				file.outputStream().use { outputStream -> copyInputStreamToOutputStream(inputStream, outputStream) }
			}
			inputStream?.close()
		}
	}

	fun putAttachment(attachmentIdentity: AttachmentIdentity, byteArray: ByteArray) {
		try {
			val attachmentFile = File("${attachmentDir.path}/${attachmentIdentity.parentId}/${attachmentIdentity.fileName}")
			attachmentFile.delete()
			attachmentFile.createNewFile()
			attachmentFile.writeBytes(byteArray)
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
		}
	}

	fun getAttachmentDir(): File = attachmentDir

	fun getAttachment(attachmentIdentity: AttachmentIdentity) : File = File("${attachmentDir.path}/${attachmentIdentity.parentId}/${attachmentIdentity.fileName}")

	fun getNoteAttachmentDir(parentId: RealmUUID): File = File("${attachmentDir.path}/$parentId")

	fun getAttachmentFromNote(parentId: RealmUUID): Set<File> = getNoteAttachmentDir(parentId).listFiles()?.toSet() ?: setOf()

	fun countTotalAttachment(): Int {
		return getAttachmentDir().listFiles()?.fold(0) { acc, file ->
			if (file.isDirectory) acc + (file.listFiles()?.size ?: 0)
			else acc
		} ?: 0
	}

	fun importAttachmentFromGraphite(file: File) {
		try {
			if (file.isDirectory) {
				file.listFiles()?.forEach {
					val parentId = try {
						RealmUUID.from(it.name)
					} catch (e: Exception) {
						return@forEach
					}
					val noteAttachmentDir = File("${attachmentDir.path}/$parentId")
					if (!noteAttachmentDir.exists()) noteAttachmentDir.mkdirs()
					it.listFiles()?.forEach { attachment -> putAttachment(parentId = parentId, file = attachment, keepName = true) }
				}
			}
		} catch (e: Exception) {
			if (BuildConfig.DEBUG) e.printStackTrace()
		}
	}

	fun delete(attachmentList: Collection<File>) {
		CoroutineScope(Dispatchers.IO).launch {
			attachmentList.forEach {
				try {
					if (it.exists()) it.delete()
				} catch (e: Exception) {
					if (BuildConfig.DEBUG) e.printStackTrace()
				}
			}
		}
	}

	fun delete(parentId: RealmUUID) {
		CoroutineScope(Dispatchers.IO).launch {
			getNoteAttachmentDir(parentId = parentId).deleteRecursively()
		}
	}

	fun delete(attachmentIdentity: AttachmentIdentity) {
		CoroutineScope(Dispatchers.IO).launch {
			try {
				File("${attachmentDir.path}/${attachmentIdentity.parentId}/${attachmentIdentity.fileName}").delete()
			} catch (e: Exception) {
				if (BuildConfig.DEBUG) e.printStackTrace()
			}
		}
	}

	fun deleteAll() {
		CoroutineScope(Dispatchers.IO).launch {
			attachmentDir.listFiles()?.forEach {
				try {
					if (it.exists()) it.deleteRecursively()
				} catch (e: Exception) {
					if (BuildConfig.DEBUG) e.printStackTrace()
				}
			}
		}
	}

	fun getAllAttachmentLite(): List<AttachmentIdentity> {
		return attachmentDir
			.listFiles()
			?.mapNotNull { noteAttachmentDir ->
				try {
					val parentId = RealmUUID.from(noteAttachmentDir.name)
					noteAttachmentDir?.listFiles()?.map { AttachmentIdentity(parentId = parentId, fileName = it.name) }
				} catch (e: Exception) {
					if (BuildConfig.DEBUG) e.printStackTrace()
					null
				}
			}
			?.flatten() ?: listOf()
	}

	companion object {
		fun Context.attachmentDirPath() = "${this.filesDir.path}/data/attachment"
		fun Context.attachmentDirPath(parentId: RealmUUID) = "${this.filesDir.path}/data/attachment/$parentId"
		fun Context.attachmentDir(parentId: RealmUUID, mkdir: Boolean = false) = File(attachmentDirPath(parentId = parentId)).also { if (mkdir) it.mkdirs() }

		fun Context.getAttachmentCountFromNoteId(parentId: RealmUUID): Int {
			val attachmentDir = File(attachmentDirPath(parentId = parentId))
			return if (attachmentDir.exists()) attachmentDir.listFiles()?.size ?: 0
			else 0
		}
	}
}
