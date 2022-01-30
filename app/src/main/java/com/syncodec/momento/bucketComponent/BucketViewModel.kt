package com.syncodec.momento.bucketComponent

import android.app.Application
import android.os.FileObserver
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.momento.Momento
import com.syncodec.momento.database.bucket.Bucket
import com.syncodec.momento.database.bucket.BucketDbEntry
import com.syncodec.momento.database.bucket.BucketItem
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.repository.BucketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.util.*


class BucketViewModel(application: Application) : AndroidViewModel(application) {

	val bucketRepository: BucketRepository = BucketRepository(application)

	lateinit var bucketKey: String
	lateinit var bucketItemType: BucketItemType

	lateinit var bucketActivityState: BucketActivity.BucketActivityState

	var bucket: Bucket by mutableStateOf(Bucket("", BucketItemType.TODO))
	var bucketItemList by mutableStateOf(emptyList<BucketItem>())

	private val _status: MutableState<Int> = mutableStateOf(0)
	val status: State<Int> get() = _status

	fun openBucket() {
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				try {
					bucket = bucketRepository.open(bucketKey)
					readBucket()
					withContext(Dispatchers.Main) { _status.value = 1 }
				} catch (exception: FileNotFoundException) {
					withContext(Dispatchers.Main) { _status.value = -1 }
				} catch (exception: Exception) {
					withContext(Dispatchers.Main) { _status.value = -2 }
				}
			}
		}
	}

	fun readBucket() {
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				try {
					bucketItemList = bucketRepository.readBucket(bucketKey = bucketKey)
					withContext(Dispatchers.Main) { _status.value = 1 }
				} catch (exception: FileNotFoundException) {
					withContext(Dispatchers.Main) { _status.value = -1 }
				} catch (exception: Exception) {
					withContext(Dispatchers.Main) { _status.value = -2 }
				}
			}
		}
	}

	fun insertBucket(
		bucketType: BucketItemType,
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
