package com.syncodec.momento.custom

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight


@Composable
fun DeleteDialog(
	showDeleteDialog: Boolean,
	selectedItemSize: Int,
	onDismiss: () -> Unit,
	onDelete: () -> Unit
) {
	if (showDeleteDialog) {
		AlertDialog(
			containerColor = MaterialTheme.colorScheme.background,
			onDismissRequest = { onDismiss() },
			title = {
				Text(
					text = "Are you sure you want to delete ${if (selectedItemSize == 1) "1 entry" else "$selectedItemSize entries"}?",
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onPrimaryContainer
				)
			},
			confirmButton = {
				Button(
					colors = ButtonDefaults.buttonColors(
						containerColor = MaterialTheme.colorScheme.primaryContainer
					),
					onClick = {onDelete()}
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
