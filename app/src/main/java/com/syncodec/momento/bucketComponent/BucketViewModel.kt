package com.syncodec.momento.bucketComponent

import android.app.Application
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.momento.Momento
import com.syncodec.momento.database.bucket.BucketDbEntry
import com.syncodec.momento.database.bucketItem.BucketItemDbEntry
import com.syncodec.momento.database.bucketItem.BucketItemPreviewDbEntry
import com.syncodec.momento.database.bucketItem.BucketItemState
import com.syncodec.momento.database.bucketItem.BucketItemType
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.repository.BucketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BucketViewModel(application: Application) : AndroidViewModel(application) {

	private val bucketRepository: BucketRepository = BucketRepository.getInstance(momento = application as Momento)

	lateinit var activityState: BucketActivity.ActivityState

	val status: MutableState<Status> = mutableStateOf(Status.INIT)

	lateinit var bucketKey: String
	val bucketDbEntry = mutableStateOf<BucketDbEntry?>(null)
	val bucketItemDbEntry = mutableStateOf<BucketItemPreviewDbEntry?>(null)

	val bucketItemList: SnapshotStateList<BucketItemPreviewDbEntry> = mutableStateListOf()

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
			bucketRepository.getBucketItemPreviewListAsFlow(bucketKey = bucketKey).collect {
				status.value = Status.LOADING
				bucketItemList.clear()
				bucketItemList.addAll(it)
				status.value = Status.LOADED
			}
		}
	}

	fun addTodo(
		key: String?,
		title: String,
		state: Int,
	) {
		viewModelScope.launch(Dispatchers.IO) {
			BucketItemDbEntry(
				key = key ?: generatePrimaryKey(),
				bucketKey = bucketKey,
				bucketItemType = BucketItemType.TODO,
				createdTimestamp = System.currentTimeMillis()
			).apply {
				this.modifiedTimestamp = this.createdTimestamp
				this.title = title
				this.state = BucketItemState.values()[state]
				bucketRepository.putBucketItem(this)
			}
		}
	}

	fun updateItem() = viewModelScope.launch(Dispatchers.IO) { bucketDbEntry.value?.let { bucketRepository.putBucket(bucketDbEntry = it) } }

	fun deleteBucketItem(keyList: List<String>) {
		viewModelScope.launch(Dispatchers.IO) {
			bucketRepository.deleteBucketItem(bucketKey = bucketKey, keyList = keyList)
		}
	}
}
