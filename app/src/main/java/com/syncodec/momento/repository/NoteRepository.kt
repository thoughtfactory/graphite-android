package com.syncodec.momento.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.LatLng
import com.syncodec.momento.Momento
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.chapter.ChapterDbEntry
import com.syncodec.momento.database.chapter.ChapterTableDao
import com.syncodec.momento.database.note.Note
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.database.note.NoteTableDao
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.database.notebook.NotebookTableDao
import com.syncodec.momento.miscellaneous.bitmapToBase64String
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.noteComponent.TempAttachmentData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Singleton

@Singleton
class NoteRepository(val momento: Momento) {
	private var noteTableDao: NoteTableDao = UserDatabase.getInstance(momento).noteTableDao
	private var notebookTableDao: NotebookTableDao = UserDatabase.getInstance(momento).notebookTableDao
	private var chapterTableDao: ChapterTableDao = UserDatabase.getInstance(momento).chapterTableDao

	val noteDbEntryListLiveData: LiveData<List<NoteDbEntry>> = noteTableDao.getAllAsLiveData()
	val diaryDbEntryKeyListLiveData: LiveData<List<String>> = noteTableDao.getKeyAsLiveData()
	val notebookDbEntryListLiveData: LiveData<List<NotebookDbEntry>> = notebookTableDao.getAllAsLiveData()

	suspend fun insert(noteDbEntry: NoteDbEntry) = withContext(Dispatchers.IO) { noteTableDao.insert(noteDbEntry) }
	suspend fun insert(chapterDbEntry: ChapterDbEntry) = withContext(Dispatchers.IO) { chapterTableDao.insert(chapterDbEntry) }
	suspend fun insert(notebookDbEntry: NotebookDbEntry) = withContext(Dispatchers.IO) { notebookTableDao.insert(notebookDbEntry) }

	suspend fun getNote(key: String): NoteDbEntry? = withContext(Dispatchers.IO){ noteTableDao.get(key = key) }
	suspend fun getChapter(key: String): ChapterDbEntry? = withContext(Dispatchers.IO){ chapterTableDao.get(key = key) }
	suspend fun getNotebook(key: String): NotebookDbEntry? = withContext(Dispatchers.IO){ notebookTableDao.get(key = key) }

	suspend fun deleteNote(key: String) = withContext(Dispatchers.IO) { noteTableDao.delete(key = key) }
	suspend fun deleteChapter(key: String) = withContext(Dispatchers.IO) { chapterTableDao.delete(key = key) }
	suspend fun deleteNotebook(key: String) = withContext(Dispatchers.IO) { notebookTableDao.delete(key) }

	suspend fun getAllKey(): List<String> = TODO()

	fun getNoteAsLiveData(notebookKey: String): LiveData<List<NoteDbEntry>> = noteTableDao.getFromNotebookAsLiveData(notebookKey = notebookKey)
	fun getChapterAsLiveData(notebookKey: String): LiveData<List<ChapterDbEntry>> = chapterTableDao.getFromNotebookAsLiveData(notebookKey = notebookKey)

	suspend fun putNote(
		note: Note,
		attachmentList: MutableList<TempAttachmentData>,
		deletedTimestamp: Long
	) {
		withContext(Dispatchers.IO) {

			NoteDbEntry(
				key = note.primaryKey,
				timezoneOffset = note.timezoneOffset,
				notebookKey = note.notebookKey,
				chapterPath = note.chapterPath
			).apply {
				this.createdTimestamp = note.createdTimestamp
				this.modifiedTimestamp = note.modifiedTimestamp
				this.userTimestamp = note.userTimestamp
				this.title = note.title
				this.contentThumbnail = note.contentThumbnail
				this.latLng = note.location?.longitude?.let { note.location?.latitude?.let { it1 -> LatLng(it1, it) } }
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
				momento.putNote(note = note)
			}
		}
	}

	suspend fun putNotebook(
		title: String,
		description: String?,
		color: Int?,
		image: Bitmap?
	): String {
		val primaryKey = generatePrimaryKey()
		withContext(Dispatchers.IO) {
			val currentTimestamp = System.currentTimeMillis()

			if (image != null) {
				momento.putNotebookImage(notebookKey = primaryKey, image = image)
			}

			NotebookDbEntry(
				key = primaryKey,
				createdTimestamp = currentTimestamp,
			).apply {
				this.title = title
				this.description = description
				this.modifiedTimestamp = currentTimestamp
				this.color = color

				insert(this)
			}
		}

		return primaryKey
	}

	suspend fun putChapter(
		title: String,
		description: String?,
		notebookKey: String,
		currentRoute: MutableList<String>
	) {
		withContext(Dispatchers.IO) {
			val primaryKey = generatePrimaryKey()
			val currentTimestamp = System.currentTimeMillis()

			ChapterDbEntry(
				key = primaryKey,
				notebookKey = notebookKey,
				chapterPath = currentRoute
			).apply {
				this.createdTimestamp = currentTimestamp
				this.modifiedTimestamp = currentTimestamp
				this.title = title
				this.description = description
			}.apply {
				insert(this)
			}
		}
	}

	fun loadDiary(primaryKey: String): Note {
		TODO()
		return momento.getNote("", "")
	}

	companion object {
		private var INSTANCE: NoteRepository? = null

		fun getInstance(momento: Momento): NoteRepository {
			synchronized(lock = this) {
				var instance = INSTANCE
				if (instance == null) {
					instance = NoteRepository(momento = momento)
					INSTANCE = instance
				}
				return instance
			}
		}
	}
}
