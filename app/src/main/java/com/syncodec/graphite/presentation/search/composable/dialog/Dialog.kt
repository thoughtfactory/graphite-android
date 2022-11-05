package com.syncodec.graphite.presentation.search.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.search.SearchActivity


enum class SearchDialogType {
	WHERE
}

@Composable
fun Dialog() {

	val currentChapter = SearchActivity.currentChapter.current
	val chapterList = SearchActivity.chapterList.current
	val chapterPath = SearchActivity.chapterPath.current

	val showWhereDialog = SearchActivity.showWhereDialog.current

	val closeDialog = SearchActivity.closeDialog.current

	val onWhere = SearchActivity.onWhere.current

	WhereDialog(
		showDialog = showWhereDialog,
		currentChapter = currentChapter,
		chapterList = chapterList,
		chapterPath = chapterPath,
		onWhere = onWhere,
	) {
		closeDialog(SearchDialogType.WHERE)
	}
}
