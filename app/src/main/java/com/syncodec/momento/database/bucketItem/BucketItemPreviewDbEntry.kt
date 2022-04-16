package com.syncodec.momento.database.bucketItem

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

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
)
