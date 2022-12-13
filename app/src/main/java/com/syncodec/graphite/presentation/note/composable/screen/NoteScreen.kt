package com.syncodec.graphite.presentation.note.composable.screen

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.note.composable.LocalCompositionIsViewing
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOpenBottomSheet
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.note.composable.bar.TopBar
import com.syncodec.graphite.presentation.note.composable.bar.bottomBar.BottomBar
import com.syncodec.graphite.presentation.note.composable.bottomSheet.NoteBottomSheetType
import com.syncodec.graphite.presentation.note.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.note.composable.buildingBlock.LocationSnackbarHost
import com.syncodec.graphite.presentation.note.composable.dialog.NoteDialog
import com.syncodec.graphite.presentation.note.composable.dialog.NoteDialogType
import java.io.File


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
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
	setLocation: (LatLng, String?) -> Unit,
) {
	val isViewing = LocalCompositionIsViewing.current

	val openDialog = LocalCompositionOpenDialog.current
	val openSheet = LocalCompositionOpenBottomSheet.current

	Box(
		modifier = Modifier.fillMaxSize()
	) {
		ModalBottomSheetLayout(
			sheetState = modalBottomSheetState,
			sheetElevation = 0.dp,
			sheetContent = {
				SheetLayout(
					bottomSheetType = bottomSheetType,
					onAddAttachmentToBuffer = onAddAttachmentToBuffer,
					onRemoveAttachment = onRemoveAttachment,
					onRemoveLocation = onRemoveLocation,
					onReloadLocation = onReloadLocation,
				)
			},
			sheetBackgroundColor = Color.Transparent,
			modifier = Modifier.fillMaxSize(),
		) {
			Scaffold(
				modifier = Modifier.fillMaxSize(),
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
				snackbarHost = {
					SnackbarHost(hostState = locationSnackbarHostState) {
						LocationSnackbarHost(snackbarData = it)
					}
				},
			) {
				Crossfade(
					targetState = isViewing,
					modifier = Modifier
						.fillMaxSize()
						.padding(it)
				) {
					when(it) {
						true -> ViewerScreen()
						false -> EditorScreen()
						null -> LoadingView()
					}
				}
			}
		}

		NoteDialog(setLocation = setLocation)
	}
}
