package com.syncodec.graphite.presentation.bucketItem.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog


enum class BucketItemDialogType {
	DELETE,
}

@Composable
fun BucketItemDialog(
	showDeleteDialog : Boolean = false,
	onDelete : () -> Unit = {},
	closeDialog : (BucketItemDialogType) -> Unit = {},
) {
	DeleteDialog(
		showDialog = showDeleteDialog,
		message = "Are you sure you want to delete this item? This action cannot be undone.",
		onDismiss = { closeDialog(BucketItemDialogType.DELETE) },
	) {
		onDelete()
		closeDialog(BucketItemDialogType.DELETE)
	}
}
