package com.syncodec.momento.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.notebook.Notebook
import com.syncodec.momento.database.notebook.NotebookTableDao

class NotebookRepository(application: Application) {
	private var notebookTableDao: NotebookTableDao
	init {
		notebookTableDao = UserDatabase.getInstance(application).notebookTableDao
	}

	val notebookListLiveData: LiveData<List<Notebook>> = notebookTableDao.getAllAsLiveData()
	suspend fun insert(notebook: Notebook) {
		notebookTableDao.insert(notebook)
	}

	suspend fun delete(primaryKey: String) {
		notebookTableDao.delete(primaryKey)
	}

	suspend fun deleteAll() {
		notebookTableDao.deleteAll()
	}
}
