package com.syncodec.graphite.presentation.note.screen.viewerScreen.bottomSheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.realm.kotlin.types.RealmUUID


enum class ViewerBottomSheetType {
	Metadata,
	Share,
}

@Preview
@Composable
fun SheetLayout(
	bottomSheetType : ViewerBottomSheetType = ViewerBottomSheetType.Metadata,
	noteId : RealmUUID? = null,
	createdTimestamp : Long? = null,
	modifiedTimestamp : Long? = null,
) {
	when (bottomSheetType) {
		ViewerBottomSheetType.Metadata -> MetadataBottomSheet(
			noteId = noteId,
			createdTimestamp = createdTimestamp,
			modifiedTimestamp = modifiedTimestamp,
		)

		ViewerBottomSheetType.Share -> ShareBottomSheet()
	}
}
