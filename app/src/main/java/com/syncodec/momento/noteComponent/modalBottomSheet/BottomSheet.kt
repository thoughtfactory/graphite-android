package com.syncodec.momento.noteComponent.modalBottomSheet

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.MapView
import com.syncodec.momento.database.attachment.AttachmentDbEntry
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.database.tag.TagDbEntry
import com.syncodec.momento.noteComponent.NoteActivity
import com.syncodec.momento.noteComponent.NoteViewModel

sealed class BottomSheetType {
	object MenuBottomSheet : BottomSheetType()
	object MetadataBottomSheet : BottomSheetType()
	object AttachmentBottomSheet : BottomSheetType()
	object TagBottomSheet : BottomSheetType()
}

@Composable
fun SheetLayout(
	bottomSheetType: BottomSheetType,
	note: NoteDbEntry?,
	tagList: List<TagDbEntry>,
	connectedTag: List<String>,
	attachmentMap: Map<String, Pair<AttachmentDbEntry, Uri>>,
	addressState: NoteActivity.AddressState,
	mapView: MapView,
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	when (bottomSheetType) {
		BottomSheetType.MenuBottomSheet -> MenuBottomSheet(onAction = onAction)
		BottomSheetType.MetadataBottomSheet -> MetadataBottomSheet(
			note = note,
			addressState = addressState,
			mapView = mapView,
			onAction = onAction
		)
		BottomSheetType.AttachmentBottomSheet -> AttachmentBottomSheet(
			attachmentMap = attachmentMap,
			onAction = onAction
		)
		BottomSheetType.TagBottomSheet -> TagBottomSheet(
			tagList = tagList,
			connectedTag = connectedTag,
			onAction = onAction
		)
	}
}
