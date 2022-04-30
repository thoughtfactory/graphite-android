package com.syncodec.graphite.repository

import android.net.Uri
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.database.UserDatabase
import com.syncodec.graphite.database.attachment.AttachmentDbEntry
import com.syncodec.graphite.database.attachment.AttachmentTableDao
import com.syncodec.graphite.database.note.NoteTableDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AttachmentRepository(val graphite: Graphite) {
	private var attachmentTableDao: AttachmentTableDao =
		UserDatabase.getInstance(graphite).attachmentTableDao
	private var noteTableDao: NoteTableDao = UserDatabase.getInstance(graphite).noteTableDao

	suspend fun putAttachment(attachmentList: List<Pair<AttachmentDbEntry, Uri>>, noteKey: String) {
		withContext(Dispatchers.IO) {
			val attachmentKeyList: MutableList<String> = mutableListOf()
			attachmentList.forEach { attachmentKeyList.add(it.first.key) }
			attachmentTableDao.getForNote(noteKey = noteKey).forEach {
				if (it.key !in attachmentKeyList) delete(it.key)
			}
			attachmentList.forEach {
				if (attachmentTableDao.isAttachmentPresent(it.first.key) == null) {
					val isCopied = graphite.putAttachment(key = it.first.key, uri = it.second)
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
	fun getAttachmentUri(key: String) = graphite.getAttachment(key = key)
	fun getAttachmentUri(keyList: List<String>) = graphite.getAttachment(keyList = keyList)

	suspend fun delete(key: String) {
		attachmentTableDao.delete(key)
		graphite.deleteAttachment(keyList = listOf(key))
	}

	suspend fun delete(keyList: List<String>) {
		attachmentTableDao.delete(keyList)
		graphite.deleteAttachment(keyList = keyList)
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

		fun getInstance(graphite: Graphite): AttachmentRepository {
			synchronized(lock = this) {
				var instance = INSTANCE
				if (instance == null) {
					instance = AttachmentRepository(graphite = graphite)
					INSTANCE = instance
				}
				return instance
			}
		}
	}
}
