package com.syncodec.momento.database.chapter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.syncodec.momento.database.notebook.NotebookDbEntry

@Entity(
	tableName = "chapter_table", foreignKeys = [
		ForeignKey(
			entity = NotebookDbEntry::class,
			parentColumns = ["key"],
			childColumns = ["notebook_key"],
			onDelete = ForeignKey.NO_ACTION
		)
	]
)
data class ChapterDbEntry(
	@PrimaryKey(autoGenerate = false)
	@ColumnInfo(name = "key")
	val key: String,

	@ColumnInfo(name = "notebook_key")
	val notebookKey: String,

	@ColumnInfo(name = "chapter_path")
	val chapterPath: MutableList<String>
) {

	@ColumnInfo(name = "created_timestamp")
	var createdTimestamp: Long = -1

	@ColumnInfo(name = "modified_timestamp")
	var modifiedTimestamp: Long = -1

	@ColumnInfo(name = "title")
	lateinit var title: String

	@ColumnInfo(name = "description")
	var description: String? = null

	@ColumnInfo(name = "is_favourite")
	var isFavourite: Boolean = false

	@ColumnInfo(name = "is_archived")
	var isArchived: Boolean = false

	@ColumnInfo(name = "is_locked")
	var isLocked: Boolean = false

	@ColumnInfo(name = "deleted_timestamp")
	var deletedTimestamp: Long = -1

	@ColumnInfo(name = "g_drive_file_id")
	var gDriveFileId: String? = null

	@ColumnInfo(name = "hash")
	var hash: Long? = null
}
