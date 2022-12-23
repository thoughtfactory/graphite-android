package com.syncodec.graphite.presentation.common.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons


@Composable
fun DiscardDialog(
	showDiscardDialog : Boolean,
	onDismiss : () -> Unit,
	onDiscard : () -> Unit
) {
	GenericDialog(
		showDialog = showDiscardDialog,
		title = "Discard",
		onDismissRequest = onDismiss
	) {
		Text(
			text = "Are you sure you want to discard changes?",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
		)

		Spacer(modifier = Modifier.height(12.dp))

		DualActionButtons(
			primaryText = "Discard",
			secondaryText = "Cancel",
			onPrimaryClick = onDiscard,
			onSecondaryClick = onDismiss
		)
	}
}
