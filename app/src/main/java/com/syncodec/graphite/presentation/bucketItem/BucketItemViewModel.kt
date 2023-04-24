package com.syncodec.graphite.presentation.bucketItem

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.di.repository.koinRepository.KoinRepository
import com.syncodec.graphite.utils.Status
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class BucketItemViewModel(private val repository : KoinRepository) : ViewModel() {

	val repositoryState = repository.repositoryState
	val status : MutableState<Status> = mutableStateOf(Status.INIT)

	val isNew = mutableStateOf<Boolean?>(null)

	val id : MutableState<RealmUUID?> = mutableStateOf(null)
	val createdTimestamp : MutableState<Long?> = mutableStateOf(null)
	val modifiedTimestamp : MutableState<Long?> = mutableStateOf(null)
	val bucketType : MutableState<BucketType?> = mutableStateOf(null)
	val title : MutableState<String?> = mutableStateOf(null)
	val state : MutableState<BucketItemState?> = mutableStateOf(null)
	val isFavourite : MutableState<Boolean?> = mutableStateOf(null)
	val isLocked : MutableState<Boolean?> = mutableStateOf(null)
	val parentId = mutableStateOf<RealmUUID?>(null)

	val bucketItemObject : MutableState<BucketItemObject?> = mutableStateOf(null)

	val key : MutableStateFlow<String?> = MutableStateFlow(null)
	val data : MutableStateFlow<String?> = MutableStateFlow(null)
	val thumbnail : MutableStateFlow<String?> = MutableStateFlow(null)

	val showType : MutableStateFlow<ShowType?> = MutableStateFlow(null)

	fun initData(bucketId : RealmUUID, bucketType : BucketType, showType : ShowType? = null) {
		this.isNew.value = true
		this.createdTimestamp.value = System.currentTimeMillis()
		this.modifiedTimestamp.value = System.currentTimeMillis()
		this.bucketType.value = bucketType
		this.isFavourite.value = false
		this.isLocked.value = false
		this.parentId.value = bucketId

		this.showType.tryEmit(showType)
	}

	fun loadData(bucketItemId : RealmUUID, showType : ShowType? = null) {
		this.isNew.value = false
		this.showType.tryEmit(showType)

		viewModelScope.launch(Dispatchers.Default) {
			repository.getBucketItemAsFlow(bucketItemId).collect { bucketItem ->

				withContext(Dispatchers.Main) {
					this@BucketItemViewModel.bucketItemObject.value = bucketItem

					this@BucketItemViewModel.id.value = bucketItem?.id
					this@BucketItemViewModel.createdTimestamp.value = bucketItem?.createdTimestamp
					this@BucketItemViewModel.modifiedTimestamp.value = bucketItem?.modifiedTimestamp
					this@BucketItemViewModel.bucketType.value = BucketType.values().find { it.name == bucketItem?.bucketType }
					this@BucketItemViewModel.title.value = bucketItem?.title
					this@BucketItemViewModel.state.value = BucketItemState.values().find { it.name == bucketItem?.state }
					this@BucketItemViewModel.isFavourite.value = bucketItem?.isFavourite
					this@BucketItemViewModel.isLocked.value = bucketItem?.isLocked
					this@BucketItemViewModel.parentId.value = bucketItem?.parentId

					this@BucketItemViewModel.data.tryEmit(bucketItem?.data)
					this@BucketItemViewModel.thumbnail.tryEmit(bucketItem?.thumbnail)
				}
			}
		}
	}

	fun putBucketItem() {
		viewModelScope.launch(Dispatchers.Default) {
			BucketItemObject().apply {
				this@BucketItemViewModel.id.value?.let { this.id = it }
				this.createdTimestamp = this@BucketItemViewModel.createdTimestamp.value ?: System.currentTimeMillis()
				this.modifiedTimestamp = System.currentTimeMillis()
				this.bucketType = this@BucketItemViewModel.bucketType.value?.name ?: BucketType.UNKNOWN.name
				this.title = this@BucketItemViewModel.title.value
				this.state = this@BucketItemViewModel.state.value?.name ?: BucketItemState.ALPHA.name
				this.isFavourite = this@BucketItemViewModel.isFavourite.value ?: false
				this.isLocked = this@BucketItemViewModel.isLocked.value ?: false
				this.parentId = this@BucketItemViewModel.parentId.value

				this.data = this@BucketItemViewModel.data.value
				this.key = this@BucketItemViewModel.key.value
				this.thumbnail = this@BucketItemViewModel.thumbnail.value

				repository.putBucketItem(this)
				loadData(this.id, showType = showType.value)
			}
		}
	}

	fun onToggleFavourite() {
		isFavourite.value = isFavourite.value?.not()
		putBucketItem()
	}

	fun onToggleLock() {
		isLocked.value = isLocked.value?.not()
		putBucketItem()
	}

	fun onChangeState(state : Int) {
		this.state.value = BucketItemState.values()[state]
		putBucketItem()
	}

	fun delete(id : RealmUUID, callback : suspend () -> Unit) {
		repository.deleteSuspended(id = id, callback = callback)
	}
}
