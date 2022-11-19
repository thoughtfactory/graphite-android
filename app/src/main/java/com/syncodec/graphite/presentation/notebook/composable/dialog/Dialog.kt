package com.syncodec.graphite.presentation.notebook.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedRealmUUIDList
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog
import com.syncodec.graphite.presentation.main.composable.bottomSheet.FilterBottomSheet
import com.syncodec.graphite.presentation.notebook.NotebookActivity


enum class NotebookDialogType {
	EDIT_CHAPTER,
	DELETE,
}

@Composable
fun NotebookDialog() {

	val showEditChapterDialog = NotebookActivity.LocalShowEditChapterDialog.current
	val showDeleteDialog = NotebookActivity.LocalShowDeleteDialog.current

	val title = NotebookActivity.LocalTitle.current
	val description = NotebookActivity.LocalDescription.current
	val color = NotebookActivity.LocalColor.current
	val thumbnail = NotebookActivity.LocalThumbnail.current

	val selectedItemSize = LocalCompositionSelectedRealmUUIDList.current.size

	val onUpdateChapter = NotebookActivity.LocalOnUpdateChapter.current
	val onDelete = NotebookActivity.LocalOnDelete.current
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
		showDeleteDialog = showDeleteDialog,
		selectedItemSize = selectedItemSize,
		onDismiss = { closeDialog(NotebookDialogType.DELETE) },
		onDelete = onDelete
	)
}
