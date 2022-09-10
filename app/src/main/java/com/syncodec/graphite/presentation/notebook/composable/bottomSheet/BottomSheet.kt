package com.syncodec.graphite.presentation.notebook.composable.bottomSheet

import androidx.compose.runtime.Composable


enum class BucketBottomSheetType {
	MENU,
	CHAPTER,
	NOTE,
}

@Composable
fun SheetLayout(
	bottomSheetType: BucketBottomSheetType,
	closeSheet: () -> Unit
) {
	when (bottomSheetType) {
		BucketBottomSheetType.MENU -> MenuBottomSheet(closeSheet = closeSheet)
		BucketBottomSheetType.CHAPTER -> ChapterBottomSheet(closeSheet = closeSheet)
		BucketBottomSheetType.NOTE -> NoteBottomSheet(closeSheet = closeSheet)
	}
}
