package com.syncodec.momento.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.attachment.Attachment
import com.syncodec.momento.database.attachment.AttachmentTableDao

class AttachmentRepository(application: Application) {
	private var attachmentTableDao: AttachmentTableDao
	init {
		attachmentTableDao = UserDatabase.getInstance(application).attachmentTableDao
	}

	val attachmentListLiveData: LiveData<List<Attachment>> = attachmentTableDao.getAllAsLiveData()
	suspend fun insert(attachment: Attachment) {
		attachmentTableDao.insert(attachment)
	}

	suspend fun delete(primaryKey: String) {
		attachmentTableDao.delete(primaryKey)
	}

	suspend fun deleteAll() {
		attachmentTableDao.deleteAll()
	}
}
