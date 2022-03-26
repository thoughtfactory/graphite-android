package com.syncodec.momento.notebookComponent

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.momento.Momento
import com.syncodec.momento.database.chapter.ChapterDbEntry
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NotebookViewModel(application: Application) : AndroidViewModel(application) {

	private val noteRepository: NoteRepository = NoteRepository(momento = application as Momento)

	lateinit var activityState: NotebookActivity.ActivityState

	lateinit var notebookKey: String
	lateinit var notebookDbEntry: NotebookDbEntry
	var noteList: SnapshotStateList<NoteDbEntry> = mutableStateListOf()
	var chapterList: SnapshotStateList<ChapterDbEntry> = mutableStateListOf()

	var status: MutableState<Status> = mutableStateOf(Status.INIT)

	val chapterPath = mutableStateListOf<String>()
	val chapterNamePath = mutableStateListOf<String>()

	suspend fun initData() {
		status.value = Status.LOADING

		noteRepository.getNotebook(key = notebookKey).also {
			if (it == null) {
				status.value = Status.ERROR
			} else {
				status.value = Status.LOADED
				viewModelScope.launch(Dispatchers.IO) {
					noteRepository.getNoteAsFlow(notebookKey = notebookKey).collect {
						noteList.removeAll { true }
						noteList.addAll(it)
					}
				}
				viewModelScope.launch(Dispatchers.IO) {
					noteRepository.getChapterAsFlow(notebookKey = notebookKey).collect {
						chapterList.removeAll { true }
						chapterList.addAll(it)
					}
				}
			}
		}
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
