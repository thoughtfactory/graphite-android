package com.syncodec.graphite.presentation.note.composable.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.presentation.note.composable.bar.TopBar
import com.syncodec.graphite.presentation.note.composable.bottomSheet.NoteBottomSheetType
import com.syncodec.graphite.presentation.note.composable.bottomSheet.SheetLayout
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.note.NoteViewModel
import com.syncodec.graphite.presentation.note.composable.bar.BottomBar2
import com.syncodec.graphite.presentation.note.composable.buildingBlock.snackbar.LocationSnackbarHost
import com.syncodec.graphite.utils.LocalRichTextEditor
import com.syncodec.graphite.utils.LocalSaveNote


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun NoteScreen() {

	val activity = LocalContext.current as NoteActivity
	val scope = rememberCoroutineScope()

	val viewModel : NoteViewModel = viewModel()

	val keyboardController = LocalSoftwareKeyboardController.current

	val isViewer by viewModel.isViewer
	val isSaving by viewModel.isSaving

	val richTextEditor = LocalRichTextEditor.current

	var bottomSheetType : NoteBottomSheetType by rememberSaveable { mutableStateOf(NoteBottomSheetType.MENU) }
	val modalBottomSheetState = rememberModalBottomSheetState(
		initialValue = ModalBottomSheetValue.Hidden,
		confirmStateChange = { keyboardController?.hide(); true }
	)
	val closeSheet = { scope.launch { modalBottomSheetState.hide() } }
	val openSheet = { scope.launch { modalBottomSheetState.show() } }


	val locationSnackbarHostState = viewModel.locationSnackbarHostState

	val noteIdList = viewModel.noteIdList

	val onSave = LocalSaveNote.current

	Box(
		modifier = Modifier.fillMaxSize()
	) {
		ModalBottomSheetLayout(
			modifier = Modifier.fillMaxSize(),
			sheetState = modalBottomSheetState,
			sheetElevation = 0.dp,
			sheetContent = { SheetLayout(bottomSheetType = bottomSheetType) { closeSheet() } },
			sheetBackgroundColor = Color.Transparent,
		) {
			Scaffold(
				topBar = {
					TopBar(
						onClickSave = onSave,
						onClickBack = { activity.onBackPressed() },
						onClickMenu = {
							bottomSheetType = NoteBottomSheetType.MENU
							openSheet()
						}
					)
				},
				bottomBar = {
					BottomBar2(
						onClickMetadata = {
							bottomSheetType = NoteBottomSheetType.METADATA
							openSheet()
						},
						onClickLocation = {
							bottomSheetType = NoteBottomSheetType.LOCATION
							openSheet()
						},
						onClickAttachment = {
							bottomSheetType = NoteBottomSheetType.ATTACHMENT
							openSheet()
						},
						onClickTag = { viewModel.showTagDialog.value = true }
					)
				},
				snackbarHost = {
					SnackbarHost(hostState = locationSnackbarHostState) {
						LocationSnackbarHost(snackbarData = it)
					}
				},
				modifier = Modifier.fillMaxSize()
			) {
				Box(
					modifier = Modifier.padding(it)
				) {
					Crossfade(
						targetState = isViewer,
						animationSpec = tween(300)
					) {
						if (it) {
							Crossfade(
								targetState = noteIdList,
								animationSpec = tween(300)
							) {
								if (it.isEmpty()) NoEntryView() else ViewerScreen(noteIdList = it)
							}
						} else {
							EditorScreen()
						}
					}
				}
			}
		}

		AnimatedVisibility(
			visible = isSaving,
			enter = fadeIn(tween(300)),
			exit = fadeOut(tween(300))
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(Color.Black.copy(alpha = 0.5f))
					.clickable { }
			) {
				CircularProgressIndicator(
					modifier = Modifier
						.align(Alignment.Center)
						.size(48.dp)
				)
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
