package com.syncodec.graphite.presentation.main.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog
import com.syncodec.graphite.presentation.common.permission.NotificationPermissionDialog


enum class MainDialogType {
	NotificationPermission,
	Delete,
}

@Composable
fun MainDialog(
	showNotificationPermissionDialog : Boolean = false,
	showDeleteDialog : Boolean = false,
	onDelete : () -> Unit = {},
	closeDialog : (MainDialogType) -> Unit = {},
) {
	DeleteDialog(
		showDialog = showDeleteDialog,
		message = "Are you sure you want to delete selected items?",
		onDismiss = { closeDialog(MainDialogType.Delete) },
	) {
		onDelete()
		closeDialog(MainDialogType.Delete)
	}

	NotificationPermissionDialog(
		showDialog = showNotificationPermissionDialog,
		onDismiss = { closeDialog(MainDialogType.NotificationPermission) },
	) { closeDialog(MainDialogType.NotificationPermission) }
}
