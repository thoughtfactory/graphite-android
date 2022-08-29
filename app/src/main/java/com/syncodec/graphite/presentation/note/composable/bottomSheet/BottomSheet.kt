package com.syncodec.graphite.presentation.note.composable.bottomSheet

import androidx.compose.runtime.Composable


enum class NoteBottomSheetType {
	METADATA,
	ATTACHMENT,
	TAGS
}

@Composable
fun SheetLayout(
	bottomSheetType: NoteBottomSheetType,
	closeSheet: () -> Unit
) {
	when (bottomSheetType) {
		NoteBottomSheetType.METADATA -> MetadataBottomSheet(closeSheet = closeSheet)
		NoteBottomSheetType.ATTACHMENT -> AttachmentBottomSheet(closeSheet = closeSheet)
		NoteBottomSheetType.TAGS -> null
	}
}
