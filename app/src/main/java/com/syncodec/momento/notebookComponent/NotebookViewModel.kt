package com.syncodec.momento.notebookComponent

import android.app.Application
import android.os.FileObserver
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.momento.Momento
import com.syncodec.momento.database.notebook.Notebook
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.repository.NotebookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException


class NotebookViewModel(application: Application) : AndroidViewModel(application) {

	private val notebookRepository: NotebookRepository = NotebookRepository(application)

	lateinit var notebookActivityState: NotebookActivity.NotebookActivityState

	lateinit var notebookKey: String
	private val _status: MutableState<Status> = mutableStateOf(Status.INIT)
	val status: State<Status> get() = _status

	lateinit var fileObserver: FileObserver

	var notebook by mutableStateOf(
		Notebook(
			primaryKey = ""
		)
	)
	var currentRoute = mutableStateListOf<String>()

	fun observeNotebook() {
		fileObserver = object : FileObserver(
			File(getApplication<Momento>().getNotebookDataPath(notebookKey = notebookKey)),
			CLOSE_WRITE
		) {
			/*
				1       ACCESS
				2       MODIFY
				4       ATTRIB
				8       CLOSE_WRITE
				16      CLOSE_NOWRITE
				32      OPEN
				64      MOVED_FROM
				128     MOVED_TO
				256     CREATE
				512     DELETE
				1024    DELETE_SELF
				2048    MOVE_SELF
				4095    ALL_EVENTS
			 */
			override fun onEvent(event: Int, file: String?) {
				if (event == CLOSE_WRITE) {
					openNotebook()
				}
			}
		}
		fileObserver.startWatching()
	}

	fun openNotebook() {
		viewModelScope.launch {
			withContext(Dispatchers.Main) { _status.value = Status.LOADING }
			try {
				withContext(Dispatchers.IO) {
					val tmpNotebook = notebookRepository.openNotebook(primaryKey = notebookKey)
					notebook.apply {
						this.primaryKey = tmpNotebook.primaryKey
						this.createdTimestamp = tmpNotebook.createdTimestamp
						this.modifiedTimestamp = tmpNotebook.modifiedTimestamp
						this.contentThumbnail = tmpNotebook.contentThumbnail
						this.title = tmpNotebook.title
						this.description = tmpNotebook.description
						this.chapterMap = tmpNotebook.chapterMap
						this.noteList = tmpNotebook.noteList
						this.isFavourite = tmpNotebook.isFavourite
						this.isArchived = tmpNotebook.isArchived
						this.isLocked = tmpNotebook.isLocked
					}
					withContext(Dispatchers.Main) { _status.value = Status.SUCCESS }
				}
			} catch (exception: FileNotFoundException) {
				withContext(Dispatchers.Main) { _status.value = Status.ERROR }
			} catch (exception: Exception) {
				withContext(Dispatchers.Main) { _status.value = Status.ERROR }
			}
		}
	}

	fun createNewChapter(
		title: String,
		description: String?
	) {
		viewModelScope.launch {
			notebookRepository.createNewChapter(
				title = title,
				description = description,
				notebookKey = notebookKey,
				currentRoute = currentRoute
			)
		}
	}

//	fun readChapter() {
//		viewModelScope.launch {
//			withContext(Dispatchers.IO) {
//				try {
//					currentChapter = notebookRepository.openChapter(
//						notebookKey = notebookKey,
//						currentRoute = currentRoute
//					)
//					withContext(Dispatchers.Main) { _status.value = 1 }
//				} catch (exception: FileNotFoundException) {
//					withContext(Dispatchers.Main) { _status.value = -1 }
//				} catch (exception: Exception) {
//					withContext(Dispatchers.Main) { _status.value = -2 }
//				}
//			}
//		}
//	}
}
