package com.syncodec.graphite.bucketComponent

import android.app.Application
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.database.bucket.BucketDbEntry
import com.syncodec.graphite.database.bucketItem.*
import com.syncodec.graphite.konstant.Status
import com.syncodec.graphite.miscellaneous.generatePrimaryKey
import com.syncodec.graphite.repository.BucketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BucketViewModel(application: Application) : AndroidViewModel(application) {

	private val objectMapper = jsonMapper { addModule(kotlinModule()) }.configure(
		DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

	private val bucketRepository: BucketRepository =
		BucketRepository.getInstance(graphite = application as Graphite)

	lateinit var activityState: BucketActivity.ActivityState

	val status: MutableState<Status> = mutableStateOf(Status.INIT)

	lateinit var bucketKey: String
	lateinit var bucketItemType: BucketItemType
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
			if (bucketItemType == BucketItemType.LINK) {
				bucketRepository.getBucketItemPreviewWithExtraListAsFlow(bucketKey = bucketKey).collect {
					status.value = Status.LOADING
					bucketItemList.clear()
					bucketItemList.addAll(it)
					status.value = Status.LOADED
				}
			} else {
				bucketRepository.getBucketItemPreviewListAsFlow(bucketKey = bucketKey).collect {
					status.value = Status.LOADING
					bucketItemList.clear()
					bucketItemList.addAll(it)
					status.value = Status.LOADED
				}
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

	fun addLink(openGraphResult: OpenGraphResult?, url: String) {
		viewModelScope.launch(Dispatchers.IO) {
			val bucketItemDbEntry = BucketItemDbEntry(
				key = generatePrimaryKey(),
				bucketKey = bucketKey,
				bucketItemType = BucketItemType.LINK,
				createdTimestamp = System.currentTimeMillis()
			).apply {
				this.modifiedTimestamp = this.createdTimestamp
				this.title = openGraphResult?.title
				this.state = BucketItemState.ALPHA

				LinkData(
					title = openGraphResult?.title,
					description = openGraphResult?.description,
					image = null,
					type = openGraphResult?.type,
					url = openGraphResult?.url,
					originalUrl = url,
					siteName = openGraphResult?.siteName
				).apply { data = BucketItem(mutableListOf(), objectMapper.writeValueAsString(this)) }

				bucketRepository.putBucketItem(this)
			}

			openGraphResult?.image?.also {
				val request = ImageRequest.Builder(getApplication())
					.data(it)
					.target {
						bucketItemDbEntry.thumbnail = it.toBitmap()
						CoroutineScope(Dispatchers.IO).launch {
							bucketRepository.putBucketItem(bucketItemDbEntry)
						}
					}
					.build()

				val imageLoader = ImageLoader.Builder(getApplication()).build()

				imageLoader.execute(request = request)
			}
		}
	}

	fun updateItem() = viewModelScope.launch(Dispatchers.IO) {
		bucketDbEntry.value?.let {
			bucketRepository.putBucket(bucketDbEntry = it)
		}
	}

	fun deleteBucketItem(keyList: List<String>) {
		viewModelScope.launch(Dispatchers.IO) {
			bucketRepository.deleteBucketItem(bucketKey = bucketKey, keyList = keyList)
		}
	}

	fun updateBucket(bucketTitle: String) =
		viewModelScope.launch(Dispatchers.IO) {
			bucketDbEntry.value?.apply {
				this.title = bucketTitle
				bucketRepository.putBucket(this)
			}
		}
}
