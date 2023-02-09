package com.syncodec.graphite.presentation.bucket.composable.screen

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import com.syncodec.graphite.di.repository.RepositoryState
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class BucketScreenCommonViewModel(private val repository : KoinRepository) : ViewModel() {

	val repositoryState = repository.repositoryState

	val bucketObject : MutableStateFlow<BucketObject?> = MutableStateFlow(null)

	val id : MutableState<RealmUUID?> = mutableStateOf(null)

	val bucketItemList : MutableState<List<BucketItemObject>> = mutableStateOf(listOf())
	var isLoadedFirstTime : MutableState<Boolean> = mutableStateOf(false)

	fun initBucket(realmUUID : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				when (it) {
					RepositoryState.SUCCESS -> refreshBucket(realmUUID = realmUUID)
					else -> null
				}
			}
		}
	}

	private fun refreshBucket(realmUUID : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			repository.getBucketAsFlow(id = realmUUID).collect {
				withContext(Dispatchers.Main) {
					bucketObject.tryEmit(it)
					id.value = it?.id
				}
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			repository.getBucketItemFromParentIdAsFlow(parentId = realmUUID).collect {
				repository.getBucketFromId(id = realmUUID)?.let {
					bucketItemList.value = it.bucketItemList
					isLoadedFirstTime.value = true
				}
			}
		}
	}

	fun onReorderBucketItem(bucketItemList : List<BucketItemObject>) {
		viewModelScope.launch(Dispatchers.Default) {
			id.value?.let {
				repository.reorderBucketItem(it, bucketItemList.map { it.id }) { _, _ -> }
			}
		}
	}

	fun toggleFavourite(bucketItemObject : BucketItemObject) {
		viewModelScope.launch(Dispatchers.Default) {
			bucketItemObject.clone().apply {
				this.isFavourite = ! this.isFavourite
				this.parentId?.let {
					repository.putBucketItem(bucketId = it, bucketItemObject = this) { _, _ -> }
				}
			}
		}
	}

	fun toggleLock(bucketItemObject : BucketItemObject) {
		viewModelScope.launch(Dispatchers.Default) {
			bucketItemObject.clone().apply {
				this.isLocked = ! this.isLocked
				this.parentId?.let {
					repository.putBucketItem(bucketId = it, bucketItemObject = this) { _, _ -> }
				}
			}
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
				this.parentId?.let {
					repository.putBucketItem(bucketId = it, bucketItemObject = this) { _, _ -> }
				}
			}
		}
	}
}
