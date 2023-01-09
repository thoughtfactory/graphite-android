package com.syncodec.graphite.presentation.search.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.common.dialog.whereDialog.WhereDialog
import com.syncodec.graphite.presentation.search.SearchActivity


enum class SearchDialogType {
	WHERE
}

@Composable
fun Dialog() {

	val parentChapter = SearchActivity.parentChapter.current

	val showWhereDialog = SearchActivity.showWhereDialog.current

	val closeDialog = SearchActivity.closeDialog.current

	val onSetWhere = SearchActivity.setOnWhere.current

	WhereDialog(
		showDialog = showWhereDialog,
		parentChapter = parentChapter,
		onSetChapter = {
			onSetWhere(it)
			closeDialog(SearchDialogType.WHERE)
		}
	) {
		closeDialog(SearchDialogType.WHERE)
	}
}
