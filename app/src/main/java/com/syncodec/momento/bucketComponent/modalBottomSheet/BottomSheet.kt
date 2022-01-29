package com.syncodec.momento.bucketComponent.modalBottomSheet

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.bucketComponent.BucketViewModel

sealed class BottomSheetType {
	object AddBookSheet : BottomSheetType()
	object AddMovieSheet : BottomSheetType()
}

@Composable
fun SheetLayout() {
	val viewModel: BucketViewModel = viewModel()
	when (viewModel.bucketActivityState.bottomSheetType.value) {
		BottomSheetType.AddBookSheet -> AddBookSheet()
		BottomSheetType.AddMovieSheet -> AddMovieSheet()
	}
}
