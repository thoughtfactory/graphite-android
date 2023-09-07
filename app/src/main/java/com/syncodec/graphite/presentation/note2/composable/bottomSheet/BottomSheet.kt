package com.syncodec.graphite.presentation.note2.composable.bottomSheet

import android.net.Uri
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.note2.KitKat
import com.syncodec.graphite.presentation.note2.NoteViewModel2
import com.syncodec.graphite.utils.LocationData
import com.syncodec.graphite.utils.export.ExportNote
import com.syncodec.graphite.utils.share
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch
import org.apache.commons.text.StringEscapeUtils
import java.io.File


enum class NoteBottomSheet {
	EditorMetadata,
	ViewerMetadata,
	EditorLocation,
	ViewerLocation,
	Attachment,
	Export,
	Tag,
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun BottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isEditorMetadataBottomSheetVisible : Boolean = false,
	isViewerMetadataBottomSheetVisible : Boolean = false,
	isEditorLocationBottomSheetVisible : Boolean = false,
	isViewerLocationBottomSheetVisible : Boolean = false,
	isAttachmentBottomSheetVisible : Boolean = false,
	isExportBottomSheetVisible : Boolean = false,
	isTagsBottomSheetVisible : Boolean = false,
	kitKat: KitKat = KitKat(context = LocalContext.current),
	noteId : RealmUUID? = null,
	createdTimestamp : Long? = null,
	modifiedTimestamp : Long? = null,
	parentChapter : ChapterObjectLite? = null,
	locationData : LocationData = LocationData.Init,
	allTagList: List<TagObject> = listOf(),
	tagStateMap : Map<TagObject, NoteViewModel2.Companion.TagObjectState> = mapOf(),
	savedFileList: List<File> = listOf(),
	newFileList: List<Uri> = listOf(),
	toRemoveFileList: List<File> = listOf(),
	onClickRemoveLocation: () -> Unit = {},
	onClickReloadLocation: () -> Unit = {},
	onAddNewFile: (List<Uri>) -> Unit = {},
	onRemoveNewFile: (List<Uri>) -> Unit = {},
	onRemoveSavedFile: (List<File>) -> Unit = {},
	onClickTag: (TagObject) -> Unit = {},
	closeLocationPickerDialog : () -> Unit = {},
	onDismissRequest : (NoteBottomSheet) -> Unit = {}
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	EditorMetadataBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isEditorMetadataBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); onDismissRequest(NoteBottomSheet.EditorMetadata) } },
		noteId = noteId,
		createdTimestamp = createdTimestamp,
		modifiedTimestamp = modifiedTimestamp,
		parentId = parentChapter?.id,
	)

	ViewerMetadataBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isViewerMetadataBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); onDismissRequest(NoteBottomSheet.ViewerMetadata) } },
		noteId = noteId,
		createdTimestamp = createdTimestamp,
		modifiedTimestamp = modifiedTimestamp,
		parentId = parentChapter?.id,
	)

	EditorLocationBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isEditorLocationBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); onDismissRequest(NoteBottomSheet.EditorLocation) } },
		locationData = locationData,
		onClickSelectLocation = {
			scope.launch {
				closeLocationPickerDialog()
				bottomSheetState.hide()
				onDismissRequest(NoteBottomSheet.EditorLocation)
			}
		},
		onClickRemoveLocation = onClickRemoveLocation,
		onClickReloadLocation = onClickReloadLocation,
	)

	ViewerLocationBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isViewerLocationBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); onDismissRequest(NoteBottomSheet.ViewerLocation) } },
		locationData = locationData,
	)

	AttachmentBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isAttachmentBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); onDismissRequest(NoteBottomSheet.Attachment) } },
		savedFileList = savedFileList,
		newFileList = newFileList,
		toRemoveFileList = toRemoveFileList,
		onAddNewFile = onAddNewFile,
		onRemoveNewFile = onRemoveNewFile,
		onRemoveSavedFile = onRemoveSavedFile,
	)

	ExportBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isExportBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); onDismissRequest(NoteBottomSheet.Export) } },
		onClickExportAsTxt = {
			kitKat.onKitKatAction(KitKat.Companion.KitKatAction.Export.Text {
				val textString = StringEscapeUtils.unescapeJava(it)
				ExportNote.exportData(context = context, dataString = textString, noteId = noteId?.toString() ?: "note", ext = "txt")
			})
		},
		onClickExportAsPdf = { noteId?.toString()?.let { kitKat.print(it) } },
		onClickExportAsHtml = {
			kitKat.onKitKatAction(KitKat.Companion.KitKatAction.Export.Html {
				val htmlString = StringEscapeUtils.unescapeJava(it)
				ExportNote.exportData(context = context, dataString = htmlString, noteId = noteId?.toString() ?: "note", ext = "html")
			})
		},
		onClickExportAsJson = {
			kitKat.onKitKatAction(KitKat.Companion.KitKatAction.Export.Json {
				val jsonString = StringEscapeUtils.unescapeJava(it)
				ExportNote.exportData(context = context, dataString = jsonString, noteId = noteId?.toString() ?: "note", ext = "json")
			})
		},
		onClickExportAsMarkdown = {
			kitKat.onKitKatAction(KitKat.Companion.KitKatAction.Export.Markdown {
				val markdownString = StringEscapeUtils.unescapeJava(it)
				ExportNote.exportData(context = context, dataString = markdownString, noteId = noteId?.toString() ?: "note", ext = "md")
			})
		},
		onClickExportAttachments = { savedFileList.share(context = context) },
	)

	TagsBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isTagsBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); onDismissRequest(NoteBottomSheet.Tag) } },
		allTagList = allTagList,
		tagStateMap = tagStateMap,
		onClickTag = onClickTag,
	)
}
