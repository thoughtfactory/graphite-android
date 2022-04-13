package com.syncodec.momento.attachmentComponent.screen

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
	onAction: (AttachmentActivity.Action, Any?) -> Unit
) {
	LazyVerticalGrid(
		columns = GridCells.Adaptive(144.dp),
		modifier = Modifier.padding(12.dp, 0.dp)
	) {
		attachmentList.forEachIndexed { index, data ->
			item {
				AttachmentCard(
					key = data.first.key,
					uri = data.second
				) { onAction(AttachmentActivity.Action.CLICK_ATTACHMENT, Pair(index, data.first)) }
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttachmentCard(
	key: String,
	uri: Uri,
	onClick: ()-> Unit
) {
	Card(
		modifier = Modifier
			.fillMaxSize()
			.aspectRatio(1f)
			.padding(8.dp),
		containerColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1),
		shape = SquircleShape(6.0),
		onClick = { onClick() }
	) {
		Image(
			painter = rememberImagePainter(
				data = uri,
				builder = { crossfade(true) }
			),
			contentDescription = null,
			contentScale = ContentScale.Crop
		)
	}
}
