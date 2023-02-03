package com.syncodec.graphite.presentation.note.screen.editorScreen.bottomSheet

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.TagObjectLite
import com.syncodec.graphite.utils.LocationDataState
import io.realm.kotlin.types.RealmUUID
import java.io.File


enum class EditorBottomSheetType {
	METADATA,
	LOCATION,
	ATTACHMENT,
	TAG
}

@Preview
@Composable
fun SheetLayout(
	bottomSheetType : EditorBottomSheetType = EditorBottomSheetType.METADATA,
	noteId : RealmUUID? = null,
	createdTimestamp : Long? = null,
	modifiedTimestamp : Long? = null,
	latLng : LatLng? = null,
	address : String? = null,
	parentChapterObject : ChapterObject? = null,
	locationDataState : LocationDataState = LocationDataState.INIT,
	attachmentListSaved : List<File> = listOf(),
	attachmentListToAdd : List<Uri> = listOf(),
	attachmentListToRemove : List<File> = listOf(),
	tagList : List<TagObjectLite> = listOf(),
	tagListSaved : List<TagObjectLite> = listOf(),
	tagListToAdd : List<TagObjectLite> = listOf(),
	tagListToRemove : List<TagObjectLite> = listOf(),
	onClickSelectParentChapter : () -> Unit = {},
	onAddAttachmentToBuffer : (List<Uri>) -> Unit = {},
	onRemoveBufferedAttachment : (Uri) -> Unit = {},
	onRemoveSavedAttachment : (File) -> Unit = {},
	onAddTagToBuffer : (TagObjectLite) -> Unit = {},
	onRemoveBufferedTag : (TagObjectLite) -> Unit = {},
	onRemoveSavedTag : (TagObjectLite) -> Unit = {},
	onRemoveLocation : () -> Unit = {},
	onReloadLocation : () -> Unit = {},
) {
	when (bottomSheetType) {
		EditorBottomSheetType.METADATA -> MetadataBottomSheet(
			noteId = noteId,
			createdTimestamp = createdTimestamp,
			modifiedTimestamp = modifiedTimestamp,
			parentChapterObject = parentChapterObject,
			onClickSelectParentChapter = onClickSelectParentChapter,
		)

		EditorBottomSheetType.LOCATION -> LocationBottomSheet(
			locationDataState = locationDataState,
			latLng = latLng,
			address = address,
			onRemoveLocation = onRemoveLocation,
			onReloadLocation = onReloadLocation,
		)

		EditorBottomSheetType.ATTACHMENT -> AttachmentBottomSheet(
			attachmentListSaved = attachmentListSaved,
			attachmentListToAdd = attachmentListToAdd,
			attachmentListToRemove = attachmentListToRemove,
			onAddAttachmentToBuffer = onAddAttachmentToBuffer,
			onRemoveBufferedAttachment = onRemoveBufferedAttachment,
			onRemoveSavedAttachment = onRemoveSavedAttachment,
		)

		EditorBottomSheetType.TAG -> TagBottomSheet(
			tagList = tagList,
			tagListSaved = tagListSaved,
			tagListToAdd = tagListToAdd,
			tagListToRemove = tagListToRemove,
			onAddTagToBuffer = onAddTagToBuffer,
			onRemoveBufferedTag = onRemoveBufferedTag,
			onRemoveSavedTag = onRemoveSavedTag,
		)
	}
}
