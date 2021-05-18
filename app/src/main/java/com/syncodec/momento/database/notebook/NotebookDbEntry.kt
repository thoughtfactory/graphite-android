package com.syncodec.momento.database.notebook

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.syncodec.momento.database.note.Note

@Entity(tableName = "notebook_table")
data class NotebookDbEntry(
	@PrimaryKey
	@ColumnInfo(name = "key")
	val key: String,

	@ColumnInfo(name =  "created_timestamp")
	val createdTimestamp: Long,
) {
	@ColumnInfo(name =  "modified_timestamp")
	var modifiedTimestamp: Long = System.currentTimeMillis()

	@ColumnInfo(name = "title")
	lateinit var title: String

	@ColumnInfo(name = "description")
	var description: String? = null

	@ColumnInfo(name = "color")
	var color: Int? = null

	@ColumnInfo(name = "isFavourite")
	var isFavourite: Boolean = false

	@ColumnInfo(name = "isArchived")
	var isArchived: Boolean = false

	@ColumnInfo(name = "isLocked")
	var isLocked: Boolean = false

	@ColumnInfo(name = "g_drive_file_id")
	var gDriveFileId: String? = null

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as NotebookDbEntry

		if (key != other.key) return false

		return true
	}

	override fun hashCode(): Int {
		return key.hashCode()
	}
}
