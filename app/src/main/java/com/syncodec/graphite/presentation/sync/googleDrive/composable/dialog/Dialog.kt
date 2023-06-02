package com.syncodec.graphite.presentation.sync.googleDrive.composable.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.dropbox.core.v2.files.Metadata
import com.google.api.services.drive.model.File
import com.syncodec.graphite.di.cloud.dropbox.DBox
import com.syncodec.graphite.presentation.sync.googleDrive.GoogleDriveSyncActivity


enum class GoogleDriveDialogType {
	Disconnect,
	RestoreSnapshot,
	RestoringSnapshot,
}
@Preview
@Composable
fun GoogleDriveDialog(
	showDisconnectDialog : Boolean = false,
	snapshot : File? = null,
	showRestoringSnapshotDialog : Boolean = false,
	aboutState: GoogleDriveSyncActivity.Companion.AboutState = GoogleDriveSyncActivity.Companion.AboutState.Init,
	onDisconnect : () -> Unit = {},
	onShare : (File) -> Unit = {},
	onRestore : (File) -> Unit = {},
	onClickDelete : (File) -> Unit = {},
	closeDialog : (GoogleDriveDialogType) -> Unit = {},
) {
	DisconnectDialog(
		showDialog = showDisconnectDialog,
		onDisconnect = onDisconnect,
		onDismiss = { closeDialog(GoogleDriveDialogType.Disconnect) }
	)

	 RestoreSnapshotDialog(
		snapshot = snapshot,
		onShare = onShare,
		onRestore = onRestore,
		onClickDelete = onClickDelete,
		onDismiss = { closeDialog(GoogleDriveDialogType.RestoreSnapshot) }
	)

//	RestoringSnapshotDialog(
//		showDialog = showRestoringSnapshotDialog,
//	)
}
