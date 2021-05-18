package com.syncodec.momento.notebookComponent

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.syncodec.momento.Momento
import com.syncodec.momento.database.chapter.ChapterDbEntry
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.repository.NoteRepository
import kotlinx.coroutines.launch


class NotebookViewModel(application: Application) : AndroidViewModel(application) {

	private val noteRepository: NoteRepository = NoteRepository(momento = application as Momento)

	lateinit var activityState: NotebookActivity.ActivityState

	lateinit var notebookKey: String
	lateinit var notebookDbEntry: NotebookDbEntry
	var noteList: LiveData<List<NoteDbEntry>> = MutableLiveData()
	var chapterList: LiveData<List<ChapterDbEntry>> = MutableLiveData()

	var status: MutableState<Status> = mutableStateOf(Status.INIT)

	val chapterPath = mutableStateListOf<String>()
	val chapterNamePath = mutableStateListOf<String>()

	suspend fun initData() {
		if (status.value != Status.ERROR) status.value = Status.LOADING

		val tmpNotebookDbEntry = noteRepository.getNotebook(key = notebookKey)
		if (tmpNotebookDbEntry!=null) {
			notebookDbEntry = tmpNotebookDbEntry
		} else {
			status.value = Status.ERROR
		}

		noteList = noteRepository.getNoteAsLiveData(notebookKey = notebookKey)
		chapterList = noteRepository.getChapterAsLiveData(notebookKey = notebookKey)

		if (status.value != Status.ERROR) status.value = Status.LOADED
	}

	fun putChapter(
		title: String,
		description: String?
	) {
		viewModelScope.launch {
			noteRepository.putChapter(
				title = title,
				description = description,
				notebookKey = notebookKey,
				currentRoute = chapterPath
			)
		}
	}
}
