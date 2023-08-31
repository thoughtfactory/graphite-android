package com.syncodec.graphite.presentation.explorer.meta

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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

abstract class AbstractExploreViewModel(repositoryStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _currentChapterId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	private val _currentChapter: MutableStateFlow<ChapterObjectLite?> = MutableStateFlow(null)
	val currentChapter: StateFlow<ChapterObjectLite?> = _currentChapter

	private val _noteList: MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())
	private val _tagList: MutableStateFlow<List<TagObject>> = MutableStateFlow(listOf())
	private val _taggedNoteList: MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())

	private val _chapterFilteredNoteList: MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())
	val chapterFilteredNoteList: StateFlow<List<NoteObjectLite>> = _chapterFilteredNoteList

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryStateFlow.collect { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}

//		Updates chapterObject
		viewModelScope.launch(Dispatchers.Default) {
			combine(_repository, _currentChapterId) { repository1, currentChapterId1 -> Pair(repository1, currentChapterId1) }.collectLatest { (repository1, currentChapterId1) ->
				repository1?.getChapterFromIdAsFlow(id = currentChapterId1)?.collect { chapterObject1 ->
					this@AbstractExploreViewModel._currentChapter.tryEmit(chapterObject1?.toLite())
				}
			}
		}

//		Observe notes
		viewModelScope.launch(Dispatchers.Default) {
			_repository.collectLatest { repository1 ->
				repository1?.getAllNoteLiteAsFlow()?.collectLatest { noteObjectLiteList ->
					this@AbstractExploreViewModel._noteList.tryEmit(noteObjectLiteList)
				}
			}
		}

//		Observe tags
		viewModelScope.launch(Dispatchers.Default) {
			_repository.collectLatest { repository1 ->
				repository1?.getAllTagAsFlow()?.collectLatest { tagObjectList ->
					this@AbstractExploreViewModel._tagList.tryEmit(tagObjectList)
				}
			}
		}

//		Merge tags with notes
		viewModelScope.launch(Dispatchers.Default) {
			combine(_noteList, _tagList) { noteList1, tagList1 -> Pair(noteList1, tagList1) }.collectLatest { (noteList1, tagList1) ->
				val noteList = noteList1.map { noteObjectLite ->
					noteObjectLite.copy(tagList = tagList1.filter { tag -> noteObjectLite.id in tag.objectIdList }.map { tag -> tag.toLite() })
				}
				this@AbstractExploreViewModel._taggedNoteList.tryEmit(noteList)
			}
		}

//		Filter notes according to current chapter
		viewModelScope.launch(Dispatchers.Default) {
			combine(_currentChapterId, _taggedNoteList) { currentChapterId1, noteList1 -> Pair(currentChapterId1, noteList1) }.collectLatest { (currentChapterId1, noteList1) ->
				this@AbstractExploreViewModel._chapterFilteredNoteList.tryEmit(noteList1.filter { if (currentChapterId1 == null) true else it.parentId == currentChapterId1 })
			}
		}
	}

	fun loadChapter(chapterId: RealmUUID?) {
		this._currentChapterId.tryEmit(chapterId)
	}
}
