package com.syncodec.graphite.searchComponent

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.database.tag.TagDbEntry
import com.syncodec.graphite.database.tag.TagKeyDbEntry
import com.syncodec.graphite.miscellaneous.logger
import com.syncodec.graphite.repository.NoteRepository
import com.syncodec.graphite.repository.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class SearchViewModel(application: Application) : AndroidViewModel(application) {

	private val noteRepository: NoteRepository = NoteRepository.getInstance(graphite = application as Graphite)
	private val tagRepository: TagRepository = TagRepository.getInstance(graphite = application as Graphite)

	lateinit var activityState: SearchActivity.ActivityState

	val noteList: SnapshotStateMap<NoteDbEntry, Boolean> = mutableStateMapOf()
	val tagList: SnapshotStateList<TagDbEntry> = mutableStateListOf()
	val tagKeyList: SnapshotStateList<TagKeyDbEntry> = mutableStateListOf()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			noteRepository.getAllAsFlow().collect {
				try {
					noteList.clear()
					it.forEach { noteList[it] = false }
				} catch (exception: Exception) {

				}
			}
		}
		viewModelScope.launch {
			tagRepository.tagList.collect {
				try {
					tagList.clear()
					tagList.addAll(it)
				} catch (exception: Exception) {

				}
			}
		}
		viewModelScope.launch {
			tagRepository.tagKeyList.collect {
				tagKeyList.clear()
				tagKeyList.addAll(it)
			}
		}
	}

	fun searchInNote() {
		noteList.forEach { (note, _) ->
			getApplication<Graphite>().getNoteString(note.key)?.also { noteContent ->
				activityState.queryStringList.forEach { query ->
					val pattern = Pattern.compile("(?:\"text\":\")(\\\\.|[^\\\"])$query(\\\\.|[^\\\"])\\\"")
					noteList[note] = pattern.matcher(noteContent).matches()
					noteList[note] = true
					logger("content : $noteContent")
					logger("match : ${pattern.matcher(noteContent).matches()}")
//					noteList[note] = it.contains("(?:\"text\":\")(\\\\.|[^\\\"])$it(\\\\.|[^\\\"])\\\"")
				}
			}
		}
	}
}
