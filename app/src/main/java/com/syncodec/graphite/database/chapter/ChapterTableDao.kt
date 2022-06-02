package com.syncodec.graphite.database.chapter

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterTableDao {
	//    !!!   TODO    Is conflict strategy correct
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(chapterDbEntry: ChapterDbEntry)

	@Update
	fun update(chapterDbEntry: ChapterDbEntry)

	@Query(value = "SELECT * FROM chapter_table WHERE `key` = :key")
	suspend fun get(key: String): ChapterDbEntry?

	@Query(value = "SELECT * FROM chapter_table WHERE notebook_key = :notebookKey ORDER BY created_timestamp DESC")
	fun getFromNotebookAsFlow(notebookKey: String) : Flow<List<ChapterDbEntry>>

	@Query(value = "DELETE FROM chapter_table WHERE `key` = :key")
	suspend fun delete(key: String)

	@Query("DELETE FROM chapter_table WHERE `key` IN (:keyList)")
	fun delete(keyList: List<String>)

	@Delete
	suspend fun delete(chapterDbEntries: List<ChapterDbEntry>)
}
