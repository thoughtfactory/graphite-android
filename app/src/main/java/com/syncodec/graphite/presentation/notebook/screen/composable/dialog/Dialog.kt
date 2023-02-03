package com.syncodec.graphite.presentation.notebook.screen.composable.dialog

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog


enum class NotebookDialogType {
	EditChapter,
	DeleteSelected,
	DeleteChapter,
}

@Preview
@Composable
fun NotebookDialog(
	title: String? = null,
	description: String? = null,
	color: Color? = null,
	thumbnail: Bitmap? = null,
	showEditChapterDialog: Boolean = false,
	showDeleteSelectedDialog: Boolean = false,
	showDeleteChapterDialog: Boolean = false,
	onUpdateChapter: (String?, String?, Color?, Bitmap?) -> Unit = { _, _, _, _ -> },
	onDeleteSelected: () -> Unit = {},
	onDeleteChapter: () -> Unit = {},
	closeDialog: (NotebookDialogType) -> Unit = {},
) {
	EditChapterDialog(
		title = title,
		description = description,
		color = color,
		thumbnail = thumbnail,
		showDialog = showEditChapterDialog,
		onSave = onUpdateChapter,
	) { closeDialog(NotebookDialogType.EditChapter) }

	DeleteDialog(
		showDialog = showDeleteSelectedDialog,
		message = "Are you sure you want to delete selected items? Deleting chapter will also delete all the items in it.",
		onDismiss = { closeDialog(NotebookDialogType.DeleteSelected) },
	) {
		onDeleteSelected()
		closeDialog(NotebookDialogType.DeleteSelected)
	}

	DeleteDialog(
		showDialog = showDeleteChapterDialog,
		message = "Are you sure you want to delete this chapter? This will delete all items in it.",
		onDismiss = { closeDialog(NotebookDialogType.DeleteChapter) },
	) {
		onDeleteChapter()
		closeDialog(NotebookDialogType.DeleteChapter)
	}
}
