package com.syncodec.graphite.presentation.bucket.composable.buildingBlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import io.realm.kotlin.types.RealmUUID


@Composable
fun BucketItemGridThumbnail(
	modifier: Modifier = Modifier,
	id: RealmUUID = RealmUUID.random(),
	thumbnail: String? = null,
	onErrorIcon : Int = R.drawable.ic_fa_bucket_book,
) {
	val context = LocalContext.current

	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.47f), MaterialTheme.shapes.medium)
			.clip(MaterialTheme.shapes.medium)
	) {
		SubcomposeAsyncImage(
			model = ImageRequest.Builder(context)
				.data(thumbnail?.decodeBase64ToBitmap())
				.diskCachePolicy(CachePolicy.ENABLED)
				.memoryCachePolicy(CachePolicy.ENABLED)
				.diskCacheKey(id.toString())
				.memoryCacheKey(id.toString())
				.crossfade(ANIMATION_DURATION_MILLIS)
				.build(),
			contentDescription = stringResource(id = R.string.thumbnail),
			loading = { LoadingView() },
			error = {
				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier.fillMaxSize()
				) {
					Icon(
						painter = painterResource(id = onErrorIcon),
						contentDescription = stringResource(id = R.string.thumbnail),
						tint = MaterialTheme.colorScheme.onSurface,
						modifier = Modifier.requiredSize(32.dp)
					)
				}
			},
			contentScale = ContentScale.Crop,
			modifier = Modifier.fillMaxSize()
		)
	}
}

@Preview
@Composable
fun BucketItemListThumbnail(
	modifier: Modifier = Modifier,
	id: RealmUUID = RealmUUID.random(),
	thumbnail: String? = null,
	onErrorIcon : Int = R.drawable.ic_fa_bucket_book,
	containerColor : Color = MaterialTheme.colorScheme.surface,
	contentColor : Color = MaterialTheme.colorScheme.onSurface,
) {
	val context = LocalContext.current

	SubcomposeAsyncImage(
		model = ImageRequest.Builder(context)
			.data(thumbnail?.decodeBase64ToBitmap())
			.diskCachePolicy(CachePolicy.ENABLED)
			.memoryCachePolicy(CachePolicy.ENABLED)
			.diskCacheKey(id.toString())
			.memoryCacheKey(id.toString())
			.crossfade(ANIMATION_DURATION_MILLIS)
			.build(),
		contentDescription = stringResource(id = R.string.thumbnail),
		loading = { LoadingView() },
		error = {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.fillMaxSize()
					.background(containerColor)
			) {
				Icon(
					painter = painterResource(id = onErrorIcon),
					contentDescription = stringResource(id = R.string.thumbnail),
					tint = contentColor,
					modifier = Modifier.requiredSize(32.dp)
				)
			}
		},
		contentScale = ContentScale.Crop,
		modifier = modifier
			.width(72.dp)
			.aspectRatio(0.6666f)
			.clip(MaterialTheme.shapes.medium)
	)
}
