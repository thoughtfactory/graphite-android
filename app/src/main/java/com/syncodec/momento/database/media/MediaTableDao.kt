package com.syncodec.momento.database.media

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MediaTableDao {
	//    !!!   TODO    Is conflict strategy correct
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(media: Media)

	@Query(value = "UPDATE media_table SET g_drive_file_id = :gDriveFileId WHERE primary_key = :primaryKey")
	fun addGDriveFileId(primaryKey: String, gDriveFileId: String)

	@Update
	fun update(media: Media)

	@Query(value = "SELECT * FROM media_table WHERE primary_key = :primaryKey")
	suspend fun get(primaryKey: String): Media?

	@Query(value = "SELECT * FROM media_table ORDER BY created_timestamp DESC")
	fun getAllAsLiveData() : LiveData<List<Media>>

	@Query(value = "SELECT * FROM media_table ORDER BY created_timestamp DESC")
	suspend fun getAll() : List<Media>

	@Query(value = "DELETE FROM media_table WHERE primary_key = :primaryKey")
	suspend fun delete(primaryKey: String)

	@Delete
	suspend fun delete(diaryEntries: List<Media>)

	//	TODO REMOVE THIS
	@Query(value = "DELETE FROM media_table")
	suspend fun deleteAll()


//    @Query(value = "SELECT * FROM diary_entry_table ORDER BY modified_timestamp DESC")
//    fun getAll(): LiveData<List<DiaryEntry>>
}
