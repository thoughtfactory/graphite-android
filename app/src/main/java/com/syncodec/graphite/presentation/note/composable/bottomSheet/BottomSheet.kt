package com.syncodec.graphite.presentation.note.composable.bottomSheet

import android.net.Uri
import androidx.compose.runtime.Composable
import com.syncodec.graphite.di.model.AttachmentObject
import io.realm.kotlin.types.RealmUUID


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
	onRemoveAttachment: (AttachmentObject) -> Unit,
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
