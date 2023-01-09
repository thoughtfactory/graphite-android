package com.syncodec.graphite.presentation.calendar.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.calendar.CalendarActivity
import com.syncodec.graphite.presentation.common.LocalCompositionCloseDialog
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog
import com.syncodec.graphite.presentation.common.dialog.DialogType
import com.syncodec.graphite.presentation.common.dialog.whereDialog.WhereDialog


@Composable
fun CalendarDialog(
	parentChapter: ChapterObjectLite?,
) {
	val showDeleteDialog = CalendarActivity.LocalShowDeleteDialog.current
	val showWhereDialog = CalendarActivity.LocalShowWhereDialog.current

	val closeDialog = LocalCompositionCloseDialog.current

	val onDelete = CalendarActivity.LocalOnDelete.current

	val onSetWhere = CalendarActivity.LocalSetOnWhere.current

	WhereDialog(
		showDialog = showWhereDialog,
		parentChapter = parentChapter,
		onSetChapter = onSetWhere
	) { closeDialog(DialogType.WHERE) }


	DeleteDialog(
		showDialog = showDeleteDialog,
		message = "Are you sure you want to delete selected items?",
		onDismiss = { closeDialog(DialogType.DELETE) },
	) {
		onDelete()
		closeDialog(DialogType.DELETE)
	}
}
