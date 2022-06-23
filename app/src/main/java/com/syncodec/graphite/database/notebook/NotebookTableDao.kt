package com.syncodec.graphite.database.notebook

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotebookTableDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(notebookDbEntry: NotebookDbEntry)

	@Query(value = "UPDATE notebook_table SET g_drive_file_id = :gDriveFileId WHERE `key` = :key")
	fun addGDriveFileId(key: Long, gDriveFileId: String)

	@Update
	fun update(notebookDbEntry: NotebookDbEntry)

	@Query(value = "SELECT * FROM notebook_table WHERE `key` = :key")
	suspend fun get(key: String): NotebookDbEntry?

	@Query(value = "SELECT * FROM notebook_table WHERE `key` = :key")
	fun getAsFlow(key: String): Flow<NotebookDbEntry>

	@Query(value = "SELECT * FROM notebook_table ORDER BY created_timestamp DESC")
	fun getAllAsFlow() : Flow<List<NotebookDbEntry>>

	@Query(value = "SELECT * FROM notebook_table ORDER BY created_timestamp DESC")
	suspend fun getAll() : List<NotebookDbEntry>

	@Query(value = "SELECT `key` FROM notebook_table ORDER BY created_timestamp DESC")
	suspend fun getAllKeys() : List<String>

	@Query(value = "DELETE FROM notebook_table WHERE `key` = :key")
	suspend fun delete(key: String)

	@Query("DELETE FROM notebook_table WHERE `key` IN (:keyList)")
	fun delete(keyList: List<String>)

	@Delete
	suspend fun delete(diaryEntries: List<NotebookDbEntry>)

	@Query("DELETE FROM notebook_table")
	suspend fun deleteAll()
}
