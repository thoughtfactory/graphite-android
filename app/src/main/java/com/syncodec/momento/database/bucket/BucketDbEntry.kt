package com.syncodec.momento.database.bucket

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = "bucket_table")
data class BucketDbEntry(
	@PrimaryKey(autoGenerate = false)
	@ColumnInfo(name = "key")
	val key: String,

	@ColumnInfo(name = "bucket_type")
	val bucketType: Int
) {
	@ColumnInfo(name = "created_timestamp")
	var createdTimestamp: Long = -1

	@ColumnInfo(name = "modified_timestamp")
	var modifiedTimestamp: Long = -1

	@ColumnInfo(name = "content_thumbnail")
	var contentThumbnail: String? = null

	@ColumnInfo(name = "title")
	lateinit var title: String

	@ColumnInfo(name = "bucket_size")
	var containerSize: Int = 0

	@ColumnInfo(name = "is_favourite")
	var isFavourite: Boolean = false

	@ColumnInfo(name = "is_archived")
	var isArchived: Boolean = false

	@ColumnInfo(name = "is_locked")
	var isLocked: Boolean = false

	@ColumnInfo(name = "g_drive_file_id")
	var gDriveFileId: String? = null

	@ColumnInfo(name = "hash")
	var hash: Long? = null

	override fun hashCode(): Int {
		return key.toInt()
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as BucketDbEntry

		if (key != other.key) return false

		return true
	}
}
