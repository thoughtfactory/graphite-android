package com.syncodec.graphite.presentation.main.composable.screen.noteScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.local.NoteObject
import com.syncodec.graphite.di.model.local.NoteObjectLite
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.di.repository.group.RealmObjectGroupList
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class NoteScreenViewModel(lockableRepo: LockableRepo) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _defaultChapterId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	val defaultChapterId: StateFlow<RealmUUID?> = _defaultChapterId

	private val _tagList: MutableStateFlow<List<TagObject>> = MutableStateFlow(listOf())
	val tagList: StateFlow<List<TagObject>> = _tagList

	private val _noteGroupList: MutableStateFlow<RealmObjectGroupList<NoteObjectLite>?> = MutableStateFlow(null)
	val noteGroupList: StateFlow<RealmObjectGroupList<NoteObjectLite>?> = _noteGroupList

	private var defaultChapterChildObserverJob: Job? = null

	init {
		viewModelScope.launch(Dispatchers.Default) {
			lockableRepo.repositoryStatusFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			_repository.collectLatest { repository1 ->
				launch { observeDefaultChapter(repository = repository1) }
				launch { observeTags(repository = repository1) }
			}
		}
	}

	private suspend fun observeDefaultChapter(repository: Repository?) {
		repository?.getDefaultChapterIdAsFlow()?.collectLatest { defaultChapterId1 ->
			this@NoteScreenViewModel._defaultChapterId.tryEmit(defaultChapterId1)
			observeNotes(repository = repository, chapterId = defaultChapterId1)
		}
	}

	private fun observeNotes(repository: Repository?, chapterId: RealmUUID?) {
		defaultChapterChildObserverJob?.cancel()
		viewModelScope.launch(Dispatchers.Default) {
			Job(viewModelScope.coroutineContext.job).let { job ->
				defaultChapterChildObserverJob = job
				launch(Dispatchers.Default + job) {
					repository?.getDefaultNoteLiteMapAsFlow3()?.collectLatest { noteGroupList1 ->
						this@NoteScreenViewModel._noteGroupList.tryEmit(noteGroupList1)
					}
				}
			}
		}
	}

	private suspend fun observeTags(repository: Repository?) {
		repository?.getAllObjectOfTypeAsFlow<TagObject>(includeLocked = true)?.collectLatest { tagList1 ->
			this@NoteScreenViewModel._tagList.tryEmit(tagList1)
		}
	}

	fun onClickMultiFavourite(idList: Set<RealmUUID>, isAllFavourite: Boolean) {
		viewModelScope.launch(Dispatchers.Default) {
			idList.forEach { noteId ->
				_repository.value?.getObjectFromId<NoteObject>(id = noteId)?.clone()?.apply {
					this.isFavourite = !isAllFavourite
					_repository.value?.putNote(this)
				}
			}
		}
	}

	fun onClickMultiLock(idList: Set<RealmUUID>, isAllLocked: Boolean) {
		viewModelScope.launch(Dispatchers.Default) {
			idList.forEach { noteId ->
				_repository.value?.getObjectFromId<NoteObject>(id = noteId)?.clone()?.apply {
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
