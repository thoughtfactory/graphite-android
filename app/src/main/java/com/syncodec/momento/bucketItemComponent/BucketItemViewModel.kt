package com.syncodec.momento.bucketItemComponent

import android.app.Application
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.momento.Momento
import com.syncodec.momento.bucketComponent.modalBottomSheet.BookData
import com.syncodec.momento.database.bucket.BucketItemDbEntry
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.repository.BucketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BucketItemViewModel(application: Application) : AndroidViewModel(application) {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	private val bucketRepository: BucketRepository = BucketRepository(momento = application as Momento)
	lateinit var activityState: BucketItemActivity.ActivityState

	val status: MutableState<Status> = mutableStateOf(Status.INIT)

	lateinit var bucketItemType: BucketItemType.Type
	lateinit var bucketKey: String
	var bucketItemKey: String? = null

	var bucketItemDbEntry = mutableStateOf(
		BucketItemDbEntry(
			primaryKey = "",
			bucketKey = "",
			bucketItemType = -1
		)
	)

	var bookData = mutableStateOf<BookData?>(null)

	var thumbnail = mutableStateOf<String?>(null)
	var thoughtList: SnapshotStateList<String> = mutableStateListOf()

	fun updateThought() {
		viewModelScope.launch {
			bucketRepository.putThought(
				bucketKey = bucketKey,
				bucketItemKey = bucketItemKey!!,
				thoughtList = thoughtList
			)
		}
	}

	fun getThought() {
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				thoughtList = bucketRepository.getThought(bucketKey = bucketKey, bucketItemKey = bucketItemKey!!).toMutableStateList()
			}
		}
	}

	fun putItem() {
		viewModelScope.launch {
			when (bucketItemType) {
				BucketItemType.Type.TODO -> {
				}
				BucketItemType.Type.BOOKS -> bucketRepository.putBucketItem(
					bucketKey = bucketKey,
					bucketItemKey = bucketItemKey,
					bucketItemType = bucketItemType,
					title = bookData.value!!.title,
					state = bucketItemDbEntry.value.state,
					thoughtList = thoughtList,
					jsonString = objectMapper.writeValueAsString(bookData.value)
				)
				BucketItemType.Type.MOVIES -> {
				}
				BucketItemType.Type.TVSHOWS -> {
				}
				BucketItemType.Type.MEDIA -> {
				}
				BucketItemType.Type.LINKS -> {
				}
			}
		}
	}

	fun getItem() {
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				when (bucketItemType) {
					BucketItemType.Type.TODO -> {
					}
					BucketItemType.Type.BOOKS -> {
						try {
							val bucketItemData = bucketRepository.getBucketItemData(
								bucketKey = bucketKey,
								bucketItemKey = bucketItemKey!!
							)
							if (bucketItemData.first != null) {
								bucketItemDbEntry.value = bucketItemData.first!!
								bookData.value = objectMapper.readValue(bucketItemData.second)
								thumbnail.value = getThumbnail()
								bucketRepository.getThought(
									bucketKey = bucketKey,
									bucketItemKey = bucketItemKey!!
								).forEach {
									thoughtList.add(it)
								}
								status.value = Status.LOADED
							} else {
								status.value = Status.ERROR
							}
						} catch (exception: Exception) {
							status.value = Status.ERROR
						}
					}
					BucketItemType.Type.MOVIES -> {
					}
					BucketItemType.Type.TVSHOWS -> {
					}
					BucketItemType.Type.MEDIA -> {
					}
					BucketItemType.Type.LINKS -> {
					}
				}
			}
		}
	}

	fun updateItem() {
		viewModelScope.launch {
			withContext(Dispatchers.IO) {
				when (bucketItemType) {
					BucketItemType.Type.TODO -> {
					}
					BucketItemType.Type.BOOKS -> {
						if (bucketItemDbEntry.value.primaryKey.isNotEmpty()) {
							bucketRepository.updateBucketItem(bucketItemDbEntry = bucketItemDbEntry.value)
						}
					}
					BucketItemType.Type.MOVIES -> {
					}
					BucketItemType.Type.TVSHOWS -> {
					}
					BucketItemType.Type.MEDIA -> {
					}
					BucketItemType.Type.LINKS -> {
					}
				}
			}
		}
	}

	private fun getThumbnail(): String {
		return bucketRepository.getBucketItemThumbnail(
			bucketKey = bucketKey,
			bucketItemKey = bucketItemKey!!
		)
	}
}
