package com.syncodec.momento.database.media

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose


@Entity(tableName = "media_table")
data class Media(
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

	@ColumnInfo(name = "media_type")
	@Expose
	val mediaType: String

) {
	@ColumnInfo(name = "content_thumbnail")
	@Expose
	var contentThumbnail: String? = null

	@ColumnInfo(name = "g_drive_file_id")
	var gDriveFileId: String? = null

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Media

		if (primaryKey != other.primaryKey) return false

		return true
	}
}
