package com.syncodec.momento.database.bucketItem

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BucketItemDbTableDao {
	//    !!!   TODO    Is conflict strategy correct
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(bucketItemDbEntry: BucketItemDbEntry)

	@Query(value = "UPDATE bucket_item_table SET g_drive_file_id = :gDriveFileId WHERE `key` = :key")
	fun addGDriveFileId(key: String, gDriveFileId: String)

	@Update
	fun update(bucketItemDbEntry: BucketItemDbEntry)

	@Query(value = "SELECT * FROM bucket_item_table WHERE `key` = :key")
	suspend fun get(key: String): BucketItemDbEntry?

	@Query(value = "SELECT * FROM bucket_item_table WHERE `key` = :key")
	fun getAsFlow(key: String): Flow<BucketItemDbEntry?>

	@Query(value = "SELECT `key`, created_timestamp, modified_timestamp, title, thumbnail, state FROM bucket_item_table WHERE bucket_key = :bucketKey")
	fun getForPreviewAsFlow(bucketKey: String): Flow<List<BucketItemPreviewDbEntry>>

	@Query(value = "SELECT * FROM bucket_item_table WHERE bucket_key = :bucketKey ORDER BY created_timestamp DESC")
	fun getFromBucketAsFlow(bucketKey: String) : Flow<List<BucketItemDbEntry>>

	@Query(value = "SELECT * FROM bucket_item_table WHERE bucket_key = :bucketKey ORDER BY created_timestamp DESC")
	suspend fun getAllBucketItem(bucketKey: String) : List<BucketItemDbEntry>

	@Query(value = "SELECT COUNT(*) FROM bucket_item_table WHERE bucket_key = :bucketKey")
	fun countBucketSizeAsFlow(bucketKey: String) : Flow<Int>

	@Query(value = "SELECT COUNT(*) FROM bucket_item_table WHERE bucket_key = :bucketKey")
	suspend fun countBucketSize(bucketKey: String) : Int

	@Query(value = "DELETE FROM bucket_item_table WHERE `key` = :key")
	suspend fun delete(key: String)

	@Query("DELETE FROM bucket_item_table WHERE `key` IN (:keyList)")
	suspend fun delete(keyList: List<String>)

	@Query("DELETE FROM bucket_item_table WHERE bucket_key IN (:bucketKeyList)")
	suspend fun deleteWithBucket(bucketKeyList: List<String>)
}
