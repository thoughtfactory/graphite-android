package com.syncodec.graphite.presentation.bucket.composable.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.OpenGraphApi
import com.syncodec.graphite.di.network.OpenGraphResponse
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.ContentStatus
import com.syncodec.graphite.utils.encodeBase64
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class BucketScreenCommonViewModel(repositoryStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>) : ViewModel() {

	val repositoryState: StateFlow<Repository.Companion.RepositoryStatus> = repositoryStateFlow
	private val repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _bucketObject: MutableStateFlow<BucketObject?> = MutableStateFlow(null)
	val bucketObject: StateFlow<BucketObject?> = _bucketObject

	private val _id: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	val id: StateFlow<RealmUUID?> = _id

	private val _bucketItemList: MutableStateFlow<List<BucketItemObject>> = MutableStateFlow(listOf())
	private val _orderedBucketItemList: MutableStateFlow<List<BucketItemObject>> = MutableStateFlow(listOf())
	val orderedBucketItemList: StateFlow<List<BucketItemObject>> = _orderedBucketItemList

	private val _searchQueryList: MutableStateFlow<Set<String>> = MutableStateFlow(setOf())
	val searchQueryList: StateFlow<Set<String>> = _searchQueryList

	private val _previewBucketItemObject: MutableStateFlow<BucketItemObject?> = MutableStateFlow(null)
	val previewBucketItemObject: StateFlow<BucketItemObject?> = _previewBucketItemObject

	private var previewBucketItemObjectCoroutine: CoroutineScope? = null

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryStateFlow.collect { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) repository.tryEmit(repositoryStatus.repository)
			}
		}
		viewModelScope.launch(Dispatchers.Default) {
			combine(repository, id) { repository1, id1 -> Pair(repository1, id1) }.collect { (repository1, id1) ->
				id1?.let {
					this.launch { repository1?.getBucketAsFlow(id = it)?.collect { _bucketObject.tryEmit(it) } }
					this.launch { repository1?.getBucketItemWithParentIdAsFlow(parentId = it)?.collect { _bucketItemList.tryEmit(it) } }
				}
			}
		}
		viewModelScope.launch(Dispatchers.Default) {
			combine(_bucketObject, _bucketItemList, _searchQueryList) { bucketObject1, bucketItemList1, filterQueryList1 ->
				bucketItemList1.sortedBy { bucketObject1?.bucketItemOrderList?.indexOf(it.id) }.let { sortedBucketItemList ->
					if (filterQueryList1.isEmpty()) sortedBucketItemList
					else sortedBucketItemList.filter { bucketItemObject ->
						when (bucketItemObject.bucketType) {
							BucketType.TODO.name -> {
								val title = bucketItemObject.title
								filterQueryList1.any { title?.contains(other = it, ignoreCase = true) ?: false }
							}

							BucketType.BOOK.name -> false
							BucketType.SHOW.name -> false
							BucketType.LINK.name -> false
							else -> false
						}
					}
				}
			}.collect { _orderedBucketItemList.tryEmit(it) }
		}
	}

	fun initBucket(realmUUID: RealmUUID) {
		_id.tryEmit(realmUUID)
	}

	fun onReorderBucketItem(idOrderList: List<RealmUUID>) {
		id.value?.let { repository.value?.reorderBucketItemListSuspended(parentId = it, idOrderList = idOrderList) }
	}

	fun selectBucketItemObject(id: RealmUUID?) {
		viewModelScope.launch(Dispatchers.Default) {
			previewBucketItemObjectCoroutine?.cancel()
			previewBucketItemObjectCoroutine = this
			_bucketItemList.collect { bucketItemList1 -> _previewBucketItemObject.tryEmit(bucketItemList1.first { it.id == id }) }
		}
	}

	fun putTodo(
		realmUUID: RealmUUID?,
		title: String,
		state: BucketItemState
	) {
		BucketItemObject().apply {
			realmUUID?.let { this.id = it }
			this.bucketType = BucketType.TODO.name
			this.title = title
			this.state = state.name
			this.parentId = this@BucketScreenCommonViewModel.bucketObject.value?.id
			this.key = title

			repository.value?.putBucketItemSuspended(this)
		}
	}

	fun putLink(
		url: String,
		state: BucketItemState,
	) {
		viewModelScope.launch(Dispatchers.Default) {
			BucketItemObject().apply {
				this.bucketType = BucketType.LINK.name
				this.state = state.name
				this.parentId = this@BucketScreenCommonViewModel.bucketObject.value?.id
				this.key = url
				OpenGraphApi.getData(url = url) { openGraphResponse ->
					if (openGraphResponse is OpenGraphResponse.Success) {
						this.title = openGraphResponse.openGraphResult.title
						this.thumbnail = openGraphResponse.bitmap?.encodeBase64()
						this.putOpenGraphResult(openGraphResponse.openGraphResult.copy(url = url))

					} else {
						this.title = url
					}
					repository.value?.putBucketItemSuspended(this.clone())
				}
			}
		}
	}

	fun toggleFavourite(bucketItemObject: BucketItemObject) {
		bucketItemObject.clone().apply {
			this.isFavourite = !this.isFavourite
			repository.value?.putBucketItemSuspended(bucketItemObject = this)
		}
	}

	fun toggleLock(bucketItemObject: BucketItemObject) {
		bucketItemObject.clone().apply {
			this.isLocked = !this.isLocked
			repository.value?.putBucketItemSuspended(bucketItemObject = this)
		}
	}

	fun toggleFavourite(idList: Set<RealmUUID>) {
		viewModelScope.launch(Dispatchers.Default) {
			val areAllFavourite = orderedBucketItemList.value.filter { it.id in idList }.all { it.isFavourite }
			repository.value?.let { repo ->
				idList.forEach {
					repo.getBucketItemFromId(it)?.clone()?.apply {
						this.isFavourite = !areAllFavourite
						repo.putBucketItemSuspended(bucketItemObject = this)
					}
				}
			}
		}
	}

	fun toggleLock(idList: Set<RealmUUID>) {
		viewModelScope.launch(Dispatchers.Default) {
			val areAllLocked = orderedBucketItemList.value.filter { it.id in idList }.all { it.isLocked }
			repository.value?.let { repo ->
				idList.forEach {
					repo.getBucketItemFromId(it)?.clone()?.apply {
						this.isLocked = !areAllLocked
						repo.putBucketItemSuspended(bucketItemObject = this)
					}
				}
			}
		}
	}

	fun toggleBucketItemState(bucketItemObject: BucketItemObject) {
		viewModelScope.launch(Dispatchers.Default) {
			bucketItemObject.clone().apply {
				this.state = when (this.state) {
					BucketItemState.ALPHA.name -> BucketItemState.BETA.name
					BucketItemState.BETA.name -> BucketItemState.GAMMA.name
					BucketItemState.GAMMA.name -> BucketItemState.ALPHA.name
					else -> BucketItemState.ALPHA.name
				}
				repository.value?.putBucketItemSuspended(bucketItemObject = this)
			}
		}
	}

	fun updateBucketItemState(bucketItemObject: BucketItemObject, stateInt: Int) {
		viewModelScope.launch(Dispatchers.Default) {
			bucketItemObject.clone().apply {
				this.setState(stateInt)
				repository.value?.putBucketItemSuspended(bucketItemObject = this)
			}
		}
	}

	fun updateBucketItemTitle(bucketItemObject: BucketItemObject, title: String) {
		viewModelScope.launch(Dispatchers.Default) {
			bucketItemObject.clone().apply {
				this.title = title
				repository.value?.putBucketItemSuspended(bucketItemObject = this)
			}
		}
	}

	fun filterBySearchAdd(query: String) {
		_searchQueryList.value.toMutableSet().apply {
			add(query)
			_searchQueryList.tryEmit(this.toSet())
		}
	}

	fun filterBySearchRemove(query: String) {
		_searchQueryList.value.toMutableSet().apply {
			remove(query)
			_searchQueryList.tryEmit(this.toSet())
		}
	}

	fun clearSearchFilter() {
		_searchQueryList.tryEmit(setOf())
	}

}
