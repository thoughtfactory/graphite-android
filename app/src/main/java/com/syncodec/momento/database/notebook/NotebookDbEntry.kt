package com.syncodec.momento.database.notebook

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = "notebook_table")
data class NotebookDbEntry(
	@PrimaryKey
	@ColumnInfo(name = "primary_key")
	@Expose
	val primaryKey: String,

	@ColumnInfo(name =  "created_timestamp")
	@Expose
	val createdTimestamp: Long,
) {
	@ColumnInfo(name =  "modified_timestamp")
	@Expose
	var modifiedTimestamp: Long = System.currentTimeMillis()

	@ColumnInfo(name = "title")
	@Expose
	lateinit var title: String

	@ColumnInfo(name = "description")
	@Expose
	var description: String? = null

	@ColumnInfo(name = "color")
	@Expose
	var color: Int? = null

	@ColumnInfo(name =  "number_chapter")
	var numberChapter: Int = 0

	@ColumnInfo(name =  "number_note")
	var numberNote: Int = 0

	@ColumnInfo(name =  "number_attachment")
	var numberAttachment: Int = 0

	@ColumnInfo(name = "g_drive_file_id")
	var gDriveFileId: String? = null

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as NotebookDbEntry

		if (primaryKey != other.primaryKey) return false

		return true
	}

	override fun hashCode(): Int {
		var result = primaryKey.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + title.hashCode()
		result = 31 * result + (description?.hashCode() ?: 0)
		result = 31 * result + (color?.hashCode() ?: 0)
		result = 31 * result + numberChapter
		result = 31 * result + numberNote
		result = 31 * result + numberAttachment
		result = 31 * result + (gDriveFileId?.hashCode() ?: 0)
		return result
	}
}
