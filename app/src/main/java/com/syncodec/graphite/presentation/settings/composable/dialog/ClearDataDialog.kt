package com.syncodec.graphite.presentation.settings.composable.dialog

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialog2
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialogDefaults
import com.syncodec.graphite.presentation.settings.composable.viewModel.ClearDataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel


@Preview
@Composable
fun ClearDataDialog(
	isDialogVisible: Boolean = true,
	onDismissRequest: () -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val viewModel: ClearDataViewModel = koinViewModel()

	var isDeletingDialogVisible by remember { mutableStateOf(false) }

	fun onClickDelete() {
		scope.launch(Dispatchers.Default) {
			withContext(Dispatchers.Main) {
				onDismissRequest()
				isDeletingDialogVisible = true
			}
			val isDataCleared = viewModel.clearData()
			withContext(Dispatchers.Main) {
				if (isDataCleared) Toast.makeText(context, context.getText(R.string.clear_data_successful), Toast.LENGTH_SHORT).show()
				else Toast.makeText(context, context.getText(R.string.clear_data_failure), Toast.LENGTH_SHORT).show()

				isDeletingDialogVisible = false
			}
		}
	}

	GenericDialog2(
		isDialogVisible = isDialogVisible,
		icon = GenericDialogDefaults.genericDialogWarningIcon(),
		title = stringResource(id = R.string.clear_data),
		contentText = stringResource(id = R.string.clear_data_dialog_content),
		primaryButton = GenericDialogDefaults.genericDialogButtonPrimary(text = stringResource(id = R.string.clear_data), onClick = ::onClickDelete),
		secondaryButton = GenericDialogDefaults.genericDialogButtonDismiss(onClick = onDismissRequest),
		onDismissRequest = onDismissRequest,
	)

	GenericDialog2(
		isDialogVisible = isDeletingDialogVisible,
		icon = GenericDialogDefaults.genericDialogWarningIcon(),
		title = stringResource(id = R.string.clearing_data),
		contentText = stringResource(id = R.string.clearing_data_dialog_content),
	)
}
