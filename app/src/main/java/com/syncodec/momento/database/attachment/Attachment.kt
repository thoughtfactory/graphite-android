package com.syncodec.momento.database.attachment

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose


@Entity(tableName = "attachment_table")
data class Attachment(
	@PrimaryKey
	@ColumnInfo(name = "primary_key")
	@Expose
	val primaryKey: String,

	@ColumnInfo(name = "created_timestamp")
	@Expose
	val createdTimestamp: Long,

	@ColumnInfo(name = "timezone_offset")
	@Expose
	val timezoneOffset: Int,

	@ColumnInfo(name = "mime_type")
	@Expose
	val mimeType: String?,

	@ColumnInfo(name = "note_primary_key")
	@Expose
	val notePrimaryKey: String
) {
	@ColumnInfo(name = "content_thumbnail")
	@Expose
	var contentThumbnail: String? = null

	@ColumnInfo(name = "g_drive_file_id")
	var gDriveFileId: String? = null

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Attachment

		if (primaryKey != other.primaryKey) return false

		return true
	}

	override fun hashCode(): Int {
		var result = primaryKey.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + timezoneOffset
		result = 31 * result + mimeType.hashCode()
		result = 31 * result + (contentThumbnail?.hashCode() ?: 0)
		result = 31 * result + (gDriveFileId?.hashCode() ?: 0)
		return result
	}
}
