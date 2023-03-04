package com.syncodec.graphite.presentation.common.dialog.whereDialog.composable.bottomSheet

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview


enum class WhereBottomSheetType {
	Chapter
}

@Preview
@Composable
fun SheetLayout(
	bottomSheetType : WhereBottomSheetType = WhereBottomSheetType.Chapter,
	putChapter : (String, String, Color?, Bitmap?) -> Unit = { _, _, _, _ -> },
) {
	when (bottomSheetType) {
		WhereBottomSheetType.Chapter -> ChapterBottomSheet(putChapter = putChapter)
	}
}
