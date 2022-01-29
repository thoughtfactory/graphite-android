package com.syncodec.momento.database.bucket

import org.json.JSONObject
import kotlin.properties.Delegates

data class BucketItem(
	val primaryKey: String,
	val bucketKey: String,
	val itemType: BucketItemType,
	val createdTimestamp: Long
) {
	var modifiedTimestamp by Delegates.notNull<Long>()
	var isContentThumbnailAvailable: Boolean? = null
	var title: String? = null
	var contentList: MutableList<String> = mutableListOf()
	var innerContent: String? = null
	var rating: Int? = null
	var tag: MutableList<String> = mutableListOf()
	var isFavourite: Boolean = false
	var isArchived: Boolean = false
	var isLocked: Boolean = false
}
