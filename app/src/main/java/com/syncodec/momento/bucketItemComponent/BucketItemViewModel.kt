package com.syncodec.momento.bucketItemComponent

import android.app.Application
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
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
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import com.syncodec.momento.repository.BucketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class BucketItemViewModel(application: Application) : AndroidViewModel(application) {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	private val bucketRepository: BucketRepository = BucketRepository.getInstance(momento = application as Momento).apply { tvData.value = null }
	lateinit var activityState: BucketItemActivity.ActivityState

	val status: MutableState<Status> = mutableStateOf(Status.INIT)

	lateinit var bucketItemType: BucketItemType.Type
	lateinit var bucketKey: String
	var bucketItemKey = mutableStateOf<String?>(null)

	var bucketItemDbEntry: MutableState<BucketItemDbEntry?> = mutableStateOf(null)

	var bookData = mutableStateOf<BookData?>(null)
	var showData = mutableStateOf<ShowData?>(null)
	val tvData = bucketRepository.tvData
	val movieData = bucketRepository.movieData

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
//				BucketItemType.Type.BOOKS -> bucketItemKey.value =
//					bucketRepository.putBucketItem(
//						bucketKey = bucketKey,
//						bucketItemKey = bucketItemKey.value,
//						bucketItemType = bucketItemType,
//						title = bookData.value!!.title,
//						state = _bucketItemDbEntry.value!!.state,
//						thoughtList = thoughtList,
//						downloadThumbnail = true,
// 						data = bookData.value!!
//					)
				BucketItemType.Type.SHOWS -> {
					bucketItemKey.value = bucketItemDbEntry.value!!.key
					bucketItemDbEntry.value!!.apply {
						this.title = showData.value?.title
						bucketRepository.putBucketItem(
							bucketItemDbEntry = this,
							thoughtList = thoughtList,
							downloadThumbnail = true,
							data = when (showData.value!!.showType) {
								ShowType.TV -> tvData.value
								ShowType.MOVIE -> movieData.value
							}
						)
					}
				}
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

					bucketItemDbEntry.value = BucketItemDbEntry(
						key = generatePrimaryKey(),
						bucketKey = bucketKey,
						bucketItemType = bucketItemType,
						createdTimestamp = System.currentTimeMillis()
					).apply {
						this.modifiedTimestamp = this.createdTimestamp
					}

					status.value = Status.LOADED
				} catch (exception: Exception) {
					status.value = Status.ERROR
				}
			} else {
				try {
					bucketRepository.getBucketItem(bucketItemKey = bucketItemKey.value!!, bucketKey = bucketKey).apply {

						if (first != null) {
							bucketItemDbEntry.value = first

							getThumbnail()?.let { thumbnail.value = BitmapFactory.decodeFile(it) }

							val jsonObject = JSONObject(second)
							showData.value = when (jsonObject.optString("showType")) {
								"TV" -> {
									bucketRepository.tvData.value = this.second?.let { objectMapper.readValue(it) }
									ShowData(
										id = tvData.value!!.id,
										showType = ShowType.TV,
										title = tvData.value!!.name,
										posterPath = tvData.value!!.posterPath,
										releaseDate = tvData.value!!.firstAirDate
									)
								}
								"MOVIE" -> {
									bucketRepository.movieData.value = this.second?.let { objectMapper.readValue(it) }
									ShowData(
										id = movieData.value!!.id,
										showType = ShowType.MOVIE,
										title = movieData.value!!.title,
										posterPath = movieData.value!!.posterPath,
										releaseDate = movieData.value!!.releaseDate
									)
								}
								else -> null
							}

							bucketRepository.getThought(
								bucketKey = bucketKey,
								bucketItemKey = bucketItemKey.value!!
							).forEach { thoughtList.add(it) }

							status.value = Status.LOADED
						} else {
							status.value = Status.ERROR
						}
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
					if (bucketItemDbEntry.value!!.key.isNotEmpty()) {
						bucketRepository.updateBucketItem(bucketItemDbEntry = bucketItemDbEntry.value!!)
					}
				}
				BucketItemType.Type.SHOWS -> {
					if (bucketItemDbEntry.value!!.key.isNotEmpty()) {
						bucketRepository.updateBucketItem(bucketItemDbEntry = bucketItemDbEntry.value!!)
						getItem(null)
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
			ShowType.TV -> bucketRepository.downloadTvData(id = showData.value!!.id)
			ShowType.MOVIE -> bucketRepository.downloadMovieData(id = showData.value!!.id)
		}
	}

	private fun getThumbnail(): String? {
		return bucketRepository.getBucketItemThumbnail(
			bucketKey = bucketKey,
			bucketItemKey = bucketItemKey.value!!
		)
	}
}
