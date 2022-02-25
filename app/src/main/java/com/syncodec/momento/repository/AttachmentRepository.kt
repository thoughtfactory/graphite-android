package com.syncodec.momento.repository

import androidx.lifecycle.LiveData
import com.syncodec.momento.Momento
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.attachment.Attachment
import com.syncodec.momento.database.attachment.AttachmentTableDao
import com.syncodec.momento.noteComponent.TempAttachmentData
import com.syncodec.momento.miscellaneous.copyInputStreamToOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AttachmentRepository(val momento: Momento) {
	private var attachmentTableDao: AttachmentTableDao = UserDatabase.getInstance(momento).attachmentTableDao

	val attachmentListLiveData: LiveData<List<Attachment>> = attachmentTableDao.getAllAsLiveData()

	fun insert(attachment: Attachment) {
		attachmentTableDao.insert(attachment)
	}

	suspend fun delete(primaryKey: String) {
		attachmentTableDao.delete(primaryKey)
	}

	suspend fun deleteAll() {
		attachmentTableDao.deleteAll()
	}

	suspend fun saveAttachmentList(diaryKey: String, attachmentList: List<TempAttachmentData>) {
		attachmentList.forEach { tempAttachmentData ->
			saveAttachment(diaryKey = diaryKey, tempAttachmentData = tempAttachmentData)
		}
	}

	suspend fun saveAttachment(diaryKey: String, tempAttachmentData: TempAttachmentData) {
		withContext(Dispatchers.IO) {
			File(momento.getDiaryDirPath(diaryKey = diaryKey)).mkdirs()

			val inputStream = tempAttachmentData.file!!.inputStream()
			val outputStream = File("${momento.getDiaryDirPath(diaryKey = diaryKey)}/attachment_${tempAttachmentData.primaryKey}").outputStream()
			copyInputStreamToOutputStream(inputStream = inputStream, outputStream = outputStream)

			Attachment(
				primaryKey = tempAttachmentData.primaryKey,
				createdTimestamp = System.currentTimeMillis(),
				timezoneOffset = 330 * 60,
				mimeType = tempAttachmentData.mimeType,
				notePrimaryKey = diaryKey
			).apply {
				insert(attachment = this)
			}
		}
	}
}
