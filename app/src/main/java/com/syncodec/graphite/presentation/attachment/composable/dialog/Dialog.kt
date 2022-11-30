package com.syncodec.graphite.presentation.attachment.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.attachment.AttachmentActivity
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog


enum class AttachmentDialogType {
	DELETE,
}

@Composable
fun AttachmentDialog() {

	val showDeleteDialog = AttachmentActivity.LocalShowDeleteDialog.current
	val closeDialog = AttachmentActivity.LocalCloseDialog.current
	val onDelete = AttachmentActivity.LocalOnDelete.current

	DeleteDialog(
		showDialog = showDeleteDialog,
		message = "Are you sure you want to delete selected attachments?",
		onDismiss = { closeDialog(AttachmentDialogType.DELETE) },
	) {
		onDelete()
		closeDialog(AttachmentDialogType.DELETE)
	}
}
