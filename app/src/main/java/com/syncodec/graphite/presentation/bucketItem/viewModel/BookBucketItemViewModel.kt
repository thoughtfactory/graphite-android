package com.syncodec.graphite.presentation.bucketItem.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.OpenLibraryApi
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.encodeBase64
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class BookBucketItemViewModel(repositoryStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus>) : ViewModel() {

	val repositoryState: StateFlow<Repository.Companion.RepositoryStatus> = repositoryStateFlow
	private val repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _isNew: MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val isNew: StateFlow<Boolean?> = _isNew

	private val _id: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	val id: StateFlow<RealmUUID?> = _id

	private val _parentId: MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	val parentId: StateFlow<RealmUUID?> = _parentId

	private val _bucketItemObject: MutableStateFlow<BucketItemObject?> = MutableStateFlow(null)
	val bucketItemObject: StateFlow<BucketItemObject?> = _bucketItemObject

	init {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryStateFlow.collect { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) repository.tryEmit(repositoryStatus.repository)
			}
		}
		viewModelScope.launch(Dispatchers.Default) {
			combine(repository, id) { repository1, id1 -> Pair(repository1, id1) }.collect { (repository1, bucketItemId) ->
				bucketItemId?.let {
					this.launch {
						repository1?.getBucketItemAsFlow(id = bucketItemId)?.collect {
							this@BookBucketItemViewModel._bucketItemObject.tryEmit(it)
						}
					}
				}
			}
		}
	}

	fun initBucketItem(bookId: String, parentId: RealmUUID) {
		viewModelScope.launch(Dispatchers.IO) {
			this@BookBucketItemViewModel._isNew.tryEmit(true)
			val bookData = OpenLibraryApi.getBookDataFromCache(bookKey = bookId)

			val bucketItemObject = BucketItemObject().apply {
				this.bucketType = BucketType.BOOK.name
				this.title = bookData?.title
				this.parentId = parentId
				this.data = bookData?.toJsonString()
				this.key = bookData?.key
			}
			this@BookBucketItemViewModel._bucketItemObject.tryEmit(bucketItemObject)

//		    retrieveThumbnail and retrieveDescription is called after bucketItemObject is emitted because thumbnail will be updated in object when retrieved
			retrieveThumbnail(coverI = bookData?.coverI)
			retrieveDescription(bookData = bookData)

			this@BookBucketItemViewModel._parentId.tryEmit(parentId)
		}
	}

	fun readBucketItem(id: RealmUUID, parentId: RealmUUID) {
		this._isNew.tryEmit(false)
		this._id.tryEmit(id)
		this._parentId.tryEmit(parentId)
	}

	fun putBucketItem() {
		viewModelScope.launch(Dispatchers.Default) {
			this@BookBucketItemViewModel.bucketItemObject.value?.let { bucketItemObject1 ->
				repository.value?.putBucketItemSuspended(bucketItemObject1)
				bucketItemObject1.parentId?.let { readBucketItem(id = bucketItemObject1.id, parentId = it) } ?: Log.e("npr71", "bucket item parent id is null")
			}
		}
	}

	private fun retrieveDescription(bookData: BucketItemObject.Companion.BucketItemData.BookData?) {
		this@BookBucketItemViewModel.bucketItemObject.value?.clone()?.apply {
			val description = OpenLibraryApi.retrieveDescriptionFromKey(bookKey = bookData?.key)
			this.data = bookData?.copy(description = description)?.toJsonString()
			this@BookBucketItemViewModel._bucketItemObject.tryEmit(this)
		}
	}

	private fun retrieveThumbnail(coverI: String?) {
		val thumbnail = OpenLibraryApi.retrieveBookCover(coverI = coverI)
		this@BookBucketItemViewModel.bucketItemObject.value?.clone()?.apply {
			this.thumbnail = thumbnail?.encodeBase64()
			this@BookBucketItemViewModel._bucketItemObject.tryEmit(this)
		}
	}

	fun onClickFavourite() {
		this.bucketItemObject.value?.clone()?.apply {
			this.isFavourite = this.isFavourite.not()
			this@BookBucketItemViewModel._bucketItemObject.tryEmit(this)
			putBucketItem()
		}
	}

	fun onClickLock() {
		this.bucketItemObject.value?.clone()?.apply {
			this.isLocked = this.isLocked.not()
			this@BookBucketItemViewModel._bucketItemObject.tryEmit(this)
			putBucketItem()
		}
	}

	fun onChangeState(newState : BucketItemState) {
		this.bucketItemObject.value?.clone()?.apply {
			this.state = newState.name
			this@BookBucketItemViewModel._bucketItemObject.tryEmit(this)
			putBucketItem()
		}
	}
}

