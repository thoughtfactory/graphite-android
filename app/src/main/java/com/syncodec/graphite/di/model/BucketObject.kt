package com.syncodec.graphite.di.model

import androidx.room.PrimaryKey
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject


enum class BucketType {
	TODO,
	BOOK,
	SHOW,
	LINK,
	UNKNOWN
}


class BucketObject: RealmObject {
	@PrimaryKey var id: ObjectId = ObjectId.create()

	var createdTimestamp: Long = System.currentTimeMillis()
	var modifiedTimestamp: Long = System.currentTimeMillis()
	var title: String = ""
	var description: String? = null
	var bucketType: String = ""
	var isFavourite: Boolean = false
	var isLocked: Boolean = false

	var bucketItemList: RealmList<BucketItemObject> = realmListOf()

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + title.hashCode()
		result = 31 * result + (description?.hashCode() ?: 0)
		result = 31 * result + bucketType.hashCode()
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isLocked.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is BucketObject) return false

		if (id != other.id) return false
		if (createdTimestamp != other.createdTimestamp) return false
		if (modifiedTimestamp != other.modifiedTimestamp) return false
		if (title != other.title) return false
		if (description != other.description) return false
		if (bucketType != other.bucketType) return false
		if (isFavourite != other.isFavourite) return false
		if (isLocked != other.isLocked) return false

		return true
	}
}
