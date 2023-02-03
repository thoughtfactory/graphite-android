package com.syncodec.graphite.presentation.explorer.screen.searchScreen.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog
import com.syncodec.graphite.presentation.common.dialog.whereDialog.WhereDialog


enum class SearchDialogType {
	Where,
	Delete,
}

@Preview
@Composable
fun SearchDialog(
	isWhereDialogVisible : Boolean = true,
	isDeleteDialogVisible : Boolean = true,
	searchInChapter : ChapterObjectLite? = null,
	onSetSearchInChapter : (ChapterObjectLite?) -> Unit = {},
	onDelete : () -> Unit = {},
	closeDialog : (SearchDialogType) -> Unit = {},
) {
	WhereDialog(
		showDialog = isWhereDialogVisible,
		onSetChapter = onSetSearchInChapter,
		onDismiss = { closeDialog(SearchDialogType.Where) },
	)

	DeleteDialog(
		showDialog = isDeleteDialogVisible,
		onDismiss = { closeDialog(SearchDialogType.Delete) },
	) {
		onDelete()
		closeDialog(SearchDialogType.Delete)
	}
}
