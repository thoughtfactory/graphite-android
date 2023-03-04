package com.syncodec.graphite.presentation.bucket.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog


enum class BucketDialogType {
	Edit,
	DeleteBucketItems,
	DeleteBucket,
}

@Composable
fun BucketDialog(
	bucketObject : BucketObject? = null,
	showEditBucketDialog : Boolean = false,
	showDeleteBucketItemsDialog : Boolean = false,
	showDeleteBucketDialog : Boolean = false,
	onUpdateBucket : (title : String?, description : String?) -> Unit = { _, _ -> },
	onDelete : () -> Unit = {},
	onDeleteBucket : () -> Unit = {},
	closeDialog : (BucketDialogType) -> Unit = {}
) {
	EditBucketDialog(
		title = bucketObject?.title,
		description = bucketObject?.description,
		showDialog = showEditBucketDialog,
		onDismiss = { closeDialog(BucketDialogType.Edit) },
		onSave = onUpdateBucket
	)

	DeleteDialog(
		showDialog = showDeleteBucketItemsDialog,
		message = "Are you sure you want to delete the selected items? This action is not reversible.",
		onDismiss = { closeDialog(BucketDialogType.DeleteBucketItems) },
	) {
		onDelete()
		closeDialog(BucketDialogType.DeleteBucketItems)
	}

	DeleteDialog(
		showDialog = showDeleteBucketDialog,
		message = "Are you sure you want to delete this bucket? This will also delete all the items in this bucket. This action is not reversible.",
		onDismiss = { closeDialog(BucketDialogType.DeleteBucket) },
	) {
		onDeleteBucket()
		closeDialog(BucketDialogType.DeleteBucket)
	}
}
