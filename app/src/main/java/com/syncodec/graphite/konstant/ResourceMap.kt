package com.syncodec.graphite.konstant

import com.syncodec.graphite.R
import com.syncodec.graphite.database.bucketItem.BucketItemType
import compose.icons.tablericons.*

class ResourceMap {
	companion object {
		val bucketTypeToIcon: Map<BucketItemType, Int> = mapOf(
			BucketItemType.TODO to R.drawable.ic_todo,
			BucketItemType.BOOKS to R.drawable.ic_book,
			BucketItemType.SHOWS to R.drawable.ic_show,
		)

		val BucketItemNameMap: Map<BucketItemType, String> = mapOf(
			BucketItemType.TODO to "Todo",
			BucketItemType.BOOKS to "Books",
			BucketItemType.SHOWS to "Movies / Series",
		)
	}
}
