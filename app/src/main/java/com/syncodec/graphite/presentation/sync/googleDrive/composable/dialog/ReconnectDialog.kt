package com.syncodec.graphite.presentation.sync.googleDrive.composable.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.di.cloud.dropbox.DBox
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import com.syncodec.graphite.presentation.sync.googleDrive.GoogleDriveSyncActivity


@Preview
@Composable
fun ReconnectDialog(
	showDialog : Boolean = false,
	aboutState: GoogleDriveSyncActivity.Companion.AboutState = GoogleDriveSyncActivity.Companion.AboutState.Init,
	onReconnect : () -> Unit = {},
	onDismiss : () -> Unit = {},
) {
	val email = if (aboutState is GoogleDriveSyncActivity.Companion.AboutState.Success) aboutState.about.user.emailAddress else ""
	GenericDialog(
		showDialog = showDialog,
		title = "Reconnect Dropbox",
		contentText = "You are already connected to Dropbox with $email. Reconnecting with same or different account will not delete Graphite associated data from Dropbox.",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Reconnect",
				secondaryText = "Cancel",
				onClickPrimary = onReconnect,
				onClickSecondary = onDismiss,
			)
		},
		onDismissRequest = onDismiss,
	)
}
