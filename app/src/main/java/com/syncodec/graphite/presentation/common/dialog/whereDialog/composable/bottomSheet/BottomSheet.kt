package com.syncodec.graphite.presentation.common.dialog.whereDialog.composable.bottomSheet

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview


enum class WhereBottomSheetType {
	CHAPTER
}

@Preview
@Composable
fun SheetLayout(
	bottomSheetType : WhereBottomSheetType = WhereBottomSheetType.CHAPTER,
	putChapter : (String, String, Color?, Bitmap?) -> Unit = { _, _, _, _ -> },
) {
	when (bottomSheetType) {
		WhereBottomSheetType.CHAPTER -> ChapterBottomSheet(putChapter = putChapter)
	}
}
