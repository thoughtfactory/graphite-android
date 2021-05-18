package com.syncodec.momento.database.notebook

import androidx.lifecycle.LiveData
import androidx.room.*

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

	@Query(value = "SELECT * FROM notebook_table ORDER BY created_timestamp DESC")
	fun getAllAsLiveData() : LiveData<List<NotebookDbEntry>>

	@Query(value = "SELECT * FROM notebook_table ORDER BY created_timestamp DESC")
	suspend fun getAll() : List<NotebookDbEntry>

	@Query(value = "DELETE FROM notebook_table WHERE `key` = :key")
	suspend fun delete(key: String)

	@Delete
	suspend fun delete(diaryEntries: List<NotebookDbEntry>)
}
