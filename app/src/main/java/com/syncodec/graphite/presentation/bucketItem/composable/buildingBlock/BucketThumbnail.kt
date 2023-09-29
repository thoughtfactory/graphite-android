package com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock

import android.graphics.Bitmap
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import io.realm.kotlin.types.RealmUUID


@Preview
@Composable
fun BucketThumbnail(
	modifier: Modifier = Modifier,
	key: String? = null,
	thumbnail: Bitmap? = null,
) {
	val context = LocalContext.current

	SubcomposeAsyncImage(
		model = ImageRequest.Builder(context)
			.data(thumbnail)
			.diskCachePolicy(CachePolicy.ENABLED)
			.memoryCachePolicy(CachePolicy.ENABLED)
			.diskCacheKey(key = key)
			.memoryCacheKey(key = key)
			.crossfade(130)
			.build(),
		contentDescription = "Thumbnail",
		contentScale = ContentScale.Crop,
		modifier = modifier
			.widthIn(96.dp, 256.dp)
			.aspectRatio(0.6666f)
			.clip(MaterialTheme.shapes.extraLarge)
	)
}
