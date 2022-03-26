package com.syncodec.momento.database.bucket

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bucket_table")
data class BucketDbEntry(
	@PrimaryKey(autoGenerate = false)
	@ColumnInfo(name = "key")
	val key: String,

	@ColumnInfo(name = "bucket_type")
	val bucketItemType: Int
) {
	@ColumnInfo(name = "created_timestamp")
	var createdTimestamp: Long = -1

	@ColumnInfo(name = "modified_timestamp")
	var modifiedTimestamp: Long = -1

	@ColumnInfo(name = "title")
	lateinit var title: String

//	@ColumnInfo(name = "tag_list")
//	val tagList: MutableList<TagDbEntry> = mutableListOf()

	@ColumnInfo(name = "g_drive_file_id")
	var gDriveFileId: String? = null

	@ColumnInfo(name = "hash")
	var hash: Long? = null

	override fun equals(other: Any?): Boolean {
		if (this.hashCode() != other.hashCode()) return false
		if (javaClass != other?.javaClass) return false

		other as BucketDbEntry
		if (key != other.key) return false
		return true
	}

	override fun hashCode(): Int {
		var result = key.hashCode()
		result = 31 * result + bucketItemType
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + title.hashCode()
		result = 31 * result + (gDriveFileId?.hashCode() ?: 0)
		result = 31 * result + (hash?.hashCode() ?: 0)
		return result
	}
}
