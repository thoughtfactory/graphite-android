package com.syncodec.momento.repository

import android.net.Uri
import com.syncodec.momento.Momento
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.attachment.AttachmentDbEntry
import com.syncodec.momento.database.attachment.AttachmentTableDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AttachmentRepository(val momento: Momento) {
	private var attachmentTableDao: AttachmentTableDao = UserDatabase.getInstance(momento).attachmentTableDao

	suspend fun putAttachment(attachmentList: List<Pair<AttachmentDbEntry, Uri>>) {
		withContext(Dispatchers.IO) {
			attachmentList.forEach {
				val isCopied = momento.putAttachment(key = it.first.key, uri = it.second)
				if (isCopied) { attachmentTableDao.insert(it.first) }
			}
		}
	}

	fun getAttachment(noteKey: String) = attachmentTableDao.getForNoteAsFlow(noteKey = noteKey)
	fun getAttachmentUri(key: String) = momento.getAttachment(key = key)

	suspend fun delete(primaryKey: String) {
		attachmentTableDao.delete(primaryKey)
	}
}
