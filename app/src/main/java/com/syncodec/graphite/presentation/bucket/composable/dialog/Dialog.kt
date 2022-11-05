package com.syncodec.graphite.presentation.bucket.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionCloseDialog
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnDelete
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionShowDeleteDialog
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionShowLinkDialog
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog


enum class BucketDialogType {
	DELETE,
}

@Composable
fun BucketDialog() {

	val showDeleteDialog = LocalCompositionShowDeleteDialog.current
	val showLinkDialog = LocalCompositionShowLinkDialog.current

	val openDialog = LocalCompositionOpenDialog.current
	val closeDialog = LocalCompositionCloseDialog.current

	val onDelete = LocalCompositionOnDelete.current

	DeleteDialog(
		showDialog = showDeleteDialog,
		message = "Are you sure you want to delete selected bucket items?",
		onDismiss = { closeDialog(BucketDialogType.DELETE) }
	) {
		onDelete()
		closeDialog(BucketDialogType.DELETE)
	}
}
