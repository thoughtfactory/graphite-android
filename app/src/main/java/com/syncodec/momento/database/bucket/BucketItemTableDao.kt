package com.syncodec.momento.database.bucket

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BucketItemTableDao {
	//    !!!   TODO    Is conflict strategy correct
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(bucketItemDbEntry: BucketItemDbEntry)

	@Query(value = "UPDATE bucket_item_table SET g_drive_file_id = :gDriveFileId WHERE primary_key = :primaryKey")
	fun addGDriveFileId(primaryKey: String, gDriveFileId: String)

	@Update
	fun update(bucketItemDbEntry: BucketItemDbEntry)

	@Query(value = "SELECT * FROM bucket_item_table WHERE primary_key = :primaryKey")
	suspend fun get(primaryKey: String): BucketItemDbEntry?

	@Query(value = "SELECT * FROM bucket_item_table WHERE bucket_key = :bucketKey ORDER BY created_timestamp DESC")
	fun getAllAsLiveData(bucketKey: String) : LiveData<List<BucketItemDbEntry>>

	@Query(value = "SELECT * FROM bucket_item_table ORDER BY created_timestamp DESC")
	suspend fun getAll() : List<BucketItemDbEntry>

	@Query(value = "DELETE FROM bucket_item_table WHERE primary_key = :primaryKey")
	suspend fun delete(primaryKey: String)

	@Delete
	suspend fun delete(diaryEntries: List<BucketItemDbEntry>)

	//	TODO REMOVE THIS
	@Query(value = "DELETE FROM bucket_table")
	suspend fun deleteAll()


//    @Query(value = "SELECT * FROM diary_entry_table ORDER BY modified_timestamp DESC")
//    fun getAll(): LiveData<List<DiaryEntry>>
}
