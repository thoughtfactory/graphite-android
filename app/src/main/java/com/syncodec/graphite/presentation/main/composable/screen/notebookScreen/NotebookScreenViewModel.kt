package com.syncodec.graphite.presentation.main.composable.screen.notebookScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.ContentStatus
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class NotebookScreenViewModel(private val repository : Repository) : ViewModel() {

	val repositoryState = repository.repositoryState

	private var notebookObserverCoroutine : CoroutineScope? = null
	private var notebookOrderObserverCoroutine : CoroutineScope? = null

	private val _notebookUnorderedList : MutableStateFlow<List<ChapterObject>> = MutableStateFlow(listOf())
	private val _notebookOrderList : MutableStateFlow<List<RealmUUID>> = MutableStateFlow(listOf())
	private val _notebookListStatus : MutableStateFlow<ContentStatus<List<ChapterObject>>> = MutableStateFlow(ContentStatus.Init)
	val notebookListStatus : StateFlow<ContentStatus<List<ChapterObject>>> = _notebookListStatus

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				when (it) {
					Repository.Companion.RepositoryState.Success -> observeNotebooks()
					else -> null
				}
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(
				_notebookUnorderedList,
				_notebookOrderList
			) { unorderedList, orderList ->
				unorderedList.sortedBy { orderList.indexOf(it.id) }
			}.collectLatest {
				if (it.isEmpty()) _notebookListStatus.tryEmit(ContentStatus.LoadedEmpty) else _notebookListStatus.tryEmit(ContentStatus.Loaded(it))
			}
		}
	}

	private fun observeNotebooks() {
		viewModelScope.launch(Dispatchers.Default) {
			_notebookListStatus.tryEmit(ContentStatus.Loading)
			notebookObserverCoroutine?.cancel()
			notebookObserverCoroutine = this
			_notebookUnorderedList.tryEmit(listOf())
			repository.getChapterWithParentIdAsFlow(parentId = null).cancellable().collect { notebookList ->
				_notebookUnorderedList.tryEmit(notebookList.list)
				if (notebookList.list.isEmpty()) _notebookListStatus.tryEmit(ContentStatus.LoadedEmpty) else _notebookListStatus.tryEmit(
					ContentStatus.Loaded(
						notebookList.list
					)
				)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			notebookOrderObserverCoroutine?.cancel()
			notebookOrderObserverCoroutine = this
			_notebookOrderList.tryEmit(listOf())
			repository.getBaseObjectAsFlow().cancellable().collectLatest {
				_notebookOrderList.tryEmit(it?.notebookIdOrderList ?: listOf())
			}
		}
	}

	fun onReorderBucketList(idOrderList : List<RealmUUID>) {
		repository.reorderNotebookList(idOrderList)
	}

	fun refresh() = observeNotebooks()

	fun delete(idList : List<RealmUUID>) {
		repository.deleteSuspended(idList)
	}
}
