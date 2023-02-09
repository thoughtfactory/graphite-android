package com.syncodec.graphite.di.model

import androidx.annotation.Keep
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey


enum class BucketType {
	TODO,
	BOOK,
	SHOW,
	LINK,
	UNKNOWN
}


@Keep
class BucketObject: RealmObject {
	@PrimaryKey
	var id: RealmUUID = RealmUUID.random()

	var createdTimestamp: Long = System.currentTimeMillis()
	var modifiedTimestamp: Long = System.currentTimeMillis()
	var title: String? = null
	var description: String? = null
	var bucketType: String = BucketType.UNKNOWN.name
	var isFavourite: Boolean = false
	var isLocked: Boolean = false

	var bucketItemList: RealmList<BucketItemObject> = realmListOf()

	fun clone() : BucketObject = BucketObject().apply {
		this.id = this@BucketObject.id
		this.createdTimestamp = this@BucketObject.createdTimestamp
		this.modifiedTimestamp = this@BucketObject.modifiedTimestamp
		this.title = this@BucketObject.title
		this.description = this@BucketObject.description
		this.bucketType = this@BucketObject.bucketType
		this.isFavourite = this@BucketObject.isFavourite
		this.isLocked = this@BucketObject.isLocked
		this.bucketItemList = this@BucketObject.bucketItemList
	}

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
