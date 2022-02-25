package com.syncodec.momento.noteComponent.modalBottomSheet

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.noteComponent.NoteViewModel

sealed class BottomSheetType {
	object MenuBottomSheet : BottomSheetType()
	object MetadataBottomSheet : BottomSheetType()
	object AttachmentBottomSheet : BottomSheetType()
}

@Composable
fun SheetLayout() {
	val noteViewModel: NoteViewModel = viewModel()
	when (noteViewModel.activityState.bottomSheetType.value) {
		BottomSheetType.MenuBottomSheet -> MenuBottomSheet()
		BottomSheetType.MetadataBottomSheet -> MetadataBottomSheet()
		BottomSheetType.AttachmentBottomSheet -> AttachmentBottomSheet()
	}
}
