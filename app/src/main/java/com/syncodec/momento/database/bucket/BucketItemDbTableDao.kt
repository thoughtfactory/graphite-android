package com.syncodec.momento.database.bucket

import androidx.lifecycle.LiveData
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
	fun getAsLiveData(key: String): Flow<BucketItemDbEntry>

	@Query(value = "SELECT * FROM bucket_item_table WHERE bucket_key = :bucketKey ORDER BY created_timestamp DESC")
	fun getFromBucketAsLiveData(bucketKey: String) : LiveData<List<BucketItemDbEntry>>

	@Query(value = "SELECT * FROM bucket_item_table WHERE bucket_key = :bucketKey ORDER BY created_timestamp DESC")
	suspend fun getAllBucketItem(bucketKey: String) : List<BucketItemDbEntry>

	@Query(value = "DELETE FROM bucket_item_table WHERE `key` = :key")
	suspend fun delete(key: String)

//	@Delete
//	suspend fun delete(diaryEntries: List<BucketItemDbEntry>)

	@Query("DELETE FROM bucket_item_table WHERE `key` IN (:keyList)")
	suspend fun delete(keyList: List<String>)


//    @Query(value = "SELECT * FROM diary_entry_table ORDER BY modified_timestamp DESC")
//    fun getAll(): LiveData<List<DiaryEntry>>
}
