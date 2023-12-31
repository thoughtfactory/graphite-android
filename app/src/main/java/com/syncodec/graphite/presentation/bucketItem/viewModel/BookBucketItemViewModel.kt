package com.syncodec.graphite.presentation.bucketItem.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.local.BucketItemObject
import com.syncodec.graphite.di.model.local.BucketItemState
import com.syncodec.graphite.di.model.local.BucketType
import com.syncodec.graphite.di.network.OpenLibraryApi
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.utils.encodeBase64
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class BookBucketItemViewModel(lockableRepo: LockableRepo, private val openLibraryApi: OpenLibraryApi) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

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
			lockableRepo.repositoryStatusFlow.collectLatest { repositoryStatus ->
				if (repositoryStatus is Repository.Companion.RepositoryStatus.Success) _repository.tryEmit(repositoryStatus.repository)
			}
		}

		viewModelScope.launch(Dispatchers.Default) {
			combine(_repository, id) { repository1, id1 -> Pair(repository1, id1) }.collect { (repository1, bucketItemId) ->
				bucketItemId?.let {
					this.launch {
						repository1?.getObjectFromIdAsFlow<BucketItemObject>(id = bucketItemId)?.collect {
							this@BookBucketItemViewModel._bucketItemObject.tryEmit(it)
						}
					}
				}
			}
		}
	}

	fun initBucketItem(bookId: String, parentId: RealmUUID) {
		viewModelScope.launch(Dispatchers.IO) {
			val bookData = openLibraryApi.getBookDataFromCache(bookKey = bookId)?.apply {
				this.description = retrieveDescription(bookKey = this.key)
			}

			val bucketItemObject = BucketItemObject().apply {
				this.bucketType = BucketType.BOOK.name
				this.title = bookData?.title
				this.parentId = parentId
				this.setBucketItemData(bucketItemData = bookData)
				this.key = bookData?.key
			}

			this@BookBucketItemViewModel._isNew.tryEmit(true)
			this@BookBucketItemViewModel._bucketItemObject.tryEmit(bucketItemObject)

			retrieveThumbnail(coverI = bookData?.coverI)?.let { thumbnail ->
				this@BookBucketItemViewModel._bucketItemObject.value?.clone()?.apply {
					this.thumbnail = thumbnail
					this@BookBucketItemViewModel._bucketItemObject.tryEmit(this)
				}
			}

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
				_repository.value?.putBucketItemSuspended(bucketItemObject1)
				bucketItemObject1.parentId?.let { readBucketItem(id = bucketItemObject1.id, parentId = it) } ?: Log.e("npr71", "bucket item parent id is null")
			}
		}
	}

	private fun retrieveThumbnail(coverI: Int?) = openLibraryApi.retrieveBookCover(coverI = coverI)?.encodeBase64()

	private fun retrieveDescription(bookKey : String) : String? = openLibraryApi.retrieveDescriptionFromKey(bookKey = bookKey)

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

	fun delete() {
		this._id.value?.let { _repository.value?.deleteSuspended(id = it) }
	}
}

