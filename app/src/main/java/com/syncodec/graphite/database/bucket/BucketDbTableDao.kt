package com.syncodec.graphite.database.bucket

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BucketDbTableDao {
	//    !!!   TODO    Is conflict strategy correct
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(bucketDbEntry: BucketDbEntry)

	@Update
	fun update(bucketDbEntry: BucketDbEntry)

	@Query(value = "SELECT * FROM bucket_table WHERE `key` = :key")
	suspend fun get(key: String): BucketDbEntry?

	@Query(value = "SELECT `key` FROM bucket_table")
	suspend fun getAllKeys(): List<String>

	@Query(value = "SELECT * FROM bucket_table WHERE `key` = :key")
	fun getAsFlow(key: String): Flow<BucketDbEntry?>

	@Query(value = "SELECT * FROM bucket_table ORDER BY created_timestamp DESC")
	fun getAllAsFlow(): Flow<List<BucketDbEntry>>

	@Query(value = "SELECT * FROM bucket_table ORDER BY created_timestamp DESC")
	suspend fun getAll(): List<BucketDbEntry>

	@Query(value = "DELETE FROM bucket_table WHERE `key` = :key")
	suspend fun delete(key: String)

	@Query(value = "DELETE FROM bucket_table WHERE `key` IN (:keyList)")
	suspend fun delete(keyList: List<String>)

	@Query(value = "DELETE FROM bucket_table")
	suspend fun deleteAll()
}
