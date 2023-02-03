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
import com.syncodec.graphite.di.repository.KoinRepository
import com.syncodec.graphite.di.repository.RepositoryState
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
class BucketBottomSheetViewModel(private val repository : KoinRepository) : ViewModel() {

	val repositoryState = repository.repositoryState

	val bucketObject : MutableStateFlow<BucketObject?> = MutableStateFlow(null)
	private val mutableBucketItemObject = MutableStateFlow<BucketItemObject?>(null)
	val bucketItemObject : StateFlow<BucketItemObject?> = mutableBucketItemObject

	private var bucketItemObjectCoroutineScope: CoroutineScope? = null

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
				}
			}
		}
	}

	fun setBucketItemObject(bucketItemObject : BucketItemObject?) {
		viewModelScope.launch(Dispatchers.Default) {
			bucketItemObjectCoroutineScope?.cancel()
			bucketItemObjectCoroutineScope = this
			repository.getBucketItemAsFlow(id = bucketItemObject?.id).collect { latestBucketItemObject ->
				withContext(Dispatchers.Main) {
					mutableBucketItemObject.tryEmit(latestBucketItemObject)
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
							onResponse(ApiResult.Success(_data = openGraphResult to null))
							CoroutineScope(Dispatchers.IO).launch {
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

	fun putLink(url: String) {
		CoroutineScope(Dispatchers.IO).launch {
			val isUrlValid = URLUtil.isValidUrl(url)
			if (isUrlValid) {
				val bucketItemObject = BucketItemObject().apply {
					this.bucketType = BucketType.LINK.name
					this.parentId = this@BucketBottomSheetViewModel.bucketObject.value?.id
					this.key = url
					this@BucketBottomSheetViewModel.bucketObject.value?.id?.let {
						repository.putBucketItem(it, this) { _, _ -> }
					}
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
									bucketItemObject.putOpenGraphResult(openGraphResult)

									CoroutineScope(Dispatchers.Default).launch {
										this@BucketBottomSheetViewModel.bucketObject.value?.id?.let {
											repository.putBucketItem(it, bucketItemObject) { _, _ -> }
										}
									}
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
		CoroutineScope(Dispatchers.Default).launch {
			BucketItemObject().apply {
				realmUUID?.let { this.id = it }
				this.bucketType = BucketType.TODO.name
				this.title = todo
				this.state = state.name
				this.parentId = this@BucketBottomSheetViewModel.bucketObject.value?.id
				this.key = todo

				this@BucketBottomSheetViewModel.bucketObject.value?.id?.let {
					repository.putBucketItem(it, this) { _, _ -> }
				}
			}
		}
	}

	fun toggleFavourite(bucketItemObject : BucketItemObject?) {
		viewModelScope.launch(Dispatchers.Default) {
			bucketItemObject?.clone()?.apply {
				this.isFavourite = ! this.isFavourite
				this.parentId?.let {
					repository.putBucketItem(bucketId = it, bucketItemObject = this) { _, _ -> }
				}
			}
		}
	}

	fun toggleLock(bucketItemObject : BucketItemObject?) {
		viewModelScope.launch(Dispatchers.Default) {
			bucketItemObject?.clone()?.apply {
				this.isLocked = ! this.isLocked
				this.parentId?.let {
					repository.putBucketItem(bucketId = it, bucketItemObject = this) { _, _ -> }
				}
			}
		}
	}

	fun deleteBucketItem(realmUUIDList : List<RealmUUID>) {
		CoroutineScope(Dispatchers.Default).launch {
			repository.delete(realmUUIDList)
		}
	}
}
