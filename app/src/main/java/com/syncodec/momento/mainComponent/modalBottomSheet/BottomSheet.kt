package com.syncodec.momento.mainComponent.modalBottomSheet

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.mainComponent.MainViewModel


sealed class BottomSheetType {
	object MenuBottomSheet : BottomSheetType()
	object BucketBottomSheet : BottomSheetType()
	object NotebookBottomSheet : BottomSheetType()
}

@Composable
fun SheetLayout() {
	val viewModel: MainViewModel = viewModel()
	when (viewModel.mainActivityState.bottomSheetType.value) {
		BottomSheetType.MenuBottomSheet -> MenuBottomSheet()
		BottomSheetType.BucketBottomSheet -> BucketBottomSheet()
		BottomSheetType.NotebookBottomSheet -> NotebookBottomSheet()
	}
}
