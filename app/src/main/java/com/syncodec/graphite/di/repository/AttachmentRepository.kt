package com.syncodec.graphite.di.repository

import android.content.Context
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class AttachmentRepository {

	private lateinit var attachmentDir : File

	fun initRepository(context: Context) {
		attachmentDir = File("${context.filesDir.path}/data/attachment")
		this::attachmentDir.isInitialized
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

	fun delete(attachmentList : List<File>) {
		CoroutineScope(Dispatchers.IO).launch {
			attachmentList.forEach { it.delete() }
		}
	}

	fun delete(noteId : RealmUUID) {
		CoroutineScope(Dispatchers.IO).launch {
			getNoteAttachmentDir(noteId = noteId).deleteRecursively()
		}
	}

	companion object {
		fun Context.attachmentDirPath() = "${this.filesDir.path}/data/attachment"
		fun Context.attachmentDirPath(noteId : RealmUUID) = "${this.filesDir.path}/data/attachment/$noteId"
	}
}
