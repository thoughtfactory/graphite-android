package com.syncodec.graphite.presentation.bucketItem.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.di.network.TMDbApi
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
class ShowBucketItemViewModel(lockableRepo: LockableRepo) : ViewModel() {

	private val _repository: MutableStateFlow<Repository?> = MutableStateFlow(null)

	private val _isNew: MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val isNew: StateFlow<Boolean?> = _isNew

	private val _showType: MutableStateFlow<ShowType?> = MutableStateFlow(null)
	val showType: StateFlow<ShowType?> = _showType

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
			combine(_repository, id) { repository1, id1 -> Pair(repository1, id1) }.collectLatest { (repository1, bucketItemId) ->
				bucketItemId?.let {
					repository1?.getBucketItemAsFlow(id = bucketItemId)?.collectLatest {
						this@ShowBucketItemViewModel._bucketItemObject.tryEmit(it)
						this@ShowBucketItemViewModel._showType.tryEmit((it?.getShowData() as? BucketItemObject.Companion.BucketItemData.ShowData)?.type)
					}
				}
			}
		}
	}

	fun initBucketItem(showId: String, parentId: RealmUUID, showType: ShowType) {
		viewModelScope.launch(Dispatchers.IO) {
			this@ShowBucketItemViewModel._isNew.tryEmit(true)
			this@ShowBucketItemViewModel._showType.tryEmit(showType)

			when (showType) {
				ShowType.TV -> {
					val tvData = TMDbApi.retrieveTvDataFromId(id = showId)

					val bucketItemObject = BucketItemObject().apply {
						this.bucketType = BucketType.SHOW.name
						this.title = tvData?.name
						this.parentId = parentId
						this.data = BucketItemObject.Companion.BucketItemData.ShowData(type = showType, tvData = tvData).toJsonString()
						this.key = tvData?.id
					}

					this@ShowBucketItemViewModel._bucketItemObject.tryEmit(bucketItemObject)

//                  retrieveThumbnail and retrieveDescription is called after bucketItemObject is emitted because thumbnail will be updated in object when retrieved
					retrieveThumbnail(posterPath = tvData?.posterPath)
				}

				ShowType.MOVIE -> {
					val movieData = TMDbApi.retrieveMovieDataFromId(id = showId)

					val bucketItemObject = BucketItemObject().apply {
						this.bucketType = BucketType.SHOW.name
						this.title = movieData?.title
						this.parentId = parentId
						this.data = BucketItemObject.Companion.BucketItemData.ShowData(type = showType, movieData = movieData).toJsonString()
						this.key = movieData?.id
					}

					this@ShowBucketItemViewModel._bucketItemObject.tryEmit(bucketItemObject)

//                  retrieveThumbnail and retrieveDescription is called after bucketItemObject is emitted because thumbnail will be updated in object when retrieved
					retrieveThumbnail(posterPath = movieData?.posterPath)
				}
			}

			this@ShowBucketItemViewModel._parentId.tryEmit(parentId)
		}
	}

	fun readBucketItem(id: RealmUUID, parentId: RealmUUID) {
		this._isNew.tryEmit(false)
		this._id.tryEmit(id)
		this._parentId.tryEmit(parentId)
	}

	fun putBucketItem() {
		viewModelScope.launch(Dispatchers.Default) {
			this@ShowBucketItemViewModel.bucketItemObject.value?.let { bucketItemObject1 ->
				_repository.value?.putBucketItemSuspended(bucketItemObject1)
				bucketItemObject1.parentId?.let { readBucketItem(id = bucketItemObject1.id, parentId = it) } ?: Log.e("npr71", "bucket item parent id is null")
			}
		}
	}

	private fun retrieveThumbnail(posterPath: String?) {
		val thumbnail = TMDbApi.retrieveShowPoster(posterPath = posterPath)
		this@ShowBucketItemViewModel.bucketItemObject.value?.clone()?.apply {
			this.thumbnail = thumbnail?.encodeBase64()
			this@ShowBucketItemViewModel._bucketItemObject.tryEmit(this)
		}
	}

	fun onClickFavourite() {
		this.bucketItemObject.value?.clone()?.apply {
			this.isFavourite = this.isFavourite.not()
			this@ShowBucketItemViewModel._bucketItemObject.tryEmit(this)
			putBucketItem()
		}
	}

	fun onClickLock() {
		this.bucketItemObject.value?.clone()?.apply {
			this.isLocked = this.isLocked.not()
			this@ShowBucketItemViewModel._bucketItemObject.tryEmit(this)
			putBucketItem()
		}
	}

	fun onChangeState(newState: BucketItemState) {
		this.bucketItemObject.value?.clone()?.apply {
			this.state = newState.name
			this@ShowBucketItemViewModel._bucketItemObject.tryEmit(this)
			putBucketItem()
		}
	}

	fun delete() {
		this._id.value?.let { _repository.value?.deleteSuspended(id = it) }
	}
}
