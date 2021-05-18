package com.syncodec.momento.database.note

import androidx.lifecycle.LiveData
import androidx.room.*


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
	fun getAllAsLiveData() : LiveData<List<NoteDbEntry>>

	@Query(value = "SELECT * FROM note_table WHERE notebook_key = :notebookKey ORDER BY user_timestamp DESC")
	fun getFromNotebookAsLiveData(notebookKey: String) : LiveData<List<NoteDbEntry>>

	@Query(value = "SELECT `key` FROM note_table ORDER BY user_timestamp DESC")
	fun getKeyAsLiveData() : LiveData<List<String>>

	@Query(value = "SELECT * FROM note_table ORDER BY user_timestamp DESC")
	suspend fun getAll() : List<NoteDbEntry>

	@Query(value = "SELECT `key` FROM note_table ORDER BY user_timestamp DESC")
	suspend fun getAllKey() : List<String>

	@Query(value = "DELETE FROM note_table WHERE `key` = :key")
	suspend fun delete(key: String)

	@Delete
	suspend fun delete(noteDbEntryEntries: List<NoteDbEntry>)

//	TODO REMOVE THIS
	@Query(value = "DELETE FROM note_table")
	suspend fun deleteAll()


//    @Query(value = "SELECT * FROM diary_entry_table ORDER BY modified_timestamp DESC")
//    fun getAll(): LiveData<List<DiaryEntry>>
}
