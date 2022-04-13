package com.syncodec.momento.database.attachment

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentTableDao {
	//    !!!   TODO    Is conflict strategy correct
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(attachmentDbEntry: AttachmentDbEntry)

	@Update
	fun update(attachmentDbEntry: AttachmentDbEntry)

	@Query(value = "SELECT * FROM attachment_table WHERE `key` = :key")
	fun getAsFlow(key: String): Flow<AttachmentDbEntry?>

	@Query(value = "SELECT * FROM attachment_table WHERE note_key = :noteKey")
	fun getForNoteAsFlow(noteKey: String): Flow<List<AttachmentDbEntry>>

	@Query(value = "SELECT * FROM attachment_table WHERE note_key = :noteKey")
	suspend fun getForNote(noteKey: String): List<AttachmentDbEntry>

	@Query(value = "SELECT * FROM attachment_table WHERE notebook_key = :notebookKey ORDER BY created_timestamp DESC")
	fun getForNotebookAsFlow(notebookKey: String) : Flow<List<AttachmentDbEntry>>

	@Query(value = "SELECT * FROM attachment_table ORDER BY created_timestamp DESC")
	fun getAllAsFlow() : Flow<List<AttachmentDbEntry>>

	@Query(value = "DELETE FROM attachment_table WHERE `key` = :key")
	suspend fun delete(key: String)

	@Query(value = "DELETE FROM attachment_table WHERE `key` IN (:keyList)")
	suspend fun delete(keyList: List<String>)

	@Query(value = "DELETE FROM attachment_table WHERE note_key IN (:noteKeyList)")
	fun deleteWithNote(noteKeyList: List<String>)
}
