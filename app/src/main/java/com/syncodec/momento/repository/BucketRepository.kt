package com.syncodec.momento.repository

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.syncodec.momento.Momento
import com.syncodec.momento.database.UserDatabase
import com.syncodec.momento.database.bucket.BucketDbEntry
import com.syncodec.momento.database.bucket.BucketDbTableDao
import com.syncodec.momento.database.bucketItem.BucketItemDbEntry
import com.syncodec.momento.database.bucketItem.BucketItemDbTableDao
import com.syncodec.momento.database.bucketItem.BucketItemPreviewDbEntry
import com.syncodec.momento.database.bucketItem.BucketItemType
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalCoroutinesApi::class)
class BucketRepository(val momento: Momento) {

	private val bucketDbTableDao: BucketDbTableDao =
		UserDatabase.getInstance(momento).bucketDbTableDao
	private val bucketItemDbTableDao: BucketItemDbTableDao =
		UserDatabase.getInstance(momento).bucketItemDbTableDao

	var bucketList: SnapshotStateList<BucketDbEntry> = mutableStateListOf()

	init {
		CoroutineScope(Dispatchers.IO).launch {
			bucketDbTableDao.getAllAsFlow().collectLatest {
				try {
					bucketList.clear()
					bucketList.addAll(it)
				} catch (exception: Exception) {
				}
			}
		}
	}

	fun getBucketItemPreviewListAsFlow(bucketKey: String): Flow<List<BucketItemPreviewDbEntry>> {
		return bucketItemDbTableDao.getForPreviewAsFlow(bucketKey = bucketKey)
	}

	fun getBucket(bucketKey: String): Flow<BucketDbEntry?> {
		return bucketDbTableDao.getAsFlow(key = bucketKey)
	}

	fun getBucketItem(bucketItemKey: String): Flow<BucketItemDbEntry?> =
		bucketItemDbTableDao.getAsFlow(key = bucketItemKey)


	suspend fun deleteBucketItem(bucketKey: String, keyList: List<String>) {
		withContext(Dispatchers.IO) {
			bucketItemDbTableDao.delete(keyList)
			updateBucketSize(bucketKey)
		}
	}

	suspend fun deleteBucket(keyList: List<String>) {
		withContext(Dispatchers.IO) {
			bucketDbTableDao.delete(keyList)
			bucketItemDbTableDao.deleteWithBucket(keyList)
		}
	}

	suspend fun putNewBucket(bucketType: BucketItemType, title: String) {
		withContext(Dispatchers.IO) {
			val currentTimestamp = System.currentTimeMillis()
			BucketDbEntry(
				key = generatePrimaryKey(),
				bucketItemType = bucketType
			).apply {
				this.createdTimestamp = currentTimestamp
				this.modifiedTimestamp = currentTimestamp
				this.title = title

				bucketDbTableDao.insert(bucketDbEntry = this)
			}
		}
	}

	fun putBucket(bucketDbEntry: BucketDbEntry) =
		bucketDbTableDao.update(bucketDbEntry = bucketDbEntry)

	suspend fun putBucketItem(bucketItemDbEntry: BucketItemDbEntry) =
		withContext(Dispatchers.IO) {
			bucketItemDbTableDao.insert(bucketItemDbEntry = bucketItemDbEntry)
			updateBucketSize(bucketItemDbEntry.bucketKey)
		}

	suspend fun updateBucketSize(key: String) {
		bucketDbTableDao.get(key).apply {
			if (this != null) {
				this.bucketSize = bucketItemDbTableDao.countBucketSize(key)
				bucketDbTableDao.insert(this)
			}
		}

	}

	suspend fun updateBucketItem(bucketItemDbEntry: BucketItemDbEntry) =
		withContext(Dispatchers.IO) { bucketItemDbTableDao.update(bucketItemDbEntry = bucketItemDbEntry) }

	companion object {
		private var INSTANCE: BucketRepository? = null

		fun getInstance(momento: Momento): BucketRepository {
			synchronized(lock = this) {
				var instance = INSTANCE
				if (instance == null) {
					instance = BucketRepository(momento = momento)
					INSTANCE = instance
				}
				return instance
			}
		}
	}
}
