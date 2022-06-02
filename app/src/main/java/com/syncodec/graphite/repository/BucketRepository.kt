package com.syncodec.graphite.repository

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.database.UserDatabase
import com.syncodec.graphite.database.bucket.BucketDbEntry
import com.syncodec.graphite.database.bucket.BucketDbTableDao
import com.syncodec.graphite.database.bucketItem.BucketItemDbEntry
import com.syncodec.graphite.database.bucketItem.BucketItemDbTableDao
import com.syncodec.graphite.database.bucketItem.BucketItemPreviewDbEntry
import com.syncodec.graphite.database.bucketItem.BucketItemType
import com.syncodec.graphite.miscellaneous.generatePrimaryKey
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalCoroutinesApi::class)
class BucketRepository(val graphite: Graphite) {

	private val bucketDbTableDao: BucketDbTableDao =
		UserDatabase.getInstance(graphite).bucketDbTableDao
	private val bucketItemDbTableDao: BucketItemDbTableDao =
		UserDatabase.getInstance(graphite).bucketItemDbTableDao

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

	private suspend fun updateBucketSize(key: String) {
		bucketDbTableDao.get(key).apply {
			if (this != null) {
				this.bucketSize = bucketItemDbTableDao.countBucketSize(key)
				this.modifiedTimestamp = System.currentTimeMillis()
				bucketDbTableDao.insert(this)
			}
		}
	}

	suspend fun updateBucketItem(bucketItemDbEntry: BucketItemDbEntry) =
		withContext(Dispatchers.IO) {
			bucketItemDbTableDao.update(bucketItemDbEntry = bucketItemDbEntry)
			updateBucketModifyTimestamp(bucketKey = bucketItemDbEntry.bucketKey)
		}

	suspend fun updateBucketModifyTimestamp(bucketKey: String) = bucketDbTableDao.get(bucketKey)?.apply {
		this.modifiedTimestamp = System.currentTimeMillis()
		bucketDbTableDao.update(this)
	}

	companion object {
		private var INSTANCE: BucketRepository? = null

		fun getInstance(graphite: Graphite): BucketRepository {
			synchronized(lock = this) {
				var instance = INSTANCE
				if (instance == null) {
					instance = BucketRepository(graphite = graphite)
					INSTANCE = instance
				}
				return instance
			}
		}
	}
}
