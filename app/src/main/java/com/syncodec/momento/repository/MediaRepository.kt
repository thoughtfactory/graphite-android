package com.syncodec.momento.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.media.Media
import com.syncodec.momento.database.media.MediaTableDao
import com.syncodec.momento.database.notebook.Notebook
import com.syncodec.momento.database.notebook.NotebookTableDao

class MediaRepository(application: Application) {
	private var mediaTableDao: MediaTableDao
	init {
		mediaTableDao = UserDatabase.getInstance(application).mediaTableDao
	}

	val mediaListLiveData: LiveData<List<Media>> = mediaTableDao.getAllAsLiveData()
	suspend fun insert(media: Media) {
		mediaTableDao.insert(media)
	}

	suspend fun delete(primaryKey: String) {
		mediaTableDao.delete(primaryKey)
	}

	suspend fun deleteAll() {
		mediaTableDao.deleteAll()
	}
}
