package com.syncodec.momento.bucketComponent

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.syncodec.momento.MainActivity
import com.syncodec.momento.database.bucket.Bucket
import com.syncodec.momento.database.bucket.BucketDbEntry
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.repository.BucketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

class BucketViewModel(application: Application): AndroidViewModel(application) {

	val bucketRepository: BucketRepository = BucketRepository(application)

	lateinit var bucketActivityState: BucketActivity.BucketActivityState

	var bucket: Bucket by mutableStateOf(Bucket("", BucketItemType.TODO))

	private val _status: MutableState<Int> = mutableStateOf(0)
	val status: State<Int> get() = _status

	fun openBucket(primaryKey: String) {
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				try {
					bucket = bucketRepository.open(primaryKey)
					_status.value = 1
				} catch (exception: FileNotFoundException) {
					_status.value = -1
				} catch (exception: Exception) {
					_status.value = -2
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
