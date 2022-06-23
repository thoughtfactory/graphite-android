package com.syncodec.graphite.custom

import androidx.compose.material3.*
import androidx.compose.runtime.Composable


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
					color = MaterialTheme.colorScheme.onSurface
				)
			},
			text = {
				Text(
					text = "Are you sure you want to delete ${if (selectedItemSize == 1) "1 entry" else "$selectedItemSize entries"}?",
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
					Text("Delete",)
				}
			},
			dismissButton = {
				OutlinedButton(
					colors = ButtonDefaults.outlinedButtonColors(),
					onClick = { onDismiss() }
				) {
					Text("Dismiss",)
				}
			}
		)
	}
}

@Composable
fun DeleteDialog(
	showDeleteDialog: Boolean,
	key: String,
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
					color = MaterialTheme.colorScheme.onSurface
				)
			},
			text = {
				Text(
					text = "Are you sure you want to delete $key",
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
					Text("Delete",)
				}
			},
			dismissButton = {
				OutlinedButton(
					colors = ButtonDefaults.outlinedButtonColors(),
					onClick = { onDismiss() }
				) {
					Text("Dismiss",)
				}
			}
		)
	}
}
