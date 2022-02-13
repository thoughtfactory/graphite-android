package com.syncodec.momento.repository

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.syncodec.momento.Momento
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.notebook.Chapter
import com.syncodec.momento.database.notebook.Notebook
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.database.notebook.NotebookTableDao
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

class NotebookRepository(val application: Application) {
	private var notebookTableDao: NotebookTableDao = UserDatabase.getInstance(application).notebookTableDao

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

				(application as Momento).putNotebook(this)
			}

			if (image != null) {
				(application as Momento).putNotebookImage(notebookKey = primaryKey, image = image)
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
				this.modifiedTime = currentTimestamp
				this.title = title
				this.description = description

				(application as Momento).putChapter(
					chapter = this,
				)
			}
		}
	}

	@Throws(FileNotFoundException::class)
	fun openNotebook(primaryKey: String): Notebook {
		return (application as Momento).openNotebook(primaryKey = primaryKey)
	}
}
