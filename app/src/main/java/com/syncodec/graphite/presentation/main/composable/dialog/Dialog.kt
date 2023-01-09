package com.syncodec.graphite.presentation.main.composable.dialog

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog
import com.syncodec.graphite.presentation.common.permission.NotificationPermissionDialog
import com.syncodec.graphite.presentation.main.composable.LocalCompositionCloseDialog
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnDelete
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnExit
import com.syncodec.graphite.presentation.main.composable.LocalCompositionShowDeleteDialog
import com.syncodec.graphite.presentation.main.composable.LocalCompositionShowExitDialog
import com.syncodec.graphite.presentation.main.composable.LocalCompositionShowNotificationPermissionDialog
import com.syncodec.graphite.presentation.note.composable.dialog.NoteDialogType
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.rememberMaterialDialogState


enum class MainDialogType {
	NOTIFICATION_PERMISSION,
	DELETE,
	EXIT
}

@Composable
fun MainDialog() {

	val showNotificationPermissionDialog = LocalCompositionShowNotificationPermissionDialog.current
	val showDeleteDialog = LocalCompositionShowDeleteDialog.current
	val showExitDialog = LocalCompositionShowExitDialog.current

	val closeDialog = LocalCompositionCloseDialog.current
	val onDelete = LocalCompositionOnDelete.current
	val onExit = LocalCompositionOnExit.current

	DeleteDialog(
		showDialog = showDeleteDialog,
		message = "Are you sure you want to delete selected items?",
		onDismiss = { closeDialog(MainDialogType.DELETE) },
	) {
		onDelete()
		closeDialog(MainDialogType.DELETE)
	}

	ExitDialog(
		showDialog = showExitDialog,
		onDismiss = { closeDialog(MainDialogType.EXIT) },
	) {
		closeDialog(MainDialogType.EXIT)
		onExit()
	}

	NotificationPermissionDialog(
		showDialog = showNotificationPermissionDialog,
		onDismiss = { closeDialog(MainDialogType.NOTIFICATION_PERMISSION) },
	) { closeDialog(MainDialogType.NOTIFICATION_PERMISSION) }
}
