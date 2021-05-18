package com.syncodec.momento.bucketComponent.modalBottomSheet


sealed class BottomSheetType {
	object AddBookSheet : BottomSheetType()
	object AddMovieSheet : BottomSheetType()
}
