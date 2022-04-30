package com.syncodec.graphite.database.bucket

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.syncodec.graphite.database.bucketItem.BucketItemType

@Entity(tableName = "bucket_table")
data class BucketDbEntry(
	@PrimaryKey(autoGenerate = false)
	@ColumnInfo(name = "key")
	val key: String,

	@ColumnInfo(name = "bucket_type")
	val bucketItemType: BucketItemType
) {
	@ColumnInfo(name = "created_timestamp")
	var createdTimestamp: Long = -1

	@ColumnInfo(name = "modified_timestamp")
	var modifiedTimestamp: Long = -1

	@ColumnInfo(name = "title")
	lateinit var title: String

	@ColumnInfo(name = "bucket_size")
	var bucketSize = 0

	@ColumnInfo(name = "is_favourite")
	var isFavourite = false

	@ColumnInfo(name = "is_archived")
	var isArchived = false

	@ColumnInfo(name = "is_locked")
	var isLocked = false

	@ColumnInfo(name = "g_drive_file_id")
	var gDriveFileId: String? = null

	@ColumnInfo(name = "hash")
	var hash: Long? = null

	override fun equals(other: Any?): Boolean {
//		if (this.hashCode() != other.hashCode()) return false
//		if (javaClass != other?.javaClass) return false
//
//		other as BucketDbEntry
//		if (key != other.key) return false
//		return true

		return false
	}

	override fun hashCode(): Int {
		var result = key.hashCode()
		result = 31 * result + bucketItemType.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + title.hashCode()
		result = 31 * result + (gDriveFileId?.hashCode() ?: 0)
		result = 31 * result + (hash?.hashCode() ?: 0)
		return result
	}
}
