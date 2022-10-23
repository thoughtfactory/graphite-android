package com.syncodec.graphite.presentation.main.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.bucketItem.composable.dialog.DeleteDialog
import com.syncodec.graphite.presentation.main.composable.LocalCompositionCloseDialog
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnDelete
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnExit
import com.syncodec.graphite.presentation.main.composable.LocalCompositionShowDeleteDialog
import com.syncodec.graphite.presentation.main.composable.LocalCompositionShowExitDialog


enum class MainDialogType {
	DELETE,
	EXIT
}

@Composable
fun MainDialog() {

	val showDeleteDialog = LocalCompositionShowDeleteDialog.current
	val showExitDialog = LocalCompositionShowExitDialog.current

	val closeDialog = LocalCompositionCloseDialog.current
	val onDelete = LocalCompositionOnDelete.current
	val onExit = LocalCompositionOnExit.current

	DeleteDialog(
		showDeleteDialog = showDeleteDialog,
		message = "Are you sure you want to delete selected notes?",
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
}
