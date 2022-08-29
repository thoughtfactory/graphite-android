package com.syncodec.graphite.presentation.main.composable.screen

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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.graphite.presentation.main.composable.bar.BottomNavigationBar
import com.syncodec.graphite.presentation.main.composable.bar.MainNavigation
import com.syncodec.graphite.presentation.main.composable.bar.TopBar
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MainBottomSheetType
import com.syncodec.graphite.presentation.main.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.utils.*
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
	ExperimentalFoundationApi::class
)
@Composable
fun MainScreen() {
	val scope = rememberCoroutineScope()

	val navController = rememberNavController()
	val navBackStackEntry by navController.currentBackStackEntryAsState()
	val currentRoute = navBackStackEntry?.destination?.route

	var bottomSheetType: MainBottomSheetType by remember { mutableStateOf(MainBottomSheetType.MENU) }
	var currentComponentType: ComponentType by remember { mutableStateOf(ComponentType.NOTE) }

	val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

	val closeSheet = { scope.launch { modalBottomSheetState.hide() } }
	val openSheet = { scope.launch { modalBottomSheetState.show() } }

	CompositionLocalProvider(
		LocalModalBottomSheetState provides modalBottomSheetState,
		LocalModalBottomSheetType provides bottomSheetType.ordinal,
		LocalSetModalBottomSheetType provides { bottomSheetType = MainBottomSheetType.values().getOrElse(it) { MainBottomSheetType.MENU } }
	) {
		ModalBottomSheetLayout(
			modifier = Modifier.fillMaxSize(),
			sheetState = modalBottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetContent = { SheetLayout(bottomSheetType = bottomSheetType) { closeSheet() } }
		) {
			Scaffold(
				modifier = Modifier.fillMaxSize(),
				topBar = {
					TopBar(
						currentRoute = currentRoute,
						componentType = currentComponentType,
						onComponentChange = { currentComponentType = ComponentType.values()[it] },
						onOpenMenu = {
							bottomSheetType = MainBottomSheetType.MENU
							openSheet()
						},
						onOpenFilter = {
							bottomSheetType = MainBottomSheetType.FILTER
							openSheet()
						},
						onOpenSearch = {}
					)
				},
				bottomBar = { BottomNavigationBar(currentRoute = currentRoute, onNavigation = {}) }
			) {
				Box(modifier = Modifier.padding(it)) {
					MainNavigation(
						navController = navController,
						componentType = currentComponentType
					)
				}
			}
		}
	}
}
