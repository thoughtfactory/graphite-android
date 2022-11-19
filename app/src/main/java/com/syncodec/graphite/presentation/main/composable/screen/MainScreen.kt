package com.syncodec.graphite.presentation.main.composable.screen

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.graphite.presentation.main.MainViewModel
import com.syncodec.graphite.presentation.main.composable.LocalCompositionCloseBottomSheet
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOpenBottomSheet
import com.syncodec.graphite.presentation.main.composable.bar.BottomNavigationBar
import com.syncodec.graphite.presentation.main.composable.bar.MainNavigation
import com.syncodec.graphite.presentation.main.composable.bar.TopBar
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MainBottomSheetType
import com.syncodec.graphite.presentation.main.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.main.composable.dialog.MainDialog
import com.syncodec.graphite.presentation.search.SearchActivity
import com.syncodec.graphite.utils.LocalModalBottomSheetState
import com.syncodec.graphite.utils.LocalModalBottomSheetType
import com.syncodec.graphite.utils.LocalSetModalBottomSheetType
import kotlinx.coroutines.launch


enum class ComponentType {
	NOTE,
	BUCKET,
	NOTEBOOK
}

@OptIn(
	ExperimentalMaterialApi::class,
	ExperimentalMaterial3Api::class,
	ExperimentalPagerApi::class,
	ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class
)
@Composable
fun MainScreen(
	viewModel : MainViewModel,
	currentRoute : String?,
	navController : NavHostController,
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val keyboardController = LocalSoftwareKeyboardController.current
	val focusManager = LocalFocusManager.current

	var bottomSheetType : MainBottomSheetType by remember { mutableStateOf(MainBottomSheetType.MENU) }
	var currentComponentType : ComponentType by remember { mutableStateOf(ComponentType.NOTE) }

	val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

	LaunchedEffect(key1 = modalBottomSheetState.currentValue) {
		if (modalBottomSheetState.currentValue == ModalBottomSheetValue.Hidden) {
			try {
				keyboardController?.hide()
				focusManager.clearFocus()
			} catch (e : Exception) {
			}
		}
	}

	fun openSheet(_bottomSheetType : MainBottomSheetType) {
		scope.launch { bottomSheetType = _bottomSheetType; modalBottomSheetState.show() }
	}

	fun closeSheet() {
		scope.launch { modalBottomSheetState.hide() }
	}

	val defaultNotebookId by viewModel.defaultNotebookId
	val chapterObject by viewModel.chapterObject
	val notebookList = viewModel.notebookList
	val noteList = viewModel.noteList
	val bucketList = viewModel.bucketObjectList

	CompositionLocalProvider(
		LocalModalBottomSheetState provides modalBottomSheetState,
		LocalModalBottomSheetType provides bottomSheetType.ordinal,
		LocalSetModalBottomSheetType provides { bottomSheetType = MainBottomSheetType.values().getOrElse(it) { MainBottomSheetType.MENU } },
		LocalCompositionOpenBottomSheet provides ::openSheet,
		LocalCompositionCloseBottomSheet provides ::closeSheet,
	) {
		ModalBottomSheetLayout(
			modifier = Modifier.fillMaxSize(),
			sheetState = modalBottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetContent = {
				SheetLayout(
					bottomSheetType = bottomSheetType,
					putNotebook = viewModel::putNotebook,
				)
			}
		) {
			Scaffold(
				modifier = Modifier.fillMaxSize(),
				topBar = {
					TopBar(
						currentRoute = currentRoute,
						componentType = currentComponentType,
						onComponentChange = { currentComponentType = ComponentType.values()[it] },
						onClickSearch = {
							Intent(context, SearchActivity::class.java).apply {
								context.startActivity(this)
							}
						}
					)
				},
				bottomBar = {
					BottomNavigationBar(
						currentRoute = currentRoute,
						onNavigation = { navController.navigate(it) }
					)
				}
			) {
				Box(modifier = Modifier.padding(it)) {
					MainNavigation(
						navController = navController,
						componentType = currentComponentType,
						defaultNotebookId = defaultNotebookId,
						chapterObject = chapterObject,
						notebookList = notebookList,
						noteList = noteList,
						bucketList = bucketList,
					)

					MainDialog()
				}
			}
		}
	}
}
