package com.syncodec.graphite.presentation.note.composable.bottomSheet

import androidx.compose.runtime.Composable


enum class NoteBottomSheetType {
	MENU,
	METADATA,
	LOCATION,
	ATTACHMENT,
}

@Composable
fun SheetLayout(
	bottomSheetType: NoteBottomSheetType,
	closeSheet: () -> Unit
) {
	when (bottomSheetType) {
		NoteBottomSheetType.MENU -> MenuBottomSheet(closeSheet = closeSheet)
		NoteBottomSheetType.METADATA -> MetadataBottomSheet(closeSheet = closeSheet)
		NoteBottomSheetType.LOCATION -> LocationBottomSheet(closeSheet = closeSheet)
		NoteBottomSheetType.ATTACHMENT -> AttachmentBottomSheet(closeSheet = closeSheet)
	}
}
