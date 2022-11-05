package com.syncodec.graphite.presentation.main.composable.bottomSheet

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.ViewType


enum class MainBottomSheetType {
	MENU,
	FILTER,
	BUCKET,
	NOTEBOOK
}

@Composable
fun SheetLayout(
	bottomSheetType: MainBottomSheetType,
	putNotebook: (String, String, Color?, Bitmap?) -> Unit,
) {
	when (bottomSheetType) {
		MainBottomSheetType.MENU -> MenuBottomSheet()
		MainBottomSheetType.FILTER -> FilterBottomSheet()
		MainBottomSheetType.BUCKET -> BucketBottomSheet()
		MainBottomSheetType.NOTEBOOK -> NotebookBottomSheet(putNotebook)
	}
}
