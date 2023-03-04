package com.syncodec.graphite.presentation.tags.composable.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog


enum class TagDialogType {
	Edit,
	Delete
}

@Preview
@Composable
fun TagDialog(
	isEditTagDialogVisible : Boolean = false,
	isDeleteTagDialogVisible : Boolean = false,
	previewTag : TagObject? = null,
	onSave : (String, Color) -> Unit = { _, _ -> },
	deleteTag : (TagObject) -> Unit = {},
	closeDialog : (TagDialogType) -> Unit = {},
) {
	val context = LocalContext.current

	EditTagDialog(
		showDialog = isEditTagDialogVisible,
		previewTag = previewTag,
		onSave = onSave
	) {
		closeDialog(TagDialogType.Edit)
	}

	DeleteDialog(
		showDialog = isDeleteTagDialogVisible,
		message = "Are you sure you want to delete ${previewTag?.tag}?\n\n${previewTag?.objectIdList?.size} note(s) are associated with this tag but won't be affected on deleting this tag.",
		onDismiss = { closeDialog(TagDialogType.Delete) }
	) {
		previewTag?.let { deleteTag(it) }
		closeDialog(TagDialogType.Delete)
	}
}
