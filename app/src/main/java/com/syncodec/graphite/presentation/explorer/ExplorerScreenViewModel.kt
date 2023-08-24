package com.syncodec.graphite.presentation.explorer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.Repository
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class ExplorerScreenViewModel(private val repository : Repository) : ViewModel() {

	val repositoryState = repository.repositoryState

	private val _tagList : MutableStateFlow<List<TagObject>> = MutableStateFlow(listOf())
	val tagList : StateFlow<List<TagObject>> = _tagList
	private val _noteList : MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())
	private val _filteredNoteList : MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())
	val filteredNoteList : StateFlow<List<NoteObjectLite>> = _filteredNoteList

	private val _chapterObject = MutableStateFlow<ChapterObjectLite?>(null)
	val chapterObject : StateFlow<ChapterObjectLite?> = _chapterObject

	private val _filter : MutableStateFlow<(NoteObjectLite) -> Boolean> = MutableStateFlow { true }
	val filter : StateFlow<(NoteObjectLite) -> Boolean> = _filter

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				if (it == Repository.Companion.RepositoryState.Success) {
					observeNotes()
					observeTags()
					observeFilter()
				}
			}
		}
	}

	private fun observeNotes() {
		viewModelScope.launch(Dispatchers.Default) {
			repository.getAllNoteLiteAsFlow().collect { noteList ->
				this@ExplorerScreenViewModel._noteList.tryEmit(noteList)
			}
		}
	}

	private fun observeTags() {
		viewModelScope.launch(Dispatchers.Default) {
			repository.getAllTagAsFlow().collect { tagList ->
				this@ExplorerScreenViewModel._tagList.tryEmit(tagList)
			}
		}
	}

	private fun observeFilter() {
		viewModelScope.launch(Dispatchers.Default) {
			combine(
				_noteList,
				_chapterObject,
				_filter
			) { noteList1, chapterObject1, filter1 ->
				noteList1.filter { note ->
					chapterObject.value?.id?.let { note.parentId == it } ?: true && filter1(note)
				}.let { _filteredNoteList.tryEmit(it) }
			}.collect()
		}
	}

	fun filterOnDefaultChapter() {
		viewModelScope.launch(Dispatchers.Default) {
			repository.getDefaultChapterId().let { repository.getChapterFromId(it).let { chapter -> _chapterObject.tryEmit(chapter?.toLite()) } }
		}
	}

	fun filterOnChapter(chapterObject : RealmUUID?) = viewModelScope.launch(Dispatchers.Default) {
		chapterObject?.let { repository.getChapterFromId(it).let { chapter -> _chapterObject.tryEmit(chapter?.toLite()) } }
	}

	fun filterOn(filter : (NoteObjectLite) -> Boolean) = _filter.tryEmit(filter)

	fun delete(idList : List<RealmUUID>) {
		repository.deleteSuspended(idList)
	}
}
