package com.syncodec.graphite.presentation.settings.composable.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.GenericDialog2
import com.syncodec.graphite.presentation.common.dialog.GenericDialogDefaults


data class ImportDataDialogState(
	val dialogContent: String,
	val importFrom: String,
)

@Preview
@Composable
fun ImportDataDialog(
	importDataDialogState: ImportDataDialogState? = null,
	onClickSelectFile: (ImportDataDialogState?) -> Unit = {},
	onDismiss: () -> Unit = {},
) {
	GenericDialog2(
		showDialog = importDataDialogState != null,
		icon = GenericDialogDefaults.genericDialogIcon(icon = R.drawable.ic_flat_import),
		title = "Import from ${importDataDialogState?.importFrom ?: ""}",
		contentText = importDataDialogState?.dialogContent ?: "",
		primaryButton = GenericDialogDefaults.genericDialogButtonPrimary(text = "Select", onClick = { onClickSelectFile(importDataDialogState) }),
		secondaryButton = GenericDialogDefaults.genericDialogButtonDismiss(onClick = onDismiss),
	)
}
