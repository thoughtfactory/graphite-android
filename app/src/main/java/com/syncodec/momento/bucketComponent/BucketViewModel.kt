package com.syncodec.momento.bucketComponent

import android.app.Application
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.momento.Momento
import com.syncodec.momento.database.bucket.BucketDbEntry
import com.syncodec.momento.database.bucketItem.BucketItemDbEntry
import com.syncodec.momento.database.bucketItem.BucketItemType
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.repository.BucketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BucketViewModel(application: Application) : AndroidViewModel(application) {

	private val bucketRepository: BucketRepository = BucketRepository.getInstance(momento = application as Momento)

	lateinit var activityState: BucketActivity.ActivityState

	val status: MutableState<Status> = mutableStateOf(Status.INIT)

	lateinit var bucketKey: String
	lateinit var bucketItemType: BucketItemType
	val bucketDbEntry = mutableStateOf<BucketDbEntry?>(null)

	val bucketItemList: SnapshotStateList<BucketItemDbEntry> = mutableStateListOf()

	fun getBucket() {
		status.value = Status.LOADING
		viewModelScope.launch(Dispatchers.IO) {
			bucketRepository.getBucket(bucketKey = bucketKey).collect {
				if (it == null) {
					status.value = Status.ERROR
				} else {
					bucketDbEntry.value = it
					status.value = Status.LOADED
				}
			}
		}
		viewModelScope.launch(Dispatchers.IO) {
			bucketRepository.getBucketItemListAsFlow(bucketKey = bucketKey).collect {
				status.value = Status.LOADING
				bucketItemList.removeAll { true }
				bucketItemList.addAll(it)
				status.value = Status.LOADED
			}
		}
	}

	fun updateItem() = viewModelScope.launch(Dispatchers.IO) { bucketDbEntry.value?.let { bucketRepository.putBucket(bucketDbEntry = it) } }

	fun deleteBucketItem(keyList: List<String>) {
		viewModelScope.launch(Dispatchers.IO) {
			bucketRepository.deleteBucketItem(keyList = keyList)
		}
	}
}
