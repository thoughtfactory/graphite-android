package com.syncodec.graphite.presentation.notebook.composable.bottomSheet

import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.main.composable.bottomSheet.FilterBottomSheet


enum class NotebookBottomSheetType {
	MENU,
	CHAPTER,
	NOTE,
	FILTER
}

@Composable
fun SheetLayout(
	bottomSheetType : NotebookBottomSheetType
) {
	when (bottomSheetType) {
		NotebookBottomSheetType.MENU -> MenuBottomSheet()
		NotebookBottomSheetType.CHAPTER -> ChapterBottomSheet()
		NotebookBottomSheetType.NOTE -> NoteBottomSheet()
		NotebookBottomSheetType.FILTER -> FilterBottomSheet()
	}
}
