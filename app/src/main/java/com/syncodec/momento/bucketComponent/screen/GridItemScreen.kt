package com.syncodec.momento.bucketComponent.screen

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.syncodec.momento.bucketComponent.BucketActivity
import com.syncodec.momento.database.bucket.BucketItemDbEntry
import java.io.File


@OptIn(ExperimentalFoundationApi::class, com.google.accompanist.pager.ExperimentalPagerApi::class)
@Composable
fun GridItemScreen(
	bucketItemMap: SnapshotStateMap<String, Pair<BucketItemDbEntry, Int>>,
	selectedBucketItemList: SnapshotStateList<String>,
	pagerState: PagerState,
	getThumbnailPath: (String) -> String?,
	onClick: (BucketActivity.Click, Any?) -> Unit
) {
	HorizontalPager(
		count = 4,
		state = pagerState,
		userScrollEnabled = false
	) {
		LazyVerticalGrid(
			columns = GridCells.Adaptive(96.dp),
			Modifier
				.padding(12.dp, 12.dp, 12.dp, 0.dp)
				.background(Color.Transparent)
		) {
			bucketItemMap.forEach { (key, data) ->
				if (it == 0 || data.second == it - 1) {
					item {
						GridItem(
							title = data.first.title!!,
							thumbnail = getThumbnailPath(key),
							highlight = key in selectedBucketItemList,
							onLongClick = { onClick(BucketActivity.Click.LONG_CLICK_ITEM, key) }
						) {
							onClick(BucketActivity.Click.CLICK_ITEM, key)
						}
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
private fun GridItem(
	title: String,
	thumbnail: String?,
	highlight: Boolean,
	onLongClick: () -> Unit,
	onClick: () -> Unit
) {
	val borderColor by animateColorAsState(
		targetValue = if (highlight) MaterialTheme.colorScheme.onBackground else Color.Transparent,
		animationSpec = tween(400)
	)
	val scaleContent by animateFloatAsState(targetValue = if (highlight) 0.9f else 1f)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.padding(8.dp)
	) {
		Card(
			elevation = 0.dp,
			backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
			shape = RoundedCornerShape(12.dp),
			border = BorderStroke(4.dp, borderColor),
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(0.75f)
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
					Image(
						painter = rememberImagePainter(
							data = File(thumbnail),
							builder = { crossfade(true) }
						),
						contentDescription = null,
						contentScale = ContentScale.Crop,
						modifier = Modifier
							.fillMaxSize()
							.clip(RoundedCornerShape(12.dp))
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
			text = title,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier
				.padding(0.dp, 4.dp, 0.dp, 0.dp)
		)
	}
}
