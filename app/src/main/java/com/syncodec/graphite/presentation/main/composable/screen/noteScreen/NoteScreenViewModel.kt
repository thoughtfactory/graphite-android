package com.syncodec.graphite.presentation.main.composable.screen.noteScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.di.repository.group.RealmObjectGroupList
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class NoteScreenViewModel(
	repositoryStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>,
) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _defaultChapterId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	val defaultChapterId: StateFlow<RealmUUID?> = _defaultChapterId

	private val _tagList: MutableStateFlow<List<TagObject>> = MutableStateFlow(listOf())
	val tagList: StateFlow<List<TagObject>> = _tagList
	private val _noteList: MutableStateFlow<RealmObjectGroupList<NoteObjectLite>?> = MutableStateFlow(null)
	val noteList: StateFlow<RealmObjectGroupList<NoteObjectLite>?> = _noteList

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryStateFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			_repository.collectLatest { repository1 ->
				launch { observeDefaultChapter(repository = repository1) }
				launch { observeNotes(repository = repository1) }
				launch { observeTags(repository = repository1) }
			}
		}
	}

	private suspend fun observeDefaultChapter(repository: Repository?) {
		repository?.getDefaultChapterIdAsFlow()?.collectLatest { defaultChapterId1 ->
			this@NoteScreenViewModel._defaultChapterId.tryEmit(defaultChapterId1)
		}
	}

	private suspend fun observeNotes(repository: Repository?) {
		repository?.getDefaultNoteLiteMapAsFlow2()?.collectLatest { noteList1 ->
			this@NoteScreenViewModel._noteList.tryEmit(noteList1)
		}
	}

	private suspend fun observeTags(repository: Repository?) {
		repository?.getAllTagAsFlow()?.collectLatest { tagList1 ->
			this@NoteScreenViewModel._tagList.tryEmit(tagList1)
		}
	}

	fun onClickMultiFavourite(idList: Set<RealmUUID>, isAllFavourite: Boolean) {
		viewModelScope.launch(Dispatchers.Default) {
			idList.forEach { noteId ->
				_repository.value?.getNoteFromId(id = noteId)?.clone()?.apply {
					this.isFavourite = !isAllFavourite
					_repository.value?.putNote(this)
				}
			}
		}
	}

	fun onClickMultiLock(idList: Set<RealmUUID>, isAllLocked: Boolean) {
		viewModelScope.launch(Dispatchers.Default) {
			idList.forEach { noteId ->
				_repository.value?.getNoteFromId(id = noteId)?.clone()?.apply {
					this.isLocked = !isAllLocked
					_repository.value?.putNote(this)
				}
			}
		}
	}

	fun delete(idList: Set<RealmUUID>) {
		_repository.value?.deleteSuspended(idList)
	}

	fun addDebugData() {
		viewModelScope.launch(Dispatchers.IO) {
			for (i in 0..429) {
				NoteObject.getRandomInstance().apply {
					this.parentId = defaultChapterId.value
					_repository.value?.putNote(noteObject = this)
				}
				delay(100)
			}
		}
	}
}
