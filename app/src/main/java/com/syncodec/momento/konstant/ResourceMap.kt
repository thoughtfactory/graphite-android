package com.syncodec.momento.konstant

import androidx.compose.ui.graphics.vector.ImageVector
import com.syncodec.momento.database.bucket.BucketItemType
import compose.icons.TablerIcons
import compose.icons.WeatherIcons
import compose.icons.tablericons.*
import compose.icons.weathericons.DaySunny

class ResourceMap {
	companion object {
		val bucketTypeToIcon: Map<BucketItemType.Type, ImageVector> = mapOf(
			BucketItemType.Type.TODO to TablerIcons.ListCheck,
			BucketItemType.Type.BOOKS to TablerIcons.Notebook,
			BucketItemType.Type.SHOWS to TablerIcons.DeviceTv,
			BucketItemType.Type.MEDIA to TablerIcons.AspectRatio,
			BucketItemType.Type.LINKS to TablerIcons.Link,
		)

		val BucketItemNameMap: Map<BucketItemType.Type, String> = mapOf(
			BucketItemType.Type.TODO to "Todo",
			BucketItemType.Type.BOOKS to "Books",
			BucketItemType.Type.SHOWS to "Movies / Series",
			BucketItemType.Type.MEDIA to "Media",
			BucketItemType.Type.LINKS to "Links",
		)

		val weatherCode: Map<String, ImageVector> = mapOf(
			"01d" to WeatherIcons.DaySunny
		)
	}
}
