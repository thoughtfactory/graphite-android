package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import androidx.compose.runtime.*


enum class BucketBottomSheetType {
	MENU,
	ADD_TODO,
	ADD_BOOK,
	ADD_SHOW,
	ADD_LINK
}

@Composable
fun SheetLayout(
	bottomSheetType: BucketBottomSheetType,
	closeSheet: () -> Unit
) {
	when (bottomSheetType) {
		BucketBottomSheetType.MENU -> MenuBottomSheet(closeSheet = closeSheet)
		BucketBottomSheetType.ADD_TODO -> AddTodoBottomSheet(closeSheet = closeSheet)
		BucketBottomSheetType.ADD_BOOK -> AddBookBottomSheet(closeSheet = closeSheet)
		BucketBottomSheetType.ADD_SHOW -> AddShowBottomSheet(closeSheet = closeSheet)
		BucketBottomSheetType.ADD_LINK -> AddLinkBottomSheet(closeSheet = closeSheet)
	}
}
