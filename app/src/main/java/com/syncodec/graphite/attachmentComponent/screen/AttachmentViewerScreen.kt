package com.syncodec.graphite.attachmentComponent.screen

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.syncodec.graphite.attachmentComponent.AttachmentActivity
import com.syncodec.graphite.custom.LoadingView
import com.syncodec.graphite.database.attachment.AttachmentDbEntry


@OptIn(ExperimentalPagerApi::class)
@Composable
fun AttachmentViewerScreen(
	attachmentList: List<Pair<AttachmentDbEntry, Uri?>>,
	pagerState: PagerState,
	listState: LazyListState,
	onAction: (AttachmentActivity.Action, Any?) -> Unit
) {
	var isPreviewVisible by remember { mutableStateOf(true) }

	Box(
		modifier = Modifier.fillMaxSize()
	) {
		HorizontalPager(
			state = pagerState,
			count = attachmentList.size,
			itemSpacing = 2.dp,
			userScrollEnabled = true,
			verticalAlignment = Alignment.Bottom,
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.background),
		) { page ->
			Crossfade(targetState = currentPage == page) {
				if (it) {
					ViewerScreen(
						key = attachmentList[page].first.key,
						uri = attachmentList[page].second
					) { isPreviewVisible = !isPreviewVisible }
				} else {
					LoadingView()
				}
			}
		}

		AnimatedVisibility(
			visible = isPreviewVisible,
			enter = fadeIn(tween(600)),
			exit = fadeOut(tween(600))
		) {
			Column(
				modifier = Modifier.fillMaxSize(),
				verticalArrangement = Arrangement.Bottom,
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				PreviewSurface(
					attachmentList = attachmentList,
					listState = listState,
					currentIndex = pagerState.currentPage
				) { action, data -> onAction(action, data) }
				Spacer(modifier = Modifier.height(8.dp))
			}
		}
	}
}

@Composable
private fun ViewerScreen(
	key: String,
	uri: Uri?,
	onClick: () -> Unit
) {
	val context = LocalContext.current

	AsyncImage(
		model = ImageRequest.Builder(context)
			.data(uri)
			.crossfade(300)
			.build(),
		placeholder = null,
		contentDescription = key,
		contentScale = ContentScale.Crop,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
			.clickable(
				interactionSource = remember { MutableInteractionSource() },
				indication = null
			) { onClick() },
	)
}

@Composable
private fun PreviewSurface(
	attachmentList: List<Pair<AttachmentDbEntry, Uri?>>,
	listState: LazyListState,
	currentIndex: Int,
	onAction: (AttachmentActivity.Action, Any?) -> Unit
) {
	val context = LocalContext.current

	LazyRow(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.Center,
		verticalAlignment = Alignment.CenterVertically,
		state = listState
	) {
		item { Spacer(modifier = Modifier.width(4.dp)) }
		attachmentList.forEachIndexed { index, data ->
			item {
				val borderColor by animateColorAsState(targetValue = if (index == currentIndex) MaterialTheme.colorScheme.primary else Color.Transparent)
				val size by animateDpAsState(targetValue = if (index == currentIndex) 64.dp else 48.dp)
				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier
						.requiredSize(64.dp)
						.clip(RoundedCornerShape(12.dp))
						.border(2.dp, borderColor, RoundedCornerShape(12.dp))
						.clickable { onAction(AttachmentActivity.Action.CLICK_PREVIEW, index) }
				) {
					AsyncImage(
						model = ImageRequest.Builder(context)
							.data(data.second)
							.crossfade(300)
							.build(),
						placeholder = null,
						contentDescription = data.first.key,
						contentScale = ContentScale.Crop,
						modifier = Modifier.fillMaxSize(),
					)
				}
			}
		}
	}
}
