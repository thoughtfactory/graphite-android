package com.syncodec.graphite.presentation.settings.composable.dialog

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialog2
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialogDefaults


@Preview
@Composable
fun ImportDataGraphiteDialog(
	isDialogVisible: Boolean = true,
	onDismissRequest: () -> Unit = {},
) {
	val context = LocalContext.current

	val filePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
		if (uri == null) {
			Toast.makeText(context, context.getText(R.string.toast_no_file_selected), Toast.LENGTH_SHORT).show()
		} else {
			Toast.makeText(context, "uri : $uri", Toast.LENGTH_SHORT).show()
		}
	}

	GenericDialog2(
		isDialogVisible = isDialogVisible,
		icon = GenericDialogDefaults.genericDialogIcon(icon = R.drawable.ic_fa_import),
		title = stringResource(id = R.string.import_data_from) + " Graphite",
		contentText = "Select an exported Graphite data file to import. It will a zip file.",
		primaryButton = GenericDialogDefaults.genericDialogButtonPrimary(text = stringResource(id = R.string.select), onClick = { filePicker.launch(arrayOf("application/zip")) }),
		secondaryButton = GenericDialogDefaults.genericDialogButtonDismiss(onClick = onDismissRequest),
		onDismissRequest = onDismissRequest,
	)
}
