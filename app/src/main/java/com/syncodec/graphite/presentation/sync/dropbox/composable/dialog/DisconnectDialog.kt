package com.syncodec.graphite.presentation.sync.dropbox.composable.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons


@Preview
@Composable
fun DisconnectDialog(
	showDialog : Boolean = false,
	onDisconnect : () -> Unit = {},
	onDismiss : () -> Unit = {},
) {
	GenericDialog(
		showDialog = showDialog,
		title = "Disconnect Dropbox",
		contentText = "Disconnecting will not delete Graphite associated files from Dropbox but need to be deleted manually.",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Disconnect",
				secondaryText = "Cancel",
				onClickPrimary = onDisconnect,
				onClickSecondary = onDismiss,
			)
		},
		onDismissRequest = onDismiss,
	)
}
