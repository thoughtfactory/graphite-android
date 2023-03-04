package com.syncodec.graphite.presentation.note.screen.editorScreen.bottomSheet

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.TagObjectLite
import com.syncodec.graphite.utils.LocationData
import io.realm.kotlin.types.RealmUUID
import java.io.File


enum class EditorBottomSheetType {
	Metadata,
	Location,
	Attachment,
	Tag
}

@Preview
@Composable
fun SheetLayout(
	bottomSheetType : EditorBottomSheetType = EditorBottomSheetType.Metadata,
	noteId : RealmUUID? = null,
	createdTimestamp : Long? = null,
	modifiedTimestamp : Long? = null,
	parentChapterObject : ChapterObject? = null,
	locationData : LocationData = LocationData.Init,
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
	onSetLocationManually : () -> Unit = {},
) {
	when (bottomSheetType) {
		EditorBottomSheetType.Metadata -> MetadataBottomSheet(
			noteId = noteId,
			createdTimestamp = createdTimestamp,
			modifiedTimestamp = modifiedTimestamp,
			parentChapterObject = parentChapterObject,
			onClickSelectParentChapter = onClickSelectParentChapter,
		)

		EditorBottomSheetType.Location -> LocationBottomSheet(
			locationData = locationData,
			onRemoveLocation = onRemoveLocation,
			onReloadLocation = onReloadLocation,
			onSetLocationManually = onSetLocationManually,
		)

		EditorBottomSheetType.Attachment -> AttachmentBottomSheet(
			attachmentListSaved = attachmentListSaved,
			attachmentListToAdd = attachmentListToAdd,
			attachmentListToRemove = attachmentListToRemove,
			onAddAttachmentToBuffer = onAddAttachmentToBuffer,
			onRemoveBufferedAttachment = onRemoveBufferedAttachment,
			onRemoveSavedAttachment = onRemoveSavedAttachment,
		)

		EditorBottomSheetType.Tag -> TagBottomSheet(
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
