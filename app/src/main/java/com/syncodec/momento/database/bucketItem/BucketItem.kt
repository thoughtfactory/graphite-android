package com.syncodec.momento.database.bucketItem

data class BucketItem(
	val key: String,
	val thoughtList: MutableList<String>,
	val extra: Any?,
)
