package com.syncodec.graphite.presentation.note.composable.screen

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.dialog.whereDialog.WhereDialog
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.note.composable.LocalCompositionCloseDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionIsViewing
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOnMoveChapter
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOnSelectChapter
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOpenBottomSheet
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionShowChapterSelectionDialog
import com.syncodec.graphite.presentation.note.composable.bar.TopBar
import com.syncodec.graphite.presentation.note.composable.bar.bottomBar.BottomBar
import com.syncodec.graphite.presentation.note.composable.bottomSheet.NoteBottomSheetType
import com.syncodec.graphite.presentation.note.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.note.composable.buildingBlock.LocationSnackbarHost
import com.syncodec.graphite.presentation.note.composable.dialog.NoteDialog
import com.syncodec.graphite.presentation.note.composable.dialog.NoteDialogType
import java.io.File


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NoteScreen(
	locationSnackbarHostState : SnackbarHostState,
	modalBottomSheetState : ModalBottomSheetState,
	bottomSheetType : NoteBottomSheetType,
	onClickBack : () -> Unit,
	onClickLock : () -> Unit,
	onClickFavourite : () -> Unit,
	onAddAttachmentToBuffer : (List<Uri>) -> Unit,
	onRemoveAttachment : (File?, Uri?) -> Unit,
	onRemoveLocation : () -> Unit,
	onReloadLocation : () -> Unit,
	setLocation : (LatLng, String?) -> Unit,
) {
	val context = LocalContext.current

	val isViewing = LocalCompositionIsViewing.current

	val openDialog = LocalCompositionOpenDialog.current
	val openSheet = LocalCompositionOpenBottomSheet.current

	val showChapterSelectionDialog = LocalCompositionShowChapterSelectionDialog.current
	val onSelectChapter = LocalCompositionOnMoveChapter.current
	val closeDialog = LocalCompositionCloseDialog.current

	GenericScaffold(
		modalBottomSheetState = modalBottomSheetState,
		sheetContent = {
			SheetLayout(
				bottomSheetType = bottomSheetType,
				onAddAttachmentToBuffer = onAddAttachmentToBuffer,
				onRemoveAttachment = onRemoveAttachment,
				onRemoveLocation = onRemoveLocation,
				onReloadLocation = onReloadLocation,
			)
		},
		topBar = {
			TopBar(
				onClickBack = onClickBack,
				onClickMenu = { openSheet(NoteBottomSheetType.MENU) },
				onClickLock = onClickLock,
				onClickFavourite = onClickFavourite,
			)
		},
		bottomBar = {
			BottomBar(
				onClickTimePicker = { openDialog(NoteDialogType.DATE_PICKER, null) },
				onClickMetadata = { openSheet(NoteBottomSheetType.METADATA) },
				onClickLocation = { openSheet(NoteBottomSheetType.LOCATION) },
				onClickAttachment = { openSheet(NoteBottomSheetType.ATTACHMENT) },
				onClickTag = { openSheet(NoteBottomSheetType.TAG) },
			)
		},
		dialogContent = { NoteDialog(setLocation = setLocation) },
		snackbarHost = {
			SnackbarHost(hostState = locationSnackbarHostState) {
				LocationSnackbarHost(snackbarData = it)
			}
		}
	) {
		Crossfade(
			targetState = isViewing,
			modifier = Modifier.fillMaxSize()
		) {
			when (it) {
				true -> ViewerScreen()
				false -> EditorScreen()
				null -> LoadingView()
			}
		}
	}

	WhereDialog(
		showDialog = showChapterSelectionDialog,
		onSetChapter = { onSelectChapter(it?.id) },
	) { closeDialog(NoteDialogType.CHAPTER_SELECTION) }
}
