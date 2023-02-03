package com.syncodec.graphite.presentation.explorer.screen.explorerScreen.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog
import com.syncodec.graphite.presentation.common.dialog.whereDialog.WhereDialog
import com.syncodec.graphite.presentation.explorer.screen.searchScreen.dialog.SearchDialogType


enum class ExplorerDialogType {
	Where,
	Delete
}

@Preview
@Composable
fun ExplorerDialog(
	isWhereDialogVisible : Boolean = true,
	isDeleteDialogVisible : Boolean = true,
	searchInChapter : ChapterObjectLite? = null,
	onSetSearchInChapter : (ChapterObjectLite?) -> Unit = {},
	onDelete : () -> Unit = {},
	closeDialog : (ExplorerDialogType) -> Unit = {},
) {
	WhereDialog(
		showDialog = isWhereDialogVisible,
		onSetChapter = onSetSearchInChapter,
		onDismiss = { closeDialog(ExplorerDialogType.Where) },
	)

	DeleteDialog(
		showDialog = isDeleteDialogVisible,
		onDismiss = { closeDialog(ExplorerDialogType.Delete) },
	) {
		onDelete()
		closeDialog(ExplorerDialogType.Delete)
	}
}
