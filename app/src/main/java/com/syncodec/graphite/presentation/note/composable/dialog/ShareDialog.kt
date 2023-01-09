package com.syncodec.graphite.presentation.note.composable.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons


@Composable
fun ShareDialog(
	showDialog: Boolean,
	onShareText: () -> Unit,
	onShareAttachment: () -> Unit,
	onDismiss: () -> Unit
) {
	GenericDialog(
		showDialog = showDialog,
		title = "Share",
		onDismissRequest = onDismiss
	) {
		Text(
			text = "Sharing both text and files is not supported.",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
		)

		Spacer(modifier = Modifier.height(8.dp))

		DualActionButtons(
			primaryText = "Text",
			secondaryText = "Attachments",
			onPrimaryClick = onShareText,
			onSecondaryClick = onShareAttachment
		)
	}
}
