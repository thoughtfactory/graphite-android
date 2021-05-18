package com.syncodec.momento.bucketItemComponent

import android.app.Application
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.momento.Momento
import com.syncodec.momento.bucketComponent.modalBottomSheet.BookData
import com.syncodec.momento.bucketComponent.modalBottomSheet.ShowData
import com.syncodec.momento.bucketComponent.modalBottomSheet.ShowType
import com.syncodec.momento.database.bucket.BucketItemDbEntry
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.repository.BucketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BucketItemViewModel(application: Application) : AndroidViewModel(application) {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	private val bucketRepository: BucketRepository = BucketRepository.getInstance(momento = application as Momento).apply { tvData.value = null }
	lateinit var activityState: BucketItemActivity.ActivityState

	val status: MutableState<Status> = mutableStateOf(Status.INIT)

	lateinit var bucketItemType: BucketItemType.Type
	lateinit var bucketKey: String
	var bucketItemKey = mutableStateOf<String?>(null)

	var bucketItemDbEntry = mutableStateOf(
		BucketItemDbEntry(
			key = "",
			bucketKey = "",
			bucketItemType = -1
		)
	)

	var bookData = mutableStateOf<BookData?>(null)
	var showData = mutableStateOf<ShowData?>(null)
	val tvData = bucketRepository.tvData

	var thumbnail = mutableStateOf<Any?>(null)
	var thoughtList: SnapshotStateList<String> = mutableStateListOf()

	fun updateThought() {
		viewModelScope.launch {
			bucketRepository.putThought(
				bucketKey = bucketKey,
				bucketItemKey = bucketItemKey.value!!,
				thoughtList = thoughtList
			)
		}
	}

	fun getThought() {
		viewModelScope.launch(Dispatchers.IO) {
			thoughtList = bucketRepository.getThought(bucketKey = bucketKey, bucketItemKey = bucketItemKey.value!!).toMutableStateList()
		}
	}

	fun putItem() {
		viewModelScope.launch(Dispatchers.IO) {
			when (bucketItemType) {
				BucketItemType.Type.TODO -> {
				}
				BucketItemType.Type.BOOKS -> bucketItemKey.value =
					bucketRepository.putBucketItem(
						bucketKey = bucketKey,
						bucketItemKey = bucketItemKey.value,
						bucketItemType = bucketItemType,
						title = bookData.value!!.title,
						state = bucketItemDbEntry.value.state,
						thoughtList = thoughtList,
 						data = bookData.value!!
					)
				BucketItemType.Type.SHOWS -> bucketItemKey.value =
					bucketRepository.putBucketItem(
						bucketKey = bucketKey,
						bucketItemKey = bucketItemKey.value,
						bucketItemType = bucketItemType,
						title = showData.value!!.title,
						state = bucketItemDbEntry.value.state,
						thoughtList = thoughtList,
						data = tvData.value!!
					)
				BucketItemType.Type.MEDIA -> {
				}
				BucketItemType.Type.LINKS -> {
				}
			}
		}
	}

	fun getItem(intent: Intent?) {
		viewModelScope.launch(Dispatchers.IO) {
			if (bucketItemKey.value == null) {
				try {
					when (bucketItemType) {
						BucketItemType.Type.TODO -> null
						BucketItemType.Type.BOOKS -> bookData.value =
							objectMapper.readValue(intent!!.getStringExtra(Konstant.Companion.Konstant.BUCKET_ITEM_DATA.name)!!)
						BucketItemType.Type.SHOWS -> {
							showData.value = objectMapper.readValue(intent!!.getStringExtra(Konstant.Companion.Konstant.BUCKET_ITEM_DATA.name)!!)
							thumbnail.value = "https://image.tmdb.org/t/p/w500${showData.value!!.posterPath}"
							getShowData()
						}
						BucketItemType.Type.MEDIA -> null
						BucketItemType.Type.LINKS -> null
					}
					status.value = Status.LOADED
				} catch (exception: Exception) {
					status.value = Status.ERROR
				}
			} else {
				try {
					val bucketItemData = bucketRepository.getBucketItemData(
						bucketKey = bucketKey,
						bucketItemKey = bucketItemKey.value!!
					)
					if (bucketItemData.first != null) {
						bucketItemDbEntry.value = bucketItemData.first!!
						thumbnail.value = BitmapFactory.decodeFile(getThumbnail())
						bucketRepository.getThought(
							bucketKey = bucketKey,
							bucketItemKey = bucketItemKey.value!!
						).forEach {
							thoughtList.add(it)
						}

						when (bucketItemType) {
							BucketItemType.Type.TODO -> {
							}
							BucketItemType.Type.BOOKS -> bookData.value = objectMapper.readValue(bucketItemData.second)
							BucketItemType.Type.SHOWS -> bucketRepository.tvData.value = objectMapper.readValue(bucketItemData.second)
							BucketItemType.Type.MEDIA -> {
							}
							BucketItemType.Type.LINKS -> {
							}
						}
						status.value = Status.LOADED
					} else {
						status.value = Status.ERROR
					}
				} catch (exception: Exception) {
					status.value = Status.ERROR
					exception.printStackTrace()
				}
			}
		}
	}

	fun updateItem() {
		viewModelScope.launch(Dispatchers.IO) {
			when (bucketItemType) {
				BucketItemType.Type.TODO -> {
				}
				BucketItemType.Type.BOOKS -> {
					if (bucketItemDbEntry.value.key.isNotEmpty()) {
						bucketRepository.updateBucketItem(bucketItemDbEntry = bucketItemDbEntry.value)
					}
				}
				BucketItemType.Type.SHOWS -> {
					if (bucketItemDbEntry.value.key.isNotEmpty()) {
						bucketRepository.updateBucketItem(bucketItemDbEntry = bucketItemDbEntry.value)
					}
				}
				BucketItemType.Type.MEDIA -> {
				}
				BucketItemType.Type.LINKS -> {
				}
			}
		}
	}

	private fun getShowData() {
		when (showData.value!!.showType) {
			ShowType.TV -> {
				bucketRepository.downloadTvData(id = showData.value!!.id)
			}
			ShowType.MOVIE -> null
		}
	}

	private fun getThumbnail(): String? {
		return bucketRepository.getBucketItemThumbnail(
			bucketKey = bucketKey,
			bucketItemKey = bucketItemKey.value!!
		)
	}
}
