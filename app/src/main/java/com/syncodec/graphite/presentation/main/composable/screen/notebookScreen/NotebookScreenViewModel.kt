package com.syncodec.graphite.presentation.main.composable.screen.notebookScreen

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.presentation.common.reorderable.ItemPosition
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.encodeBase64
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
	private val dataStoreInstance: DataStoreInstance
) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _notebookList : MutableStateFlow<List<ChapterObject>> = MutableStateFlow(listOf())
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
				launch { observeNotebook(repository = repository1) }
				launch { observeNotebookOrder(repository = repository1) }
				launch { mergeBucket() }
				launch { observeBucketOrder(repository = repository1) }
				launch { sortAndFilterBucket() }
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(
				_unorderedObjectList,
				_orderedIdList,
				dataStoreInstance.getSortBy,
				dataStoreInstance.getSortOn,
			) { unorderedObjectList, idOrderList, sortBy, sortOn ->
				when (sortOn) {
					SortOn.Title -> if (sortBy == SortBy.Ascending) unorderedObjectList.sortedBy { it.title } else unorderedObjectList.sortedByDescending { it.title }
					SortOn.Timestamp -> if (sortBy == SortBy.Ascending) unorderedObjectList.sortedBy { it.createdTimestamp } else unorderedObjectList.sortedByDescending { it.createdTimestamp }
					SortOn.Modified -> if (sortBy == SortBy.Ascending) unorderedObjectList.sortedBy { it.modifiedTimestamp } else unorderedObjectList.sortedByDescending { it.modifiedTimestamp }
					SortOn.Custom -> unorderedObjectList.sortedBy { idOrderList.indexOf(it.id) }
					else -> if (sortBy == SortBy.Ascending) unorderedObjectList.sortedBy { it.title } else unorderedObjectList.sortedByDescending { it.title }
				}
			}.collectLatest {
				_objectList.tryEmit(it)
			}
		}
	}

	private suspend fun observeNotebook(repository: Repository?) {
		repository?.getChapterWithParentIdAsFlow(parentId = null)?.cancellable()?.collectLatest {
			this@NotebookScreenViewModel._notebookList.tryEmit(it.list)
		}
	}

	private fun observeNotebookOrder(repository: Repository?) {
		notebookOrderObserverCoroutine?.cancel()
		notebookOrderObserverCoroutine = viewModelScope.launch(Dispatchers.Default) {
			repository.value?.getBaseObjectAsFlow()?.cancellable()?.collectLatest { _orderedIdList.tryEmit(it?.notebookIdOrderList ?: listOf()) }
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
			repository.value?.getChapterWithParentId(parentChapterId = null)?.let {
				if (it.size < 4) repository.value?.putChapterSuspended(chapterObject)
			}
		}
	}

	fun onTryReorderBucketList(from: ItemPosition, to: ItemPosition) {
		viewModelScope.launch(Dispatchers.Default) {
			this@NotebookScreenViewModel.notebookList.value.let {
				this@NotebookScreenViewModel._orderedIdList.value.toMutableList().apply {
					add(to.index, removeAt(from.index))
					this@NotebookScreenViewModel._objectList.tryEmit(it.sortedBy { this.indexOf(it.id) })
				}
			}
		}
	}

	fun onReorderBucketList(from: Int, to: Int) {
		viewModelScope.launch(Dispatchers.Default) {
			this@NotebookScreenViewModel.notebookList.value.let {
				this@NotebookScreenViewModel._orderedIdList.value.toMutableList().apply {
					add(to, removeAt(from))
					repository?.value?.reorderNotebookList(this)
				}
			}
		}
	}

	fun onReorderBucketList2(idOrderList: List<RealmUUID>) {
		viewModelScope.launch(Dispatchers.Default) { repository.value?.reorderNotebookList(idOrderList) }
	}

	fun onClickMultiFavourite(idList: Set<RealmUUID>, isAllFavourite: Boolean) {
		viewModelScope.launch(Dispatchers.Default) {
			idList.forEach { noteId ->
				_repository.value?.getBucketFromId(id = noteId)?.clone()?.apply {
					this.isFavourite = !isAllFavourite
					_repository.value?.putBucket(this)
				}
			}
		}
	}

	fun onClickMultiLock(idList: Set<RealmUUID>, isAllLocked: Boolean) {
		viewModelScope.launch(Dispatchers.Default) {
			idList.forEach { noteId ->
				_repository.value?.getBucketFromId(id = noteId)?.clone()?.apply {
					this.isLocked = !isAllLocked
					_repository.value?.putBucket(this)
				}
			}
		}
	}

	fun delete(idList: Set<RealmUUID>) {
		_repository.value?.deleteSuspended(idList)
	}
}
