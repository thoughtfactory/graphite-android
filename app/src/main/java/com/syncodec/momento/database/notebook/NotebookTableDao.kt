package com.syncodec.momento.database.notebook

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NotebookTableDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(notebookDbEntry: NotebookDbEntry)

	@Query(value = "UPDATE notebook_table SET g_drive_file_id = :gDriveFileId WHERE primary_key = :key")
	fun addGDriveFileId(key: Long, gDriveFileId: String)

	@Update
	fun update(notebookDbEntry: NotebookDbEntry)

	@Query(value = "SELECT * FROM notebook_table WHERE primary_key = :primaryKey")
	suspend fun get(primaryKey: String): NotebookDbEntry?

	@Query(value = "SELECT * FROM notebook_table ORDER BY created_timestamp DESC")
	fun getAllAsLiveData() : LiveData<List<NotebookDbEntry>>

	@Query(value = "SELECT * FROM notebook_table ORDER BY created_timestamp DESC")
	suspend fun getAll() : List<NotebookDbEntry>

	@Query(value = "DELETE FROM notebook_table WHERE primary_key = :primaryKey")
	suspend fun delete(primaryKey: String)

	@Delete
	suspend fun delete(diaryEntries: List<NotebookDbEntry>)

	//	TODO REMOVE THIS
	@Query(value = "DELETE FROM notebook_table")
	suspend fun deleteAll()


//    @Query(value = "SELECT * FROM diary_entry_table ORDER BY modified_timestamp DESC")
//    fun getAll(): LiveData<List<DiaryEntry>>
}
