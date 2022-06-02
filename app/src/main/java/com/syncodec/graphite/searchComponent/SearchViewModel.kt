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
import com.syncodec.graphite.miscellaneous.CollectionUtils.Companion.listOfField
import com.syncodec.graphite.repository.NoteRepository
import com.syncodec.graphite.repository.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.random.Random

class SearchViewModel(application: Application) : AndroidViewModel(application) {

	val regex = Regex(pattern = "\"text\"\\s*:\\s*\"([^\"]+)\",?")

	private val noteRepository: NoteRepository =
		NoteRepository.getInstance(graphite = application as Graphite)
	private val tagRepository: TagRepository =
		TagRepository.getInstance(graphite = application as Graphite)

	lateinit var activityState: SearchActivity.ActivityState

	var showLocked: Boolean = false

	val noteList: SnapshotStateMap<NoteDbEntry, Boolean> = mutableStateMapOf()
	val tagList: SnapshotStateList<TagDbEntry> = mutableStateListOf()
	val tagKeyList: SnapshotStateList<TagKeyDbEntry> = mutableStateListOf()

	private var searchCanceller = Random.nextInt()

	init {
		viewModelScope.launch(Dispatchers.IO) {
			noteRepository.getAllAsFlow().collect {
				try {
					noteList.clear()
					it.filter { it.isLocked == showLocked }.forEach { noteList[it] = false }
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
		searchCanceller = Random.nextInt()
		val currentSearchCanceller = searchCanceller
		viewModelScope.launch(Dispatchers.Default) {
			noteList.forEach { (note, _) -> noteList[note] = false }
			noteList.forEach { (note, _) ->
				getApplication<Graphite>().getNoteString(note.key)?.also { noteContent ->
					activityState.queryStringList.forEach { query ->
						if (currentSearchCanceller != searchCanceller) this.cancel()
						regex.findAll(noteContent).forEach matcher@{
							it.value.substring(7)
								.dropLast(1)
								.contains(query, ignoreCase = true)
								.also {
									if (it) {
										noteList[note] = true
										return@matcher
									}
								}
						}
					}
				}
			}
		}
	}

	fun searchInTag() {
		searchCanceller = Random.nextInt()
		val currentSearchCanceller = searchCanceller
		viewModelScope.launch(Dispatchers.Default) {
			noteList.forEach { (note, _) -> noteList[note] = false }

			val filteredNoteKeyList = tagKeyList.filter { it.tag in activityState.queryTagList}.listOfField(TagKeyDbEntry::key)
			noteList.forEach { (note, _) ->
				if (currentSearchCanceller != searchCanceller) this.cancel()
				noteList[note] = note.key in filteredNoteKeyList
			}
		}
	}
}
