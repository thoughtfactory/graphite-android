package com.syncodec.graphite.bucketItemComponent.miscellaneous

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest


@Composable
fun ThumbnailCard(
	thumbnail: Any?
) {
	val context = LocalContext.current
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	Card(
		elevation = 8.dp,
		shape = RoundedCornerShape(12.dp),
		modifier = Modifier
			.width(screenWidth * 0.5f)
			.aspectRatio(0.75f)
			.padding(0.dp),
	) {
		AsyncImage(
			model = ImageRequest.Builder(context)
				.data(thumbnail)
				.crossfade(300)
				.build(),
			placeholder = null,
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier.fillMaxSize(),
		)
	}
}
