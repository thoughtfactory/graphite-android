package com.syncodec.momento

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.syncodec.momento.mainComponent.miscellaneous.DeleteDialog
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.miscellaneous.BottomNavigationBar
import com.syncodec.momento.mainComponent.miscellaneous.BottomNavigationItem
import com.syncodec.momento.mainComponent.miscellaneous.MainNavigation
import com.syncodec.momento.mainComponent.miscellaneous.MainTopBar
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.mainComponent.modalBottomSheet.SheetLayout
import com.syncodec.momento.mainComponent.screen.MomentoScreenType
import com.syncodec.momento.miscellaneous.VAULT_KEY
import com.syncodec.momento.miscellaneous.dataStore
import com.syncodec.momento.ui.theme.MomentoTheme
import com.syncodec.momento.vaultComponent.VaultOpenerScreen
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

	private val viewModel by viewModels<MainViewModel>()

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			viewModel.mainActivityState = rememberMainActivityState()
			MomentoTheme {
				MainScreen()
				AddDataPopup()
			}
		}
	}

	override fun onBackPressed() {
		if (viewModel.mainActivityState.vaultState.value == Momento.Companion.VaultState.TRY_OPEN) {
			viewModel.mainActivityState.vaultState.value = Momento.Companion.VaultState.NOT_OPENED
		} else {
			if (viewModel.mainActivityState.isSelected.value) {
				viewModel.mainActivityState.isSelected.value = false
				viewModel.mainActivityState.selectedEntryList.removeAll { true }
			} else {
				super.onBackPressed()
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

		Crossfade(
			targetState = viewModel.mainActivityState.vaultState.value,
			animationSpec = tween(
				durationMillis = 400
			)
		) {
			when (it) {
				Momento.Companion.VaultState.TRY_OPEN -> {
					systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)

					VaultOpenerScreen(
						onSuccess = { viewModel.mainActivityState.vaultState.value = Momento.Companion.VaultState.OPENED }
					) {}
				}
				else -> {
					systemUiController.setStatusBarColor(MaterialTheme.colorScheme.primaryContainer)

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
								topBar = {
									MainTopBar()
								},
								floatingActionButtonPosition = FabPosition.End,
								floatingActionButton = {
									when (currentRoute) {
										BottomNavigationItem.Me.route -> Box(modifier = Modifier)
										else -> {
											FloatingActionButton(
												onClick = {
													when (currentRoute) {
														BottomNavigationItem.Momento.route -> {
															when (viewModel.mainActivityState.momentoScreenType.value) {
																MomentoScreenType.Diary -> startActivity(Intent(this@MainActivity, DiaryActivity::class.java))
																MomentoScreenType.Notebook -> openSheet(BottomSheetType.NotebookBottomSheet)
																MomentoScreenType.Scratchpad -> startActivity(
																	Intent(
																		this@MainActivity,
																		DiaryActivity::class.java
																	)
																)
															}
														}
														BottomNavigationItem.Bucket.route -> openSheet(BottomSheetType.BucketBottomSheet)
														BottomNavigationItem.Calendar.route -> startActivity(
															Intent(
																this@MainActivity,
																DiaryActivity::class.java
															)
														)
														BottomNavigationItem.Atlas.route -> startActivity(Intent(this@MainActivity, DiaryActivity::class.java))
													}
												},
												modifier = Modifier
													.navigationBarsPadding(),
											) {
												Crossfade(targetState = currentRoute) { route ->
													when (route) {
														BottomNavigationItem.Momento.route -> when (viewModel.mainActivityState.momentoScreenType.value) {
															MomentoScreenType.Diary -> Icon(imageVector = TablerIcons.Pencil, contentDescription = null)
															MomentoScreenType.Notebook -> Icon(imageVector = TablerIcons.Notebook, contentDescription = null)
															MomentoScreenType.Scratchpad -> Icon(imageVector = TablerIcons.Notes, contentDescription = null)
														}
														BottomNavigationItem.Bucket.route -> Icon(imageVector = TablerIcons.Plus, contentDescription = null)
														BottomNavigationItem.Calendar.route -> Icon(imageVector = TablerIcons.Pencil, contentDescription = null)
														BottomNavigationItem.Atlas.route -> Icon(
															imageVector = TablerIcons.ArrowsMinimize,
															contentDescription = null
														)
													}
												}
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
							DeleteDialog()

						}
					}
				}
			}
		}
	}

	data class UserPreferences(val showCompleted: Boolean)

	@OptIn(ExperimentalMaterialApi::class)
	inner class MainActivityState(
		val bottomSheetState: ModalBottomSheetState,
	) {
		val vaultKeyFlow: Flow<String?> = dataStore.data.map { preferences -> preferences[VAULT_KEY] }
		var vaultState = (application as Momento).vaultState
		var bottomSheetType: MutableState<BottomSheetType> = mutableStateOf(BottomSheetType.MenuBottomSheet)
		var momentoScreenType: MutableState<MomentoScreenType> = mutableStateOf(MomentoScreenType.Diary)
		var isSelected = mutableStateOf(false)
		var selectedEntryList: SnapshotStateList<String> = mutableStateListOf()
		var showDeleteDialog = mutableStateOf(false)

		var showArchived = mutableStateOf(false)
		var showFavourite = mutableStateOf(false)
		var showTrash = mutableStateOf(false)
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	fun rememberMainActivityState(
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
	) = remember {
		MainActivityState(bottomSheetState)
	}
}
