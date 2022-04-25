package com.syncodec.momento.bucketComponent.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.syncodec.momento.R
import com.syncodec.momento.bucketComponent.BucketActivity
import com.syncodec.momento.database.bucketItem.BucketItemPreviewDbEntry
import com.syncodec.momento.database.bucketItem.BucketItemState


@OptIn(ExperimentalFoundationApi::class, com.google.accompanist.pager.ExperimentalPagerApi::class)
@Composable
fun TodoScreen(
	bucketItemList: List<BucketItemPreviewDbEntry>,
	selectedBucketItemList: SnapshotStateList<String>,
	pagerState: PagerState,
	onAction: (BucketActivity.Action, Any?) -> Unit
) {
	HorizontalPager(
		count = 4,
		state = pagerState,
		userScrollEnabled = false,
		verticalAlignment = Alignment.Top
	) {
		LazyColumn(
			modifier = Modifier,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			item { Spacer(modifier = Modifier.height(16.dp)) }
			bucketItemList.asReversed().forEach { bucketItem ->
				if (it == 0 || bucketItem.state.ordinal == it - 1) {
					item {
						TodoItem(
							bucketItem = bucketItem,
							isSelected = bucketItem.key in selectedBucketItemList,
							onClick = {
								onAction(BucketActivity.Action.CLICK_ITEM, bucketItem.key)
							},
							onLongClick = {
								onAction(BucketActivity.Action.LONG_CLICK_ITEM, bucketItem.key)
							}
						)
					}
					item {
						Spacer(
							modifier = Modifier
								.fillMaxWidth(0.71f)
								.height(1.dp)
								.clip(RoundedCornerShape(50))
								.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.13f))
						)
					}
				}
			}
		}
	}
}

@OptIn(
	ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
	ExperimentalAnimationApi::class
)
@Composable
private fun TodoItem(
	bucketItem: BucketItemPreviewDbEntry,
	isSelected: Boolean,
	onClick: () -> Unit,
	onLongClick: () -> Unit
) {
	val containerColor by animateColorAsState(targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp)
			.clip(RoundedCornerShape(12.dp))
			.background(containerColor)
			.combinedClickable(
				onClick = { onClick() },
				onLongClick = { onLongClick() }
			),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(16.dp))

		AnimatedContent(
			targetState = bucketItem.state,
			transitionSpec = {
				(scaleIn(tween(600), 0f) with scaleOut(
					tween(600),
					1f
				)).using(SizeTransform(clip = false))
			}
		) {
			when (it) {
				BucketItemState.ALPHA -> Icon(
					painter = painterResource(id = R.drawable.ic_todo),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary,
					modifier = Modifier.requiredSize(24.dp)
				)
				BucketItemState.BETA -> Icon(
					painter = painterResource(id = R.drawable.ic_clock),
					contentDescription = null,
					tint = Color(0xFFF5761A),
					modifier = Modifier.requiredSize(24.dp)
				)
				BucketItemState.GAMMA -> Icon(
					painter = painterResource(id = R.drawable.ic_done),
					contentDescription = null,
					tint = Color(0xFF519259),
					modifier = Modifier.requiredSize(24.dp)
				)
			}
		}

		Spacer(modifier = Modifier.width(16.dp))

		Text(
			text = bucketItem.title ?: "",
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onBackground
		)
	}
}
