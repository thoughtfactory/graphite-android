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
	NOTEBOOK,
	BUCKET
}

@Composable
fun SheetLayout(
	bottomSheetType: MainBottomSheetType,
	sortOn : SortOn,
	sortBy : SortBy,
	onSortOnChanged : (SortOn) -> Unit,
	onSortByChanged : (SortBy) -> Unit,
	putNotebook: (String, String, Color?, Bitmap?) -> Unit,
	putBucket: (String?, String?, BucketType) -> Unit,
	closeSheet: () -> Unit
) {
	when (bottomSheetType) {
		MainBottomSheetType.MENU -> MenuBottomSheet(closeSheet = closeSheet)
		MainBottomSheetType.FILTER -> FilterBottomSheet(
			sortOn = sortOn,
			sortBy = sortBy,
			onUpdateSortOn = onSortOnChanged,
			onUpdateSortBy = onSortByChanged,
			closeSheet = closeSheet
		)
		MainBottomSheetType.NOTEBOOK -> NotebookBottomSheet(putNotebook, closeSheet = closeSheet)
		MainBottomSheetType.BUCKET -> BucketBottomSheet(putBucket, closeSheet = closeSheet)
	}
}
