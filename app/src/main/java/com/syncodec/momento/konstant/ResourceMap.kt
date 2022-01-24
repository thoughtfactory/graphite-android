package com.syncodec.momento.konstant

import androidx.compose.ui.graphics.vector.ImageVector
import com.syncodec.momento.database.bucket.BucketItemType
import compose.icons.TablerIcons
import compose.icons.WeatherIcons
import compose.icons.tablericons.*
import compose.icons.weathericons.DaySunny

class ResourceMap {
	companion object {
		val bucketTypeToIcon: Map<BucketItemType, ImageVector> = mapOf(
			BucketItemType.TODO to TablerIcons.ListCheck,
			BucketItemType.BOOKS to TablerIcons.Notebook,
			BucketItemType.MOVIES to TablerIcons.Movie,
			BucketItemType.TVSHOWS to TablerIcons.DeviceTv,
			BucketItemType.MEDIA to TablerIcons.AspectRatio,
			BucketItemType.LINKS to TablerIcons.Link,
		)

		val weatherCode: Map<String, ImageVector> = mapOf(
			"01d" to WeatherIcons.DaySunny
		)
	}
}
