package com.syncodec.graphite.presentation.note.composable.bottomSheet

import android.net.Uri
import androidx.compose.runtime.Composable
import io.realm.kotlin.types.ObjectId


enum class NoteBottomSheetType {
	MENU,
	METADATA,
	LOCATION,
	ATTACHMENT,
}

@Composable
fun SheetLayout(
	bottomSheetType: NoteBottomSheetType,
	onAddAttachmentToBuffer: (List<Uri>) -> Unit,
	onRemoveAttachment: (ObjectId) -> Unit,
	onUpdateTitle: (String?) -> Unit,
	onRemoveLocation : () -> Unit,
	onReloadLocation : () -> Unit,
) {
	when (bottomSheetType) {
		NoteBottomSheetType.MENU -> MenuBottomSheet(false)
		NoteBottomSheetType.METADATA -> MetadataBottomSheet(onUpdateTitle = onUpdateTitle)
		NoteBottomSheetType.LOCATION -> LocationBottomSheet(
			onRemoveLocation = onRemoveLocation,
			onReloadLocation = onReloadLocation,
		)
		NoteBottomSheetType.ATTACHMENT -> AttachmentBottomSheet(
			onAddAttachmentToBuffer = onAddAttachmentToBuffer,
			onRemoveAttachment = onRemoveAttachment,
		)
	}
}
