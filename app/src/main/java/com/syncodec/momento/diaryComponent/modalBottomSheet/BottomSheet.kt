package com.syncodec.momento.diaryComponent.modalBottomSheet

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.diaryComponent.DiaryViewModel

sealed class BottomSheetType {
	object MenuBottomSheet : BottomSheetType()
	object MetadataBottomSheet : BottomSheetType()
	object AttachmentBottomSheet : BottomSheetType()
}

@Composable
fun SheetLayout() {
	val viewModel: DiaryViewModel = viewModel()
	when (viewModel.diaryActivityState.bottomSheetType.value) {
		BottomSheetType.MenuBottomSheet -> MenuBottomSheet()
		BottomSheetType.MetadataBottomSheet -> MetadataBottomSheet()
		BottomSheetType.AttachmentBottomSheet -> AttachmentBottomSheet()
	}
}
