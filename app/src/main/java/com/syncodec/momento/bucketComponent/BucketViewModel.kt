package com.syncodec.momento.bucketComponent

import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BucketViewModel(application: Application) : AndroidViewModel(application) {

	private val bucketRepository: BucketRepository = BucketRepository.getInstance(momento = application as Momento)

	lateinit var activityState: BucketActivity.ActivityState

	private val _status: MutableState<Status> = mutableStateOf(Status.INIT)
	val status: State<Status> get() = _status

	lateinit var bucketKey: String
	lateinit var bucketItemType: BucketItemType.Type
	lateinit var bucketDbEntry: BucketDbEntry

	val bucketItemMap: SnapshotStateMap<String, Pair<BucketItemDbEntry, Int>> = mutableStateMapOf()



	fun getBucket() {
		_status.value = Status.LOADING
		viewModelScope.launch(Dispatchers.IO) {
			bucketRepository.getBucket(bucketKey).also {
				if (it!=null) {
					bucketDbEntry = it
					_status.value = Status.LOADED
				} else {
					_status.value = Status.ERROR
				}
			}

			bucketRepository.getBucketItemListAsLiveData(bucketKey = bucketKey).collect {
				bucketItemMap.clear()
				it.forEach {
					bucketItemMap[it.primaryKey] = Pair(it, it.state)
				}
			}
		}
	}

	fun getThumbnail(
		bucketItemKey: String
	) : String? {
		return bucketRepository.getBucketItemThumbnail(
			bucketKey = bucketKey,
			bucketItemKey = bucketItemKey
		)
	}

	fun deleteBucketItem(keyList: List<String>) {
		viewModelScope.launch(Dispatchers.IO) {
			keyList.forEach{
				bucketRepository.deleteBucketItem(bucketKey = bucketKey, bucketItemKey = it)
			}
		}
	}
}
