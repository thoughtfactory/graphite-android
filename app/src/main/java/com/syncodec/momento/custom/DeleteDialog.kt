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
			containerColor = MaterialTheme.colorScheme.surface,
			onDismissRequest = { onDismiss() },
			title = {
				Text(
					text = "Delete",
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface
				)
			},
			text = {
				Text(
					text = "Are you sure you want to delete ${if (selectedItemSize == 1) "1 entry" else "$selectedItemSize entries"}?",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface
				)
			},
			confirmButton = {
				OutlinedButton(
					colors = ButtonDefaults.buttonColors(
						containerColor = MaterialTheme.colorScheme.primary,
						contentColor = MaterialTheme.colorScheme.onPrimary
					),
					onClick = { onDelete() }
				) {
					Text(
						"Delete",
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold,
					)
				}
			},
			dismissButton = {
				OutlinedButton(
					colors = ButtonDefaults.outlinedButtonColors(),
					onClick = { onDismiss() }
				) {
					Text(
						"Dismiss",
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold,
					)
				}
			}
		)
	}
}
