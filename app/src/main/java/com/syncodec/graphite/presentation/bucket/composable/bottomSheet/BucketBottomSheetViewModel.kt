package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import android.graphics.Bitmap
import android.webkit.URLUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kedia.ogparser.OpenGraphCallback
import com.kedia.ogparser.OpenGraphParser
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.ApiResult
import com.syncodec.graphite.di.network.Network
import com.syncodec.graphite.di.network.OpenLibraryApi
import com.syncodec.graphite.di.network.OpenLibraryTitleSearchResult
import com.syncodec.graphite.di.network.TMDbApi
import com.syncodec.graphite.di.network.TMDbMovieSearchResult
import com.syncodec.graphite.di.network.TMDbTvSearchResult
import com.syncodec.graphite.di.repository.repository.Repository
import com.syncodec.graphite.utils.encodeBase64
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel


@KoinViewModel
class BucketBottomSheetViewModel(private val repository : Repository) : ViewModel() {

	val repositoryState = repository.repositoryState

	val bucketObject : MutableStateFlow<BucketObject?> = MutableStateFlow(null)
	private val mutablePreviewBucketItemObject = MutableStateFlow<BucketItemObject?>(null)
	val previewBucketItemObject : StateFlow<BucketItemObject?> = mutablePreviewBucketItemObject

	private var bucketItemObjectCoroutineScope : CoroutineScope? = null

	fun initBucket(realmUUID : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			repositoryState.collect {
				when (it) {
					Repository.Companion.RepositoryState.Success -> refreshBucket(realmUUID = realmUUID)
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
				}
			}
		}
	}

	fun setPreviewBucketItemObject(id : RealmUUID) {
		viewModelScope.launch(Dispatchers.Default) {
			bucketItemObjectCoroutineScope?.cancel()
			bucketItemObjectCoroutineScope = this
			repository.getBucketItemAsFlow(id = id).collect { latestBucketItemObject ->
				withContext(Dispatchers.Main) {
					mutablePreviewBucketItemObject.tryEmit(latestBucketItemObject)
				}
			}
		}
	}

	fun searchForBook(query : String, onResponse : (ApiResult<OpenLibraryTitleSearchResult>) -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			OpenLibraryApi.searchForBook(
				query = query,
				requestType = OpenLibraryApi.OpenLibraryApiRequestType.QUERY,
				onResponse = onResponse
			)
		}
	}

	fun searchForMovie(query : String, onResponse : (ApiResult<TMDbMovieSearchResult>) -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			TMDbApi.searchForMovieTitle(title = query, onResponse = onResponse)
		}
	}

	fun searchForTvShow(query : String, onResponse : (ApiResult<TMDbTvSearchResult>) -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			TMDbApi.searchForTvTitle(title = query, onResponse = onResponse)
		}
	}

	fun getLinkPreview(url : String, onResponse : (ApiResult<Pair<OpenGraphResult, Bitmap?>>) -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			val isUrlValid = URLUtil.isValidUrl(url)
			if (isUrlValid) {
				val openGraphParser = OpenGraphParser(
					listener = object : OpenGraphCallback {
						override fun onError(error : String) {
							onResponse(ApiResult.Error(exception = error))
						}

						override fun onPostResponse(openGraphResult : OpenGraphResult) {
							onResponse(ApiResult.Success(_data = openGraphResult.copy(url = url) to null))
							viewModelScope.launch(Dispatchers.IO) {
								Network.retrieveImage(openGraphResult.image) { bitmap ->
									onResponse(ApiResult.Success(_data = openGraphResult to bitmap))
								}
							}
						}
					}
				)

				openGraphParser.parse(url)
			} else {
				onResponse(ApiResult.Error(exception = "Invalid URL"))
			}
		}
	}

	fun putLink(url : String) {
		CoroutineScope(Dispatchers.IO).launch {
			val isUrlValid = URLUtil.isValidUrl(url)
			if (isUrlValid) {
				val bucketItemObject = BucketItemObject().apply {
					this.bucketType = BucketType.LINK.name
					this.parentId = this@BucketBottomSheetViewModel.bucketObject.value?.id
					this.key = url
					repository.putBucketItem(this)
				}

				val openGraphParser = OpenGraphParser(
					listener = object : OpenGraphCallback {
						override fun onError(error : String) {

						}

						override fun onPostResponse(openGraphResult : OpenGraphResult) {
							CoroutineScope(Dispatchers.IO).launch {
								Network.retrieveImage(openGraphResult.image) { bitmap ->
									bucketItemObject.title = openGraphResult.title
									bucketItemObject.thumbnail = bitmap?.encodeBase64()
									bucketItemObject.putOpenGraphResult(openGraphResult.copy(url = url))

									repository.putBucketItemSuspended(bucketItemObject)
								}
							}
						}
					}
				)

				openGraphParser.parse(url)
			} else {

			}
		}
	}

	fun putTodo(
		realmUUID : RealmUUID?,
		todo : String,
		state : BucketItemState
	) {
		BucketItemObject().apply {
			realmUUID?.let { this.id = it }
			this.bucketType = BucketType.TODO.name
			this.title = todo
			this.state = state.name
			this.parentId = this@BucketBottomSheetViewModel.bucketObject.value?.id
			this.key = todo

			repository.putBucketItemSuspended(this)
		}
	}

	fun toggleFavourite(bucketItemObject : BucketItemObject?) {
		bucketItemObject?.clone()?.apply {
			this.isFavourite = ! this.isFavourite
			repository.putBucketItemSuspended(bucketItemObject = this)
		}
	}

	fun toggleLock(bucketItemObject : BucketItemObject?) {
		bucketItemObject?.clone()?.apply {
			this.isLocked = ! this.isLocked
			repository.putBucketItemSuspended(bucketItemObject = this)
		}
	}

	fun deleteBucketItem(realmUUIDList : List<RealmUUID>) {
		repository.deleteSuspended(realmUUIDList)
	}
}
