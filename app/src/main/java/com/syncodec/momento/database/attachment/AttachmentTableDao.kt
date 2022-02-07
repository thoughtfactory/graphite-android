package com.syncodec.momento.database.attachment

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AttachmentTableDao {
	//    !!!   TODO    Is conflict strategy correct
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(attachment: Attachment)

	@Query(value = "UPDATE attachment_table SET g_drive_file_id = :gDriveFileId WHERE primary_key = :primaryKey")
	fun addGDriveFileId(primaryKey: String, gDriveFileId: String)

	@Update
	fun update(attachment: Attachment)

	@Query(value = "SELECT * FROM attachment_table WHERE primary_key = :primaryKey")
	suspend fun get(primaryKey: String): Attachment?

	@Query(value = "SELECT * FROM attachment_table ORDER BY created_timestamp DESC")
	fun getAllAsLiveData() : LiveData<List<Attachment>>

	@Query(value = "SELECT * FROM attachment_table ORDER BY created_timestamp DESC")
	suspend fun getAll() : List<Attachment>

	@Query(value = "DELETE FROM attachment_table WHERE primary_key = :primaryKey")
	suspend fun delete(primaryKey: String)

	@Delete
	suspend fun delete(diaryEntries: List<Attachment>)

	//	TODO REMOVE THIS
	@Query(value = "DELETE FROM attachment_table")
	suspend fun deleteAll()


//    @Query(value = "SELECT * FROM diary_entry_table ORDER BY modified_timestamp DESC")
//    fun getAll(): LiveData<List<DiaryEntry>>
}
