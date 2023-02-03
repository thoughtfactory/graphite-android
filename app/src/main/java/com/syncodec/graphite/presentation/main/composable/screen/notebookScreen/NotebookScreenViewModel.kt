package com.syncodec.graphite.presentation.main.composable.screen.notebookScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.repository.KoinRepository
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.utils.ContentStatus
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class NotebookScreenViewModel(private val repository : KoinRepository) : ViewModel() {

	val repositoryState = repository.repositoryState

	private var notebookObserverCoroutine : CoroutineScope? = null
	private var notebookOrderObserverCoroutine : CoroutineScope? = null

	private val notebookUnorderedList : MutableStateFlow<List<ChapterObject>> = MutableStateFlow(listOf())
	private val notebookOrderList : MutableStateFlow<List<RealmUUID>> = MutableStateFlow(listOf())
	val notebookList : MutableStateFlow<List<ChapterObject>> = MutableStateFlow(listOf())

	val isNotebookRefreshing : MutableStateFlow<Boolean> = MutableStateFlow(false)
	val contentStatus : MutableStateFlow<ContentStatus> = MutableStateFlow(ContentStatus.Init)

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				when (it) {
					RepositoryState.SUCCESS -> observeNotebooks()
					else -> null
				}
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			notebookUnorderedList.combine(notebookOrderList) { unorderedList, orderList ->
				unorderedList.sortedBy { orderList.indexOf(it.id) }
			}.collectLatest { notebookList.tryEmit(it) }
		}
	}

	private fun observeNotebooks() {
		viewModelScope.launch(Dispatchers.Default) {
			isNotebookRefreshing.tryEmit(true)
			notebookObserverCoroutine?.cancel()
			notebookObserverCoroutine = this
			repository.getChapterWithParentIdAsFlow(parentId = null).cancellable().collect { notebookObjectList ->
				notebookUnorderedList.tryEmit(notebookObjectList.list)
				if (notebookObjectList.list.isEmpty()) contentStatus.tryEmit(ContentStatus.LoadedEmpty) else contentStatus.tryEmit(ContentStatus.Loaded)
				isNotebookRefreshing.tryEmit(false)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			notebookOrderObserverCoroutine?.cancel()
			notebookOrderObserverCoroutine = this
			repository.getBaseObjectAsFlow().cancellable().collectLatest {
				notebookOrderList.tryEmit(it?.notebookIdOrderList ?: listOf())
			}
		}
	}

	fun onReorderNotebookList(newNotebookListOrder : List<ChapterObject>) {
		viewModelScope.launch(Dispatchers.Default) {
			repository.reorderNotebookList(newNotebookListOrder.map { it.id }) { _, _ -> }
		}
	}

	fun refresh() = observeNotebooks()

	fun delete(idList : List<RealmUUID>) = viewModelScope.launch(Dispatchers.Default) { repository.delete(idList) }
}
