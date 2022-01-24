package com.syncodec.momento.database.bucket

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.syncodec.momento.database.diary.DiaryDbEntry

@Entity(tableName = "bucket_table")
data class BucketDbEntry(
	@PrimaryKey(autoGenerate = false)
	@ColumnInfo(name = "primary_key")
	@Expose
	val primaryKey: String,

	@ColumnInfo(name = "bucket_type")
	@Expose
	val bucketType: Int
) {
	@ColumnInfo(name = "created_timestamp")
	@Expose
	var createdTimestamp: Long = -1

	@ColumnInfo(name = "modified_timestamp")
	@Expose
	var modifiedTimestamp: Long = -1

	@ColumnInfo(name = "content_thumbnail")
	@Expose
	var contentThumbnail: String? = null

	@ColumnInfo(name = "title")
	@Expose
	lateinit var title: String

	@ColumnInfo(name = "bucket_size")
	@Expose
	var containerSize: Int = 0

	@ColumnInfo(name = "is_favourite")
	@Expose
	var isFavourite: Boolean = false

	@ColumnInfo(name = "is_archived")
	@Expose
	var isArchived: Boolean = false

	@ColumnInfo(name = "is_locked")
	@Expose
	var isLocked: Boolean = false

	@ColumnInfo(name = "g_drive_file_id")
	var gDriveFileId: String? = null

	@Expose
	@ColumnInfo(name = "hash")
	var hash: Long? = null

	override fun hashCode(): Int {
		return primaryKey.toInt()
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as BucketDbEntry

		if (primaryKey != other.primaryKey) return false

		return true
	}
}
