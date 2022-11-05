package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import androidx.compose.runtime.*


enum class BucketBottomSheetType {
	MENU,
	ADD_TODO,
	ADD_BOOK,
	ADD_SHOW,
	ADD_LINK,
	CURRENT_LINK
}

@Composable
fun SheetLayout(
	bottomSheetType: BucketBottomSheetType,
) {
	when (bottomSheetType) {
		BucketBottomSheetType.MENU -> MenuBottomSheet()
		BucketBottomSheetType.ADD_TODO -> AddTodoBottomSheet()
		BucketBottomSheetType.ADD_BOOK -> AddBookBottomSheet()
		BucketBottomSheetType.ADD_SHOW -> AddShowBottomSheet()
		BucketBottomSheetType.ADD_LINK -> AddLinkBottomSheet()
		BucketBottomSheetType.CURRENT_LINK -> CurrentLinkBottomSheet()
	}
}
