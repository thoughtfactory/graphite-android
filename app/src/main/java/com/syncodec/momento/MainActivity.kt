package com.syncodec.momento

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.debug.AddDataPopup
import com.syncodec.momento.diaryComponent.DiaryActivity
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.miscellaneous.*
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.mainComponent.modalBottomSheet.SheetLayout
import com.syncodec.momento.mainComponent.screen.MomentoScreenType
import com.syncodec.momento.ui.theme.MomentoTheme
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

	private val viewModel by viewModels<MainViewModel>()

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

//		window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

//		val windowInsetsController =
//			ViewCompat.getWindowInsetsController(window.decorView) ?: return
//		// Configure the behavior of the hidden system bars
//		windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//		// Hide both the status bar and the navigation bar
//		windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
//
//		window.statusBarColor = 0

		setContent {
			viewModel.mainActivityState = rememberMainActivityState()
			MomentoTheme {
				MainScreen()
				AddDataPopup()
			}
		}
	}

	@OptIn(ExperimentalAnimatedInsets::class)
	@Preview
	@ExperimentalPagerApi
	@ExperimentalMaterialApi
	@ExperimentalFoundationApi
	@ExperimentalMaterial3Api
	@Composable
	fun MainScreen() {
		val systemUiController = rememberSystemUiController()
		systemUiController.setStatusBarColor(androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer)

		val scope = rememberCoroutineScope()

		val navController = rememberNavController()
		val navBackStackEntry by navController.currentBackStackEntryAsState()
		val currentRoute = navBackStackEntry?.destination?.route

		val openSheet: (BottomSheetType) -> Unit = { bottomSheetType ->
			viewModel.mainActivityState.bottomSheetType.value = bottomSheetType
			scope.launch {
				viewModel.mainActivityState.bottomSheetState.show()
			}
		}

		ModalBottomSheetLayout(
			sheetState = viewModel.mainActivityState.bottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			sheetShape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
			sheetContent = {
				SheetLayout()
			},
		) {
			Box(
				modifier = Modifier
					.background(Color.Black)
			) {
				Scaffold(
					bottomBar = { BottomNavigationBar(navController) },
					floatingActionButtonPosition = FabPosition.End,
					floatingActionButton = {
						FloatingActionButton(
							onClick = {
								when (currentRoute) {
									BottomNavigationItem.Momento.route -> {
										when(viewModel.mainActivityState.momentoScreenType.value) {
											MomentoScreenType.Diary -> startActivity(Intent(this@MainActivity, DiaryActivity::class.java))
											MomentoScreenType.Notebook -> openSheet(BottomSheetType.NotebookBottomSheet)
											MomentoScreenType.Scratchpad -> startActivity(Intent(this@MainActivity, DiaryActivity::class.java))
										}
									}
									BottomNavigationItem.Bucket.route -> openSheet(BottomSheetType.BucketBottomSheet)
									BottomNavigationItem.Calendar.route -> startActivity(Intent(this@MainActivity, DiaryActivity::class.java))
									BottomNavigationItem.Atlas.route -> startActivity(Intent(this@MainActivity, DiaryActivity::class.java))
									BottomNavigationItem.Me.route -> startActivity(Intent(this@MainActivity, DiaryActivity::class.java))
								}
							},
							modifier = Modifier
								.navigationBarsPadding(),
						) {
							Crossfade(targetState = navController.currentDestination?.route) { route ->
								when (route) {
									BottomNavigationItem.Momento.route -> when(viewModel.mainActivityState.momentoScreenType.value) {
										MomentoScreenType.Diary -> Icon(imageVector = TablerIcons.Pencil, contentDescription = null)
										MomentoScreenType.Notebook -> Icon(imageVector = TablerIcons.Notebook, contentDescription = null)
										MomentoScreenType.Scratchpad -> Icon(imageVector = TablerIcons.Notes, contentDescription = null)
									}
									BottomNavigationItem.Bucket.route -> Icon(imageVector = TablerIcons.Plus, contentDescription = null)
									BottomNavigationItem.Calendar.route -> Icon(imageVector = TablerIcons.Pencil, contentDescription = null)
									BottomNavigationItem.Atlas.route -> Icon(imageVector = TablerIcons.ArrowsMinimize, contentDescription = null)
									BottomNavigationItem.Me.route -> Icon(imageVector = TablerIcons.Pencil, contentDescription = null)
									else -> Icon(imageVector = TablerIcons.Pencil, contentDescription = null)
								}
							}
						}
					},
				) {
					val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
						"No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
					}

					CompositionLocalProvider(
						LocalViewModelStoreOwner provides viewModelStoreOwner
					) {
						MainNavigation(navController = navController, viewModelStoreOwner = viewModelStoreOwner)
					}
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	class MainActivityState(
		val bottomSheetState: ModalBottomSheetState,
	) {
		var bottomSheetType: MutableState<BottomSheetType> = mutableStateOf(BottomSheetType.MenuBottomSheet)
		var momentoScreenType: MutableState<MomentoScreenType> = mutableStateOf(MomentoScreenType.Diary)
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	fun rememberMainActivityState(
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
	) = remember {
		MainActivityState(bottomSheetState)
	}

	enum class HomeScreenFilter {
		ARCHIVED,
		FAVOURITE,
		PINNED,
		TRASH
	}
}
