package com.syncodec.graphite.presentation.note.composable.dialog.printDialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.dialog.GenericDialog


@Composable
fun PrintDialog(
	showDialog: Boolean,
	onDismiss: () -> Unit,
) {
	if (showDialog) {
		GenericDialog(
			showDialog = showDialog,
			onDismissRequest = onDismiss
		) {
			Text(
				text = "Print Note",
				style = MaterialTheme.typography.headlineMedium,
				color = MaterialTheme.colorScheme.onSurface,
			)

			Spacer(modifier = Modifier.height(12.dp))
		}
	}
}
