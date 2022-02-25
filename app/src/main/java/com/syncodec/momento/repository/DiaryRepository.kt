package com.syncodec.momento.repository

import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import androidx.lifecycle.LiveData
import com.syncodec.momento.Momento
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.diary.DiaryDbEntry
import com.syncodec.momento.database.diary.DiaryTableDao
import com.syncodec.momento.database.diary.Note
import com.syncodec.momento.noteComponent.TempAttachmentData
import com.syncodec.momento.miscellaneous.bitmapToBase64String
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class DiaryRepository(val momento: Momento) {
	private var diaryTableDao: DiaryTableDao = UserDatabase.getInstance(momento).diaryTableDao

	val diaryDbEntryListLiveData: LiveData<List<DiaryDbEntry>> = diaryTableDao.getAsLiveData()
	val diaryDbEntryKeyListLiveData: LiveData<List<String>> = diaryTableDao.getKeyAsLiveData()

	suspend fun saveDiary(
		note: Note,
		attachmentList: MutableList<TempAttachmentData>,
		deletedTimestamp: Long
	) {
		withContext(Dispatchers.IO) {
			momento.putDiary(note = note)

			DiaryDbEntry(
				primaryKey = note.primaryKey,
				timezoneOffset = note.timezoneOffset
			).apply {
				this.createdTimestamp = note.createdTimestamp
				this.modifiedTimestamp = note.modifiedTimestamp
				this.userTimestamp = note.userTimestamp
				this.contentThumbnail = note.contentThumbnail
				this.latitude = note.location?.latitude
				this.longitude = note.location?.longitude
				this.address = note.address
				this.attachmentCount = attachmentList.size
				this.isArchived = note.isArchived
				this.isFavourite = note.isFavourite
				this.isLocked = note.isLocked
				this.deletedTimestamp = deletedTimestamp

				attachmentList.forEach { tempAttachmentData ->
					if (this.attachmentThumbnail==null) {
						when(tempAttachmentData.mimeType?.split("/")?.first()) {
							"image" -> {
								try {
									val THUMBSIZE = 64

									val thumbImage = ThumbnailUtils.extractThumbnail(
										BitmapFactory.decodeFile(tempAttachmentData.file!!.path),
										THUMBSIZE,
										THUMBSIZE
									)

									this.attachmentThumbnail = thumbImage.bitmapToBase64String()
								} catch (exception: Exception) {

								}
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
		withContext(Dispatchers.IO) {
			diaryTableDao.insert(diaryDbEntry)
		}
	}

	fun loadDiary(primaryKey: String): Note {
		return momento.getDiary(primaryKey = primaryKey)
	}

	suspend fun delete(primaryKey: String) {
		withContext(Dispatchers.IO) {
			diaryTableDao.delete(primaryKey)
		}
	}

	suspend fun moveToTrash(primaryKey: String) {
		withContext(Dispatchers.IO) {
			diaryTableDao.get(primaryKey)?.apply {
				this.deletedTimestamp = System.currentTimeMillis()
				diaryTableDao.insert(this)
			}
		}
	}

	suspend fun deleteAll() {
		diaryTableDao.deleteAll()
	}
}
