package com.syncodec.momento.mainComponent.miscellaneous

import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.mainComponent.MainViewModel

@Composable
fun DeleteDialog() {
	val context = LocalContext.current
	val viewModel: MainViewModel = viewModel()

	var showDeleteDialog by viewModel.activityState.showDeleteDialog


	val selectedEntryList = viewModel.activityState.selectedEntryList

	if (showDeleteDialog) {
		AlertDialog(
			containerColor = MaterialTheme.colorScheme.background,
			onDismissRequest = { showDeleteDialog = false },
			title = {
				Text(
					text = "Are you sure you want to delete ${if (selectedEntryList.size == 1) "1 entry" else "${selectedEntryList.size} entries"}?",
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onPrimaryContainer
				)
			},
			confirmButton = {
				Button(
					colors = ButtonDefaults.buttonColors(
						containerColor = MaterialTheme.colorScheme.primaryContainer
					),
					onClick = {
						selectedEntryList.forEach { primaryKey ->
							viewModel.moveDiaryToTrash(primaryKey)
						}
						Toast.makeText(
							context,
							"${if (selectedEntryList.size == 1) "1 entry" else "${selectedEntryList.size} entries"} deleted",
							Toast.LENGTH_SHORT
						).show()
						selectedEntryList.removeAll { true }
						viewModel.activityState.isSelected.value = false
						showDeleteDialog = false
					}
				) {
					Text(
						"Delete",
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onPrimaryContainer
					)
				}
			},
		)
	}
}
