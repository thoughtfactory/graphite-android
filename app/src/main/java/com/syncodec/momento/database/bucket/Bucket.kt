package com.syncodec.momento.database.bucket

import kotlin.properties.Delegates

data class Bucket(
	val primaryKey: String,
	val bucketType: BucketItemType
) {
	var createdTimestamp by Delegates.notNull<Long>()
	var modifiedTimestamp by Delegates.notNull<Long>()
	var contentThumbnail: String? = null
	lateinit var title: String
	var bucketItemKeyList: MutableSet<String> = mutableSetOf()
	var containerSize: Int = bucketItemKeyList.size
	var isFavourite: Boolean = false
	var isArchived: Boolean = false
	var isLocked: Boolean = false
}
