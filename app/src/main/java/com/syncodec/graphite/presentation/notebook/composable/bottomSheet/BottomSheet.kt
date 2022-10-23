package com.syncodec.graphite.presentation.notebook.composable.bottomSheet

import androidx.compose.runtime.Composable
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.TagObject


enum class BucketBottomSheetType {
	MENU,
	CHAPTER,
	NOTE,
}

@Composable
fun SheetLayout(
	chapterObject : ChapterObject?,
	tagList : List<TagObject>,
	bottomSheetType: BucketBottomSheetType,
	closeSheet: () -> Unit
) {
	when (bottomSheetType) {
		BucketBottomSheetType.MENU -> MenuBottomSheet(chapterObject = chapterObject, tagList = tagList, closeSheet = closeSheet)
		BucketBottomSheetType.CHAPTER -> ChapterBottomSheet(closeSheet = closeSheet)
		BucketBottomSheetType.NOTE -> NoteBottomSheet(closeSheet = closeSheet)
	}
}
