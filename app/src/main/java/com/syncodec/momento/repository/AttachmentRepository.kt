package com.syncodec.momento.repository

import android.net.Uri
import com.syncodec.momento.Momento
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.attachment.AttachmentDbEntry
import com.syncodec.momento.database.attachment.AttachmentTableDao
import com.syncodec.momento.database.note.NoteTableDao
import com.syncodec.momento.miscellaneous.CollectionUtils.Companion.listOfField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AttachmentRepository(val momento: Momento) {
	private var attachmentTableDao: AttachmentTableDao =
		UserDatabase.getInstance(momento).attachmentTableDao
	private var noteTableDao: NoteTableDao = UserDatabase.getInstance(momento).noteTableDao

	suspend fun putAttachment(attachmentList: List<Pair<AttachmentDbEntry, Uri>>, noteKey: String) {
		withContext(Dispatchers.IO) {
			val attachmentKeyList: MutableList<String> = mutableListOf()
			attachmentList.forEach { attachmentKeyList.add(it.first.key) }
			attachmentTableDao.getForNote(noteKey = noteKey).forEach {
				if (it.key !in attachmentKeyList) delete(it.key)
			}
			attachmentList.forEach {
				if (attachmentTableDao.isAttachmentPresent(it.first.key) == null) {
					val isCopied = momento.putAttachment(key = it.first.key, uri = it.second)
					if (isCopied) {
						attachmentTableDao.insert(it.first)
					}
				}
			}
		}
	}

	fun getAttachmentForNoteAsFlow(noteKey: String) =
		attachmentTableDao.getForNoteAsFlow(noteKey = noteKey)
	suspend fun getAttachment(noteKey: String) = attachmentTableDao.getForNote(noteKey = noteKey)
	fun getAttachmentForNotebookAsFlow(notebookKey: String) =
		attachmentTableDao.getForNotebookAsFlow(notebookKey = notebookKey)
	fun getAttachmentUri(key: String) = momento.getAttachment(key = key)
	fun getAttachmentUri(keyList: List<String>) = momento.getAttachment(keyList = keyList)

	suspend fun delete(key: String) {
		attachmentTableDao.delete(key)
		momento.deleteAttachment(keyList = listOf(key))
	}

	suspend fun delete(keyList: List<String>) {
		attachmentTableDao.delete(keyList)
		momento.deleteAttachment(keyList = keyList)
	}

	suspend fun deleteFromNote(key: String, noteKey: String) {
		attachmentTableDao.delete(key)
		noteTableDao.get(key = noteKey).also {
			if (it != null) {
				it.attachmentKeyList.remove(key)
				noteTableDao.insert(it)
			}
		}
	}

	fun deleteWithNote(noteKeyList: List<String>) {
		attachmentTableDao.deleteWithNote(noteKeyList = noteKeyList)
	}

	companion object {
		private var INSTANCE: AttachmentRepository? = null

		fun getInstance(momento: Momento): AttachmentRepository {
			synchronized(lock = this) {
				var instance = INSTANCE
				if (instance == null) {
					instance = AttachmentRepository(momento = momento)
					INSTANCE = instance
				}
				return instance
			}
		}
	}
}
