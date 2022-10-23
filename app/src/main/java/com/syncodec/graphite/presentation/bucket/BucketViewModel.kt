package com.syncodec.graphite.presentation.bucket

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.repository.Repository2
import com.syncodec.graphite.di.repository.RepositoryState
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject


class BucketViewModel  @Inject constructor(private val repository2 : Repository2)  : ViewModel() {

	val repositoryState = repository2.repositoryState

	val bucketObject: MutableState<BucketObject?> = mutableStateOf(null)

	fun loadAndViewData(id: ObjectId) {
		viewModelScope.launch(Dispatchers.IO) {
			repositoryState.collect {
				when(it) {
					RepositoryState.INIT -> Log.d("BucketViewModel", "Init")
					RepositoryState.LOADING -> Log.d("BucketViewModel", "Loading")
					RepositoryState.SUCCESS -> {
						if (repositoryState.value != RepositoryState.SUCCESS) this.cancel()
						repository2.getBucketAsFlow(id).collect { bucketObject.value = it }
					}
					RepositoryState.ERROR -> Log.d("BucketViewModel", "Error")
				}
			}
		}
	}

	fun putLink(url: String) {
//		viewModelScope.launch(Dispatchers.IO) {
//			val isUrlValid = URLUtil.isValidUrl(url)
//			if (isUrlValid) {
//				val bucketItemObject = BucketItemObject().apply {
//					this.bucketType = BucketType.LINK.name
//					this.title = url
//					bucketObject.value?.let { it1 -> Repository.putBucketItem(it1.id, this) }
//				}
//
//				val openGraphParser = OpenGraphParser(
//					listener = object : OpenGraphCallback {
//						override fun onError(error: String) {
//							TODO("Not yet implemented")
//						}
//
//						override fun onPostResponse(openGraphResult: OpenGraphResult) {
//
//							bucketItemObject.title = openGraphResult.title
//							bucketItemObject.setOpenGraphResult(openGraphResult)
//
//							bucketObject.value?.let { it1 -> Repository.putBucketItemLink(it1.id, bucketItemObject) }
//						}
//					}
//				)
//
//				openGraphParser.parse(url)
//			} else {
////				TODO : show error
//			}
//		}
	}

	fun toggleLock() {
//		if (bucketObject.value != null) {
//			try {
//				Repository.updateBucket(
//					id = bucketObject.value!!.id,
//					title = bucketObject.value!!.title,
//					description = bucketObject.value!!.description,
//					isFavourite = bucketObject.value!!.isFavourite,
//					isLocked = !bucketObject.value!!.isLocked
//				)
//			} catch (e: Exception) {
////				TODO Show error
//				e.printStackTrace()
//				Log.i("npr71", "Error updating chapter")
//			}
//		}
	}

	fun toggleFavourite() {
//		if (bucketObject.value != null) {
//			try {
//				Repository.updateBucket(
//					id = bucketObject.value!!.id,
//					title = bucketObject.value!!.title,
//					description = bucketObject.value!!.description,
//					isFavourite = !bucketObject.value!!.isFavourite,
//					isLocked = bucketObject.value!!.isLocked
//				)
//			} catch (e: Exception) {
////				TODO Show error
//				e.printStackTrace()
//				Log.i("npr71", "Error updating chapter")
//			}
//		}
	}
}
