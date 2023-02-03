package com.syncodec.graphite.presentation.settings.composable.dialog.clearData

import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Preview
@Composable
fun ClearDataDialog(
	showDialog : Boolean = true,
	onDismiss : () -> Unit = {},
) {
	val scope = rememberCoroutineScope()
	val context = LocalContext.current
	val viewModel : ClearDataViewModel = koinViewModel()

	var isClearingData by remember { mutableStateOf(false) }

	GenericDialog(
		showDialog = showDialog,
		title = "Clear Data",
		contentText = "Clearing data is an irreversible action. Are you sure you want to clear all data? It is advised to backup your data before clearing it.",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Clear Data",
				secondaryText = "Cancel",
				primaryColor = MaterialTheme.colorScheme.errorContainer,
				secondaryEnabled = ! isClearingData,
				onClickPrimary = {
					isClearingData = true
					viewModel.clearData { success, exception ->
						scope.launch(Dispatchers.Main) {
							if (success) {
								onDismiss()
								isClearingData = false
								Toast.makeText(context, "Data cleared successfully", Toast.LENGTH_SHORT).show()
							} else {
								isClearingData = false
								Toast.makeText(context, "Failed to clear data", Toast.LENGTH_SHORT).show()
								exception?.printStackTrace()
							}
						}
					}
				},
				onClickSecondary = { onDismiss() },
			)
		},
		onDismissRequest = { if (! isClearingData) onDismiss() }
	)
}
