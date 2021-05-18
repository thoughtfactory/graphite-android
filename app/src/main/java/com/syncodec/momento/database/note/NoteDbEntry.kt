package com.syncodec.momento.database.note

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.Expose
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
	val timezoneOffset: Int,

	@ColumnInfo(name = "notebook_key")
	val notebookKey: String,

	@ColumnInfo(name = "chapter_path")
	val chapterPath: MutableList<String>
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

	@ColumnInfo(name = "attachment_thumbnail")
	@Expose
	var attachmentThumbnail: String? = null

	@ColumnInfo(name = "attachment_count")
	var attachmentCount: Int = 0

	@ColumnInfo(name = "latlng")
	var latLng: LatLng? = null

	@ColumnInfo(name = "address")
	var address: String? = null

	@ColumnInfo(name = "mood")
	var mood: Int = 0

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

	override fun hashCode(): Int {
		return key.hashCode()
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as NoteDbEntry

		if (key != other.key) return false

		return true
	}
}
