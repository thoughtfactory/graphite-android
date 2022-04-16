package com.syncodec.momento.konstant

import androidx.compose.ui.graphics.vector.ImageVector
import com.syncodec.momento.R
import com.syncodec.momento.database.bucketItem.BucketItemType
import compose.icons.TablerIcons
import compose.icons.WeatherIcons
import compose.icons.tablericons.*
import compose.icons.weathericons.DaySunny

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
