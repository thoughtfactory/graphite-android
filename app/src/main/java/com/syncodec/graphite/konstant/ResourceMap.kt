package com.syncodec.graphite.konstant

import com.syncodec.graphite.R
import com.syncodec.graphite.database.bucketItem.BucketItemType

class ResourceMap {
	companion object {
		val bucketTypeToIcon: Map<BucketItemType, Int> = mapOf(
			BucketItemType.TODO to R.drawable.ic_todo,
			BucketItemType.BOOK to R.drawable.ic_book,
			BucketItemType.SHOW to R.drawable.ic_show,
			BucketItemType.LINK to R.drawable.ic_link,
		)

		val BucketItemNameMap: Map<BucketItemType, String> = mapOf(
			BucketItemType.TODO to "Todo",
			BucketItemType.BOOK to "Books",
			BucketItemType.SHOW to "Movies / Series",
			BucketItemType.LINK to "Link",
		)
	}
}
