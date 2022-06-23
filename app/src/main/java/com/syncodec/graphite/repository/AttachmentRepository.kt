package com.syncodec.graphite.repository

import android.net.Uri
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.database.UserDatabase
import com.syncodec.graphite.database.attachment.AttachmentDbEntry
import com.syncodec.graphite.database.attachment.AttachmentTableDao
import com.syncodec.graphite.database.note.NoteTableDao
import com.syncodec.graphite.miscellaneous.CollectionUtils.Companion.listOfField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AttachmentRepository(val graphite: Graphite) {
	private var attachmentTableDao: AttachmentTableDao =
		UserDatabase.getInstance(graphite).attachmentTableDao
	private var noteTableDao: NoteTableDao = UserDatabase.getInstance(graphite).noteTableDao

	suspend fun putAttachment(
		attachmentList: List<Pair<AttachmentDbEntry, Uri?>>,
		noteKey: String
	) {
		withContext(Dispatchers.IO) {
			val attachmentKeyList: MutableList<String> = mutableListOf()
			attachmentList.forEach { attachmentKeyList.add(it.first.key) }
			attachmentTableDao.getForNote(noteKey = noteKey).forEach {
				if (it.key !in attachmentKeyList) delete(it.key)
			}
			attachmentList.forEach {
				try {
					if (attachmentTableDao.isAttachmentPresent(it.first.key) == null) {
						val isCopied = graphite.putAttachment(
							key = it.first.key,
							uri = it.second,
							extension = it.first.extension
						)
						if (isCopied) attachmentTableDao.insert(it.first)
					}
				} catch (exception: Exception) {
				}
			}
		}
	}

	fun getAttachmentForNoteAsFlow(noteKey: String) =
		attachmentTableDao.getForNoteAsFlow(noteKey = noteKey)

	suspend fun getAttachment(noteKey: String) = attachmentTableDao.getForNote(noteKey = noteKey)
	fun getAttachmentForNotebookAsFlow(notebookKey: String) =
		attachmentTableDao.getForNotebookAsFlow(notebookKey = notebookKey)

	suspend fun getAttachmentForNotebook(notebookKey: String): Map<String, Pair<AttachmentDbEntry, Uri?>> {
		return attachmentTableDao.getForNotebook(notebookKey = notebookKey)
			.associate { it.key to Pair(it, getAttachmentUri(key = it.key, it.extension)) }
	}

	fun getAttachmentUri(key: String, extension: String?) = graphite.getAttachment(key = key)

	fun getAttachmentUri(keyList: List<String>, extensionList: List<String?>) =
		graphite.getAttachment(keyList = keyList, extensionList = extensionList)

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

	suspend fun deleteWithNote(noteKeyList: List<String>) {
		noteKeyList.forEach {
			val attachmentKeyList =
				attachmentTableDao.getForNote(it).listOfField(AttachmentDbEntry::key)
			graphite.deleteAttachment(attachmentKeyList)
			attachmentTableDao.delete(attachmentKeyList)
		}
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
