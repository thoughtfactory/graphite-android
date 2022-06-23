package com.syncodec.graphite.noteComponent.modalBottomSheet

import android.net.Uri
import androidx.compose.runtime.Composable
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.syncodec.graphite.database.attachment.AttachmentDbEntry
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.database.tag.TagDbEntry
import com.syncodec.graphite.noteComponent.NoteActivity

sealed class BottomSheetType {
	object MetadataBottomSheet : BottomSheetType()
	object AttachmentBottomSheet : BottomSheetType()
	object TagBottomSheet : BottomSheetType()
}

@Composable
fun SheetLayout(
	bottomSheetType: BottomSheetType,
	key: String?,
	title: String?,
	createdTimestamp: Long,
	modifiedTimestamp: Long,
	latLng: LatLng?,
	address: String?,
	tagList: List<TagDbEntry>,
	connectedTag: List<String>,
	attachmentMap: Map<String, Pair<AttachmentDbEntry, Uri?>>,
	addressState: NoteActivity.AddressState,
	mapView: MapView,
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	when (bottomSheetType) {
		BottomSheetType.MetadataBottomSheet -> MetadataBottomSheet(
			key = key,
			title = title,
			createdTimestamp = createdTimestamp,
			modifiedTimestamp = modifiedTimestamp,
			latLng = latLng,
			address = address,
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
