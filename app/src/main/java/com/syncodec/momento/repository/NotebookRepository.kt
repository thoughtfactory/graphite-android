package com.syncodec.momento.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import androidx.lifecycle.LiveData
import com.syncodec.momento.Momento
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.diary.Note
import com.syncodec.momento.database.notebook.Chapter
import com.syncodec.momento.database.notebook.Notebook
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.database.notebook.NotebookTableDao
import com.syncodec.momento.miscellaneous.bitmapToBase64String
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.noteComponent.TempAttachmentData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import javax.inject.Singleton

@Singleton
class NotebookRepository(val momento: Momento) {
	private var notebookTableDao: NotebookTableDao = UserDatabase.getInstance(momento).notebookTableDao

	val notebookDbEntryListLiveData: LiveData<List<NotebookDbEntry>> = notebookTableDao.getAllAsLiveData()
	suspend fun insert(notebookDbEntry: NotebookDbEntry) {
		notebookTableDao.insert(notebookDbEntry)
	}

	suspend fun delete(primaryKey: String) {
		notebookTableDao.delete(primaryKey)
	}

	suspend fun deleteAll() {
		notebookTableDao.deleteAll()
	}

	suspend fun createNewNotebook(
		title: String,
		description: String?,
		color: Int?,
		image: Bitmap?
	) {
		withContext(Dispatchers.IO) {
			val primaryKey = generatePrimaryKey()
			val currentTimestamp = System.currentTimeMillis()

			Notebook(
				primaryKey = primaryKey
			).apply {
				this.createdTimestamp = currentTimestamp
				this.modifiedTimestamp = currentTimestamp
				this.title = title
				this.description = description
				this.color = color

				momento.putNotebook(this)
			}

			if (image != null) {
				momento.putNotebookImage(notebookKey = primaryKey, image = image)
			}

			NotebookDbEntry(
				primaryKey = primaryKey,
				createdTimestamp = currentTimestamp,
			).apply {
				this.title = title
				this.description = description
				this.modifiedTimestamp = currentTimestamp
				this.color = color

				insert(this)
			}
		}
	}

	suspend fun createNewChapter(
		title: String,
		description: String?,
		notebookKey: String,
		currentRoute: MutableList<String>
	) {
		withContext(Dispatchers.IO) {
			val primaryKey = generatePrimaryKey()
			val currentTimestamp = System.currentTimeMillis()

			Chapter(
				primaryKey = primaryKey,
				notebookKey = notebookKey,
				notebookRoute = currentRoute
			).apply {
				this.createdTimestamp = currentTimestamp
				this.modifiedTimestamp = currentTimestamp
				this.title = title
				this.description = description

				momento.putChapter(
					chapter = this,
				)
			}
		}
	}

	@Throws(FileNotFoundException::class)
	fun openNotebook(primaryKey: String): Notebook {
		return momento.getNotebook(primaryKey = primaryKey)
	}

	suspend fun saveNote(
		note: Note,
		attachmentList: MutableList<TempAttachmentData>,
		deletedTimestamp: Long
	) {
		withContext(Dispatchers.IO) {
			attachmentList.forEach { tempAttachmentData ->
				if (note.attachmentThumbnail==null) {
					when(tempAttachmentData.mimeType?.split("/")?.first()) {
						"image" -> {
							try {
								val THUMBSIZE = 64

								val thumbImage = ThumbnailUtils.extractThumbnail(
									BitmapFactory.decodeFile(tempAttachmentData.file!!.path),
									THUMBSIZE,
									THUMBSIZE
								)

								note.attachmentThumbnail = thumbImage.bitmapToBase64String()
							} catch (exception: Exception) {

							}
						}
						"video" -> {}
					}
				}
			}
			momento.putNote(note = note)
		}
	}
}
