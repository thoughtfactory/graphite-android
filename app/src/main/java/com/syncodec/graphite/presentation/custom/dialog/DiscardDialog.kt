package com.syncodec.graphite.presentation.custom.dialog

import androidx.compose.material3.*
import androidx.compose.runtime.Composable


@Composable
fun DiscardDialog(
	showDiscardDialog: Boolean,
	onDismiss: () -> Unit,
	onDelete: () -> Unit
) {
	if (showDiscardDialog) {
		AlertDialog(
			containerColor = MaterialTheme.colorScheme.surface,
			onDismissRequest = { onDismiss() },
			title = {
				Text(
					text = "Discard",
					color = MaterialTheme.colorScheme.onSurface
				)
			},
			text = {
				Text(
					text = "Are you sure you want to discard changes?",
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
					Text("Discard",)
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
