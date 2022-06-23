package com.syncodec.graphite.database.tag

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TagKeyDbTableDao {
	//    !!!   TODO    Is conflict strategy correct
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(tagKeyDbEntry: TagKeyDbEntry)

	@Update
	fun update(tagKeyDbEntry: TagKeyDbEntry)

	@Query(value = "SELECT * FROM tag_key_table WHERE `key` = :key")
	suspend fun get(key: String): TagKeyDbEntry?

	@Query(value = "SELECT * FROM tag_key_table WHERE `key` = :key")
	fun getTagsForEntryAsFlow(key: String): Flow<List<TagKeyDbEntry>>

	@Query(value = "SELECT * FROM tag_key_table WHERE tag = :tag")
	fun getEntriesForTagAsFlow(tag: String): Flow<List<TagKeyDbEntry>>

	@Query(value = "SELECT `key` FROM tag_key_table")
	suspend fun getAllKeys(): List<String>

	@Query(value = "SELECT * FROM tag_key_table ORDER BY tag DESC")
	fun getAllAsFlow(): Flow<List<TagKeyDbEntry>>

	@Query(value = "SELECT * FROM tag_key_table ORDER BY tag DESC")
	suspend fun getAll(): List<TagKeyDbEntry>

	@Query(value = "DELETE FROM tag_key_table WHERE tag = :tag AND `key` = :key")
	suspend fun delete(tag: String, key: String)

	@Query(value = "DELETE FROM tag_key_table WHERE `key` = :key")
	suspend fun deleteConnection(key: String)

	@Query(value = "DELETE FROM tag_key_table")
	suspend fun deleteAll()
}
