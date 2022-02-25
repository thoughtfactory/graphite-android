package com.syncodec.momento.bucketComponent.modalBottomSheet

import androidx.compose.runtime.Composable


sealed class BottomSheetType {
	object AddBookSheet : BottomSheetType()
//	object AddMovieSheet : BottomSheetType()
}

@Composable
fun SheetLayout(
	bottomSheetType: BottomSheetType
) {
//	when (bottomSheetType) {
//		BottomSheetType.AddBookSheet -> AddBookSheet()
//	}
}
