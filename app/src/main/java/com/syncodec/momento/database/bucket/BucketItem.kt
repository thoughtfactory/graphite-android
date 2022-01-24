package com.syncodec.momento.database.bucket

import androidx.room.PrimaryKey
import kotlin.properties.Delegates

data class BucketItem(
	val primaryKey: String,
	val bucketKey: String,
	val itemType: BucketItemType
) {
	var createdTimestamp by Delegates.notNull<Long>()
	var modifiedTimestamp by Delegates.notNull<Long>()
	var contentThumbnail: String? = null
	var title: String? = null
	var content: String? = null
	var rating: Int? = null
	var tag: List<String> = mutableListOf()
	var isFavourite: Boolean = false
	var isArchived: Boolean = false
	var isLocked: Boolean = false
}
