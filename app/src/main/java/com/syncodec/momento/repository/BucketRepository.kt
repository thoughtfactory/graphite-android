package com.syncodec.momento.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.momento.Momento
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.bucket.*
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

class BucketRepository(val application: Application) {

	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	private var bucketTableDao: BucketTableDao = UserDatabase.getInstance(application).bucketTableDao

	val bucketDbEntryListLiveData: LiveData<List<BucketDbEntry>> = bucketTableDao.getAllAsLiveData()
	suspend fun get(primaryKey: String): BucketDbEntry? {
		return bucketTableDao.get(primaryKey)
	}

	@Throws(FileNotFoundException::class)
	suspend fun open(primaryKey: String): Bucket {
		return (application as Momento).getBucket(primaryKey)
	}

	suspend fun insert(bucketDbEntry: BucketDbEntry) {
		bucketTableDao.insert(bucketDbEntry)
	}

	suspend fun createNewBucket(title: String, bucketType: BucketItemType) {
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

	suspend fun readBucket(
		bucketKey: String, bucketItemKeyList: Set<String>
	): MutableList<BucketItem> {
		return (application as Momento).getAllBucketItems(
			bucketKey = bucketKey,
			bucketItemKeyList = bucketItemKeyList
		)
	}

	suspend fun insertBucketItem(
		bucketItem: BucketItem,
		isNewItem: Boolean = false
	) {
		(application as Momento).putBucketItem(
			bucketItem = bucketItem,
			isNewItem = isNewItem
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

	suspend fun delete(primaryKey: String) {
		bucketTableDao.delete(primaryKey)
	}

	suspend fun deleteAll() {
		bucketTableDao.deleteAll()
	}
}
