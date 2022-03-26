package com.syncodec.momento.database.attachment

import android.content.Context
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

	@ColumnInfo(name = "timezone_offset")
	val timezoneOffset: Int,

	@ColumnInfo(name = "mime_type")
	val mimeType: String?,

	@ColumnInfo(name = "note_key")
	val noteKey: String
) {
	@ColumnInfo(name = "g_drive_file_id")
	var gDriveFileId: String? = null

	override fun equals(other: Any?): Boolean {
		if (this.hashCode() != other.hashCode()) return false
		if (javaClass != other?.javaClass) return false

		other as AttachmentDbEntry

		if (key != other.key) return false

		return true
	}

	override fun hashCode(): Int {
		var result = key.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + timezoneOffset
		result = 31 * result + mimeType.hashCode()
		result = 31 * result + (gDriveFileId?.hashCode() ?: 0)
		return result
	}
}

fun AttachmentDbEntry.getMimeType() = mimeType?.split("/")?.firstOrNull()
