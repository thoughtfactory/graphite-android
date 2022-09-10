package com.syncodec.graphite.presentation.notebook.composable.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.presentation.notebook.NotebookViewModel
import com.syncodec.graphite.presentation.notebook.composable.bar.BottomBar
import com.syncodec.graphite.presentation.notebook.composable.bar.TopBar
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.BucketBottomSheetType
import com.syncodec.graphite.presentation.notebook.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.notebook.composable.dialog.EditChapterDialog
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NotebookScreen() {
	val scope = rememberCoroutineScope()

	val viewModel: NotebookViewModel = viewModel()

	val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

	var bottomSheetType: BucketBottomSheetType by rememberSaveable { mutableStateOf(BucketBottomSheetType.MENU) }

	val openSheet = { scope.launch { modalBottomSheetState.show() } }
	val closeSheet = { scope.launch { modalBottomSheetState.hide() } }

	var showEditChapterDialog by viewModel.showEditChapterDialog
	var chapterObject by viewModel.currentChapterObject

	ModalBottomSheetLayout(
		modifier = Modifier.fillMaxSize(),
		sheetState = modalBottomSheetState,
		sheetElevation = 0.dp,
		sheetBackgroundColor = Color.Transparent,
		sheetContent = {
			SheetLayout(bottomSheetType = bottomSheetType) {
				closeSheet()
			}
		}
	) {
		Scaffold(
			modifier = Modifier.fillMaxSize(),
			topBar = {
				TopBar(title = "notebook") {
					bottomSheetType = BucketBottomSheetType.MENU
					openSheet()
				}
			},
			bottomBar = {
				BottomBar {
					bottomSheetType = it
					openSheet()
				}
			}
		) {
			Box(
				modifier = Modifier.padding(it)
			) {
				ExplorerScreen()

				if (chapterObject != null) {
					EditChapterDialog(
						title = chapterObject!!.title,
						description = chapterObject!!.description,
						color = Color(chapterObject!!.color),
						showDialog = showEditChapterDialog
					) {
						showEditChapterDialog = false
					}
				}
			}
		}
	}
}
