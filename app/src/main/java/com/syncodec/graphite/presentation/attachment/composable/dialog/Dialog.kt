package com.syncodec.graphite.presentation.attachment.composable.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


enum class AttachmentDialogType {
	Delete,
}

@Preview
@Composable
fun AttachmentDialog(
	isDeleteDialogVisible : Boolean = false,
	onDelete : () -> Unit = {},
	closeDialog : (AttachmentDialogType) -> Unit = {  },
) {
//	DeleteDialog(
//		showDialog = isDeleteDialogVisible,
//		message = "Deleting attachments is an irreversible action. Are you sure you want to delete the selected attachments?",
//		onDismiss = { closeDialog(AttachmentDialogType.Delete) },
//	) {
//		onDelete()
//		closeDialog(AttachmentDialogType.Delete)
//	}
}
