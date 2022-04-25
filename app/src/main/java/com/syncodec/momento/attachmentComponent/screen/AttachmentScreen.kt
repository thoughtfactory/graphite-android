package com.syncodec.momento.attachmentComponent.screen

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.syncodec.momento.attachmentComponent.AttachmentActivity
import com.syncodec.momento.custom.squircle.SquircleShape
import com.syncodec.momento.database.attachment.AttachmentDbEntry
import com.syncodec.momento.miscellaneous.ThemeUtils.Companion.tone


@Composable
fun AttachmentScreen(
	attachmentList: List<Pair<AttachmentDbEntry, Uri>>,
	selectedItemList: List<String>,
	onAction: (AttachmentActivity.Action, Any?) -> Unit
) {
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun AttachmentCard(
	uri: Uri,
	isSelected: Boolean,
	onClick: () -> Unit,
	onLongClick: () -> Unit
) {
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
		Image(
			painter = rememberImagePainter(
				data = uri,
				builder = { crossfade(true) }
			),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier
		)
	}
}
