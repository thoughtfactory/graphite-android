package com.syncodec.graphite.presentation.bucket.composable.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class BucketScreenCommonViewModel(private val repository : KoinRepository) : ViewModel() {

	val repositoryState = repository.repositoryState

	val bucketObject : MutableStateFlow<BucketObject?> = MutableStateFlow(null)

	val id : MutableStateFlow<RealmUUID?> = MutableStateFlow(null)

	private val _bucketItemList : MutableStateFlow<List<BucketItemObject>> = MutableStateFlow(listOf())
	private val _orderedBucketItemList : MutableStateFlow<List<BucketItemObject>> = MutableStateFlow(listOf())
	val bucketItemList : StateFlow<List<BucketItemObject>> = _orderedBucketItemList
	var isLoadedFirstTime : MutableStateFlow<Boolean> = MutableStateFlow(false)

	init {
		viewModelScope.launch(Dispatchers.Default) {
			combine(
				bucketObject,
				_bucketItemList,
			) { bucketObject1, bucketItemList1 ->
				bucketObject1?.bucketItemOrderList?.let { bucketItemOrder ->
					bucketItemList1.sortedBy { bucketItemOrder.indexOf(it.id) }
				} ?: bucketItemList1
			}.collect {
				_orderedBucketItemList.tryEmit(it)
			}
		}
	}

	fun initBucket(realmUUID : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				when (it) {
					RepositoryState.SUCCESS -> loadBucket(realmUUID = realmUUID)
					else -> null
				}
			}
		}
	}

	private fun loadBucket(realmUUID : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			repository.getBucketAsFlow(id = realmUUID).collect {
				withContext(Dispatchers.Main) {
					bucketObject.tryEmit(it)
					id.tryEmit(realmUUID)
				}
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			repository.getBucketItemWithParentIdAsFlow(parentId = realmUUID).collect {
				_bucketItemList.tryEmit(it)
				isLoadedFirstTime.tryEmit(true)
			}
		}
	}

	fun onReorderBucketItem(idOrderList : List<RealmUUID>) {
		id.value?.let { repository.reorderBucketItemListSuspended(parentId = it, idOrderList = idOrderList) }
	}

	fun toggleFavourite(bucketItemObject : BucketItemObject) {
		bucketItemObject.clone().apply {
			this.isFavourite = ! this.isFavourite
			repository.putBucketItem(bucketItemObject = this)
		}
	}

	fun toggleLock(bucketItemObject : BucketItemObject) {
		bucketItemObject.clone().apply {
			this.isLocked = ! this.isLocked
			repository.putBucketItem(bucketItemObject = this)
		}
	}

	fun toggleBucketItemState(bucketItemObject : BucketItemObject) {
		viewModelScope.launch(Dispatchers.Default) {
			bucketItemObject.clone().apply {
				this.state = when (this.state) {
					BucketItemState.ALPHA.name -> BucketItemState.BETA.name
					BucketItemState.BETA.name -> BucketItemState.GAMMA.name
					BucketItemState.GAMMA.name -> BucketItemState.ALPHA.name
					else -> BucketItemState.ALPHA.name
				}
				repository.putBucketItem(bucketItemObject = this)
			}
		}
	}
}
