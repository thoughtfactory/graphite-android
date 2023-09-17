package com.syncodec.graphite.presentation.bucket.composable.screen

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.OpenGraphApi
import com.syncodec.graphite.di.network.OpenGraphResponse
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.encodeBase64
import com.syncodec.graphite.utils.shareUtil.ShareBucketItemUtil
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class BucketScreenCommonViewModel(repositoryStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _bucketObject: MutableStateFlow<BucketObject?> = MutableStateFlow(null)
	val bucketObject: StateFlow<BucketObject?> = _bucketObject

	private val _id: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	val id: StateFlow<RealmUUID?> = _id

	private val _bucketItemList: MutableStateFlow<List<BucketItemObject>> = MutableStateFlow(listOf())
	private val _orderedBucketItemList: MutableStateFlow<List<BucketItemObject>> = MutableStateFlow(listOf())
	val orderedBucketItemList: StateFlow<List<BucketItemObject>> = _orderedBucketItemList

	private val _searchQueryList: MutableStateFlow<Set<String>> = MutableStateFlow(setOf())
	val searchQueryList: StateFlow<Set<String>> = _searchQueryList

	private val _previewBucketItemObjectId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	private val _previewBucketItemObject: MutableStateFlow<BucketItemObject?> = MutableStateFlow(null)
	val previewBucketItemObject: StateFlow<BucketItemObject?> = _previewBucketItemObject

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryStateFlow.collect { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}
		viewModelScope.launch(Dispatchers.Default) {
			combine(_repository, id) { repository1, id1 -> Pair(repository1, id1) }.collect { (repository1, id1) ->
				id1?.let { id2 ->
					repository1?.let { repository2 ->
						launch { observeBucket(repository = repository2, id = id2) }
						launch { observeBucketItems(repository = repository2, id = id2) }
						launch { sortAndFilter() }
						launch { observePreviewBucketItem() }
					}
				}
			}
		}
	}

	private suspend fun observeBucket(repository: Repository, id: RealmUUID) {
		repository.getBucketAsFlow(id = id).collectLatest { bucketObject1 ->
			this@BucketScreenCommonViewModel._bucketObject.tryEmit(bucketObject1)
		}
	}

	private suspend fun observeBucketItems(repository: Repository, id: RealmUUID) {
		repository.getBucketItemWithParentIdAsFlow(parentId = id).collectLatest {
			this@BucketScreenCommonViewModel._bucketItemList.tryEmit(it)
		}
	}

	private suspend fun sortAndFilter() {
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

	private suspend fun observePreviewBucketItem() {
		combine(_previewBucketItemObjectId, _bucketItemList) { previewBucketItemObjectId1, bucketItemList1 ->
			bucketItemList1.firstOrNull { it.id == previewBucketItemObjectId1 }
		}.collectLatest {
			this@BucketScreenCommonViewModel._previewBucketItemObject.tryEmit(it)
		}
	}

	fun initBucket(realmUUID: RealmUUID) {
		_id.tryEmit(realmUUID)
	}

	fun onReorderBucketItem(idOrderList: List<RealmUUID>) {
		id.value?.let { _repository.value?.reorderBucketItemList(parentId = it, idOrderList = idOrderList) }
	}

	fun selectBucketItemObject(id: RealmUUID?) {
		_previewBucketItemObjectId.tryEmit(id)
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

			_repository.value?.putBucketItemSuspended(this)
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
					_repository.value?.putBucketItemSuspended(this.clone())
				}
			}
		}
	}

	fun toggleFavourite(id: RealmUUID?) {
		_repository.value?.setObjectFromIdSuspended<BucketItemObject>(id = id) {
			this.updateModifyTimestamp()
			this.isFavourite = this.isFavourite.not()
		}
	}

	fun toggleLock(id: RealmUUID?) {
		_repository.value?.setObjectFromIdSuspended<BucketItemObject>(id = id) {
			this.updateModifyTimestamp()
			this.isLocked = this.isLocked.not()
		}
	}

	fun toggleFavourite(idList: Set<RealmUUID>) {
		val areAllFavourite = orderedBucketItemList.value.filter { it.id in idList }.all { it.isFavourite }
		_repository.value?.setMultiObjectFromIdSuspended<BucketItemObject>(idList = idList) {
			this.updateModifyTimestamp()
			this.isFavourite = !areAllFavourite
		}
	}

	fun toggleLock(idList: Set<RealmUUID>) {
		val areAllLocked = orderedBucketItemList.value.filter { it.id in idList }.all { it.isLocked }
		_repository.value?.setMultiObjectFromIdSuspended<BucketItemObject>(idList = idList) {
			this.updateModifyTimestamp()
			this.isLocked = !areAllLocked
		}
	}

	fun toggleBucketItemState(id: RealmUUID?) {
		_repository.value?.setObjectFromIdSuspended<BucketItemObject>(id = id) {
			this.updateModifyTimestamp()
			this.state = when (this.state) {
				BucketItemState.ALPHA.name -> BucketItemState.BETA.name
				BucketItemState.BETA.name -> BucketItemState.GAMMA.name
				BucketItemState.GAMMA.name -> BucketItemState.ALPHA.name
				else -> BucketItemState.ALPHA.name
			}
		}
	}

	fun updateBucketItemState(id: RealmUUID?, stateInt: Int) {
		_repository.value?.setObjectFromIdSuspended<BucketItemObject>(id = id) {
			this.updateModifyTimestamp()
			this.setState(stateInt)
		}
	}

	fun updateBucketItemState(idList: Set<RealmUUID>, stateInt: Int) {
		_repository.value?.setMultiObjectFromIdSuspended<BucketItemObject>(idList = idList) {
			this.updateModifyTimestamp()
			this.setState(stateInt)
		}
	}

	fun updateBucketItemTitle(id: RealmUUID?, title: String) {
		_repository.value?.setObjectFromIdSuspended<BucketItemObject>(id = id) {
			this.updateModifyTimestamp()
			this.title = title
		}
	}

	fun moveBucketItem(idList: Set<RealmUUID>, newParentId: RealmUUID) {
		_repository.value?.setMultiObjectFromIdSuspended<BucketItemObject>(idList = idList) {
			this.parentId = newParentId
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

	fun deleteMultiple(idList: Set<RealmUUID>) {
		viewModelScope.launch(Dispatchers.Default) {
			_repository.value?.deleteSuspended(idList = idList)
		}
	}

	fun shareBucketItems(idList: Set<RealmUUID>, callback : suspend (String) -> Unit){
		viewModelScope.launch(Dispatchers.Default) {
			val filteredBucketItemList = this@BucketScreenCommonViewModel._bucketItemList.value.filter { it.id in idList }

			val shareText = when(bucketObject.value?.bucketType) {
				BucketType.TODO.name -> ShareBucketItemUtil.getTodoItemShareText(bucketItemList = filteredBucketItemList)
				BucketType.BOOK.name -> ShareBucketItemUtil.getBookItemShareText(bucketItemList = filteredBucketItemList)
				BucketType.SHOW.name -> ShareBucketItemUtil.getShowItemShareText(bucketItemList = filteredBucketItemList)
				BucketType.LINK.name -> ShareBucketItemUtil.getLinkItemShareText(bucketItemList = filteredBucketItemList)
				else -> ""
			}

			callback(shareText)
		}
	}
}
