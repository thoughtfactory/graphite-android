package com.syncodec.graphite.presentation.main.composable.screen.notebookScreen

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.encodeBase64
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
class NotebookScreenViewModel(
	repositoryStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>,
	private val dataStoreInstance: DataStoreInstance,
	private val isAuthenticated: StateFlow<Boolean>,
) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _defaultChapterId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)

	private val _unorderedNotebookList: MutableStateFlow<List<ChapterObject>> = MutableStateFlow(listOf())
	private val _orderedIdList: MutableStateFlow<List<RealmUUID>> = MutableStateFlow(listOf())

	private val _orderedNotebookList: MutableStateFlow<List<ChapterObject>> = MutableStateFlow(listOf())
	val orderedNotebookList: StateFlow<List<ChapterObject>> = _orderedNotebookList

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryStateFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}
		viewModelScope.launch(Dispatchers.Default) {
			_repository.collectLatest { repository1 ->
				launch { observeDefaultChapter(repository = repository1) }
				launch { observeNotebook(repository = repository1) }
				launch { observeNotebookOrder(repository = repository1) }
				launch { sortAndFilterNotebook() }
			}
		}
	}

	private suspend fun observeDefaultChapter(repository: Repository?) {
		repository?.getDefaultChapterIdAsFlow()?.collectLatest { defaultChapterId1 ->
			this@NotebookScreenViewModel._defaultChapterId.tryEmit(defaultChapterId1)
		}
	}

	private suspend fun observeNotebook(repository: Repository?) {
		repository?.getChapterWithParentIdAsFlow(parentId = null)?.cancellable()?.collectLatest {
			this@NotebookScreenViewModel._unorderedNotebookList.tryEmit(it.list)
		}
	}

	private suspend fun observeNotebookOrder(repository: Repository?) {
		repository?.getBaseObjectAsFlow()?.cancellable()?.collectLatest {
			this@NotebookScreenViewModel._orderedIdList.tryEmit(it?.notebookIdOrderList ?: listOf())
		}
	}

	private suspend fun sortAndFilterNotebook() {
		viewModelScope.launch(Dispatchers.Default) {
			combine(
				_unorderedNotebookList,
				_orderedIdList,
				isAuthenticated,
				dataStoreInstance.getSortBy,
				dataStoreInstance.getSortOn,
			) { args ->
				val unorderedNotebookList1 = args[0] as List<ChapterObject>
				val orderedIdList1 = args[1] as List<RealmUUID>
				val isAuthenticated1 = args[2] as Boolean
				val sortBy1 = args[3] as SortBy
				val sortOn1 = args[4] as SortOn

				val lockFilteredNotebookList = if (!isAuthenticated1) unorderedNotebookList1.filter { !it.isLocked } else unorderedNotebookList1

				when (sortBy1) {
					SortBy.Ascending -> when (sortOn1) {
						SortOn.Title -> lockFilteredNotebookList.sortedBy { it.title?.lowercase() ?: "." }
						SortOn.Timestamp -> lockFilteredNotebookList.sortedBy { timestampToCalendarDay(it.createdTimestamp) }
						SortOn.Modified -> lockFilteredNotebookList.sortedBy { timestampToCalendarDay(it.modifiedTimestamp) }
						SortOn.Custom -> lockFilteredNotebookList.sortedBy { orderedIdList1.indexOf(it.id) }
						else -> lockFilteredNotebookList.sortedBy { it.title?.lowercase() ?: "." }
					}

					SortBy.Descending -> when (sortOn1) {
						SortOn.Title -> lockFilteredNotebookList.sortedByDescending { it.title?.lowercase() ?: "." }
						SortOn.Timestamp -> lockFilteredNotebookList.sortedByDescending { timestampToCalendarDay(it.createdTimestamp) }
						SortOn.Modified -> lockFilteredNotebookList.sortedByDescending { timestampToCalendarDay(it.modifiedTimestamp) }
						SortOn.Custom -> lockFilteredNotebookList.sortedByDescending { orderedIdList1.indexOf(it.id) }
						else -> lockFilteredNotebookList.sortedByDescending { it.title?.lowercase() ?: "." }
					}
				}
			}.collectLatest {
				this@NotebookScreenViewModel._orderedNotebookList.tryEmit(it)
			}
		}
	}

	fun putNotebook(
		title: String,
		description: String,
		color: Color?,
		bitmap: Bitmap?,
	) {
		val chapterObject = ChapterObject().apply {
			this.title = title
			this.description = description
			this.color = color?.toArgb()
			this.thumbnail = bitmap?.encodeBase64()
		}
		viewModelScope.launch(Dispatchers.Default) {
			_repository.value?.getChapterWithParentId(parentChapterId = null)?.let {
				if (it.size < 4) _repository.value?.putChapterSuspended(chapterObject)
			}
		}
	}

	fun onReorderNotebookList(idList: List<RealmUUID>) {
		viewModelScope.launch(Dispatchers.Default) { _repository.value?.reorderNotebookList(idList) }
	}

	fun onClickMultiFavourite(idList: Set<RealmUUID>, isAllFavourite: Boolean) {
		viewModelScope.launch(Dispatchers.Default) {
			idList.forEach { chapterId ->
				_repository.value?.getChapterFromId(id = chapterId)?.clone()?.apply {
					this.isFavourite = !isAllFavourite
					_repository.value?.putChapter(this)
				}
			}
		}
	}

	fun onClickMultiLock(idList: Set<RealmUUID>, isAllLocked: Boolean) {
		viewModelScope.launch(Dispatchers.Default) {
			idList.filter { it != _defaultChapterId.value }.forEach { chapterId ->
				_repository.value?.getChapterFromId(id = chapterId)?.clone()?.apply {
					this.isLocked = !isAllLocked
					_repository.value?.putChapter(this)
				}
			}
		}
	}

	fun delete(idList: Set<RealmUUID>) {
		_repository.value?.deleteSuspended(idList.filter { it != _defaultChapterId.value })
	}
}
