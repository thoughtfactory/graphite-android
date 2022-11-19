package com.syncodec.graphite.presentation.atlas.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.atlas.AtlasActivity
import com.syncodec.graphite.presentation.common.LocalCompositionCloseDialog
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog
import com.syncodec.graphite.presentation.common.dialog.DialogType
import com.syncodec.graphite.presentation.common.dialog.WhereDialog


@Composable
fun AtlasDialog(
	parentChapter: ChapterObjectLite?,
) {
	val chapterList = AtlasActivity.LocalChapterList.current
	val chapterPath = AtlasActivity.LocalChapterPath.current

	val showDeleteDialog = AtlasActivity.LocalShowDeleteDialog.current
	val showWhereDialog = AtlasActivity.LocalShowWhereDialog.current

	val closeDialog = LocalCompositionCloseDialog.current

	val onDelete = AtlasActivity.LocalOnDelete.current

	val onWhere = AtlasActivity.LocalOnWhere.current
	val onSetWhere = AtlasActivity.LocalSetOnWhere.current

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
		message = "Are you sure you want to delete selected notes?",
		onDismiss = { closeDialog(DialogType.DELETE) },
	) {
		onDelete()
		closeDialog(DialogType.DELETE)
	}
}
