package com.syncodec.graphite.di.model

import androidx.annotation.Keep
import androidx.room.PrimaryKey
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID


enum class BucketType {
	TODO,
	BOOK,
	SHOW,
	LINK,
	UNKNOWN
}


@Keep
class BucketObject: RealmObject {
	@PrimaryKey var id: RealmUUID = RealmUUID.random()

	var createdTimestamp: Long = System.currentTimeMillis()
	var modifiedTimestamp: Long = System.currentTimeMillis()
	var title: String? = null
	var description: String? = null
	var bucketType: String = BucketType.UNKNOWN.name
	var isFavourite: Boolean = false
	var isLocked: Boolean = false

	var bucketItemList: RealmList<BucketItemObject> = realmListOf()

	fun toSnapshot() = BucketSnapshot(
		id = this.id.toString(),
		createdTimestamp = this.createdTimestamp,
		modifiedTimestamp = this.modifiedTimestamp,
		title = this.title,
		description = this.description,
		bucketType = this.bucketType,
		isFavourite = this.isFavourite,
		isLocked = this.isLocked,
		bucketItemIdList = this.bucketItemList.map { it.id.toString() }
	)

	override fun hashCode(): Int {
		var result = id.hashCode()
		result = 31 * result + createdTimestamp.hashCode()
		result = 31 * result + modifiedTimestamp.hashCode()
		result = 31 * result + title.hashCode()
		result = 31 * result + (description?.hashCode() ?: 0)
		result = 31 * result + bucketType.hashCode()
		result = 31 * result + isFavourite.hashCode()
		result = 31 * result + isLocked.hashCode()
		result = 31 * result + bucketItemList.hashCode()
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
		if (bucketItemList != other.bucketItemList) return false

		return true
	}
}

@Keep
data class BucketSnapshot(
	val id: String,
	val createdTimestamp: Long,
	val modifiedTimestamp: Long,
	val title: String?,
	val description: String?,
	val bucketType: String,
	val isFavourite: Boolean,
	val isLocked: Boolean,
	val bucketItemIdList: List<String>
) {
	fun toObject() = BucketObject().apply {
		this.id = RealmUUID.from(this@BucketSnapshot.id)
		this.createdTimestamp = this@BucketSnapshot.createdTimestamp
		this.modifiedTimestamp = this@BucketSnapshot.modifiedTimestamp
		this.title = this@BucketSnapshot.title
		this.description = this@BucketSnapshot.description
		this.bucketType = this@BucketSnapshot.bucketType
		this.isFavourite = this@BucketSnapshot.isFavourite
		this.isLocked = this@BucketSnapshot.isLocked
	}
}
