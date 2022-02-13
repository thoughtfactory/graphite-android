package com.syncodec.momento.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.syncodec.momento.Momento
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.bucket.*
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

class BucketRepository(val application: Application) {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	private var bucketTableDao: BucketTableDao = UserDatabase.getInstance(application).bucketTableDao

	val bucketDbEntryListLiveData: LiveData<List<BucketDbEntry>> = bucketTableDao.getAllAsLiveData()
	suspend fun get(bucketKey: String): BucketDbEntry? {
		return bucketTableDao.get(bucketKey)
	}

	@Throws(FileNotFoundException::class)
	fun open(primaryKey: String): Bucket {
		return (application as Momento).getBucket(primaryKey)
	}

	fun insert(bucketDbEntry: BucketDbEntry) {
		bucketTableDao.insert(bucketDbEntry)
	}

	suspend fun createNewBucket(
		title: String,
		bucketType: BucketItemType
	) {
		withContext(Dispatchers.IO) {
			val primaryKey = generatePrimaryKey()
			val currentTimestamp = System.currentTimeMillis()

			Bucket(
				primaryKey = primaryKey,
				bucketType = bucketType
			).apply {
				this.createdTimestamp = currentTimestamp
				this.modifiedTimestamp = currentTimestamp
				this.title = title

				(application as Momento).putBucket(this)
			}

			BucketDbEntry(
				primaryKey = primaryKey,
				bucketType = bucketType.ordinal
			).apply {
				this.createdTimestamp = currentTimestamp
				this.modifiedTimestamp = currentTimestamp
				this.title = title
				insert(this)
			}
		}
	}

	fun readBucket(
		bucketKey: String
	): MutableList<BucketItem> {
		return (application as Momento).getAllBucketItems(
			bucketKey = bucketKey,
		)
	}

	suspend fun updateBucketSize(bucketKey: String) {
		get(bucketKey = bucketKey)!!.apply {
			containerSize = (application as Momento).getBucketSize(bucketKey = bucketKey)
			insert(this)
		}
	}

	suspend fun createNewBucketItem(
		bucketItem: BucketItem
	) {
		(application as Momento).putBucketItem(
			bucketItem = bucketItem,
		)
		updateBucketSize(bucketItem.bucketKey)
	}

	suspend fun putBucketItem(
		bucketItem: BucketItem,
	) {
		(application as Momento).putBucketItem(
			bucketItem = bucketItem,
		)
	}

	suspend fun readBucketItem(
		bucketKey: String,
		bucketItemKey: String
	): BucketItem {
		return (application as Momento).getBucketItem(
			bucketKey = bucketKey,
			bucketItemKey = bucketItemKey
		)
	}

	fun deleteBucketItem(
		bucketKey: String,
		bucketItemKey: String
	) {
		(application as Momento).deleteBucketItem(
			bucketKey = bucketKey,
			bucketItemKey = bucketItemKey
		)
	}

	suspend fun delete(primaryKey: String) {
		bucketTableDao.delete(primaryKey)
	}

	suspend fun deleteAll() {
		bucketTableDao.deleteAll()
	}
}
