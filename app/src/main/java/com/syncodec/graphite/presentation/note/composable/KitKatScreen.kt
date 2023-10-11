package com.syncodec.graphite.presentation.note.composable

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.notification.NotePinNotification
import com.syncodec.graphite.presentation.common.dialog.dialog2.DeleteDialog
import com.syncodec.graphite.presentation.common.dialog.where.whereChapterDialog2.WhereChapterDialog2
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.note.kitKat.KitKat
import com.syncodec.graphite.presentation.note.NoteViewModel
import com.syncodec.graphite.presentation.note.composable.bar.editor.EditorBottomBar
import com.syncodec.graphite.presentation.note.composable.bar.editor.EditorTopBar
import com.syncodec.graphite.presentation.note.composable.bar.editor.dialog.DateTimePickerDialog
import com.syncodec.graphite.presentation.note.composable.bar.viewer.ViewerBottomBar
import com.syncodec.graphite.presentation.note.composable.bar.viewer.ViewerTopBar
import com.syncodec.graphite.presentation.note.composable.bottomSheet.BottomSheet
import com.syncodec.graphite.presentation.note.composable.bottomSheet.NoteBottomSheet
import com.syncodec.graphite.presentation.note.composable.buildingBlock.ViewerHeader
import com.syncodec.graphite.presentation.note.composable.dialog.LocationPickerDialog
import com.syncodec.graphite.utils.LocationData
import io.realm.kotlin.types.RealmUUID
import java.time.Instant


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun KitKatScreen(
	kitKat: KitKat = KitKat(LocalContext.current),
	isEditing: Boolean? = true,
	noteObject : NoteObject? = null,
	locationData: LocationData = LocationData.Init,
	parentChapter: ChapterObjectLite? = null,
	allTagList: List<TagObject> = listOf(),
	tagStateMap: Map<TagObject, NoteViewModel.Companion.TagObjectState> = mapOf(),
	attachmentList: List<NoteViewModel.Companion.AttachmentState> = listOf(),
	onClickSave: () -> Unit = {},
	onClickEdit: () -> Unit = {},
	onSetUserTimestamp: (Long) -> Unit = {},
	onSetLocation: (LatLng, String?) -> Unit = { _, _ -> },
	onClickRemoveLocation: () -> Unit = {},
	onClickReloadLocation: () -> Unit = {},
	onAddNewAttachment: (List<Uri>) -> Unit = {},
	toggleAttachment: (NoteViewModel.Companion.AttachmentState) -> Unit = {},
	onSelectChapter: (RealmUUID) -> Unit = {},
	onClickTag: (TagObject) -> Unit = {},
	putTag: (String, Color) -> Boolean = { _, _ -> false },
	onClickFavourite: () -> Unit = {},
	onClickLock: () -> Unit = {},
	onConfirmDelete: () -> Unit = {},
	onClickBack: () -> Unit = {},
) {
	val context = LocalContext.current

	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp

	val kitKatFormat by kitKat.kitKatFormat.collectAsState()

	val noteId by remember { derivedStateOf { noteObject?.id } }
	val createdTimestamp by remember { derivedStateOf { noteObject?.createdTimestamp } }
	val modifiedTimestamp by remember { derivedStateOf { noteObject?.modifiedTimestamp } }
	val userTimestamp by remember { derivedStateOf { noteObject?.userTimestamp } }
	val isFavourite by remember { derivedStateOf { noteObject?.isFavourite } }
	val isLocked by remember { derivedStateOf { noteObject?.isLocked } }

	var isEditorMetadataBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isEditorLocationBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isAttachmentBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isTagsBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

	var isViewerMetadataBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isViewerLocationBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isExportBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

	var isDateTimePickerDialogVisible by rememberSaveable { mutableStateOf(false) }
	var isLocationPickerDialogVisible by rememberSaveable { mutableStateOf(false) }
	var isWhereDialogVisible by rememberSaveable { mutableStateOf(false) }
	var isDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }

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
				onClickPin = {
					noteId?.let {
						NotePinNotification.pinToNotification(
							context = context,
							noteId = it,
							title = noteObject?.title,
							content = kitKatFormat.getPlainString(),
						)
					}
				},
				onClickDelete = { isDeleteDialogVisible = true },
				onClickBack = onClickBack,
			)
		},
		bottomBar = {
			if (isEditing == true) EditorBottomBar(
				kitKatFormat = kitKatFormat,
				userTimestamp = userTimestamp ?: Instant.now().toEpochMilli(),
				locationData = locationData,
				onClickDatePicker = { isDateTimePickerDialogVisible = true },
				onClickMetadata = { isEditorMetadataBottomSheetVisible = true },
				noClickLocation = { isEditorLocationBottomSheetVisible = true },
				onClickAttachments = { isAttachmentBottomSheetVisible = true },
				onClickTags = { isTagsBottomSheetVisible = true },
				onKitKatAction = { kitKat.onKitKatActionAsync(it) }
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
					savedAttachmentList = attachmentList.filterIsInstance<NoteViewModel.Companion.AttachmentState.Saved>(),
					connectedTagList = tagStateMap.filterValues { it == NoteViewModel.Companion.TagObjectState.Saved }.keys,
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

	DateTimePickerDialog(
		isDialogVisible = isDateTimePickerDialogVisible,
		onDismissRequest = { isDateTimePickerDialogVisible = false },
		currentUserTimestamp = userTimestamp ?: Instant.now().toEpochMilli(),
		onSelectDateTime = onSetUserTimestamp,
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

	DeleteDialog(
		isDialogVisible = isDeleteDialogVisible,
		onDismissRequest = { isDeleteDialogVisible = false },
		title = stringResource(id = R.string.delete_item),
		contentText = stringResource(id = R.string.are_you_sure_delete),
		onConfirmDelete = onConfirmDelete,
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
