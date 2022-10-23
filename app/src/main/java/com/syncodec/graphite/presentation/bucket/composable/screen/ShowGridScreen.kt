package com.syncodec.graphite.presentation.bucket.composable.screen

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.BucketActivity
import com.syncodec.graphite.presentation.bucketItem.BucketItemActivity
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.decodeBase64ToBitmap


@Composable
fun ShowGridScreen(
	bucketObject: BucketObject,
	viewState: Int
) {
	val activity: BucketActivity = LocalContext.current as BucketActivity

	LazyVerticalGrid(
		columns = GridCells.Adaptive(128.dp),
		horizontalArrangement = Arrangement.Center,
		modifier = Modifier
			.fillMaxSize()
			.padding(4.dp, 0.dp),
	) {
		bucketObject.bucketItemList.filter { it.state == BucketItemState.values()[viewState].name }?.forEach { bucketItemObject ->
			item {
				GridItem(
					title = bucketItemObject.title,
					thumbnail = bucketItemObject.thumbnail?.decodeBase64ToBitmap(),
					highlight = false,
					onLongClick = { /*TODO*/ }
				) {
					Intent(activity, BucketItemActivity::class.java).apply {
						putExtra(Extra.Companion.Constant.IS_NEW.name, false)
						putExtra(Extra.Companion.Constant.BUCKET_ID.name, bucketObject.id.toString())
						putExtra(Extra.Companion.Constant.BUCKET_TYPE.name, BucketType.SHOW.name)
						putExtra(Extra.Companion.Constant.BUCKET_ITEM_ID.name, bucketItemObject.id.toString())

						activity.startActivity(this)
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridItem(
	title: String?,
	thumbnail: Bitmap?,
	highlight: Boolean,
	onLongClick: () -> Unit,
	onClick: () -> Unit
) {
	val context = LocalContext.current

	val borderColor by animateColorAsState(
		targetValue = if (highlight) MaterialTheme.colorScheme.onBackground else Color.Transparent,
		animationSpec = tween(300)
	)
	val scaleContent by animateFloatAsState(targetValue = if (highlight) 0.9f else 1f)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.padding(8.dp)
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(0.75f)
				.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
				.clip(RoundedCornerShape(12.dp))
				.combinedClickable(
					onClick = { onClick() },
					onLongClick = { onLongClick() }
				),
		) {
			Box(
				modifier = Modifier
					.clip(RoundedCornerShape(12.dp))
					.graphicsLayer {
						scaleX = scaleContent
						scaleY = scaleContent
						shape = RoundedCornerShape(12.dp)
					},
				contentAlignment = Alignment.Center
			) {
				if (thumbnail != null) {
					AsyncImage(
						model = ImageRequest.Builder(context)
							.data(thumbnail)
							.crossfade(300)
							.build(),
						placeholder = null,
						contentDescription = null,
						contentScale = ContentScale.Crop,
						modifier = Modifier
							.fillMaxSize()
							.clip(RoundedCornerShape(12.dp)),
					)
				} else {
					Text(
						text = "Thumbnail unavailable",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						maxLines = 2,
						overflow = TextOverflow.Ellipsis,
						textAlign = TextAlign.Center,
						modifier = Modifier
							.padding(8.dp, 0.dp)
					)
				}
			}
		}

		Text(
			text = title ?: "Untitled",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			fontStyle = if (title == null) FontStyle.Italic else FontStyle.Normal,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier
				.padding(0.dp, 4.dp, 0.dp, 0.dp)
		)
	}
}
