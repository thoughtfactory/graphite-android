package com.syncodec.graphite.presentation.main.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons


@Composable
fun ExitDialog(
	showDialog : Boolean,
	onDismiss : () -> Unit,
	onExit : () -> Unit
) {
	GenericDialog(
		showDialog = showDialog,
		title = "Exit",
		contentText = "Are you sure you want to exit?",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Exit",
				secondaryText = "Cancel",
				onPrimaryClick = onExit,
				onSecondaryClick = onDismiss
			)
		},
		onDismissRequest = onDismiss
	)
}
