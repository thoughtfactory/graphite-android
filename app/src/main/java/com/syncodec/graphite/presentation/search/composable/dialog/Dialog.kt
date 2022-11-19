package com.syncodec.graphite.presentation.search.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.common.dialog.WhereDialog
import com.syncodec.graphite.presentation.search.SearchActivity


enum class SearchDialogType {
	WHERE
}

@Composable
fun Dialog() {

	val parentChapter = SearchActivity.parentChapter.current
	val chapterList = SearchActivity.chapterList.current
	val chapterPath = SearchActivity.chapterPath.current

	val showWhereDialog = SearchActivity.showWhereDialog.current

	val closeDialog = SearchActivity.closeDialog.current

	val onWhere = SearchActivity.onWhere.current
	val onSetWhere = SearchActivity.setOnWhere.current

	WhereDialog(
		showDialog = showWhereDialog,
		parentChapter = parentChapter,
		chapterList = chapterList,
		chapterPath = chapterPath,
		onWhere = onWhere,
		onSetWhere = {
			onSetWhere(it)
			closeDialog(SearchDialogType.WHERE)
		}
	) {
		closeDialog(SearchDialogType.WHERE)
	}
}
