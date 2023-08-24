package com.syncodec.graphite.presentation.settings.composable.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.GenericDialog2
import com.syncodec.graphite.presentation.common.dialog.GenericDialogDefaults


@Preview
@Composable
fun DeleteAccountDialog(
	showDialog: Boolean = false,
	onConfirmDelete: () -> Unit = {},
	onDismiss: () -> Unit = {},
) {
	GenericDialog2(
		isDialogVisible = showDialog,
		icon = GenericDialogDefaults.genericDialogIcon(icon = R.drawable.ic_flat_delete_account, tint = MaterialTheme.colorScheme.error),
		title = "Delete account",
		contentText = "Deleting your account will remove all your data from the server. Are you sure you want to delete your account? This does not affect your local data or data on cloud providers which are uploaded for sync and backups.",
		primaryButton = GenericDialogDefaults.genericDialogButtonWarning(text = "Delete", onClick = onConfirmDelete,),
		secondaryButton = GenericDialogDefaults.genericDialogButtonDismiss(onClick = onDismiss),
	)
}
