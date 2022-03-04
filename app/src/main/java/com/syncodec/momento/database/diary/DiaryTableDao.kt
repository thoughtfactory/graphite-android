package com.syncodec.momento.database.diary

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface DiaryTableDao {
	//    !!!   TODO    Is conflict strategy correct
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(diaryDbEntry: DiaryDbEntry)

	@Query(value = "UPDATE diary_table SET g_drive_file_id = :gDriveFileId WHERE primary_key = :primaryKey")
	fun addGDriveFileId(primaryKey: String, gDriveFileId: String)

	@Update
	fun update(diaryDbEntry: DiaryDbEntry)

	@Query(value = "SELECT * FROM diary_table WHERE primary_key = :primaryKey")
	suspend fun get(primaryKey: String): DiaryDbEntry?

	@Query(value = "SELECT * FROM diary_table ORDER BY user_timestamp DESC")
	fun getAllAsLiveData() : LiveData<List<DiaryDbEntry>>

	@Query(value = "SELECT primary_key FROM diary_table ORDER BY user_timestamp DESC")
	fun getKeyAsLiveData() : LiveData<List<String>>

	@Query(value = "SELECT * FROM diary_table ORDER BY user_timestamp DESC")
	suspend fun getAll() : List<DiaryDbEntry>

	@Query(value = "SELECT primary_key FROM diary_table ORDER BY user_timestamp DESC")
	suspend fun getAllKey() : List<String>

	@Query(value = "DELETE FROM diary_table WHERE primary_key = :primaryKey")
	suspend fun delete(primaryKey: String)

	@Delete
	suspend fun delete(diaryDbEntryEntries: List<DiaryDbEntry>)

//	TODO REMOVE THIS
	@Query(value = "DELETE FROM diary_table")
	suspend fun deleteAll()


//    @Query(value = "SELECT * FROM diary_entry_table ORDER BY modified_timestamp DESC")
//    fun getAll(): LiveData<List<DiaryEntry>>
}
