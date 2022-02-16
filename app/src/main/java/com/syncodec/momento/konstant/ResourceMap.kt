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
			BucketItemType.Type.MOVIES to TablerIcons.Movie,
			BucketItemType.Type.TVSHOWS to TablerIcons.DeviceTv,
			BucketItemType.Type.MEDIA to TablerIcons.AspectRatio,
			BucketItemType.Type.LINKS to TablerIcons.Link,
		)

		val BucketItemNameMap: Map<BucketItemType.Type, String> = mapOf(
			BucketItemType.Type.TODO to "Todo",
			BucketItemType.Type.BOOKS to "Books",
			BucketItemType.Type.MOVIES to "Movies",
			BucketItemType.Type.TVSHOWS to "Tv shows",
			BucketItemType.Type.MEDIA to "Media",
			BucketItemType.Type.LINKS to "Links",
		)

		val weatherCode: Map<String, ImageVector> = mapOf(
			"01d" to WeatherIcons.DaySunny
		)
	}
}
