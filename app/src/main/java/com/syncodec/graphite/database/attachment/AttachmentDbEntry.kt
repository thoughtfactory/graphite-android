package com.syncodec.graphite.database.attachment

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "attachment_table")
data class AttachmentDbEntry(
	@PrimaryKey
	@ColumnInfo(name = "key")
	val key: String,

	@ColumnInfo(name = "created_timestamp")
	val createdTimestamp: Long,

	@ColumnInfo(name = "mime_type")
	val mimeType: String?,

	@ColumnInfo(name = "extension")
	val extension: String?,

	@ColumnInfo(name = "note_key")
	val noteKey: String,

	@ColumnInfo(name = "chapter_path")
	val chapterPath: MutableList<String>,

	@ColumnInfo(name = "notebook_key")
	val notebookKey: String
) {
	@ColumnInfo(name = "g_drive_file_id")
	var gDriveFileId: String? = null

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as AttachmentDbEntry

		if (key != other.key) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (mimeType != other.mimeType) return false
		if (noteKey != other.noteKey) return false
		if (chapterPath != other.chapterPath) return false
		if (notebookKey != other.notebookKey) return false
		if (gDriveFileId != other.gDriveFileId) return false

		return true
	}

	override fun hashCode(): Int {
		var result = key.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + (mimeType?.hashCode() ?: 0)
		result = 31 * result + (extension?.hashCode() ?: 0)
		result = 31 * result + noteKey.hashCode()
		result = 31 * result + chapterPath.hashCode()
		result = 31 * result + notebookKey.hashCode()
		result = 31 * result + (gDriveFileId?.hashCode() ?: 0)
		return result
	}
}
