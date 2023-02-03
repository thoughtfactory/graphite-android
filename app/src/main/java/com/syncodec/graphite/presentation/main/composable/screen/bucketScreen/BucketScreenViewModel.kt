package com.syncodec.graphite.presentation.main.composable.screen.bucketScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.BucketObject
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
class BucketScreenViewModel(private val repository : KoinRepository) : ViewModel() {

	val repositoryState = repository.repositoryState

	private var bucketObserverCoroutine : CoroutineScope? = null
	private var bucketOrderObserverCoroutine : CoroutineScope? = null

	private val bucketUnorderedList : MutableStateFlow<List<BucketObject>> = MutableStateFlow(listOf())
	private val bucketOrderList : MutableStateFlow<List<RealmUUID>> = MutableStateFlow(listOf())
	val bucketList : MutableStateFlow<List<BucketObject>> = MutableStateFlow(listOf())

	val isBucketRefreshing : MutableStateFlow<Boolean> = MutableStateFlow(false)
	val contentStatus : MutableStateFlow<ContentStatus> = MutableStateFlow(ContentStatus.Init)

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				when (it) {
					RepositoryState.SUCCESS -> observeBuckets()
					else -> null
				}
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			bucketUnorderedList.combine(bucketOrderList) { unorderedList, orderList ->
				unorderedList.sortedBy { orderList.indexOf(it.id) }
			}.collectLatest { bucketList.tryEmit(it) }
		}
	}

	private fun observeBuckets() {
		viewModelScope.launch(Dispatchers.Default) {
			isBucketRefreshing.tryEmit(true)
			bucketObserverCoroutine?.cancel()
			bucketObserverCoroutine = this
			repository.getAllBucketAsFlow().cancellable().collect { bucketObjectList ->
				bucketUnorderedList.tryEmit(bucketObjectList)
				if (bucketObjectList.isEmpty()) contentStatus.tryEmit(ContentStatus.LoadedEmpty) else contentStatus.tryEmit(ContentStatus.Loaded)
				isBucketRefreshing.tryEmit(false)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			bucketOrderObserverCoroutine?.cancel()
			bucketOrderObserverCoroutine = this
			repository.getBaseObjectAsFlow().cancellable().collectLatest {
				bucketOrderList.tryEmit(it?.bucketIdOrderList ?: listOf())
			}
		}
	}

	fun onReorderBucketList(newBucketListOrder : List<BucketObject>) {
		viewModelScope.launch(Dispatchers.Default) {
			repository.reorderBucketList(newBucketListOrder.map { it.id }) { _, _ -> }
		}
	}

	fun refresh() = observeBuckets()

	fun delete(idList : List<RealmUUID>) = viewModelScope.launch(Dispatchers.Default) { repository.delete(idList) }
}
