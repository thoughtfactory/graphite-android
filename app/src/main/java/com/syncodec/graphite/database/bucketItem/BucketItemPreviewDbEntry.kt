package com.syncodec.graphite.database.bucketItem

import android.graphics.Bitmap
import androidx.room.ColumnInfo

data class BucketItemPreviewDbEntry(
	@ColumnInfo(name = "key")
	val key: String,

	@ColumnInfo(name = "created_timestamp")
	var createdTimestamp: Long = -1,

	@ColumnInfo(name = "modified_timestamp")
	var modifiedTimestamp: Long = -1,

	@ColumnInfo(name = "title")
	var title: String? = null,

	@ColumnInfo(name = "thumbnail", typeAffinity = ColumnInfo.BLOB)
	var thumbnail: Bitmap? = null,

	@ColumnInfo(name = "state")
	var state: BucketItemState = BucketItemState.ALPHA
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as BucketItemPreviewDbEntry

		if (key != other.key) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false
		if (title != other.title) return false
		if (thumbnail != other.thumbnail) return false
		if (state != other.state) return false

		return true
	}

	override fun hashCode(): Int {
		var result = key.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + (title?.hashCode() ?: 0)
		result = 31 * result + (thumbnail?.hashCode() ?: 0)
		result = 31 * result + state.hashCode()
		return result
	}
}
