package com.syncodec.graphite.presentation.common.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import io.realm.kotlin.types.RealmUUID


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
	id: RealmUUID?,
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
					text = "Are you sure you want to delete $id",
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
	showDialog: Boolean,
	message: String? = null,
	onDismiss: () -> Unit,
	onDelete: () -> Unit
) {
	GenericDialog(
		showDialog = showDialog,
		title = "Delete",
		onDismissRequest = onDismiss
	) {
		Text(
			text = message ?: "Are you sure you want to delete this item?",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
		)

		Spacer(modifier = Modifier.height(12.dp))

		DualActionButtons(
			primaryText = "Delete",
			secondaryText = "Dismiss",
			onPrimaryClick = onDelete,
			onSecondaryClick = onDismiss
		)
	}
}
