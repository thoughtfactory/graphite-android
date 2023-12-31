package com.syncodec.graphite.presentation.explorer.meta

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.local.ChapterObject
import com.syncodec.graphite.di.model.local.ChapterObjectLite
import com.syncodec.graphite.di.model.local.NoteObject
import com.syncodec.graphite.di.model.local.NoteObjectLite
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

abstract class AbstractExploreViewModel(lockableRepo: LockableRepo) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _defaultChapterId : MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	val defaultChapterId : StateFlow<RealmUUID?> = _defaultChapterId

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
			lockableRepo.repositoryStatusFlow.collect { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			_repository.collectLatest { repository ->
				repository?.getDefaultChapterIdAsFlow()?.collectLatest { defaultChapterId ->
					this@AbstractExploreViewModel._defaultChapterId.tryEmit(defaultChapterId )
				}
			}
		}

//		Updates chapterObject
		viewModelScope.launch(Dispatchers.Default) {
			combine(_repository, _currentChapterId) { repository1, currentChapterId1 -> Pair(repository1, currentChapterId1) }.collectLatest { (repository1, currentChapterId1) ->
				repository1?.getObjectFromIdAsFlow<ChapterObject>(id = currentChapterId1)?.collectLatest { chapterObject1 ->
					this@AbstractExploreViewModel._currentChapter.tryEmit(chapterObject1?.toLite())
				}
			}
		}

//		Observe notes
		viewModelScope.launch(Dispatchers.Default) {
			_repository.collectLatest { repository1 ->
				repository1?.getAllNoteLiteAsFlow2()?.collectLatest { noteObjectLiteList ->
					Log.d("npr71", "noteObjectLiteList : ${noteObjectLiteList.size}")
					this@AbstractExploreViewModel._noteList.tryEmit(noteObjectLiteList)
				}
			}
		}

//		Observe tags
		viewModelScope.launch(Dispatchers.Default) {
			_repository.collectLatest { repository1 ->
				repository1?.getAllObjectOfTypeAsFlow<TagObject>(includeLocked = true)?.collectLatest { tagObjectList ->
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

	fun onClickMultiFavourite(idList: Set<RealmUUID>) {
		viewModelScope.launch(Dispatchers.Default) {
			val isAllFavourite = chapterFilteredNoteList.value.filter { it.id in idList }.all { it.isFavourite }
			_repository.value?.setMultiObjectFromIdSuspended<NoteObject>(idList = idList) {
				this.isFavourite = !isAllFavourite
			}
		}
	}

	fun onClickMultiLock(idList: Set<RealmUUID>) {
		viewModelScope.launch(Dispatchers.Default) {
			val isAllLocked = chapterFilteredNoteList.value.filter { it.id in idList }.all { it.isLocked }
			_repository.value?.setMultiObjectFromIdSuspended<NoteObject>(idList = idList) {
				this.isLocked = !isAllLocked
			}
		}
	}

	fun delete(idList: Set<RealmUUID>) {
		_repository.value?.deleteSuspended(idList)
	}
}
