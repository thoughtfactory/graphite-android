package com.syncodec.momento.bucketComponent

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.momento.database.bucket.Bucket
import com.syncodec.momento.database.bucket.BucketDbEntry
import com.syncodec.momento.database.bucket.BucketItem
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.repository.BucketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException


class BucketViewModel(application: Application) : AndroidViewModel(application) {

	private val bucketRepository: BucketRepository = BucketRepository(application)

	lateinit var bucketKey: String
	lateinit var bucketItemType: BucketItemType.Type

	lateinit var bucketActivityState: BucketActivity.BucketActivityState

	var bucket: Bucket by mutableStateOf(Bucket("", BucketItemType.Type.TODO))
	var bucketItemList by mutableStateOf(emptyList<BucketItem>())

	private val _status: MutableState<Status> = mutableStateOf(Status.INIT)
	val status: State<Status> get() = _status

	fun openBucket() {
		viewModelScope.launch {
			withContext(Dispatchers.Main) { _status.value = Status.LOADING }
			withContext(Dispatchers.IO) {
				bucket = bucketRepository.open(bucketKey)
				readBucket()
			}
		}
	}

	private suspend fun readBucket() {
		withContext(Dispatchers.IO) {
			try {
				bucketItemList = bucketRepository.readBucket(bucketKey = bucketKey)
				withContext(Dispatchers.Main) { _status.value = Status.LOADED }
			} catch (exception: FileNotFoundException) {
				withContext(Dispatchers.Main) { _status.value = Status.ERROR }
			} catch (exception: Exception) {
				withContext(Dispatchers.Main) { _status.value = Status.ERROR }
			}
		}
	}

	fun deleteBucketItem(bucketItemKeyList: List<String>) {
		bucketItemKeyList.forEach { bucketItemKey ->
			bucketRepository.deleteBucketItem(
				bucketKey = bucketKey,
				bucketItemKey = bucketItemKey
			)
		}
		openBucket()
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				bucketRepository.updateBucketSize(bucketKey = bucketKey)
			}
		}
	}

	fun insertBucket(
		bucketType: BucketItemType.Type,
		title: String,
	) {
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				val currentTimestamp = System.currentTimeMillis()
				BucketDbEntry(
					primaryKey = generatePrimaryKey(),
					bucketType = bucketType.ordinal
				).apply {
					this.createdTimestamp = currentTimestamp
					this.modifiedTimestamp = currentTimestamp
					this.title = title
					bucketRepository.insert(this)
				}
			}
		}
	}

}
