package com.syncodec.graphite.presentation.note.composable.bottomSheet

import android.net.Uri
import androidx.compose.runtime.Composable
import java.io.File


enum class NoteBottomSheetType {
	MENU,
	METADATA,
	LOCATION,
	ATTACHMENT,
	TAG
}

@Composable
fun SheetLayout(
	bottomSheetType: NoteBottomSheetType,
	onAddAttachmentToBuffer: (List<Uri>) -> Unit,
	onRemoveAttachment: (File?, Uri?) -> Unit,
	onUpdateTitle: (String?) -> Unit,
	onRemoveLocation : () -> Unit,
	onReloadLocation : () -> Unit,
) {
	when (bottomSheetType) {
		NoteBottomSheetType.MENU -> MenuBottomSheet()
		NoteBottomSheetType.METADATA -> MetadataBottomSheet(onUpdateTitle = onUpdateTitle)
		NoteBottomSheetType.LOCATION -> LocationBottomSheet(
			onRemoveLocation = onRemoveLocation,
			onReloadLocation = onReloadLocation,
		)
		NoteBottomSheetType.ATTACHMENT -> AttachmentBottomSheet(
			onAddAttachmentToBuffer = onAddAttachmentToBuffer,
			onRemoveAttachment = onRemoveAttachment,
		)
		NoteBottomSheetType.TAG -> TagBottomSheet()
	}
}
