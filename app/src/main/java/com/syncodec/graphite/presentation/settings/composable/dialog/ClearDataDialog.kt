package com.syncodec.graphite.presentation.settings.composable.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.GenericDialog2
import com.syncodec.graphite.presentation.common.dialog.GenericDialogDefaults


@Preview
@Composable
fun ClearDataDialog(
	showDialog: Boolean = true,
	onConfirmDelete: () -> Unit = {},
	onDismiss: () -> Unit = {},
) {
	GenericDialog2(
		showDialog = showDialog,
		icon = GenericDialogDefaults.genericDialogIcon(icon = R.drawable.ic_flat_warning, tint = MaterialTheme.colorScheme.error),
		title = "Clear data",
		contentText = "Are you sure you want to clear all local data from this device? This will not affect snapshots or data on cloud providers.",
		primaryButton = GenericDialogDefaults.genericDialogButtonWarning(text = "Delete", onClick = onConfirmDelete,),
		secondaryButton = GenericDialogDefaults.genericDialogButtonDismiss(onClick = onDismiss),
	)
}
