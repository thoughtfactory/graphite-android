package com.syncodec.graphite.presentation.bucket.composable.screen

import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.local.BucketItemData
import com.syncodec.graphite.di.model.local.BucketItemObject
import com.syncodec.graphite.di.model.local.BucketItemState
import com.syncodec.graphite.di.model.local.BucketObject
import com.syncodec.graphite.di.model.local.BucketType
import com.syncodec.graphite.di.network.Network
import com.syncodec.graphite.di.network.NetworkRequest
import com.syncodec.graphite.di.network.OpenGraphApi
import com.syncodec.graphite.di.repository.LockableRepo
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
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class BucketScreenCommonViewModel(lockableRepo: LockableRepo, private val openGraphApi: OpenGraphApi, private val network: Network) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _bucketObject: MutableStateFlow<BucketObject?> = MutableStateFlow(null)
	val bucketObject: StateFlow<BucketObject?> = _bucketObject

	private val _bucketId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	private val bucketId: StateFlow<RealmUUID?> = _bucketId

	private val _bucketItemList: MutableStateFlow<List<BucketItemObject>> = MutableStateFlow(listOf())
	private val _filteredBucketItemList: MutableStateFlow<List<BucketItemObject>> = MutableStateFlow(listOf())
	val filteredBucketItemList: StateFlow<List<BucketItemObject>> = _filteredBucketItemList

	private val _filterQueryList: MutableStateFlow<Set<String>> = MutableStateFlow(setOf())
	val filterQueryList: StateFlow<Set<String>> = _filterQueryList

	private val _previewBucketItemObjectId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	private val _previewBucketItemObject: MutableStateFlow<BucketItemObject?> = MutableStateFlow(null)
	val previewBucketItemObject: StateFlow<BucketItemObject?> = _previewBucketItemObject

	init {
		viewModelScope.launch(Dispatchers.Default) {
			lockableRepo.repositoryStatusFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}
		viewModelScope.launch(Dispatchers.Default) {
			combine(_repository, bucketId) { repository1, id1 -> Pair(repository1, id1) }.collectLatest { (repository1, id1) ->
				id1?.let { id2 ->
					repository1?.let { repository2 ->
						launch { observeBucket(repository = repository2, id = id2) }
						launch { observeBucketItems(repository = repository2, id = id2) }
						launch { filterSearchQuery() }
						launch { observePreviewBucketItem() }
					}
				}
			}
		}
	}

	fun initBucket(realmUUID: RealmUUID) {
		_bucketId.tryEmit(realmUUID)
	}

	private suspend fun observeBucket(repository: Repository, id: RealmUUID) {
		repository.getObjectFromIdAsFlow<BucketObject>(id = id).collectLatest { bucketObject1 ->
			this@BucketScreenCommonViewModel._bucketObject.tryEmit(bucketObject1)
		}
	}

	private suspend fun observeBucketItems(repository: Repository, id: RealmUUID) {
		repository.getBucketItemWithParentIdAsFlow(parentId = id).collectLatest {
			this@BucketScreenCommonViewModel._bucketItemList.tryEmit(it)
		}
	}

	private suspend fun observePreviewBucketItem() {
		combine(_previewBucketItemObjectId, _bucketItemList) { previewBucketItemObjectId1, bucketItemList1 ->
			bucketItemList1.firstOrNull { it.id == previewBucketItemObjectId1 }
		}.collectLatest {
			this@BucketScreenCommonViewModel._previewBucketItemObject.tryEmit(it)
		}
	}

	private suspend fun filterSearchQuery() {
		combine(_bucketItemList, _filterQueryList) { bucketItemList1, filterQueryList1 ->
			if (filterQueryList1.isEmpty()) bucketItemList1
			else when (bucketObject.value?.bucketType) {
				BucketType.TODO.name -> bucketItemList1.filter { filterTodoBucketItem(it, filterQueryList1) }
				BucketType.BOOK.name -> bucketItemList1.filter { filterBookBucketItem(it, filterQueryList1) }
				BucketType.SHOW.name -> bucketItemList1.filter { filterShowBucketItem(it, filterQueryList1) }
				BucketType.LINK.name -> bucketItemList1.filter { filterLinkBucketItem(it, filterQueryList1) }
				else -> bucketItemList1
			}
		}.collectLatest { filteredBucketList1 ->
			this@BucketScreenCommonViewModel._filteredBucketItemList.tryEmit(filteredBucketList1)
		}
	}

	@WorkerThread
	private fun filterTodoBucketItem(bucketItemObject: BucketItemObject, filterQueryList1: Set<String>): Boolean {
		return filterQueryList1.any { bucketItemObject.title?.contains(other = it, ignoreCase = true) ?: false }
	}

	@WorkerThread
	private fun filterBookBucketItem(bucketItemObject: BucketItemObject, filterQueryList1: Set<String>): Boolean {
		return filterQueryList1.any { bucketItemObject.title?.contains(other = it, ignoreCase = true) ?: false } ||
				bucketItemObject.getBucketItemData<BucketItemData.BookData.OpenLibraryBookData>()?.let { openLibraryBookData ->
					filterQueryList1.any { query -> openLibraryBookData.title?.contains(other = query, ignoreCase = true) ?: false } ||
							filterQueryList1.any { query -> openLibraryBookData.authorList.any { author -> author.contains(other = query, ignoreCase = true) } } ||
							filterQueryList1.any { query -> openLibraryBookData.description?.contains(other = query, ignoreCase = true) ?: false }
				} ?: false
	}

	@WorkerThread
	private fun filterShowBucketItem(bucketItemObject: BucketItemObject, filterQueryList1: Set<String>): Boolean {
		return filterQueryList1.any { bucketItemObject.title?.contains(other = it, ignoreCase = true) ?: false } ||
				bucketItemObject.getBucketItemData<BucketItemData.ShowData.TMDbData>()?.let { tmdbData ->
					filterQueryList1.any { query -> tmdbData.title?.contains(other = query, ignoreCase = true) ?: false } ||
							filterQueryList1.any { query -> tmdbData.description?.contains(other = query, ignoreCase = true) ?: false }
				} ?: false
	}

	@WorkerThread
	private fun filterLinkBucketItem(bucketItemObject: BucketItemObject, filterQueryList1: Set<String>): Boolean {
		return filterQueryList1.any { bucketItemObject.title?.contains(other = it, ignoreCase = true) ?: false } ||
				bucketItemObject.getBucketItemData<BucketItemData.LinkData>()?.let { openGraphResult ->
					filterQueryList1.any { openGraphResult.title?.contains(other = it, ignoreCase = true) ?: false } ||
							filterQueryList1.any { openGraphResult.description?.contains(other = it, ignoreCase = true) ?: false } ||
							filterQueryList1.any { openGraphResult.url?.contains(other = it, ignoreCase = true) ?: false } ||
							filterQueryList1.any { openGraphResult.siteName?.contains(other = it, ignoreCase = true) ?: false }
				} ?: false
	}

	fun onReorderBucketItem(idOrderList: List<RealmUUID>) {
		bucketId.value?.let { _repository.value?.reorderBucketItemList(parentId = it, idOrderList = idOrderList) }
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

	fun putLink(url: String, state: BucketItemState) {
		viewModelScope.launch(Dispatchers.IO) {
			BucketItemObject().apply {
				this.bucketType = BucketType.LINK.name
				this.state = state.name
				this.parentId = this@BucketScreenCommonViewModel.bucketObject.value?.id
				this.key = url

				openGraphApi.getLinkData(url = url) { linkDataNetworkRequest1 ->
					if (linkDataNetworkRequest1 is NetworkRequest.Success) {
						this.title = linkDataNetworkRequest1.data.title
						viewModelScope.launch(Dispatchers.IO) {
							this@apply.thumbnail = network.retrieveImage(linkDataNetworkRequest1.data.imagePath)?.encodeBase64()
							this@apply.setBucketItemData(linkDataNetworkRequest1.data)
							_repository.value?.putBucketItemSuspended(this@apply)
						}
					} else {
						this.title = url
						_repository.value?.putBucketItemSuspended(this)
					}
				}
			}
		}
	}

	fun toggleFavourite(id: RealmUUID?) {
		_repository.value?.setObjectFromIdSuspended<BucketObject>(id = id) {
			this.updateModifyTimestamp()
			this.isFavourite = this.isFavourite.not()
		}
	}

	fun toggleLock(id: RealmUUID?) {
		_repository.value?.setObjectFromIdSuspended<BucketObject>(id = id) {
			this.updateModifyTimestamp()
			this.isLocked = this.isLocked.not()
		}
	}

	fun toggleMultiFavourite(idList: Set<RealmUUID>) {
		val areAllFavourite = this.filteredBucketItemList.value.filter { it.id in idList }.all { it.isFavourite }
		_repository.value?.setMultiObjectFromIdSuspended<BucketItemObject>(idList = idList) {
			this.updateModifyTimestamp()
			this.isFavourite = !areAllFavourite
		}
	}

	fun toggleMultiLock(idList: Set<RealmUUID>) {
		val areAllLocked = this.filteredBucketItemList.value.filter { it.id in idList }.all { it.isLocked }
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
		if (query.isNotBlank()) {
			_filterQueryList.value.toMutableSet().apply {
				add(query)
				_filterQueryList.tryEmit(this.toSet())
			}
		}
	}

	fun filterBySearchRemove(query: String) {
		_filterQueryList.value.toMutableSet().apply {
			remove(query)
			_filterQueryList.tryEmit(this.toSet())
		}
	}

	fun clearSearchFilter() {
		_filterQueryList.tryEmit(setOf())
	}

	fun delete(idList: Set<RealmUUID>) {
		_repository.value?.deleteSuspended(idList = idList)
	}

	fun shareBucketItems(idList: Set<RealmUUID>, callback: suspend (String) -> Unit) {
		viewModelScope.launch(Dispatchers.Default) {
			val filteredBucketItemList = this@BucketScreenCommonViewModel.filteredBucketItemList.value.filter { it.id in idList }

			val shareText = when (bucketObject.value?.bucketType) {
				BucketType.TODO.name -> ShareBucketItemUtil.getTodoItemShareText(bucketItemList = filteredBucketItemList)
				BucketType.BOOK.name -> ShareBucketItemUtil.getBookItemShareText(bucketItemList = filteredBucketItemList)
				BucketType.SHOW.name -> ShareBucketItemUtil.getShowItemShareText(bucketItemList = filteredBucketItemList)
				BucketType.LINK.name -> ShareBucketItemUtil.getLinkItemShareText(bucketItemList = filteredBucketItemList)
				else -> ""
			}

			callback(shareText)
		}
	}

	fun updateTitleDescription(id: RealmUUID, title: String?, description: String?) {
		_repository.value?.setObjectFromIdSuspended<BucketObject>(id = id) {
			this.updateModifyTimestamp()
			this.title = title
			this.description = description
		}
	}

	fun addTodoDebugData() {
		viewModelScope.launch(Dispatchers.Default) {
			repeat(100) {
				putTodo(realmUUID = null, title = it.toString(), state = BucketItemState.entries.random())
			}
		}
	}
}
