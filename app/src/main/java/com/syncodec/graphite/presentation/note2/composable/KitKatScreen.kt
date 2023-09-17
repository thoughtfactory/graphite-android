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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.dialog.where.whereChapterDialog2.WhereChapterDialog2
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.note2.KitKat
import com.syncodec.graphite.presentation.note2.NoteViewModel2
import com.syncodec.graphite.presentation.note2.composable.bar.editor.EditorBottomBar
import com.syncodec.graphite.presentation.note2.composable.bar.editor.EditorTopBar
import com.syncodec.graphite.presentation.note2.composable.bar.viewer.ViewerBottomBar
import com.syncodec.graphite.presentation.note2.composable.bar.viewer.ViewerTopBar
import com.syncodec.graphite.presentation.note2.composable.bottomSheet.BottomSheet
import com.syncodec.graphite.presentation.note2.composable.bottomSheet.NoteBottomSheet
import com.syncodec.graphite.presentation.note2.composable.buildingBlock.ViewerHeader
import com.syncodec.graphite.presentation.note2.composable.dialog.LocationPickerDialog
import com.syncodec.graphite.utils.LocationData
import io.realm.kotlin.types.RealmUUID


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
	tagStateMap: Map<TagObject, NoteViewModel2.Companion.TagObjectState> = mapOf(),
	attachmentList : List<NoteViewModel2.Companion.AttachmentState> = listOf(),
	onClickSave: () -> Unit = {},
	onClickEdit: () -> Unit = {},
	onSetLocation: (LatLng, String?) -> Unit = { _, _ -> },
	onClickRemoveLocation: () -> Unit = {},
	onClickReloadLocation: () -> Unit = {},
	onAddNewAttachment: (List<Uri>) -> Unit = {},
	toggleAttachment: (NoteViewModel2.Companion.AttachmentState) -> Unit = {},
	onSelectChapter: (RealmUUID) -> Unit = {},
	onClickTag: (TagObject) -> Unit = {},
	putTag: (String, Color) -> Boolean = { _, _ -> false },
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
	onClickBack: () -> Unit = {},
) {
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp

	val kitKatFormat by kitKat.kitKatFormat.collectAsState()

	var isEditorMetadataBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isEditorLocationBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isAttachmentBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isTagsBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

	var isViewerMetadataBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isViewerLocationBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isExportBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

	var isLocationPickerDialogVisible by rememberSaveable { mutableStateOf(false) }
	var isWhereDialogVisible by rememberSaveable { mutableStateOf(false) }

	val scrollState = rememberScrollState()

	BackHandler(enabled = isEditing == true) { onClickSave() }

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
				onClickMetadata = { isViewerMetadataBottomSheetVisible = true },
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
					noteId = noteId,
					title = kitKatFormat.kitKatTitle,
					userTimestamp = userTimestamp,
					locationData = locationData,
					parentChapter = parentChapter,
					savedAttachmentList = attachmentList.filterIsInstance<NoteViewModel2.Companion.AttachmentState.Saved>(),
					connectedTagList = tagStateMap.filterValues { it == NoteViewModel2.Companion.TagObjectState.Saved }.keys,
					modifier = Modifier
						.fillMaxWidth()
						.background(MaterialTheme.colorScheme.background)
						.height(screenHeight / 3)
						.graphicsLayer { translationY = 0.71f * scrollState.value },
					onClickChapterSelector = { isWhereDialogVisible = true }
				)

				KitKatView(
					kitKat = kitKat,
					modifier = Modifier
						.fillMaxWidth()
						.wrapContentHeight(unbounded = true)
						.background(MaterialTheme.colorScheme.background)
				)
			}
		} else {
			KitKatView(
				kitKat = kitKat,
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.background)
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

	BottomSheet(
		isEditorMetadataBottomSheetVisible = isEditorMetadataBottomSheetVisible,
		isViewerMetadataBottomSheetVisible = isViewerMetadataBottomSheetVisible,
		isEditorLocationBottomSheetVisible = isEditorLocationBottomSheetVisible,
		isViewerLocationBottomSheetVisible = isViewerLocationBottomSheetVisible,
		isAttachmentBottomSheetVisible = isAttachmentBottomSheetVisible,
		isExportBottomSheetVisible = isExportBottomSheetVisible,
		isTagsBottomSheetVisible = isTagsBottomSheetVisible,
		kitKat = kitKat,
		noteId = noteId,
		createdTimestamp = createdTimestamp,
		modifiedTimestamp = modifiedTimestamp,
		parentChapter = parentChapter,
		locationData = locationData,
		allTagList = allTagList,
		tagStateMap = tagStateMap,
		attachmentList = attachmentList,
		onClickRemoveLocation = onClickRemoveLocation,
		onClickReloadLocation = onClickReloadLocation,
		onAddNewAttachment = onAddNewAttachment,
		toggleAttachment = toggleAttachment,
		onClickTag = onClickTag,
		putTag = putTag,
		closeLocationPickerDialog = { isLocationPickerDialogVisible = false },
		onDismissRequest = {
			when (it) {
				NoteBottomSheet.EditorMetadata -> isEditorMetadataBottomSheetVisible = false
				NoteBottomSheet.ViewerMetadata -> isViewerMetadataBottomSheetVisible = false
				NoteBottomSheet.EditorLocation -> isEditorLocationBottomSheetVisible = false
				NoteBottomSheet.ViewerLocation -> isViewerLocationBottomSheetVisible = false
				NoteBottomSheet.Attachment -> isAttachmentBottomSheetVisible = false
				NoteBottomSheet.Export -> isExportBottomSheetVisible = false
				NoteBottomSheet.Tag -> isTagsBottomSheetVisible = false
			}
		},
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

	WhereChapterDialog2(
		isDialogVisible = isWhereDialogVisible,
		onDismissRequest = { isWhereDialogVisible = false },
		currentSelectedChapter = parentChapter?.id,
		showEveryWhere = false,
		onSelectChapter = { it?.let(onSelectChapter) },
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
