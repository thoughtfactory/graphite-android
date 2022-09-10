package com.syncodec.graphite.presentation.note

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.presentation.note.composable.bar.BottomBar
import com.syncodec.graphite.presentation.note.composable.bar.TopBar
import com.syncodec.graphite.presentation.note.composable.bottomSheet.NoteBottomSheetType
import com.syncodec.graphite.presentation.note.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.note.composable.screen.EditorScreen
import com.syncodec.graphite.presentation.note.composable.screen.ViewerScreen
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.note.composable.buildingBlock.snackbar.LocationSnackbarHost


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun NoteScreen() {
	val scope = rememberCoroutineScope()

	val viewModel: NoteViewModel = viewModel()

	val keyboardController = LocalSoftwareKeyboardController.current

	val isViewer by viewModel.isViewer

	val modalBottomSheetState = rememberModalBottomSheetState(
		initialValue = ModalBottomSheetValue.Hidden,
		confirmStateChange = {
			keyboardController?.hide()
			true
		}
	)
	var bottomSheetType: NoteBottomSheetType by rememberSaveable { mutableStateOf(NoteBottomSheetType.METADATA) }

	val locationSnackbarHostState = viewModel.locationSnackbarHostState

	val noteIdList = viewModel.noteIdList

	val closeSheet = { scope.launch { modalBottomSheetState.hide() } }

	val openSheet = { scope.launch { modalBottomSheetState.show() } }

	ModalBottomSheetLayout(
		modifier = Modifier.fillMaxSize(),
		sheetState = modalBottomSheetState,
		sheetElevation = 0.dp,
		sheetContent = { SheetLayout(bottomSheetType = bottomSheetType) { closeSheet() } },
		sheetBackgroundColor = Color.Transparent,
	) {
		Scaffold(
			modifier = Modifier.fillMaxSize(),
			topBar = {
				TopBar {
					bottomSheetType = NoteBottomSheetType.METADATA
					openSheet()
				}
			},
			bottomBar = {
				BottomBar(
					onClickAttachment = {
						bottomSheetType = NoteBottomSheetType.ATTACHMENT
						openSheet()
					},
					onClickTag = {
						bottomSheetType = NoteBottomSheetType.TAG
						openSheet()
					}
				)
			},
			snackbarHost = {
				SnackbarHost(hostState = locationSnackbarHostState) {
					LocationSnackbarHost(snackbarData = it)
				}
			}
		) {
			Box(modifier = Modifier.padding(it)) {
				Crossfade(targetState = isViewer) {
					if (it) {
						Crossfade(targetState = noteIdList) {
							if (it.isEmpty()) NoEntryView() else ViewerScreen(noteIdList = noteIdList)
						}
					} else {
						EditorScreen()
					}
				}
			}
		}
	}
}

@Composable
private fun NoEntryView() {
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Spacer(modifier = Modifier.weight(1f))
		Image(
			painter = painterResource(id = if (Random.nextBoolean()) R.drawable.il_no_entries else R.drawable.il_no_entries),
			contentDescription = "No entries found",
			contentScale = ContentScale.Fit,
			modifier = Modifier.fillMaxWidth(0.64f),
		)

		Spacer(modifier = Modifier.height(24.dp))

		Text(
			text = "The town was paper, but the memories were not",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.fillMaxWidth(0.71f)
		)

		Spacer(modifier = Modifier.height(16.dp))

		Text(
			text = "~ John Green, Paper Towns",
			style = MaterialTheme.typography.bodySmall,
			fontStyle = FontStyle.Italic,
			textAlign = TextAlign.End,
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.fillMaxWidth(0.71f)
		)
		Spacer(modifier = Modifier.weight(1f))
	}

}
