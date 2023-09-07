package com.syncodec.graphite.presentation.main.composable.screen.noteScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.timeStampToPrettyDay
import com.syncodec.graphite.utils.timestampToCalendarDay
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class NoteScreenViewModel(
	repositoryStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>,
	private val dataStoreInstance: DataStoreInstance,
	private val isAuthenticated: StateFlow<Boolean>,
) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _defaultChapterId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	val defaultChapterId: StateFlow<RealmUUID?> = _defaultChapterId

	private val _noteList: MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())
	private val _tagList: MutableStateFlow<List<TagObject>> = MutableStateFlow(listOf())
	private val _taggedNoteList: MutableStateFlow<List<NoteObjectLite>> = MutableStateFlow(listOf())
	private val _mappedNoteList: MutableStateFlow<Map<String, List<NoteObjectLite>>?> = MutableStateFlow(null)
	val mappedNoteList: StateFlow<Map<String, List<NoteObjectLite>>?> = _mappedNoteList

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
				launch { mergeSortAndFilterNote() }
			}
		}
	}

	private suspend fun observeDefaultChapter(repository: Repository?) {
		repository?.getDefaultChapterIdAsFlow()?.collectLatest { defaultChapterId1 ->
			this@NoteScreenViewModel._defaultChapterId.tryEmit(defaultChapterId1)
		}
	}

	private suspend fun observeNotes(repository: Repository?) {
		repository?.getAllNoteLiteAsFlow()?.cancellable()?.collectLatest { noteList1 ->
			this@NoteScreenViewModel._noteList.tryEmit(noteList1)
		}
	}

	private suspend fun observeTags(repository: Repository?) {
		repository?.getAllTagAsFlow()?.cancellable()?.collectLatest { tagList1 ->
			this@NoteScreenViewModel._tagList.tryEmit(tagList1)
		}
	}

	private suspend fun mergeSortAndFilterNote() {

		combine(
			_noteList,
			_defaultChapterId,
			isAuthenticated,
			_tagList,
			dataStoreInstance.getSortBy,
			dataStoreInstance.getSortOn
		) { args ->
			val noteList1 = args[0] as List<NoteObjectLite>
			val defaultChapterId1 = args[1] as RealmUUID?
			val isAuthenticated1 = args[2] as Boolean
			val tagList1 = args[3] as List<TagObject>
			val sortBy1 = args[4] as SortBy
			val sortOn1 = args[5] as SortOn

			noteList1
				.filter { it.parentId == defaultChapterId1 }
				.filter { if (!isAuthenticated1) !it.isLocked else true }
				.map { noteObjectLite ->
					noteObjectLite.copy(tagList = tagList1.filter { it.objectIdList.contains(noteObjectLite.id) }.map { it.toLite() })
				}
				.groupBy {
					when (sortOn1) {
						SortOn.Title -> it.title?.lowercase() ?: "."
						SortOn.Timestamp -> timestampToCalendarDay(it.modifiedTimestamp).timeStampToPrettyDay()
						SortOn.Modified -> timestampToCalendarDay(it.modifiedTimestamp).timeStampToPrettyDay()
						else -> timestampToCalendarDay(it.userTimestamp).timeStampToPrettyDay()
					}
				}.mapValues { (_, noteList) ->
					when (sortOn1) {
						SortOn.Title -> noteList.sortedBy { it.title?.lowercase() ?: "." }
						SortOn.Timestamp -> noteList.sortedBy { it.userTimestamp }
						SortOn.Modified -> noteList.sortedBy { it.modifiedTimestamp }
						else -> noteList.sortedBy { it.userTimestamp }
					}.let { if (sortBy1 == SortBy.Descending) it.reversed() else it }
				}
		}.collectLatest { mappedNoteList1 ->
			this@NoteScreenViewModel._mappedNoteList.tryEmit(mappedNoteList1)
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
	}
}
