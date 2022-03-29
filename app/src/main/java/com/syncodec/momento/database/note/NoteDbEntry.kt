package com.syncodec.momento.database.note

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.syncodec.momento.database.notebook.NotebookDbEntry

@Entity(
	tableName = "note_table",
	foreignKeys = [
		ForeignKey(
			entity = NotebookDbEntry::class,
			parentColumns = ["key"],
			childColumns = ["notebook_key"],
			onDelete = ForeignKey.NO_ACTION
		)
	]
)
data class NoteDbEntry(
	@PrimaryKey(autoGenerate = false)
	@ColumnInfo(name = "key")
	val key: String,

	@ColumnInfo(name = "timezone_offset")
	val timezone: String,

	@ColumnInfo(name = "chapter_path")
	val chapterPath: MutableList<String>,

	@ColumnInfo(name = "notebook_key")
	val notebookKey: String,
) {
	@ColumnInfo(name = "created_timestamp")
	var createdTimestamp: Long = -1

	@ColumnInfo(name = "modified_timestamp")
	var modifiedTimestamp: Long = -1

	@ColumnInfo(name = "user_timestamp")
	var userTimestamp: Long = -1

	@ColumnInfo(name = "title")
	var title: String? = null

	@ColumnInfo(name = "content_thumbnail")
	var contentThumbnail: String? = null

	@ColumnInfo(name = "attachment_thumbnail", typeAffinity = ColumnInfo.BLOB)
	var attachmentThumbnail: Bitmap? = null

	@ColumnInfo(name = "attachment_count")
	var attachmentCount: Int = 0

	@ColumnInfo(name = "location_data")
	var location: LocationData? = null

	@ColumnInfo(name = "address")
	var address: String? = null

	@ColumnInfo(name = "mood")
	var mood: Int = 0

	@ColumnInfo(name = "deleted_timestamp")
	var deletedTimestamp: Long = -1

	@ColumnInfo(name = "g_drive_file_id")
	var gDriveFileId: String? = null

	override fun hashCode(): Int {
		var result = key.hashCode()
		result = 31 * result + timezone.hashCode()
		result = 31 * result + notebookKey.hashCode()
		result = 31 * result + chapterPath.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + userTimestamp.hashCode()
		result = 31 * result + (title?.hashCode() ?: 0)
		result = 31 * result + (contentThumbnail?.hashCode() ?: 0)
		result = 31 * result + (attachmentThumbnail?.hashCode() ?: 0)
		result = 31 * result + attachmentCount
		result = 31 * result + (location?.hashCode() ?: 0)
		result = 31 * result + (address?.hashCode() ?: 0)
		result = 31 * result + mood
		result = 31 * result + deletedTimestamp.hashCode()
		result = 31 * result + (gDriveFileId?.hashCode() ?: 0)
		return result
	}

	override fun equals(other: Any?): Boolean {
		return false
	}

//	override fun equals(other: Any?): Boolean {
//		if (this.hashCode() != other.hashCode()) return false
//		if (javaClass != other?.javaClass) return false
//
//		other as NoteDbEntry
//
//		if (key != other.key) return false
//
//		return true
//	}
}
