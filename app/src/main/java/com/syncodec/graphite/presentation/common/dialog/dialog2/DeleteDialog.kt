package com.syncodec.graphite.presentation.common.dialog.dialog2

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R


@Preview
@Composable
fun DeleteDialog(
	isDialogVisible: Boolean = true,
	onDismissRequest: () -> Unit = {},
	title: String = "Delete",
	contentText: String = "Are you sure?",
	onConfirmDelete: () -> Unit = {}
) {
	GenericAlertDialog2(
		isDialogVisible = isDialogVisible,
		onDismissRequest = onDismissRequest,
		icon = GenericDialogIcon(icon = R.drawable.ic_fa_delete, tint = MaterialTheme.colorScheme.error),
		title = title,
		contentText = contentText,
		primaryButton = GenericDialogDefaults.genericDialogButtonWarning(text = stringResource(id = R.string.delete), onClick = onConfirmDelete),
		secondaryButton = GenericDialogDefaults.genericDialogButtonSecondary(text = stringResource(id = R.string.cancel), onClick = onDismissRequest),
		isPrimaryButtonEnabled = true,
		isSecondaryButtonEnabled = true,
	)
}
