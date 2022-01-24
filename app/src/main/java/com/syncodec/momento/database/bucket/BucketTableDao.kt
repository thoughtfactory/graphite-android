package com.syncodec.momento.database.bucket

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BucketTableDao {
	//    !!!   TODO    Is conflict strategy correct
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(bucketDbEntry: BucketDbEntry)

	@Query(value = "UPDATE bucket_table SET g_drive_file_id = :gDriveFileId WHERE primary_key = :primaryKey")
	fun addGDriveFileId(primaryKey: String, gDriveFileId: String)

	@Update
	fun update(bucketDbEntry: BucketDbEntry)

	@Query(value = "SELECT * FROM bucket_table WHERE primary_key = :primaryKey")
	suspend fun get(primaryKey: String): BucketDbEntry?

	@Query(value = "SELECT * FROM bucket_table ORDER BY created_timestamp DESC")
	fun getAllAsLiveData() : LiveData<List<BucketDbEntry>>

	@Query(value = "SELECT * FROM bucket_table ORDER BY created_timestamp DESC")
	suspend fun getAll() : List<BucketDbEntry>

	@Query(value = "DELETE FROM bucket_table WHERE primary_key = :primaryKey")
	suspend fun delete(primaryKey: String)

	@Delete
	suspend fun delete(diaryEntries: List<BucketDbEntry>)

	//	TODO REMOVE THIS
	@Query(value = "DELETE FROM bucket_table")
	suspend fun deleteAll()


//    @Query(value = "SELECT * FROM diary_entry_table ORDER BY modified_timestamp DESC")
//    fun getAll(): LiveData<List<DiaryEntry>>
}
