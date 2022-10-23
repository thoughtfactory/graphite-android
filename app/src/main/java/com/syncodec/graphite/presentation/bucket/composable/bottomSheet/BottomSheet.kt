package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import androidx.compose.runtime.*
import com.syncodec.graphite.di.model.BucketObject


enum class BucketBottomSheetType {
	MENU,
	ADD_TODO,
	ADD_BOOK,
	ADD_SHOW,
	ADD_LINK
}

@Composable
fun SheetLayout(
	bucketObject : BucketObject?,
	bottomSheetType: BucketBottomSheetType,
	closeSheet: () -> Unit
) {
	when (bottomSheetType) {
		BucketBottomSheetType.MENU -> MenuBottomSheet(closeSheet = closeSheet)
		BucketBottomSheetType.ADD_TODO -> AddTodoBottomSheet(bucketObject = bucketObject, closeSheet = closeSheet)
		BucketBottomSheetType.ADD_BOOK -> AddBookBottomSheet(bucketObject = bucketObject, closeSheet = closeSheet)
		BucketBottomSheetType.ADD_SHOW -> AddShowBottomSheet(bucketObject = bucketObject, closeSheet = closeSheet)
		BucketBottomSheetType.ADD_LINK -> AddLinkBottomSheet(bucketObject = bucketObject, closeSheet = closeSheet)
	}
}
