package com.syncodec.momento.bucketComponent

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.syncodec.momento.Momento
import com.syncodec.momento.database.bucket.BucketItemDbEntry
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.repository.BucketRepository

class BucketViewModel(application: Application) : AndroidViewModel(application) {

	private val bucketRepository: BucketRepository = BucketRepository(momento = application as Momento)

	lateinit var activityState: BucketActivity.ActivityState

	private val _status: MutableState<Status> = mutableStateOf(Status.INIT)
	val status: State<Status> get() = _status

	lateinit var bucketKey: String
	lateinit var bucketItemType: BucketItemType.Type

	fun getBucketItemList(): LiveData<List<BucketItemDbEntry>> {
		return bucketRepository.getBucketItemList(bucketKey = bucketKey)
	}

	fun getThumbnail(
		bucketItemKey: String
	) : String {
		return bucketRepository.getBucketItemThumbnail(
			bucketKey = bucketKey,
			bucketItemKey = bucketItemKey
		)
	}
}
