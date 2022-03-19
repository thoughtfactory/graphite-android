package com.syncodec.momento.bucketComponent

import android.app.Application
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.syncodec.momento.Momento
import com.syncodec.momento.database.bucket.BucketDbEntry
import com.syncodec.momento.database.bucket.BucketItemDbEntry
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.repository.BucketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class BucketViewModel(application: Application) : AndroidViewModel(application) {

	val bucketRepository: BucketRepository = BucketRepository.getInstance(momento = application as Momento)

	lateinit var activityState: BucketActivity.ActivityState

	private val _status: MutableState<Status> = mutableStateOf(Status.INIT)
	val status: State<Status> get() = _status

	lateinit var bucketKey: String
	lateinit var bucketItemType: BucketItemType.Type
	lateinit var bucketDbEntry: BucketDbEntry

	val bucketItemList: SnapshotStateList<BucketItemDbEntry> = mutableStateListOf()

	fun getBucket() {
		_status.value = Status.LOADING
		viewModelScope.launch(Dispatchers.IO) {
			bucketRepository.bucketList.value?.find { it.key == bucketKey }.also {
				if (it == null) {
					_status.value = Status.ERROR
				} else {
					bucketDbEntry = it
					bucketRepository.getBucketItemListAsLiveData(bucketKey = bucketKey).asFlow().collect{
						bucketItemList.removeAll { true }
						bucketItemList.addAll(it)
						_status.value = Status.LOADED
					}
				}
			}

		}
	}

	fun getThumbnail(
		bucketItemKey: String
	): String? {
		return bucketRepository.getBucketItemThumbnail(
			bucketKey = bucketKey,
			bucketItemKey = bucketItemKey
		)
	}

	fun deleteBucketItem(keyList: List<String>) {
		viewModelScope.launch(Dispatchers.IO) {
			bucketRepository.deleteBucketItem(bucketKey = bucketKey, keyList = keyList)
		}
	}
}
