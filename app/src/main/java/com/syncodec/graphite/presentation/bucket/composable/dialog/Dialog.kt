package com.syncodec.graphite.presentation.bucket.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionBucketObject
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnDelete
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnUpdateBucket
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionShowDeleteDialog
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionShowEditBucketDialog
import com.syncodec.graphite.presentation.common.LocalCompositionCloseDialog
import com.syncodec.graphite.presentation.common.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog
import com.syncodec.graphite.presentation.common.dialog.DialogType


@Composable
fun BucketDialog() {

	val bucketObject = LocalCompositionBucketObject.current

	val selectedIdList = LocalCompositionSelectedObjectIdList.current

	val showDeleteDialog = LocalCompositionShowDeleteDialog.current
	val showEditBucketDialog = LocalCompositionShowEditBucketDialog.current

	val openDialog = LocalCompositionOpenDialog.current
	val closeDialog = LocalCompositionCloseDialog.current

	val onDelete = LocalCompositionOnDelete.current
	val onUpdateBucket = LocalCompositionOnUpdateBucket.current

	DeleteDialog(
		showDialog = showDeleteDialog,
		message = if (selectedIdList.isEmpty()) "Are you sure you want to delete this bucket?" else "Are you sure you want to delete selected bucket items?",
		onDismiss = { closeDialog(DialogType.DELETE) }
	) {
		onDelete()
		closeDialog(DialogType.DELETE)
	}

	EditBucketDialog(
		title = bucketObject?.title,
		description = bucketObject?.description,
		showDialog = showEditBucketDialog,
		onDismiss = { closeDialog(DialogType.EDIT) },
		onSave = onUpdateBucket
	)
}
