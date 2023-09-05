package com.syncodec.graphite.presentation.main.composable.screen.bucketScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketObjectLite
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
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
class BucketScreenViewModel(
	repositoryStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>,
	private val dataStoreInstance: DataStoreInstance,
	private val isAuthenticated : StateFlow<Boolean>,
) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _bucketList: MutableStateFlow<List<BucketObjectLite>> = MutableStateFlow(listOf())
	private val _bucketSizeMap: MutableStateFlow<Map<RealmUUID?, Int>> = MutableStateFlow(mapOf())
	private val _mergedBucketList: MutableStateFlow<List<BucketObjectLite>> = MutableStateFlow(listOf())
	private val _orderedIdList: MutableStateFlow<List<RealmUUID>> = MutableStateFlow(listOf())
	private val _orderedBucketList: MutableStateFlow<List<BucketObjectLite>?> = MutableStateFlow(null)
	val orderedBucketList: StateFlow<List<BucketObjectLite>?> = _orderedBucketList

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryStateFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}
		viewModelScope.launch(Dispatchers.Default) {
			_repository.collectLatest { repository1 ->
				launch { observeBucket(repository = repository1) }
				launch { observeBucketSize(repository = repository1) }
				launch { mergeBucket() }
				launch { observeBucketOrder(repository = repository1) }
				launch { sortAndFilterBucket() }
			}
		}
	}

	private suspend fun observeBucket(repository: Repository?) {
		repository?.getAllBucketLiteAsFlow()?.cancellable()?.collectLatest { bucketList1 ->
			this@BucketScreenViewModel._bucketList.tryEmit(bucketList1)
		}
	}

	private suspend fun observeBucketSize(repository: Repository?) {
		repository?.getAllBucketSizeAsFlow()?.cancellable()?.collectLatest { bucketSizeMap1 ->
			this@BucketScreenViewModel._bucketSizeMap.tryEmit(bucketSizeMap1)
		}
	}

	private suspend fun mergeBucket() {
		combine(_bucketList, _bucketSizeMap) { bucketList1, bucketSizeMap1 ->
			bucketList1.map { it.copy(bucketItemCount = bucketSizeMap1[it.id] ?: 0) }
		}.cancellable().collectLatest { mergedBucketList1 ->
			this@BucketScreenViewModel._mergedBucketList.tryEmit(mergedBucketList1)
		}
	}

	private suspend fun observeBucketOrder(repository: Repository?) {
		repository?.getBaseObjectAsFlow()?.cancellable()?.collectLatest { baseObject ->
			this@BucketScreenViewModel._orderedIdList.tryEmit(baseObject?.bucketIdOrderList ?: listOf())
		}
	}

	private suspend fun sortAndFilterBucket() {
		combine(
			_mergedBucketList,
			_orderedIdList,
			isAuthenticated,
			dataStoreInstance.getSortBy,
			dataStoreInstance.getSortOn
		) { args ->
			val mergedBucketList1 = args[0] as List<BucketObjectLite>
			val orderedIdList1 = args[1] as List<RealmUUID>
			val isAuthenticated1 = args[2] as Boolean
			val sortBy1 = args[3] as SortBy
			val sortOn1 = args[4] as SortOn

			val lockFilteredBucketList = if (!isAuthenticated1) mergedBucketList1.filter { !it.isLocked } else mergedBucketList1

			when (sortBy1) {
				SortBy.Ascending -> when (sortOn1) {
					SortOn.Title -> lockFilteredBucketList.sortedBy { it.title?.lowercase() ?: "." }
					SortOn.Timestamp -> lockFilteredBucketList.sortedBy { timestampToCalendarDay(it.createdTimestamp) }
					SortOn.Modified -> lockFilteredBucketList.sortedBy { timestampToCalendarDay(it.modifiedTimestamp) }
					SortOn.Custom -> lockFilteredBucketList.sortedBy { orderedIdList1.indexOf(it.id) }
					else -> lockFilteredBucketList.sortedBy { timestampToCalendarDay(it.createdTimestamp) }
				}

				SortBy.Descending -> when (sortOn1) {
					SortOn.Title -> lockFilteredBucketList.sortedByDescending { it.title?.lowercase() ?: "." }
					SortOn.Timestamp -> lockFilteredBucketList.sortedByDescending { timestampToCalendarDay(it.createdTimestamp) }
					SortOn.Modified -> lockFilteredBucketList.sortedByDescending { timestampToCalendarDay(it.modifiedTimestamp) }
					SortOn.Custom -> lockFilteredBucketList.sortedByDescending { orderedIdList1.indexOf(it.id) }
					else -> lockFilteredBucketList.sortedByDescending { timestampToCalendarDay(it.createdTimestamp) }
				}
			}
		}.collectLatest { orderedBucketList1 ->
			this@BucketScreenViewModel._orderedBucketList.tryEmit(orderedBucketList1)
		}
	}

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

		if (BaseApplication.isPro.value || bucketType == BucketType.TODO) _repository.value?.putBucketSuspended(bucketObject)
		else viewModelScope.launch(Dispatchers.Default) {
			_repository.value?.getAllBucket()?.let {
				if (it.count { it.bucketType == bucketType.name } < 1) _repository.value?.putBucketSuspended(bucketObject) else callback("Join Graphite Pro to create more ${bucketType.name} buckets")
			}
		}
	}

	fun onReorderBucketList(idList : List<RealmUUID>) {
		viewModelScope.launch(Dispatchers.Default) { _repository.value?.reorderBucketList(idList) }
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
