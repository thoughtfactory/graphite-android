package com.syncodec.momento.database.bucket

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = "bucket_item_table")
data class BucketItemDbEntry(
	@PrimaryKey(autoGenerate = false)
	@ColumnInfo(name = "primary_key")
	@Expose
	val primaryKey: String,

	@ColumnInfo(name = "bucket_key")
	@Expose
	val bucketKey: String,

	@ColumnInfo(name = "bucket_item_type")
	@Expose
	val bucketItemType: Int,

	@ColumnInfo(name = "created_timestamp")
	@Expose
	var createdTimestamp: Long = -1
) {
	@ColumnInfo(name = "modified_timestamp")
	@Expose
	var modifiedTimestamp: Long = -1

	@ColumnInfo(name = "title")
	@Expose
	var title: String? = null

	@ColumnInfo(name = "state")
	@Expose
	var state: Int = BucketItemState.ALPHA.ordinal

	@ColumnInfo(name = "latitude")
	@Expose
	var latitude: Double? = null

	@ColumnInfo(name = "longitude")
	@Expose
	var longitude: Double? = null

	@ColumnInfo(name = "address")
	@Expose
	var address: String? = null

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

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as BucketItemDbEntry

		if (primaryKey != other.primaryKey) return false

		return true
	}

	override fun hashCode(): Int {
		var result = primaryKey.hashCode()
		result = 31 * result + bucketKey.hashCode()
		result = 31 * result + bucketItemType
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + (title?.hashCode() ?: 0)
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isArchived.hashCode()
		result = 31 * result + isLocked.hashCode()
		result = 31 * result + (gDriveFileId?.hashCode() ?: 0)
		result = 31 * result + (hash?.hashCode() ?: 0)
		return result
	}
}

enum class BucketItemState {
	ALPHA,
	BETA,
	GAMMA
}
