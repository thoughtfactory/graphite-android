package com.syncodec.graphite.presentation.note2.composable.bottomSheet

import android.net.Uri
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.note2.kitKat.KitKat
import com.syncodec.graphite.presentation.note2.NoteViewModel2
import com.syncodec.graphite.presentation.note2.kitKat.KitKatAction
import com.syncodec.graphite.utils.LocationData
import com.syncodec.graphite.utils.export.ExportNote
import com.syncodec.graphite.utils.share
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch
import org.apache.commons.text.StringEscapeUtils


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
	isEditorMetadataBottomSheetVisible: Boolean = false,
	isViewerMetadataBottomSheetVisible: Boolean = false,
	isEditorLocationBottomSheetVisible: Boolean = false,
	isViewerLocationBottomSheetVisible: Boolean = false,
	isAttachmentBottomSheetVisible: Boolean = false,
	isExportBottomSheetVisible: Boolean = false,
	isTagsBottomSheetVisible: Boolean = false,
	kitKat: KitKat = KitKat(context = LocalContext.current),
	noteId: RealmUUID? = null,
	createdTimestamp: Long? = null,
	modifiedTimestamp: Long? = null,
	parentChapter: ChapterObjectLite? = null,
	locationData: LocationData = LocationData.Init,
	allTagList: List<TagObject> = listOf(),
	tagStateMap: Map<TagObject, NoteViewModel2.Companion.TagObjectState> = mapOf(),
	attachmentList: List<NoteViewModel2.Companion.AttachmentState> = listOf(),
	onClickRemoveLocation: () -> Unit = {},
	onClickReloadLocation: () -> Unit = {},
	onAddNewAttachment: (List<Uri>) -> Unit = {},
	toggleAttachment: (NoteViewModel2.Companion.AttachmentState) -> Unit = {},
	onClickTag: (TagObject) -> Unit = {},
	putTag: (String, Color) -> Boolean = { _, _ -> false },
	closeLocationPickerDialog: () -> Unit = {},
	onDismissRequest: (NoteBottomSheet) -> Unit = {}
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
		attachmentList = attachmentList,
		onAddNewAttachment = onAddNewAttachment,
		toggleAttachment = toggleAttachment,
	)

	ExportBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isExportBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); onDismissRequest(NoteBottomSheet.Export) } },
		onClickExportAsTxt = {
			kitKat.onKitKatActionAsync(KitKatAction.Export.Text {
				val textString = StringEscapeUtils.unescapeJava(it)
				ExportNote.exportData(context = context, dataString = textString, noteId = noteId?.toString() ?: "note", ext = "txt")
			})
		},
		onClickExportAsPdf = { noteId?.toString()?.let { kitKat.print(it) } },
		onClickExportAsHtml = {
			kitKat.onKitKatActionAsync(KitKatAction.Export.Html {
				val htmlString = StringEscapeUtils.unescapeJava(it)
				ExportNote.exportData(context = context, dataString = htmlString, noteId = noteId?.toString() ?: "note", ext = "html")
			})
		},
		onClickExportAsJson = {
			kitKat.onKitKatActionAsync(KitKatAction.Export.Json {
				val jsonString = StringEscapeUtils.unescapeJava(it)
				ExportNote.exportData(context = context, dataString = jsonString, noteId = noteId?.toString() ?: "note", ext = "json")
			})
		},
		onClickExportAsMarkdown = {
			kitKat.onKitKatActionAsync(KitKatAction.Export.Markdown {
				val markdownString = StringEscapeUtils.unescapeJava(it)
				ExportNote.exportData(context = context, dataString = markdownString, noteId = noteId?.toString() ?: "note", ext = "md")
			})
		},
		onClickExportAttachments = { attachmentList.filterIsInstance<NoteViewModel2.Companion.AttachmentState.Saved>().map { it.file }.share(context = context) },
	)

	TagsBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isTagsBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); onDismissRequest(NoteBottomSheet.Tag) } },
		allTagList = allTagList,
		tagStateMap = tagStateMap,
		onClickTag = onClickTag,
		putTag = putTag,
	)
}
