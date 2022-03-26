package com.syncodec.momento.database.bucketItem

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "bucket_item_table")
data class BucketItemDbEntry(
	@PrimaryKey(autoGenerate = false)
	@ColumnInfo(name = "key")
	val key: String,

	@ColumnInfo(name = "bucket_key")
	val bucketKey: String,

	@ColumnInfo(name = "bucket_item_type")
	val bucketItemType: BucketItemType,

	@ColumnInfo(name = "created_timestamp")
	var createdTimestamp: Long = -1
) {
	@ColumnInfo(name = "modified_timestamp")
	var modifiedTimestamp: Long = -1

	@ColumnInfo(name = "title")
	var title: String? = null

	@ColumnInfo(name = "thumbnail", typeAffinity = ColumnInfo.BLOB)
	var thumbnail: Bitmap? = null

	@ColumnInfo(name = "state")
	var state: BucketItemState = BucketItemState.ALPHA

	@ColumnInfo(name = "latlng")
	var latLng: LatLng? = null

	@ColumnInfo(name = "address")
	var address: String? = null

//	@ColumnInfo(name = "tag_list")
//	val tagList: MutableList<TagDbEntry> = mutableListOf()

	@ColumnInfo(name = "g_drive_file_id")
	var gDriveFileId: String? = null

	@ColumnInfo(name = "hash")
	var hash: Long? = null

	override fun equals(other: Any?): Boolean {
		if (this.hashCode() != other.hashCode()) return false
		if (javaClass != other?.javaClass) return false

		other as BucketItemDbEntry
		if (key != other.key) return false
		return true
	}

	override fun hashCode(): Int {
		var result = key.hashCode()
		result = 31 * result + bucketKey.hashCode()
		result = 31 * result + bucketItemType.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + (title?.hashCode() ?: 0)
		result = 31 * result + (thumbnail?.hashCode() ?: 0)
		result = 31 * result + state.hashCode()
		result = 31 * result + (latLng?.hashCode() ?: 0)
		result = 31 * result + (address?.hashCode() ?: 0)
//		result = 31 * result + tagList.hashCode()
		result = 31 * result + (gDriveFileId?.hashCode() ?: 0)
		result = 31 * result + (hash?.hashCode() ?: 0)
		return result
	}

}
