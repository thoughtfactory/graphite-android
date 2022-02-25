package com.syncodec.momento.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.momento.Momento
import com.syncodec.momento.bucketComponent.modalBottomSheet.BookData
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.bucket.*
import com.syncodec.momento.miscellaneous.downloadImage
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class BucketRepository(val momento: Momento) {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	private val bucketDbTableDao: BucketDbTableDao = UserDatabase.getInstance(momento).bucketDbTableDao
	private val bucketItemTableDao: BucketItemTableDao = UserDatabase.getInstance(momento).bucketItemTableDao

	fun getBucketList(): LiveData<List<BucketDbEntry>> {
		return bucketDbTableDao.getAllAsLiveData()
	}

	suspend fun getBucketItem(bucketItemKey: String): BucketItemDbEntry? {
		return bucketItemTableDao.get(primaryKey = bucketItemKey)
	}

	fun getBucketItemList(bucketKey: String): LiveData<List<BucketItemDbEntry>> {
		return bucketItemTableDao.getAllAsLiveData(bucketKey = bucketKey)
	}

	suspend fun putBucket(bucketType: BucketItemType.Type, title: String) {
		withContext(Dispatchers.IO) {
			val currentTimestamp = System.currentTimeMillis()
			BucketDbEntry(
				primaryKey = generatePrimaryKey(),
				bucketType = bucketType.ordinal
			).apply {
				this.createdTimestamp = currentTimestamp
				this.modifiedTimestamp = currentTimestamp
				this.title = title
				this.containerSize = 0
				this.contentThumbnail = null
				this.isArchived = false
				this.isFavourite = false
				this.isLocked = false

				bucketDbTableDao.insert(bucketDbEntry = this)
			}
		}
	}

	suspend fun putBucketItem(
		bucketKey: String,
		bucketItemKey: String?,
		bucketItemType: BucketItemType.Type,
		title: String,
		state: Int,
		thoughtList: List<String>,
		jsonString: String
	) {
		withContext(Dispatchers.IO) {
			val currentTimestamp = System.currentTimeMillis()

			val bucketItemDbEntry = BucketItemDbEntry(
				primaryKey = bucketItemKey ?: generatePrimaryKey(),
				bucketKey = bucketKey,
				bucketItemType = bucketItemType.ordinal,
				createdTimestamp = currentTimestamp
			).apply {
				this.modifiedTimestamp = currentTimestamp
				this.title = title
				this.state = state
				this.isFavourite = false
				this.isArchived = false
				this.isLocked = false
			}
			bucketItemTableDao.insert(bucketItemDbEntry = bucketItemDbEntry)

			val thumbnail: Bitmap? = when (bucketItemType) {
				BucketItemType.Type.TODO -> null
				BucketItemType.Type.BOOKS -> {
					val bookData: BookData = objectMapper.readValue(jsonString)
					if (bookData.coverI != null) {
						try {
							downloadImage(thumbnailUrl = "https://covers.openlibrary.org/b/id/${bookData.coverI}-M.jpg")
						} catch (exception: Exception) {
							null
						}
					} else null
				}
				BucketItemType.Type.MOVIES -> null
				BucketItemType.Type.TVSHOWS -> null
				BucketItemType.Type.MEDIA -> null
				BucketItemType.Type.LINKS -> null
			}

			momento.putBucketItemData(
				bucketKey = bucketKey,
				bucketItemKey = bucketItemDbEntry.primaryKey,
				jsonString = jsonString,
				thumbnail = thumbnail
			)

			momento.putThought(
				bucketKey = bucketKey,
				bucketItemKey = bucketItemDbEntry.primaryKey,
				thoughtList = thoughtList
			)
		}
	}

	suspend fun getBucketItemData(
		bucketKey: String,
		bucketItemKey: String
	): Pair<BucketItemDbEntry?, String> {
		return Pair(
			getBucketItem(bucketItemKey = bucketItemKey), momento.getBucketItemData(
				bucketKey = bucketKey,
				bucketItemKey = bucketItemKey
			)
		)
	}

	suspend fun updateBucketItem(
		bucketItemDbEntry: BucketItemDbEntry
	) {
		withContext(Dispatchers.IO) {
			bucketItemTableDao.insert(bucketItemDbEntry = bucketItemDbEntry)
		}
	}

	suspend fun putThought(
		bucketKey: String,
		bucketItemKey: String,
		thoughtList: List<String>
	) {
		withContext(Dispatchers.IO) {
			momento.putThought(
				bucketKey = bucketKey,
				bucketItemKey = bucketItemKey,
				thoughtList = thoughtList
			)
		}
	}

	fun getThought(
		bucketKey: String,
		bucketItemKey: String
	): List<String> {
		val thoughtList = momento.getThought(bucketKey = bucketKey, bucketItemKey = bucketItemKey)
		return thoughtList
	}

	fun getBucketItemThumbnail(
		bucketKey: String,
		bucketItemKey: String
	): String {
		return momento.getBucketItemThumbnail(
			bucketKey = bucketKey,
			bucketItemKey = bucketItemKey
		)
	}
}
