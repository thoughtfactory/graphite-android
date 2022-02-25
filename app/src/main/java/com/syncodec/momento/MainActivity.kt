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
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.miscellaneous.BottomNavigationBar
import com.syncodec.momento.mainComponent.miscellaneous.BottomNavigationItem
import com.syncodec.momento.mainComponent.miscellaneous.DeleteDialog
import com.syncodec.momento.mainComponent.miscellaneous.MainNavigation
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.mainComponent.modalBottomSheet.SheetLayout
import com.syncodec.momento.mainComponent.screen.MomentoComponentType
import com.syncodec.momento.miscellaneous.PREFERENCE_KEY_VAULT_KEY
import com.syncodec.momento.miscellaneous.dataStore
import com.syncodec.momento.noteComponent.NoteActivity
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
			viewModel.activityState = rememberMainActivityState()

			MomentoTheme {
				MainScreen()
				AddDataPopup()
			}
		}
	}

	override fun onBackPressed() {
		if (viewModel.activityState.vaultState.value == Momento.Companion.VaultState.TRY_OPEN) {
			viewModel.activityState.vaultState.value = Momento.Companion.VaultState.NOT_OPENED
		} else {
			if (viewModel.activityState.isSelected.value) {
				viewModel.activityState.isSelected.value = false
				viewModel.activityState.selectedEntryList.removeAll { true }
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
			viewModel.activityState.bottomSheetType.value = bottomSheetType
			scope.launch {
				viewModel.activityState.bottomSheetState.show()
			}
		}

		Crossfade(
			targetState = viewModel.activityState.vaultState.value,
			animationSpec = tween(
				durationMillis = 400
			)
		) {
			when (it) {
				Momento.Companion.VaultState.TRY_OPEN -> {
					systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)

					VaultOpenerScreen(
						onSuccess = { viewModel.activityState.vaultState.value = Momento.Companion.VaultState.OPENED }
					) {}
				}
				else -> {
					if (currentRoute == BottomNavigationItem.Me.route) {
						systemUiController.setStatusBarColor(MaterialTheme.colorScheme.primaryContainer)
					} else {
						systemUiController.setStatusBarColor(MaterialTheme.colorScheme.primaryContainer)
					}

					ModalBottomSheetLayout(
						sheetState = viewModel.activityState.bottomSheetState,
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
									when (currentRoute) {
										BottomNavigationItem.Me.route -> Box(modifier = Modifier)
										else -> {
											FloatingActionButton(
												onClick = {
													when (currentRoute) {
														BottomNavigationItem.Momento.route -> {
															when (viewModel.activityState.momentoComponentType.value) {
																MomentoComponentType.Diary -> startActivity(
																	Intent(
																		this@MainActivity,
																		NoteActivity::class.java
																	)
																)
																MomentoComponentType.Notebook -> openSheet(BottomSheetType.NotebookBottomSheet)
															}
														}
														BottomNavigationItem.Bucket.route -> openSheet(BottomSheetType.BucketBottomSheet)
														BottomNavigationItem.Calendar.route -> startActivity(
															Intent(
																this@MainActivity,
																NoteActivity::class.java
															)
														)
														BottomNavigationItem.Atlas.route -> startActivity(Intent(this@MainActivity, NoteActivity::class.java))
													}
												},
												modifier = Modifier
													.navigationBarsPadding(),
											) {
												Crossfade(targetState = currentRoute) { route ->
													when (route) {
														BottomNavigationItem.Momento.route -> when (viewModel.activityState.momentoComponentType.value) {
															MomentoComponentType.Diary -> Icon(imageVector = TablerIcons.Pencil, contentDescription = null)
															MomentoComponentType.Notebook -> Icon(imageVector = TablerIcons.Notebook, contentDescription = null)
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

	@OptIn(ExperimentalMaterialApi::class)
	inner class ActivityState(
		val bottomSheetState: ModalBottomSheetState,
	) {
		val vaultKeyFlow: Flow<String?> = dataStore.data.map { preferences -> preferences[PREFERENCE_KEY_VAULT_KEY] }
		var vaultState = (application as Momento).vaultState
		var bottomSheetType: MutableState<BottomSheetType> = mutableStateOf(BottomSheetType.MenuBottomSheet)
		var momentoComponentType: MutableState<MomentoComponentType> = mutableStateOf(MomentoComponentType.Diary)
		var selectedEntryList: SnapshotStateList<String> = mutableStateListOf()
		var showDeleteDialog: MutableState<Boolean> = mutableStateOf(false)

		var isSelected = mutableStateOf(false)
		var showArchived = mutableStateOf(false)
		var showFavourite = mutableStateOf(false)
		var showTrash = mutableStateOf(false)
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	fun rememberMainActivityState(
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
	) = remember {
		ActivityState(bottomSheetState)
	}
}
