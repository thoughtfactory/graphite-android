package com.syncodec.graphite.presentation.main.composable.screen

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.graphite.presentation.common.dialog.ColorPickerDialog
import com.syncodec.graphite.presentation.main.MainActivity
import com.syncodec.graphite.presentation.main.composable.bar.BottomNavigationBar
import com.syncodec.graphite.presentation.main.composable.bar.MainNavigation
import com.syncodec.graphite.presentation.main.composable.bar.TopBar
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MainBottomSheetType
import com.syncodec.graphite.presentation.main.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.search.SearchActivity
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
	ExperimentalFoundationApi::class, ExperimentalAnimationApi::class
)
@Composable
fun MainScreen() {
	val activity: MainActivity = LocalContext.current as MainActivity
	val scope = rememberCoroutineScope()

	val navController = rememberAnimatedNavController()
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
						onClickOpenMenu = {
							bottomSheetType = MainBottomSheetType.MENU
							openSheet()
						},
						onClickOpenFilter = {
							bottomSheetType = MainBottomSheetType.FILTER
							openSheet()
						},
						onClickSearch = {
							Intent(activity, SearchActivity::class.java).apply {
								activity.startActivity(this)
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
						componentType = currentComponentType
					)
				}
			}
		}
	}
}
