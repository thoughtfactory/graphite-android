package com.syncodec.graphite.presentation.bucket

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.graphite.di.Repository
import com.syncodec.graphite.di.model.BucketObject
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BucketViewModel: ViewModel() {

	val bucketObject: MutableState<BucketObject?> = mutableStateOf(null)

	fun loadAndViewData(id: ObjectId) {
		viewModelScope.launch(Dispatchers.IO) {
			Repository.getBucketAsFlow(id = id).collect {
				bucketObject.value = it
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
