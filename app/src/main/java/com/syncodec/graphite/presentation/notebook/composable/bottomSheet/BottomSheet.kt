package com.syncodec.graphite.presentation.notebook.composable.bottomSheet

import androidx.compose.runtime.Composable


enum class BucketBottomSheetType {
	MENU,
}

@Composable
fun SheetLayout(
	bottomSheetType: BucketBottomSheetType,
	closeSheet: () -> Unit
) {
	when (bottomSheetType) {
		BucketBottomSheetType.MENU -> MenuBottomSheet(closeSheet = closeSheet)
	}
}
