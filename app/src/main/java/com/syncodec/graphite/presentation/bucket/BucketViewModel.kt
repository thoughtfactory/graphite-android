package com.syncodec.graphite.presentation.bucket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.di.repository.repository.Repository
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class BucketViewModel(private val repository : Repository) : ViewModel() {

	val repositoryState = repository.repositoryState

	val isOperationPending : MutableStateFlow<Boolean> = MutableStateFlow(false)

	val bucketObject : MutableStateFlow<BucketObject?> = MutableStateFlow(null)

	val id : MutableStateFlow<RealmUUID?> = MutableStateFlow(null)
	val title : MutableStateFlow<String?> = MutableStateFlow(null)
	val description : MutableStateFlow<String?> = MutableStateFlow(null)
	val bucketType : MutableStateFlow<String?> = MutableStateFlow(null)
	val isFavourite : MutableStateFlow<Boolean?> = MutableStateFlow(null)
	val isLocked : MutableStateFlow<Boolean?> = MutableStateFlow(null)

	val bucketItemObject : MutableStateFlow<BucketItemObject?> = MutableStateFlow(null)

	private var refreshCoroutine : CoroutineScope? = null

	fun loadAndViewData(realmUUID : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			this@BucketViewModel.refreshCoroutine?.cancel()
			this@BucketViewModel.refreshCoroutine = this
			repositoryState.collect {
				when (it) {
					Repository.Companion.RepositoryState.Init -> null
					Repository.Companion.RepositoryState.Locked -> null
					Repository.Companion.RepositoryState.Loading -> null
					Repository.Companion.RepositoryState.Success -> {
						if (repositoryState.value != Repository.Companion.RepositoryState.Success) this.cancel()
						getBucket(realmUUID = realmUUID)
					}

					Repository.Companion.RepositoryState.Error -> null
				}
			}
		}
	}

	private suspend fun getBucket(realmUUID : RealmUUID) {
		repository.getBucketAsFlow(realmUUID).collect {
			it?.let { bucketObject ->
				this@BucketViewModel.bucketObject.tryEmit(bucketObject)
				this@BucketViewModel.id.tryEmit(bucketObject.id)
				this@BucketViewModel.title.tryEmit(bucketObject.title)
				this@BucketViewModel.description.tryEmit(bucketObject.description)
				this@BucketViewModel.bucketType.tryEmit(bucketObject.bucketType)
				this@BucketViewModel.isFavourite.tryEmit(bucketObject.isFavourite)
				this@BucketViewModel.isLocked.tryEmit(bucketObject.isLocked)
			}
		}
	}

	private fun putBucket() {
		viewModelScope.launch(Dispatchers.Default) {
			BucketObject().apply {
				if (this@BucketViewModel.id.value != null) this.id = this@BucketViewModel.id.value !!
				this.title = this@BucketViewModel.title.value
				this.description = this@BucketViewModel.description.value
				this.bucketType = this@BucketViewModel.bucketType.value ?: BucketType.UNKNOWN.name
				this.isFavourite = this@BucketViewModel.isFavourite.value ?: false
				this.isLocked = this@BucketViewModel.isLocked.value ?: false

				repository.putBucket(this)
			}
		}
	}

	fun toggleLock() {
		this.isLocked.tryEmit(this.isLocked.value?.not())
		putBucket()
	}

	fun toggleFavourite() {
		this.isFavourite.tryEmit(this.isFavourite.value?.not())
		putBucket()
	}

	fun updateBucket(title : String?, description : String?) {
		this.title.tryEmit(title)
		this.description.tryEmit(description)
		putBucket()
	}

	fun shareBucketItems(shareAll : Boolean, realmUUIDList : List<RealmUUID>, callback : suspend (String) -> Unit) {
		viewModelScope.launch(Dispatchers.Default) {
			id.value?.let { id ->
				repository.getBucketItemWithParentId(id).let {
					it.filter { if (shareAll) true else it.id in realmUUIDList }.let {
						val baseUrl = when (bucketType.value) {
							BucketType.TODO.name -> ""
							BucketType.BOOK.name -> " - https://openlibrary.org"
							BucketType.SHOW.name -> " - https://www.themoviedb.org/"
							BucketType.LINK.name -> ""
							BucketType.UNKNOWN.name -> ""
							else -> ""
						}

						var shareText = ""
						it.forEach {
							val data = it.getData()
							if (data is BucketItemObject.Companion.BucketItemData.ShowData?) {
								val connector = when (data?.type) {
									ShowType.TV -> "tv/"
									ShowType.MOVIE -> "movie/"
									else -> ""
								}
								shareText += "${it.title} $baseUrl$connector${if (bucketType.value == BucketType.TODO.name) "" else it.key}\n"
							}
						}

						callback(shareText)
					}
				}
			}
		}
	}

	fun deleteBucketItem(realmUUIDList : List<RealmUUID>) {
		repository.deleteSuspended(realmUUIDList)
	}

	fun deleteBucket(id : RealmUUID, callback : suspend () -> Unit) {
		this@BucketViewModel.isOperationPending.tryEmit(true)
		repository.deleteSuspended(id = id, callback = callback)
	}
}
