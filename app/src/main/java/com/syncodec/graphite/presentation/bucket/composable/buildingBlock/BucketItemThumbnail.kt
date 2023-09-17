package com.syncodec.graphite.presentation.bucket.composable.buildingBlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import io.realm.kotlin.types.RealmUUID


@Composable
fun BucketItemThumbnail(
	modifier: Modifier = Modifier,
	id: RealmUUID = RealmUUID.random(),
	thumbnail: String? = null,
) {
	val context = LocalContext.current

	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
			.clip(MaterialTheme.shapes.medium)
	) {
		SubcomposeAsyncImage(
			model = ImageRequest.Builder(context)
				.data(thumbnail?.decodeBase64ToBitmap())
				.diskCachePolicy(CachePolicy.ENABLED)
				.memoryCachePolicy(CachePolicy.ENABLED)
				.diskCacheKey(id.toString())
				.memoryCacheKey(id.toString())
				.build(),
			contentDescription = stringResource(id = R.string.thumbnail),
			contentScale = ContentScale.Crop,
			error = {
				Box(
					modifier = Modifier,
					contentAlignment = Alignment.Center
				) {
					Text(
						text = stringResource(id = R.string.thumbnail_not_found),
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f)
					)
				}
			},
			loading = { LoadingView() },
			modifier = Modifier.fillMaxSize()
		)
	}
}
