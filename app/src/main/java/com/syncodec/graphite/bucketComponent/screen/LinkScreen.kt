package com.syncodec.graphite.bucketComponent.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.syncodec.graphite.R
import com.syncodec.graphite.bucketComponent.BucketActivity
import com.syncodec.graphite.database.bucketItem.BucketItemPreviewDbEntry
import com.syncodec.graphite.database.bucketItem.LinkData


@OptIn(ExperimentalPagerApi::class)
@Composable
fun LinkScreen(
	bucketItemList: List<BucketItemPreviewDbEntry>,
	selectedBucketItemList: List<String>,
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
			bucketItemList.asReversed().forEach { bucketItem ->
				if (it == 0 || bucketItem.state.ordinal == it - 1) {
					item {
						LinkItem(
							bucketItem = bucketItem,
							isSelected = bucketItem.key in selectedBucketItemList,
							onClick = {
								onAction(BucketActivity.Action.CLICK_ITEM, bucketItem.key)
							},
							onLongClick = {
								onAction(BucketActivity.Action.LONG_CLICK_ITEM, bucketItem.key)
							},
							onCopy = { onAction(BucketActivity.Action.COPY_LINK, bucketItem.key) },
							onShare = {
								onAction(
									BucketActivity.Action.SHARE_LINK,
									bucketItem.key
								)
							},
						)
					}
				}
			}
			item { Spacer(modifier = Modifier.height(256.dp)) }
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LinkItem(
	bucketItem: BucketItemPreviewDbEntry,
	isSelected: Boolean,
	onClick: () -> Unit,
	onLongClick: () -> Unit,
	onCopy: () -> Unit,
	onShare: () -> Unit
) {
	val context = LocalContext.current
	val containerColor by animateColorAsState(targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)

	val linkData = jsonMapper { addModule(kotlinModule()) }.configure(
		DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false
	).readValue<LinkData>(bucketItem.data?.extra as String)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		modifier = Modifier
			.fillMaxWidth()
			.background(containerColor)
			.combinedClickable(
				onClick = { onClick() },
				onLongClick = { onLongClick() }
			),
	) {
		Spacer(modifier = Modifier.height(8.dp))

		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			Spacer(modifier = Modifier.width(16.dp))

			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.requiredSize(96.dp)
					.clip(RoundedCornerShape(12.dp))
					.background(MaterialTheme.colorScheme.surface),
			) {
				Image(
					painter = painterResource(id = R.drawable.ic_link),
					contentDescription = null,
					colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
					modifier = Modifier.requiredSize(32.dp),
				)
				AsyncImage(
					model = ImageRequest.Builder(context)
						.data(bucketItem.thumbnail)
						.crossfade(300)
						.build(),
					placeholder = null,
					contentDescription = null,
					contentScale = ContentScale.Crop,
					modifier = Modifier
				)
			}

			Spacer(modifier = Modifier.width(16.dp))

			Column(
				modifier = Modifier.weight(1f),
				verticalArrangement = Arrangement.Top
			) {
				Text(
					text = bucketItem.title ?: "",
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onBackground,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis,
				)

				Text(
					text = linkData.originalUrl ?: "",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
					maxLines = 2,
					overflow = TextOverflow.Ellipsis,
				)

				Text(
					text = linkData.description ?: "",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.27f),
					maxLines = 2,
					overflow = TextOverflow.Ellipsis,
				)
			}

			Column(
				modifier = Modifier,
				verticalArrangement = Arrangement.SpaceBetween
			) {
				IconButton(onClick = { onCopy() }) {
					Icon(
						painter = painterResource(id = R.drawable.ic_copy),
						contentDescription = "Copy link",
						tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
					)
				}

				IconButton(onClick = { onShare() }) {
					Icon(
						painter = painterResource(id = R.drawable.ic_share),
						contentDescription = "Share link",
						tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
					)
				}
			}

			Spacer(modifier = Modifier.width(12.dp))
		}

		Spacer(modifier = Modifier.height(8.dp))

		Spacer(
			modifier = Modifier
				.fillMaxWidth(0.71f)
				.height(1.dp)
				.clip(RoundedCornerShape(50))
				.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.13f))
		)
	}
}
