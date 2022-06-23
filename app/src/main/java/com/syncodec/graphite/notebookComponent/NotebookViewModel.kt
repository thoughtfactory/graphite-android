package com.syncodec.graphite.notebookComponent

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.database.chapter.ChapterDbEntry
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.database.notebook.NotebookDbEntry
import com.syncodec.graphite.konstant.Status
import com.syncodec.graphite.miscellaneous.CollectionUtils.Companion.listOfField
import com.syncodec.graphite.repository.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NotebookViewModel(application: Application) : AndroidViewModel(application) {

	private val noteRepository: NoteRepository = NoteRepository(graphite = application as Graphite)

	lateinit var activityState: NotebookActivity.ActivityState
	var vaultState = (application as Graphite).vaultState

	lateinit var notebookKey: String
	var notebookDbEntry: MutableState<NotebookDbEntry?> = mutableStateOf(null)
	var chapterList: SnapshotStateList<ChapterDbEntry> = mutableStateListOf()
	var noteList: SnapshotStateList<NoteDbEntry> = mutableStateListOf()

	var status: MutableState<Status> = mutableStateOf(Status.INIT)

	val chapterPath = mutableStateListOf<String>()
	val chapterNamePath = mutableStateListOf<String>()

	fun initData() {
		status.value = Status.LOADING

		viewModelScope.launch(Dispatchers.IO) {
			noteRepository.getNotebookAsFlow(key = notebookKey).collect {
				CoroutineScope(Dispatchers.Main).launch {
					notebookDbEntry.value = it
					status.value = Status.LOADED
				}
			}
		}
		viewModelScope.launch(Dispatchers.IO) {
			noteRepository.getChapterAsFlow(notebookKey = notebookKey).collect {
				CoroutineScope(Dispatchers.Main).launch {
					chapterList.clear()
					chapterList.addAll(it)
				}
			}
		}
		viewModelScope.launch(Dispatchers.IO) {
			noteRepository.getNoteAsFlow(notebookKey = notebookKey).collect {
				CoroutineScope(Dispatchers.Main).launch {
					noteList.clear()
					noteList.addAll(it)
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

	fun updateNotebook(notebook: NotebookDbEntry) =
		viewModelScope.launch { noteRepository.putNotebook(notebook = notebook) }

	fun deleteNote(keyList: List<String>) =
		viewModelScope.launch(Dispatchers.IO) { noteRepository.deleteNote(keyList = keyList) }

	fun deleteChapter(keyList: List<String>) =
		viewModelScope.launch(Dispatchers.IO) {
			chapterList.filter { it.key in keyList }.forEach { chapterDbEntry ->
				val chapterPath = chapterDbEntry.chapterPath.toMutableList()
				chapterPath.add(chapterDbEntry.key)
				noteRepository.deleteNote(
					noteList.filter { it.chapterPath.containsAll(chapterPath) }
						.listOfField(NoteDbEntry::key)
				)
				noteRepository.deleteChapter(
					chapterList.filter { it.chapterPath.containsAll(chapterPath) }
						.listOfField(ChapterDbEntry::key)
				)
				noteRepository.deleteChapter(keyList = keyList)
			}
		}
}
