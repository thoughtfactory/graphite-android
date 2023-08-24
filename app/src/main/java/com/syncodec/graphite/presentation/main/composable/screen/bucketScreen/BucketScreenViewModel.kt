package com.syncodec.graphite.presentation.main.composable.screen.bucketScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
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
class BucketScreenViewModel(private val repository : Repository) : ViewModel() {

	val repositoryState = repository.repositoryState

	private var bucketObserverCoroutine : CoroutineScope? = null
	private var bucketOrderObserverCoroutine : CoroutineScope? = null

	private val _bucketUnorderedList : MutableStateFlow<List<BucketObject>> = MutableStateFlow(listOf())
	private val _bucketOrderList : MutableStateFlow<List<RealmUUID>> = MutableStateFlow(listOf())
	private val _bucketListStatus : MutableStateFlow<ContentStatus<List<BucketObject>>> = MutableStateFlow(ContentStatus.Init)
	val bucketListStatus : StateFlow<ContentStatus<List<BucketObject>>> = _bucketListStatus

	private val _bucketItemCount : MutableStateFlow<Map<RealmUUID?, Int>> = MutableStateFlow(mapOf())
	val bucketItemCount : StateFlow<Map<RealmUUID?, Int>> = _bucketItemCount

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				when (it) {
					Repository.Companion.RepositoryState.Success -> observeBuckets()
					else -> null
				}
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(
				_bucketUnorderedList,
				_bucketOrderList
			) { unorderedList, orderList ->
				unorderedList.sortedBy { orderList.indexOf(it.id) }
			}.collectLatest {
				if (it.isEmpty()) _bucketListStatus.tryEmit(ContentStatus.LoadedEmpty) else _bucketListStatus.tryEmit(ContentStatus.Loaded(it))
			}
		}
	}

	private fun observeBuckets() {
		viewModelScope.launch(Dispatchers.Default) {
			_bucketListStatus.tryEmit(ContentStatus.Loading)
			bucketObserverCoroutine?.cancel()
			bucketObserverCoroutine = this
			_bucketUnorderedList.tryEmit(listOf())
			repository.getAllBucketAsFlow().cancellable().collect { bucketObjectList ->
				_bucketUnorderedList.tryEmit(bucketObjectList)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			bucketOrderObserverCoroutine?.cancel()
			bucketOrderObserverCoroutine = this
			_bucketOrderList.tryEmit(listOf())
			repository.getBaseObjectAsFlow().cancellable().collectLatest {
				_bucketOrderList.tryEmit(it?.bucketIdOrderList ?: listOf())
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			repository.getAllBucketItemAsFlow().cancellable().collectLatest {
				it.groupingBy { it.parentId }.eachCount().let { _bucketItemCount.tryEmit(it) }
			}
		}
	}

	fun onReorderBucketList(idOrderList : List<RealmUUID>) {
		repository.reorderBucketListSuspended(idOrderList)
	}

	fun refresh() = observeBuckets()

	fun putBucket(
		title: String?,
		description: String?,
		bucketType: BucketType,
		callback: suspend (String) -> Unit,
	) {
		val bucketObject = BucketObject().apply {
			this.title = title
			this.description = description
			this.bucketType = bucketType.name
		}

		if (BaseApplication.isPro.value || bucketType == BucketType.TODO) repository.putBucketSuspended(bucketObject)
		else viewModelScope.launch(Dispatchers.Default) {
			repository.getAllBucket().let {
				if (it.count { it.bucketType == bucketType.name } < 1) repository.putBucketSuspended(bucketObject) else callback("Join Graphite Pro to create more ${bucketType.name} buckets")
			}
		}
	}

	fun delete(idList : List<RealmUUID>) {
		repository.deleteSuspended(idList)
	}
}
