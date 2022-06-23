package com.syncodec.graphite.database.tag

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDbTableDao {
	//    !!!   TODO    Is conflict strategy correct
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(tagDbEntry: TagDbEntry)

	@Update
	fun update(tagDbEntry: TagDbEntry)

	@Query(value = "SELECT tag FROM tag_table")
	fun getAllKeys(): List<String>

	@Query(value = "SELECT * FROM tag_table WHERE tag = :tag")
	fun getTagAsFlow(tag: String): Flow<TagDbEntry?>

	@Query(value = "SELECT * FROM tag_table ORDER BY tag DESC")
	fun getAllAsFlow(): Flow<List<TagDbEntry>>

	@Query(value = "DELETE FROM tag_table WHERE tag = :tag")
	suspend fun delete(tag: String)

	@Query(value = "DELETE FROM tag_table")
	suspend fun deleteAll()
}
