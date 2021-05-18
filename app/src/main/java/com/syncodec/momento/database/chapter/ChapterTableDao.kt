package com.syncodec.momento.database.chapter

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ChapterTableDao {
	//    !!!   TODO    Is conflict strategy correct
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(chapterDbEntry: ChapterDbEntry)

	@Update
	fun update(chapterDbEntry: ChapterDbEntry)

	@Query(value = "SELECT * FROM chapter_table WHERE `key` = :key")
	suspend fun get(key: String): ChapterDbEntry?

	@Query(value = "SELECT * FROM chapter_table ORDER BY created_timestamp DESC")
	fun getAllAsLiveData() : LiveData<List<ChapterDbEntry>>

	@Query(value = "SELECT * FROM chapter_table WHERE notebook_key = :notebookKey ORDER BY created_timestamp DESC")
	fun getFromNotebookAsLiveData(notebookKey: String) : LiveData<List<ChapterDbEntry>>

	@Query(value = "DELETE FROM chapter_table WHERE `key` = :key")
	suspend fun delete(key: String)

	@Delete
	suspend fun delete(chapterDbEntries: List<ChapterDbEntry>)
}
