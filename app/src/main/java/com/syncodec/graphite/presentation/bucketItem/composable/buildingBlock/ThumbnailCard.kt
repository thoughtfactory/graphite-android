package com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock

import android.graphics.Bitmap
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.valentinilk.shimmer.shimmer


@Composable
fun ThumbnailCard(
	thumbnail: Bitmap?
) {
	val context = LocalContext.current
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	Crossfade(targetState = thumbnail) {
		if (it == null) {
			Box(
				modifier = Modifier
					.width(screenWidth * 0.5f)
					.aspectRatio(0.75f)
					.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
					.clip(RoundedCornerShape(24.dp))
					.shimmer()
			) {
				Box(modifier = Modifier.fillMaxSize())
			}
		} else {
			Box(
				modifier = Modifier
					.width(screenWidth * 0.5f)
					.aspectRatio(0.75f)
					.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
					.clip(RoundedCornerShape(24.dp))
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
	}
}

@Composable
fun ThumbnailShimmerCard() {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	Box(
		modifier = Modifier
			.width(screenWidth * 0.5f)
			.aspectRatio(0.75f)
			.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
			.clip(RoundedCornerShape(24.dp))
			.shimmer()
	)
}
