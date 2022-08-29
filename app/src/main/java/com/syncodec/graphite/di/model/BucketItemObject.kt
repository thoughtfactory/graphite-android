package com.syncodec.graphite.di.model

import androidx.room.PrimaryKey
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmObject


enum class BucketItemState {
	ALPHA,
	BETA,
	GAMMA
}

class BucketItemObject: RealmObject {
	@PrimaryKey var id: ObjectId = ObjectId.create()

	var createdTimestamp: Long = 0
	var modifiedTimestamp: Long = 0
	var bucketType: String = ""
	var title: String = ""
	var state: String = ""
	var thumbnail: String? = null
	var item: String? = null
	var isFavourite: Boolean = false
	var isLocked: Boolean = false


	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + bucketType.hashCode()
		result = 31 * result + title.hashCode()
		result = 31 * result + state.hashCode()
		result = 31 * result + (item?.hashCode() ?: 0)
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isLocked.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is BucketItemObject) return false

		if (id != other.id) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false
		if (bucketType != other.bucketType) return false
		if (title != other.title) return false
		if (state != other.state) return false
		if (item != other.item) return false
		if (isFavourite != other.isFavourite) return false
		if (isLocked != other.isLocked) return false

		return true
	}
}



data class BucketItemPreview(
	val id: String,
	val createdTimestamp: String,
	val modifiedTimestamp: String,
	val bucketId: String,
	val bucketType: String,
	val title: String,
	val state: String,
	val thumbnail: String,
	val isFavourite: Boolean,
	val isLocked: Boolean
)
