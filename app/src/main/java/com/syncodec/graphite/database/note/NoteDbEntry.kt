package com.syncodec.graphite.database.note

import android.graphics.Bitmap
import androidx.room.*
import com.google.android.gms.maps.model.LatLng

@Entity(
	tableName = "note_table",
	indices = [Index("key")]
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

	@ColumnInfo(name = "attachment_key_list")
	var attachmentKeyList: MutableList<String> = mutableListOf()

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



	override fun hashCode(): Int {
		var result = key.hashCode()
		result = 31 * result + timezone.hashCode()
		result = 31 * result + chapterPath.hashCode()
		result = 31 * result + notebookKey.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + userTimestamp.hashCode()
		result = 31 * result + (title?.hashCode() ?: 0)
		result = 31 * result + (contentThumbnail?.hashCode() ?: 0)
		result = 31 * result + (attachmentThumbnail?.hashCode() ?: 0)
		result = 31 * result + attachmentKeyList.hashCode()
		result = 31 * result + (latLng?.hashCode() ?: 0)
		result = 31 * result + (address?.hashCode() ?: 0)
		result = 31 * result + mood
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isArchived.hashCode()
		result = 31 * result + isLocked.hashCode()
		result = 31 * result + deletedTimestamp.hashCode()
		result = 31 * result + (gDriveFileId?.hashCode() ?: 0)
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as NoteDbEntry

		if (key != other.key) return false
		if (timezone != other.timezone) return false
		if (chapterPath != other.chapterPath) return false
		if (notebookKey != other.notebookKey) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false
		if (userTimestamp != other.userTimestamp) return false
		if (title != other.title) return false
		if (contentThumbnail != other.contentThumbnail) return false
		if (attachmentThumbnail != other.attachmentThumbnail) return false
		if (attachmentKeyList != other.attachmentKeyList) return false
		if (latLng != other.latLng) return false
		if (address != other.address) return false
		if (mood != other.mood) return false
		if (isFavourite != other.isFavourite) return false
		if (isArchived != other.isArchived) return false
		if (isLocked != other.isLocked) return false
		if (deletedTimestamp != other.deletedTimestamp) return false
		if (gDriveFileId != other.gDriveFileId) return false

		return true
	}
}
