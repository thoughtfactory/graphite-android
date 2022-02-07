package com.syncodec.momento.notebookComponent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.notebookComponent.miscellaneous.NotebookTopBar
import com.syncodec.momento.notebookComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.notebookComponent.modalBottomSheet.SheetLayout
import com.syncodec.momento.notebookComponent.screen.NotebookScreen
import com.syncodec.momento.ui.theme.MomentoTheme
import compose.icons.TablerIcons
import compose.icons.tablericons.Pencil
import kotlinx.coroutines.launch

class NotebookActivity : ComponentActivity() {
	private val viewModel by viewModels<NotebookViewModel>()

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		viewModel.notebookKey = intent.getStringExtra(Konstant.Companion.Konstant.PRIMARY_KEY.name)!!
		viewModel.openNotebook()
		viewModel.observeNotebook()

		setContent {
			viewModel.notebookActivityState = rememberNotebookActivityState()
			MomentoTheme {
				Screen()
			}
		}
	}

	@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
	@Composable
	private fun Screen() {

		val systemUiController = rememberSystemUiController()
		systemUiController.setStatusBarColor(MaterialTheme.colorScheme.primaryContainer)

		ModalBottomSheetLayout(
			sheetState = viewModel.notebookActivityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetContent = {
				SheetLayout()
			},
		) {
			NotebookScreen()
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	class NotebookActivityState(
		val bottomSheetState: ModalBottomSheetState,
		var isSelectedToDelete: MutableState<Boolean>,
		val selectedToDeleteList: SnapshotStateList<String>
	) {
		var bottomSheetType: MutableState<BottomSheetType> = mutableStateOf(BottomSheetType.NewNoteBottomSheet)
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	fun rememberNotebookActivityState(
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
		isSelectedToDelete: MutableState<Boolean> = remember { mutableStateOf(false) },
		selectedToDeleteList: SnapshotStateList<String> = remember { mutableStateListOf() }
	) = remember {
		NotebookActivityState(bottomSheetState, isSelectedToDelete, selectedToDeleteList)
	}
}
