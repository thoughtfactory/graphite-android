package com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock

import android.graphics.Bitmap
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest


@Preview
@Composable
fun BucketThumbnail(
	modifier: Modifier = Modifier,
	thumbnail : Bitmap? = null,
) {
	val context = LocalContext.current
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	SubcomposeAsyncImage(
		model = ImageRequest.Builder(context)
			.data(thumbnail)
			.crossfade(130)
			.build(),
		contentDescription = "Thumbnail",
		contentScale = ContentScale.Crop,
		modifier = modifier
			.width(screenWidth/2)
			.aspectRatio(0.6666f)
			.clip(MaterialTheme.shapes.extraLarge)
	)
}
