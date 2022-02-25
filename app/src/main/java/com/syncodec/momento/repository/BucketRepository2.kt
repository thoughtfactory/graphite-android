package com.syncodec.momento.repository

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

//class BucketRepository(val momento: Momento) {
//
//	private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())
//
//	private val bucketDbTableDao: BucketDbTableDao = UserDatabase.getInstance(momento).bucketDbTableDao
//	private val bucketItemTableDao: BucketItemTableDao = UserDatabase.getInstance(momento).bucketItemTableDao
//
//	val bucketDbEntryListLiveData: LiveData<List<BucketDbEntry>> = bucketDbTableDao.getAllAsLiveData()
//	val bucketItemDbEntryListLiveData: LiveData<List<BucketItemDbEntry>> = bucketItemTableDao.getAllAsLiveData()
//
//	private suspend fun getBucketDbEntry(bucketKey: String): BucketDbEntry? {
//		return bucketDbTableDao.get(bucketKey)
//	}
//
//	private suspend fun getBucketItemDbEntry(bucketItemKey: String): BucketItemDbEntry? {
//		return bucketItemTableDao.get(bucketItemKey)
//	}
//
//	@Throws(FileNotFoundException::class)
//	fun open(primaryKey: String): Bucket {
//		return momento.getBucket(primaryKey)
//	}
//
//	fun insert(bucketDbEntry: BucketDbEntry) {
//		bucketDbTableDao.insert(bucketDbEntry)
//	}
//
//	fun insert(bucketItemDbEntry: BucketItemDbEntry) {
//		bucketItemTableDao.insert(bucketItemDbEntry)
//	}
//
//	suspend fun createNewBucket(
//		title: String,
//		bucketType: BucketItemType.Type
//	) {
//		withContext(Dispatchers.IO) {
//			val primaryKey = generatePrimaryKey()
//			val currentTimestamp = System.currentTimeMillis()
//
//			Bucket(
//				primaryKey = primaryKey,
//				bucketType = bucketType,
//				createdTimestamp = currentTimestamp
//			).apply {
//				this.modifiedTimestamp = currentTimestamp
//				this.title = title
//
//				momento.putBucket(this)
//			}
//
//			BucketDbEntry(
//				primaryKey = primaryKey,
//				bucketType = bucketType.ordinal
//			).apply {
//				this.createdTimestamp = currentTimestamp
//				this.modifiedTimestamp = currentTimestamp
//				this.title = title
//				insert(this)
//			}
//		}
//	}
//
//	fun readBucket(
//		bucketKey: String
//	): MutableList<BucketItem> {
//		return momento.getAllBucketItems(
//			bucketKey = bucketKey,
//		)
//	}
//
//	suspend fun updateBucketSize(bucketKey: String) {
//		getBucketDbEntry(bucketKey = bucketKey)!!.apply {
//			containerSize = momento.getBucketSize(bucketKey = bucketKey)
//			insert(this)
//		}
//	}
//
//	suspend fun createNewBucketItem(
//		bucketItem: BucketItem
//	) {
//		BucketItemDbEntry(
//			primaryKey = bucketItem.primaryKey,
//			bucketKey = bucketItem.bucketKey,
//			bucketItemType = bucketItem.itemType.ordinal,
//			createdTimestamp = bucketItem.createdTimestamp
//		).apply {
//			this.modifiedTimestamp = bucketItem.modifiedTimestamp
//			this.title = bucketItem.title
//			this.isArchived = false
//			this.isFavourite = false
//			this.isLocked = false
//
//			insert(this)
//		}
//
//		momento.putBucketItem(
//			bucketItem = bucketItem,
//		)
//		updateBucketSize(bucketItem.bucketKey)
//	}
//
//	suspend fun putBucketItem(
//		bucketItem: BucketItem
//	) {
//		momento.putBucketItem(
//			bucketItem = bucketItem,
//		)
//	}
//
//	suspend fun readBucketItem(
//		bucketKey: String,
//		bucketItemKey: String
//	): BucketItem {
//		return momento.getBucketItem(
//			bucketKey = bucketKey,
//			bucketItemKey = bucketItemKey
//		)
//	}
//
//	fun deleteBucketItem(
//		bucketKey: String,
//		bucketItemKey: String
//	) {
//		momento.deleteBucketItem(
//			bucketKey = bucketKey,
//			bucketItemKey = bucketItemKey
//		)
//	}
//
//	fun getBucketItemThumbnail(
//		bucketKey: String,
//		bucketItemKey: String
//	): String {
//		return momento.getBucketItemThumbnail(bucketKey = bucketKey, bucketItemKey = bucketItemKey)
//	}
//
//	suspend fun delete(primaryKey: String) {
//		bucketDbTableDao.delete(primaryKey)
//	}
//
//	suspend fun deleteAll() {
//		bucketDbTableDao.deleteAll()
//	}
//}
