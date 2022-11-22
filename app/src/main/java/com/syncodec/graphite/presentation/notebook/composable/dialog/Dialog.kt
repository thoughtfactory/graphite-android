package com.syncodec.graphite.presentation.notebook.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog
import com.syncodec.graphite.presentation.notebook.NotebookActivity


enum class NotebookDialogType {
	EDIT_CHAPTER,
	DELETE_SELECTED,
	DELETE_CHAPTER,
}

@Composable
fun NotebookDialog() {

	val showEditChapterDialog = NotebookActivity.LocalShowEditChapterDialog.current
	val showSelectedDeleteDialog = NotebookActivity.LocalShowSelectedDeleteDialog.current
	val showChapterDeleteDialog = NotebookActivity.LocalShowChapterDeleteDialog.current

	val title = NotebookActivity.LocalTitle.current
	val description = NotebookActivity.LocalDescription.current
	val color = NotebookActivity.LocalColor.current
	val thumbnail = NotebookActivity.LocalThumbnail.current

	val onUpdateChapter = NotebookActivity.LocalOnUpdateChapter.current
	val onDelete = NotebookActivity.LocalOnDelete.current
	val onDeleteChapter = NotebookActivity.LocalOnDeleteChapter.current
	val closeDialog = NotebookActivity.LocalCloseDialog.current

	EditChapterDialog(
		title = title,
		description = description,
		color = color,
		thumbnail = thumbnail,
		showDialog = showEditChapterDialog,
		onSave = onUpdateChapter,
	) { closeDialog(NotebookDialogType.EDIT_CHAPTER) }

	DeleteDialog(
		showDialog = showSelectedDeleteDialog,
		message = "Are you sure you want to delete selected items? Deleting chapter will also delete all the items in it.",
		onDismiss = { closeDialog(NotebookDialogType.DELETE_SELECTED) },
	) {
		onDelete()
		closeDialog(NotebookDialogType.DELETE_SELECTED)
	}

	DeleteDialog(
		showDialog = showChapterDeleteDialog,
		message = "Are you sure you want to delete this chapter? This will delete all items in it.",
		onDismiss = { closeDialog(NotebookDialogType.DELETE_CHAPTER) },
	) {
		onDeleteChapter()
		closeDialog(NotebookDialogType.DELETE_CHAPTER)
	}
}
