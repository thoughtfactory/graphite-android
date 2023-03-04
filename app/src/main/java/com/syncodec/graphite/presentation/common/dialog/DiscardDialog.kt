package com.syncodec.graphite.presentation.common.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons


@Composable
fun DiscardDialog(
	showDialog : Boolean = false,
	onDiscard : () -> Unit = {},
	onDismiss : () -> Unit = {},
) {
	GenericDialog(
		showDialog = showDialog,
		title = "Discard",
		contentText = "Are you sure you want to discard changes?",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Discard",
				secondaryText = "Cancel",
				onClickPrimary = onDiscard,
				onClickSecondary = onDismiss
			)
		},
		onDismissRequest = onDismiss
	)
}
