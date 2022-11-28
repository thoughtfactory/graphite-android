package com.syncodec.graphite.presentation.calendar.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.calendar.CalendarActivity
import com.syncodec.graphite.presentation.common.LocalCompositionCloseDialog
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog
import com.syncodec.graphite.presentation.common.dialog.DialogType
import com.syncodec.graphite.presentation.common.dialog.WhereDialog


@Composable
fun CalendarDialog(
	parentChapter: ChapterObjectLite?,
) {
	val chapterList = CalendarActivity.LocalChapterList.current
	val chapterPath = CalendarActivity.LocalChapterPath.current

	val showDeleteDialog = CalendarActivity.LocalShowDeleteDialog.current
	val showWhereDialog = CalendarActivity.LocalShowWhereDialog.current

	val closeDialog = LocalCompositionCloseDialog.current

	val onDelete = CalendarActivity.LocalOnDelete.current

	val onWhere = CalendarActivity.LocalOnWhere.current
	val onSetWhere = CalendarActivity.LocalSetOnWhere.current

	WhereDialog(
		showDialog = showWhereDialog,
		parentChapter = parentChapter,
		chapterList = chapterList,
		chapterPath = chapterPath,
		onWhere = onWhere,
		onSetWhere = onSetWhere
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
