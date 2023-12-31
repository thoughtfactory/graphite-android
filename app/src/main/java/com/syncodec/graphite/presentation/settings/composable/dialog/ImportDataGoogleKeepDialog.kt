package com.syncodec.graphite.presentation.settings.composable.dialog

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericAlertDialog2
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialog2
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialogDefaults
import com.syncodec.graphite.presentation.settings.composable.viewModel.ImportDataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel


@Preview
@Composable
fun ImportDataGoogleKeepDialog(
	isDialogVisible: Boolean = true,
	onDismissRequest: () -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val viewModel: ImportDataViewModel = koinViewModel()

	var isImportingDataDialogVisible by remember { mutableStateOf(false) }
	var importProcess by remember { mutableStateOf<ImportDataViewModel.Companion.ImportProcess?>(null) }

	val filePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
		if (uri == null) Toast.makeText(context, context.getText(R.string.toast_no_file_selected), Toast.LENGTH_SHORT).show()
		else scope.launch(Dispatchers.IO) {
			withContext(Dispatchers.Main) { onDismissRequest(); isImportingDataDialogVisible = true }
			viewModel.importFromGoogleKeep(inputUri = uri) { totalItem, currentItem ->
				withContext(Dispatchers.Main) { importProcess = ImportDataViewModel.Companion.ImportProcess(totalItem = totalItem, currentItem = currentItem) }
			}
			withContext(Dispatchers.Main) { isImportingDataDialogVisible = false }
		}
	}

	GenericAlertDialog2(
		isDialogVisible = isDialogVisible,
		icon = GenericDialogDefaults.genericDialogIcon(icon = R.drawable.ic_fa_import),
		title = stringResource(id = R.string.import_data_from) + " Google Keep",
		contentText = stringResource(id = R.string.import_google_keep_message),
		primaryButton = GenericDialogDefaults.genericDialogButtonPrimary(text = stringResource(id = R.string.select), onClick = { filePicker.launch(arrayOf("application/zip", " application/x-7z-compressed")) }),
		secondaryButton = GenericDialogDefaults.genericDialogButtonDismiss(onClick = onDismissRequest),
		onDismissRequest = onDismissRequest,
	)

	GenericDialog2(
		isDialogVisible = isImportingDataDialogVisible,
		icon = GenericDialogDefaults.genericDialogIcon(icon = R.drawable.ic_fa_import),
		title = stringResource(id = R.string.importing_data),
		contentText = stringResource(id = R.string.importing_data_dialog_content),
	) {
		importProcess.let { importProcess1 ->
			if (importProcess1 == null) LinearProgressIndicator(
				modifier = Modifier.fillMaxWidth(),
			) else Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				LinearProgressIndicator(
					progress = { importProcess1.percent() },
					modifier = Modifier.weight(1f),
				)
				Spacer(modifier = Modifier.width(12.dp))
				Text(
					text = "${importProcess1.currentItem}/${importProcess1.totalItem}",
					style = MaterialTheme.typography.bodySmall,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onBackground
				)
			}
		}
	}
}
