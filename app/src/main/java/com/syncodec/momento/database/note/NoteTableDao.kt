package com.syncodec.momento.database.note

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface NoteTableDao {
	//    !!!   TODO    Is conflict strategy correct
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(noteDbEntry: NoteDbEntry)

	@Update
	fun update(noteDbEntry: NoteDbEntry)

	@Query(value = "SELECT * FROM note_table WHERE `key` = :key")
	suspend fun get(key: String): NoteDbEntry?

	@Query(value = "SELECT * FROM note_table ORDER BY user_timestamp DESC")
	fun getAllAsFlow(): Flow<List<NoteDbEntry>>

	@Query(value = "SELECT `key`, user_timestamp FROM note_table ORDER BY user_timestamp DESC")
	fun getAllForTimelineAsFlow(): Flow<List<NoteTimelineData>>

	@Query(value = "SELECT * FROM note_table WHERE notebook_key = :notebookKey ORDER BY user_timestamp DESC")
	fun getFromNotebookAsFlow(notebookKey: String): Flow<List<NoteDbEntry>>

	@Query(value = "SELECT `key` FROM note_table WHERE notebook_key = :notebookKey AND chapter_path = :chapterPath ORDER BY user_timestamp DESC")
	fun getAllKeyFromNotebookAsFlow(
		notebookKey: String,
		chapterPath: List<String>
	): Flow<List<String>>

	@Query(value = "SELECT `key` FROM note_table ORDER BY user_timestamp DESC")
	fun getAllKeyAsFlow(): Flow<List<String>>

	@Query(value = "SELECT `key` FROM note_table WHERE notebook_key = :notebookKey ORDER BY user_timestamp DESC")
	suspend fun getAllKeyFromNotebook(notebookKey: String): List<String>

	@Query(value = "SELECT `key` FROM note_table WHERE notebook_key IN (:notebookKeyList) ORDER BY user_timestamp DESC")
	suspend fun getAllKeyFromNotebook(notebookKeyList: List<String>): List<String>

	@Query(value = "SELECT COUNT(*) FROM note_table WHERE notebook_key = :notebookKey")
	suspend fun countNotebookSize(notebookKey: String): Int

	@Query(value = "DELETE FROM note_table WHERE `key` = :key")
	suspend fun delete(key: String)

	@Query("DELETE FROM note_table WHERE `key` IN (:keyList)")
	fun delete(keyList: List<String>)

	@Query("DELETE FROM note_table WHERE notebook_key IN (:keyList)")
	fun deleteWithNotebook(keyList: List<String>)
}
