package com.syncodec.graphite.presentation.attachment.composable.buildingBlock

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.AttachmentObject
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.note.composable.buildingBlock.AttachmentPreview
import java.io.File


@Composable
fun AttachmentCard(
	uri : Uri?,
	file : File?,
	attachmentObject : AttachmentObject,
	isSelected: Boolean,
	onClick: () -> Unit,
	onLongClick: () -> Unit,
	openNote: () -> Unit,
	onShare: () -> Unit,
) {
	Box(
		modifier = Modifier
			.aspectRatio(1f)
			.padding(1.dp)
	) {
		AttachmentPreview(
			attachment = attachmentObject,
			uri = uri,
			file = file,
			clickable = true,
			showActionButton = false,
			isSelected = isSelected,
			modifier = Modifier.fillMaxSize(),
			onClick = onClick,
			onLongClick = onLongClick
		)

		Column(
			horizontalAlignment = Alignment.End,
			modifier = Modifier
				.fillMaxSize()
				.padding(6.dp)
		) {
			MenuButton(
				icon = R.drawable.ic_note,
				containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.47f),
				shape = CircleShape,
				onClick = openNote
			)

			Spacer(modifier = Modifier.weight(1f))

			MenuButton(
				icon = R.drawable.ic_share,
				containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.47f),
				shape = CircleShape,
				onClick = onShare
			)
		}
	}
}
