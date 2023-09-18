package com.syncodec.graphite.presentation.settings.composable.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialog2
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialogDefaults


@Preview
@Composable
fun ClearDataDialog(
	showDialog: Boolean = true,
	onDismissRequest: () -> Unit = {},
	onConfirmDelete: () -> Unit = {},
) {
	GenericDialog2(
		isDialogVisible = showDialog,
		icon = GenericDialogDefaults.genericDialogIcon(icon = R.drawable.ic_flat_warning, tint = MaterialTheme.colorScheme.error),
		title = "Clear data",
		contentText = "Are you sure you want to clear all local data from this device? This will not affect snapshots or data on cloud providers.",
		primaryButton = GenericDialogDefaults.genericDialogButtonWarning(text = stringResource(id = R.string.delete), onClick = onConfirmDelete),
		secondaryButton = GenericDialogDefaults.genericDialogButtonDismiss(onClick = onDismissRequest),
	)
}
