package com.syncodec.momento.repository

import android.app.Application
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import androidx.lifecycle.LiveData
import com.syncodec.momento.Momento
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.diary.DiaryDbEntry
import com.syncodec.momento.database.diary.DiaryTableDao
import com.syncodec.momento.database.diary.Note
import com.syncodec.momento.diaryComponent.TempAttachmentData
import com.syncodec.momento.miscellaneous.bitmapToBase64String
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class DiaryRepository(val application: Application) {
	private var diaryTableDao: DiaryTableDao = UserDatabase.getInstance(application).diaryTableDao

	val diaryDbEntryListLiveData: LiveData<List<DiaryDbEntry>> = diaryTableDao.getAllAsLiveData()

	suspend fun saveDiary(
		note: Note,
		attachmentList: MutableList<TempAttachmentData>
	) {
		withContext(Dispatchers.IO) {
			(application as Momento).putDiary(note = note)

			DiaryDbEntry(
				primaryKey = note.primaryKey,
				timezoneOffset = note.timezoneOffset
			).apply {
				this.createdTimestamp = note.createdTimestamp
				this.modifiedTimestamp = note.modifiedTimestamp
				this.userTimestamp = note.userTimestamp
				this.contentThumbnail = note.contentThumbnail
				this.latitude = note.location?.latitude
				this.latitude = note.location?.longitude
				this.address = note.address
				this.attachmentCount = note.attachmentKeyList.size

				attachmentList.forEach { tempAttachmentData ->
					if (this.attachmentThumbnail==null) {
						when(tempAttachmentData.mimeType?.split("/")?.first()) {
							"image" -> {
								val THUMBSIZE = 128

								val thumbImage = ThumbnailUtils.extractThumbnail(
									BitmapFactory.decodeFile(tempAttachmentData.file!!.path),
									THUMBSIZE,
									THUMBSIZE
								)

								this.attachmentThumbnail = thumbImage.bitmapToBase64String()
							}
							"video" -> {}
						}
					}
				}

				insert(this)
			}
		}
	}

	suspend fun insert(diaryDbEntry: DiaryDbEntry) {
		diaryTableDao.insert(diaryDbEntry)
	}

	suspend fun delete(primaryKey: String) {
		diaryTableDao.delete(primaryKey)
	}

	suspend fun deleteAll() {
		diaryTableDao.deleteAll()
	}
}
