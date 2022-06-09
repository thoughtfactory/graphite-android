package com.syncodec.graphite.attachmentComponent.screen

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.attachmentComponent.AttachmentActivity
import com.syncodec.graphite.database.attachment.AttachmentDbEntry
import com.syncodec.graphite.miscellaneous.ThemeUtils.Companion.tone


@Composable
fun AttachmentScreen(
	attachmentList: List<Pair<AttachmentDbEntry, Uri?>>,
	selectedItemList: List<String>,
	onAction: (AttachmentActivity.Action, Any?) -> Unit
) {
	if (attachmentList.isEmpty()) {
		AttachmentIllustration()
	} else {
		LazyVerticalGrid(
			columns = GridCells.Adaptive(144.dp),
			modifier = Modifier.padding(12.dp, 0.dp)
		) {
			item { Spacer(modifier = Modifier.height(8.dp)) }
			item { Spacer(modifier = Modifier.height(8.dp)) }
			attachmentList.forEachIndexed { index, data ->
				item {
					AttachmentCard(
						uri = data.second,
						isSelected = data.first.key in selectedItemList,
						onClick = {
							onAction(
								AttachmentActivity.Action.CLICK_ATTACHMENT,
								Pair(index, data.first)
							)
						},
						onLongClick = {
//						onAction(
//							AttachmentActivity.Action.LONG_CLICK_ATTACHMENT,
//							data.first
//						)
						}
					)
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun AttachmentCard(
	uri: Uri?,
	isSelected: Boolean,
	onClick: () -> Unit,
	onLongClick: () -> Unit
) {
	val context = LocalContext.current

	val borderColor by animateColorAsState(targetValue = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent)

	Card(
		containerColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1),
		shape = RoundedCornerShape(12.dp),
		border = BorderStroke(3.dp, borderColor),
		modifier = Modifier
			.fillMaxSize()
			.aspectRatio(1f)
			.padding(8.dp)
			.clip(RoundedCornerShape(12.dp))
			.combinedClickable(
				onClick = { onClick() },
				onLongClick = { onLongClick() },
			),
	) {
		AsyncImage(
			model = ImageRequest.Builder(context)
				.data(uri)
				.crossfade(300)
				.build(),
			placeholder = null,
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier,
		)
	}
}

@Composable
private fun AttachmentIllustration() {
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Spacer(modifier = Modifier.weight(1f))
		Image(
			painter = painterResource(id = R.drawable.il_attachment),
			contentDescription = "No attachment found",
			contentScale = ContentScale.Fit,
			modifier = Modifier.fillMaxWidth(0.64f),
		)

		Spacer(modifier = Modifier.height(24.dp))

		Text(
			text = "No attachment found",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			textAlign = TextAlign.Center,
			modifier = Modifier.fillMaxWidth(0.71f)
		)

		Spacer(modifier = Modifier.weight(1f))
	}
}
