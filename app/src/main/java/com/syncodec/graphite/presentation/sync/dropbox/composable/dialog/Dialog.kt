package com.syncodec.graphite.presentation.sync.dropbox.composable.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.dropbox.core.v2.files.Metadata
import com.syncodec.graphite.di.sync.dropbox.DBox
import com.syncodec.graphite.presentation.settings.composable.screen.localBackupScreen.dialog.RestoringSnapshotDialog


enum class DropboxDialogType {
	EnterAuthCode,
	Reconnect,
	Disconnect,
	RestoreSnapshot,
	RestoringSnapshot,
}

@Preview
@Composable
fun DropboxDialog(
	showEnterAuthCodeDialog : Boolean = false,
	showReconnectDialog : Boolean = false,
	showDisconnectDialog : Boolean = false,
	snapshot : Metadata? = null,
	showRestoringSnapshotDialog : Boolean = false,
	testConnectionResponse : DBox.Companion.TestConnectionResponse? = null,
	onAuthorize : (String) -> Unit = {},
	onReconnect : () -> Unit = {},
	onDisconnect : () -> Unit = {},
	onShare : (Metadata) -> Unit = {},
	onRestore : (Metadata) -> Unit = {},
	closeDialog : (DropboxDialogType) -> Unit = {},
) {
	EnterAuthCodeDialog(
		showDialog = showEnterAuthCodeDialog,
		onAuthorize = onAuthorize,
		onDismiss = { closeDialog(DropboxDialogType.EnterAuthCode) }
	)

	ReconnectDialog(
		showDialog = showReconnectDialog,
		testConnectionResponse = testConnectionResponse,
		onReconnect = onReconnect,
		onDismiss = { closeDialog(DropboxDialogType.Reconnect) }
	)

	DisconnectDialog(
		showDialog = showDisconnectDialog,
		onDisconnect = onDisconnect,
		onDismiss = { closeDialog(DropboxDialogType.Disconnect) }
	)

	RestoreSnapshotDialog(
		snapshot = snapshot,
		onShare = onShare,
		onRestore = onRestore,
		onDismiss = { closeDialog(DropboxDialogType.RestoreSnapshot) }
	)

	RestoringSnapshotDialog(
		showDialog = showRestoringSnapshotDialog,
	)
}
