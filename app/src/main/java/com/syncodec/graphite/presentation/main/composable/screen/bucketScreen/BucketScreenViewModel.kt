package com.syncodec.graphite.presentation.main.composable.screen.bucketScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class BucketScreenViewModel(
	repositoryStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>,
	dataStoreInstance: DataStoreInstance
) : ViewModel() {

	private val repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _unorderedObjectList: MutableStateFlow<List<BucketObject>> = MutableStateFlow(listOf())
	private val _orderedIdList: MutableStateFlow<List<RealmUUID>> = MutableStateFlow(listOf())

	private val _objectList: MutableStateFlow<List<BucketObject>> = MutableStateFlow(listOf())
	val bucketList: StateFlow<List<BucketObject>> = _objectList

	private val _bucketItemCount: MutableStateFlow<Map<RealmUUID?, Int>> = MutableStateFlow(mapOf())
	val bucketItemCount: StateFlow<Map<RealmUUID?, Int>> = _bucketItemCount

	private var bucketObserverJob: Job? = null
	private var bucketOrderObserverJob: Job? = null
	private var bucketSizeObserverJob: Job? = null

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryStateFlow.collect { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) repository.tryEmit(repositoryStatus.repository)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			repository.collect {
				if (it != null) {
					observeBuckets()
					observeBucketOrder()
				}
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

	private fun observeBuckets() {
		bucketObserverJob?.cancel()
		bucketObserverJob = viewModelScope.launch(Dispatchers.Default) {
			repository.value?.getAllBucketAsFlow()?.cancellable()?.collect { bucketObjectList ->
				_unorderedObjectList.tryEmit(bucketObjectList)
			}
		}

		bucketSizeObserverJob?.cancel()
		bucketSizeObserverJob = viewModelScope.launch(Dispatchers.Default) {
			repository.value?.getAllBucketItemAsFlow()?.cancellable()?.collectLatest {
				it.groupingBy { it.parentId }.eachCount().let { _bucketItemCount.tryEmit(it) }
			}
		}
	}

	private fun observeBucketOrder() {
		bucketOrderObserverJob?.cancel()
		bucketOrderObserverJob = viewModelScope.launch(Dispatchers.Default) {
			repository.value?.getBaseObjectAsFlow()?.cancellable()?.collectLatest {
				_orderedIdList.tryEmit(it?.bucketIdOrderList ?: listOf())
			}
		}
	}

	fun onReorderBucketList(idOrderList: List<RealmUUID>) {
		repository.value?.reorderBucketListSuspended(idOrderList)
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

		if (BaseApplication.isPro.value || bucketType == BucketType.TODO) repository.value?.putBucketSuspended(bucketObject)
		else viewModelScope.launch(Dispatchers.Default) {
			repository.value?.getAllBucket()?.let {
				if (it.count { it.bucketType == bucketType.name } < 1) repository.value?.putBucketSuspended(bucketObject) else callback("Join Graphite Pro to create more ${bucketType.name} buckets")
			}
		}
	}

	fun delete(idList: List<RealmUUID>) {
		repository.value?.deleteSuspended(idList)
	}
}
