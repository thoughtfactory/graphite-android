package com.syncodec.graphite.presentation.notebook.composable.screen

import android.content.Intent
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.button.PrimaryButton
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.notebook.NotebookActivity
import com.syncodec.graphite.presentation.notebook.composable.bar.BottomBar
import com.syncodec.graphite.presentation.notebook.composable.bar.TopBar
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.NotebookBottomSheetType
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.notebook.composable.dialog.NotebookDialog
import com.syncodec.graphite.utils.Extra
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun NotebookScreen() {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val softwareKeyboardController = LocalSoftwareKeyboardController.current

	val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
	var bottomSheetType by remember { mutableStateOf(NotebookBottomSheetType.MENU) }

	fun openSheet(_bottomSheetType : NotebookBottomSheetType) {
		scope.launch {
			bottomSheetType = _bottomSheetType
			softwareKeyboardController?.hide()
			modalBottomSheetState.show()
		}
	}

	fun closeSheet() {
		scope.launch { modalBottomSheetState.hide() }
	}

	var bottomBarSpacingPx by remember { mutableStateOf(0) }

	val chapterObject = NotebookActivity.LocalChapterObject.current

	CompositionLocalProvider(
		NotebookActivity.LocalOpenBottomSheet provides ::openSheet,
		NotebookActivity.LocalCloseBottomSheet provides ::closeSheet,
	) {
		ModalBottomSheetLayout(
			modifier = Modifier.fillMaxSize(),
			sheetState = modalBottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetContent = { SheetLayout(bottomSheetType = bottomSheetType) }
		) {
			Scaffold(
				modifier = Modifier.fillMaxSize(),
				topBar = { TopBar() },
			) {
				Crossfade(
					targetState = chapterObject,
					modifier = Modifier
						.fillMaxSize()
						.padding(it)
				) {
					if (it == null) {
						LoadingView()
					} else {
						Box(
							modifier = Modifier.fillMaxSize()
						) {
							Column(
								modifier = Modifier.fillMaxSize()
							) {
								Box(
									modifier = Modifier
										.fillMaxWidth()
										.weight(1f)
								) {
									ExplorerScreen()
								}
								BottomBar(
									modifier = Modifier
										.onGloballyPositioned {
											bottomBarSpacingPx = it.positionInParent().y.toInt()
										}
								)
							}

							PrimaryButton(
								primaryText = "Add Note",
								primaryIcon = R.drawable.ic_pencil,
								primaryDescription = "Add a new note",
								secondaryIcon = R.drawable.ic_notebook,
								secondaryDescription = "Add a new chapter",
								bottomBarSpacingPx = bottomBarSpacingPx,
								onClickPrimary = {
									Intent(context, NoteActivity::class.java).apply {
										putExtra(Extra.Companion.Constant.IS_NEW.name, true)
										putExtra(Extra.Companion.Constant.CHAPTER_ID.name, chapterObject?.id.toString())
										putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.READ_CHAPTER.name)

										context.startActivity(this)
									}
								},
								onClickSecondary = { openSheet(NotebookBottomSheetType.CHAPTER) }
							)
						}
					}
				}

				NotebookDialog()
			}
		}
	}
}
