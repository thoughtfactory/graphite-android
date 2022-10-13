package com.syncodec.graphite.presentation.bucket

import android.util.Log
import android.webkit.URLUtil
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.kedia.ogparser.OpenGraphCallback
import com.kedia.ogparser.OpenGraphParser
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.di.Repository
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BucketViewModel : ViewModel() {

	val bucketObject: MutableState<BucketObject?> = mutableStateOf(null)

	fun loadAndViewData(id: ObjectId) {
		viewModelScope.launch(Dispatchers.IO) {
			Repository.getBucketAsFlow(id = id).collect { bucketObject.value = it }
		}
	}

	fun putLink(url: String) {
		viewModelScope.launch(Dispatchers.IO) {
			val isUrlValid = URLUtil.isValidUrl(url)
			if (isUrlValid) {
				val bucketItemObject = BucketItemObject().apply {
					this.bucketType = BucketType.LINK.name
					this.title = url
					bucketObject.value?.let { it1 -> Repository.putBucketItem(it1.id, this) }
				}

				val openGraphParser = OpenGraphParser(
					listener = object : OpenGraphCallback {
						override fun onError(error: String) {
							TODO("Not yet implemented")
						}

						override fun onPostResponse(openGraphResult: OpenGraphResult) {

							bucketItemObject.title = openGraphResult.title
							bucketItemObject.setOpenGraphResult(openGraphResult)

							bucketObject.value?.let { it1 -> Repository.putBucketItemLink(it1.id, bucketItemObject) }
						}
					}
				)

				openGraphParser.parse(url)
			} else {
//				TODO : show error
			}
		}
	}

	fun toggleLock() {
		if (bucketObject.value != null) {
			try {
				Repository.updateBucket(
					id = bucketObject.value!!.id,
					title = bucketObject.value!!.title,
					description = bucketObject.value!!.description,
					isFavourite = bucketObject.value!!.isFavourite,
					isLocked = !bucketObject.value!!.isLocked
				)
			} catch (e: Exception) {
//				TODO Show error
				e.printStackTrace()
				Log.i("npr71", "Error updating chapter")
			}
		}
	}

	fun toggleFavourite() {
		if (bucketObject.value != null) {
			try {
				Repository.updateBucket(
					id = bucketObject.value!!.id,
					title = bucketObject.value!!.title,
					description = bucketObject.value!!.description,
					isFavourite = !bucketObject.value!!.isFavourite,
					isLocked = bucketObject.value!!.isLocked
				)
			} catch (e: Exception) {
//				TODO Show error
				e.printStackTrace()
				Log.i("npr71", "Error updating chapter")
			}
		}
	}
}
