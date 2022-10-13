package com.syncodec.graphite.presentation.search

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.Repository
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SearchViewModel : ViewModel() {

	val noteList = Repository.getAllNoteAsFlow()
	val tagList = Repository.getAllTagAsFlow()

	val visibleNoteList : SnapshotStateList<NoteObject> = mutableStateListOf()

	var showResultScreen : MutableState<Boolean> = mutableStateOf(false)
	var showFavourite : MutableStateFlow<Boolean> = MutableStateFlow(false)
	var showWithAttachments : MutableStateFlow<Boolean> = MutableStateFlow(false)
	var showTag : MutableStateFlow<TagObject?> = MutableStateFlow(null)
	var searchQuery : MutableStateFlow<String?> = MutableStateFlow(null)

	init {
		viewModelScope.launch(Dispatchers.IO) {
			noteList.combine(showFavourite) { noteList, showFavourite ->
				if (showFavourite) {
					noteList.filter { it.isFavourite }
				} else {
					noteList
				}
			}.combine(showWithAttachments) { noteList, showWithAttachments ->
				if (showWithAttachments) {
					noteList.filter { it.attachmentList.isNotEmpty() }
				} else {
					noteList
				}
			}.combine(showTag) { noteList, showTag ->
				if (showTag != null) {
					noteList.filter { showTag.objectIdList.contains(it.id) }
				} else {
					noteList
				}
			}.combine(searchQuery) { noteList, searchQuery ->
				if(searchQuery != null) {
					noteList.filter { it.title?.contains(searchQuery, true) == true || it.content?.contains(searchQuery, true) == true }
				} else {
					noteList
				}
			}.cancellable().collect {
				withContext(Dispatchers.Main) {
					visibleNoteList.clear()
					visibleNoteList.addAll(it)
				}
			}
		}
	}

	fun showFavourite() {
		showResultScreen.value = true
		showFavourite.tryEmit(true)
		showWithAttachments.tryEmit(false)
		showTag.tryEmit(null)
		searchQuery.tryEmit(null)
	}

	fun showWithAttachments() {
		showResultScreen.value = true
		showFavourite.tryEmit(false)
		showWithAttachments.tryEmit(true)
		showTag.tryEmit(null)
		searchQuery.tryEmit(null)
	}

	fun showTag(tag : TagObject?) {
		showResultScreen.value = true
		showFavourite.tryEmit(false)
		showWithAttachments.tryEmit(false)
		showTag.tryEmit(tag)
		searchQuery.tryEmit(null)
	}

	fun searchInNotes(query : String) {
		showResultScreen.value = true
		showFavourite.tryEmit(false)
		showWithAttachments.tryEmit(false)
		showTag.tryEmit(null)
		searchQuery.tryEmit(query)
	}
}
