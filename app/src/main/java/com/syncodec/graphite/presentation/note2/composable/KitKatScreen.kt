package com.syncodec.graphite.presentation.note2.composable

import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.note2.KitKat
import com.syncodec.graphite.presentation.note2.composable.bar.editor.EditorBottomBar
import com.syncodec.graphite.presentation.note2.composable.bar.editor.EditorTopBar
import com.syncodec.graphite.presentation.note2.composable.bar.viewer.ViewerBottomBar
import com.syncodec.graphite.presentation.note2.composable.bar.viewer.ViewerTopBar
import com.syncodec.graphite.presentation.note2.composable.bottomSheet.AttachmentBottomSheet
import com.syncodec.graphite.presentation.note2.composable.bottomSheet.AttachmentViewerBottomSheet
import com.syncodec.graphite.presentation.note2.composable.bottomSheet.EditorLocationBottomSheet
import com.syncodec.graphite.presentation.note2.composable.bottomSheet.EditorMetadataBottomSheet
import com.syncodec.graphite.presentation.note2.composable.bottomSheet.ExportBottomSheet
import com.syncodec.graphite.presentation.note2.composable.bottomSheet.TagsBottomSheet
import com.syncodec.graphite.presentation.note2.composable.bottomSheet.ViewerLocationBottomSheet
import com.syncodec.graphite.presentation.note2.composable.bottomSheet.ViewerMetadataBottomSheet
import com.syncodec.graphite.presentation.note2.composable.buildingBlock.ViewerHeader
import com.syncodec.graphite.presentation.note2.composable.dialog.LocationPickerDialog
import com.syncodec.graphite.utils.LocationData
import com.syncodec.graphite.utils.export.ExportNote
import com.syncodec.graphite.utils.share
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch
import org.apache.commons.text.StringEscapeUtils
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun KitKatScreen(
	kitKat: KitKat = KitKat(LocalContext.current),
	noteId: RealmUUID? = null,
	isEditing: Boolean? = true,
	createdTimestamp: Long? = null,
	modifiedTimestamp: Long? = null,
	userTimestamp: Long? = null,
	locationData: LocationData = LocationData.Init,
	isFavourite: Boolean? = null,
	isLocked: Boolean? = null,
	parentChapter: ChapterObjectLite? = null,
	allTagList: List<TagObject> = listOf(),
	savedFileList: List<File> = listOf(),
	newFileList: List<Uri> = listOf(),
	toRemoveFileList: List<File> = listOf(),
	onClickSave: () -> Unit = {},
	onClickEdit: () -> Unit = {},
	onSetLocation: (LatLng, String?) -> Unit = { _, _ -> },
	onClickRemoveLocation: () -> Unit = {},
	onClickReloadLocation: () -> Unit = {},
	onAddNewFile: (List<Uri>) -> Unit = {},
	onRemoveNewFile: (List<Uri>) -> Unit = {},
	onRemoveSavedFile: (List<File>) -> Unit = {},
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
	onClickBack: () -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp

	val kitKatFormat by kitKat.kitKatFormat.collectAsState()

	val bottomSheetState = rememberModalBottomSheetState()
	val fullBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	var isEditorMetadataBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isEditorLocationBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isAttachmentBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isTagsBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

	var isViewerMetadataBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isViewerAttachmentBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isViewerLocationBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isExportBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

	var isLocationPickerDialogVisible by rememberSaveable { mutableStateOf(false) }

	val scrollState = rememberScrollState()

	BackHandler(enabled = isEditing == true) {
		onClickSave()
	}

	GenericScaffold2(
		topBar = {
			if (isEditing == true) EditorTopBar(
				onClickBack = onClickSave,
				onClickSave = onClickSave,
			) else ViewerTopBar(
				isFavourite = isFavourite == true,
				isLocked = isLocked == true,
				onClickLocalOnly = {},
				onClickFavourite = onClickFavourite,
				onClickLock = onClickLock,
				onClickPin = {},
				onClickDelete = {},
				onClickBack = onClickBack,
			)
		},
		bottomBar = {
			if (isEditing == true) EditorBottomBar(
				kitKatFormat = kitKatFormat,
				locationData = locationData,
				onClickMetadata = { isEditorMetadataBottomSheetVisible = true },
				noClickLocation = { isEditorLocationBottomSheetVisible = true },
				onClickAttachments = { isAttachmentBottomSheetVisible = true },
				onClickTags = { isTagsBottomSheetVisible = true },
				onKitKatAction = { kitKat.onKitKatAction(it) }
			) else ViewerBottomBar(
				attachmentCount = savedFileList.size,
				onClickMetadata = { isViewerMetadataBottomSheetVisible = true },
				onClickAttachment = { isViewerAttachmentBottomSheetVisible = true },
				onClickLocation = { isViewerLocationBottomSheetVisible = true },
				onClickExport = { isExportBottomSheetVisible = true },
				onClickEdit = onClickEdit,
			)
		}
	) {
		if (isEditing == false) {
			Column(
				modifier = Modifier.verticalScroll(scrollState)
			) {
				ViewerHeader(
					title = kitKatFormat.kitKatTitle,
					userTimestamp = userTimestamp,
					locationData = locationData,
					parentChapter = parentChapter,
					fileList = savedFileList,
					modifier = Modifier
						.fillMaxWidth()
						.background(MaterialTheme.colorScheme.background)
						.height(screenHeight / 3)
						.graphicsLayer { translationY = 0.71f * scrollState.value }
				)

				KitKatView(
					kitKat = kitKat,
					modifier = Modifier
						.fillMaxWidth()
						.wrapContentHeight(unbounded = true)
				)
			}
		} else {
			KitKatView(
				modifier = Modifier.fillMaxSize(),
				kitKat = kitKat,
			)
		}
		if (isEditing == null) {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.background)
			) {
				CircularProgressIndicator()
			}
		}
	}

	EditorMetadataBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isEditorMetadataBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide() }; isEditorMetadataBottomSheetVisible = false },
		noteId = noteId,
		createdTimestamp = createdTimestamp,
		modifiedTimestamp = modifiedTimestamp,
		parentId = parentChapter?.id,
	)

	ViewerMetadataBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isViewerMetadataBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide() }; isViewerMetadataBottomSheetVisible = false },
		noteId = noteId,
		createdTimestamp = createdTimestamp,
		modifiedTimestamp = modifiedTimestamp,
		parentId = parentChapter?.id,
	)

	EditorLocationBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isEditorLocationBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide() }; isEditorLocationBottomSheetVisible = false },
		locationData = locationData,
		onClickSelectLocation = {
			scope.launch { bottomSheetState.hide() }
			isEditorLocationBottomSheetVisible = false
			isLocationPickerDialogVisible = true
		},
		onClickRemoveLocation = onClickRemoveLocation,
		onClickReloadLocation = onClickReloadLocation,
	)

	ViewerLocationBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isViewerLocationBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide() }; isViewerLocationBottomSheetVisible = false },
		locationData = locationData,
	)

	AttachmentBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isAttachmentBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide() }; isAttachmentBottomSheetVisible = false },
		savedFileList = savedFileList,
		newFileList = newFileList,
		toRemoveFileList = toRemoveFileList,
		onAddNewFile = onAddNewFile,
		onRemoveNewFile = onRemoveNewFile,
		onRemoveSavedFile = onRemoveSavedFile,
	)

	AttachmentViewerBottomSheet(
		bottomSheetState = fullBottomSheetState,
		isBottomSheetVisible = isViewerAttachmentBottomSheetVisible,
		onDismissRequest = { scope.launch { fullBottomSheetState.hide() }; isViewerAttachmentBottomSheetVisible = false },
		savedFileList = savedFileList
	)

	ExportBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isExportBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide() }; isExportBottomSheetVisible = false },
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
		onDismissRequest = { scope.launch { bottomSheetState.hide() }; isTagsBottomSheetVisible = false },
		allTagList = allTagList,
	)

	LocationPickerDialog(
		isDialogVisible = isLocationPickerDialogVisible,
		onDismiss = { isLocationPickerDialogVisible = false },
		currentLocation = locationData.getLatLngOrNull(),
		onSetLocation = { latLng, address ->
			onSetLocation(latLng, address)
			isLocationPickerDialogVisible = false
		},
	)
}

@Preview
@Composable
private fun KitKatView(
	modifier: Modifier = Modifier,
	kitKat: KitKat = KitKat(LocalContext.current)
) {
	AndroidView(
		modifier = modifier,
		factory = { kitKat.also { if (it.parent != null) (it.parent as ViewGroup).removeView(it) } }
	)
}
