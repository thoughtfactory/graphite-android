package com.syncodec.momento

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.attachmentComponent.AttachmentActivity
import com.syncodec.momento.bucketComponent.BucketActivity
import com.syncodec.momento.custom.DeleteDialog
import com.syncodec.momento.custom.SplashScreen
import com.syncodec.momento.database.bucketItem.BucketItemType
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.miscellaneous.BottomNavigationBar
import com.syncodec.momento.mainComponent.miscellaneous.BottomNavigationItem
import com.syncodec.momento.mainComponent.miscellaneous.MainNavigation
import com.syncodec.momento.mainComponent.miscellaneous.TopBar
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.mainComponent.modalBottomSheet.SheetLayout
import com.syncodec.momento.mainComponent.screen.LoginScreen
import com.syncodec.momento.miscellaneous.ThemeUtils.Companion.tone
import com.syncodec.momento.miscellaneous.logger
import com.syncodec.momento.noteComponent.NoteActivity
import com.syncodec.momento.notebookComponent.NotebookActivity
import com.syncodec.momento.searchComponent.SearchActivity
import com.syncodec.momento.settings.SettingsActivity
import com.syncodec.momento.ui.theme.MomentoTheme
import com.syncodec.momento.vaultComponent.EvokeReason
import com.syncodec.momento.vaultComponent.VaultScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

	private val viewModel by viewModels<MainViewModel>()
	private var showLoginScreen: MutableState<Boolean?> = mutableStateOf(null)

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		CoroutineScope(Dispatchers.IO).launch {
			viewModel.dataStore.getIsFirstTime.collect {
				showLoginScreen.value = it && viewModel.firebaseAuth.currentUser == null
			}
		}

		setContent {
			viewModel.activityState = rememberActivityState()
			val showLoginScreen by showLoginScreen

			MomentoTheme {
				Crossfade(
					targetState = showLoginScreen,
					animationSpec = tween(600)
				) {
					when (it) {
						true -> LoginScreen() { onPerformAction(it) }
						false -> Screen()
						null -> SplashScreen()
					}
				}
			}
		}
	}

	private val signInLauncher =
		registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
			onSignInResult(result = result)
		}

	private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
		if (result.resultCode == RESULT_OK) {
			if (viewModel.firebaseAuth.currentUser != null) {
				showLoginScreen.value = false
			}
		} else {
			Toast.makeText(this, "Sign in failed", Toast.LENGTH_LONG).show()
		}
	}


	@OptIn(ExperimentalMaterialApi::class)
	private fun onPerformAction(action: Action, data: Any? = null) {
		val activityState = viewModel.activityState

		when (action) {
			Action.OPEN_LOGIN_SCREEN -> showLoginScreen.value = true
			Action.LOGIN -> {
				val providers =
					arrayListOf(
						AuthUI.IdpConfig.GoogleBuilder().setScopes(listOf("profile")).build()
					)

				val signInIntent = AuthUI.getInstance()
					.createSignInIntentBuilder()
					.setAvailableProviders(providers)
					.setAlwaysShowSignInMethodScreen(true)
					.build()
				signInLauncher.launch(signInIntent)
			}
			Action.TRY_FIRST -> {
				viewModel.dataStore.putIsFirstTime(false)
				showLoginScreen.value = false
			}
			Action.NAVIGATION -> {
				data as String
				val navController = activityState.navController
				val currentRoute = navController.currentBackStackEntry?.destination?.route
				if (!activityState.isSelected.value || activityState.selectedItemList.size == 0) {
					if (currentRoute == data) {
						if (currentRoute == "momento") {
							val currentComponent = activityState.componentType.value.ordinal
							onPerformAction(
								Action.CHANGE_COMPONENT,
								if (currentComponent == 0) 1 else 0
							)
						}
					} else {
						navController.navigate(data) {
							popUpTo(navController.graph.findStartDestination().id) {
								saveState = true
							}
							launchSingleTop = true
							restoreState = true
						}
					}
				}
			}
			Action.SHOW_DELETE -> activityState.showDeleteDialog.value = true
			Action.CLICK_NOTE -> {
				data as String
				val selectedItemList = activityState.selectedItemList

				if (activityState.isSelected.value) {
					activityState.isSelected.value = true
					if (data in selectedItemList) selectedItemList.remove(data)
					else selectedItemList.add(data)
				} else {
					Intent(this, NoteActivity::class.java).apply {
						putExtra(
							Konstant.Companion.Konstant.NOTEBOOK_KEY.name,
							viewModel.defaultNotebookKey
						)
						putStringArrayListExtra(
							Konstant.Companion.Konstant.CHAPTER_KEY.name,
							java.util.ArrayList()
						)
						putExtra(Konstant.Companion.Konstant.NOTE_KEY.name, data)
						putExtra(Konstant.Companion.Konstant.IS_VIEWER.name, true)
						startActivity(this)
					}
				}
			}
			Action.LONG_CLICK_NOTE -> {
				data as String
				activityState.isSelected.value = true
				val selectedItemList = activityState.selectedItemList
				if (data in selectedItemList) selectedItemList.remove(data)
				else selectedItemList.add(data)
			}
			Action.CLICK_NOTEBOOK -> {
				data as String
				Intent(this, NotebookActivity::class.java).apply {
					putExtra(Konstant.Companion.Konstant.NOTEBOOK_KEY.name, data)
					startActivity(this)
				}
			}
			Action.LONG_CLICK_NOTEBOOK -> {
				data as String
				activityState.isSelected.value = true
				val selectedItemList = activityState.selectedItemList
				if (data in selectedItemList) selectedItemList.remove(data)
				else selectedItemList.add(data)
			}
			Action.CLICK_BUCKET -> {
				data as String
				val selectedItemList = activityState.selectedItemList

				if (activityState.isSelected.value) {
					activityState.isSelected.value = true
					if (data in selectedItemList) selectedItemList.remove(data)
					else selectedItemList.add(data)
				} else {
					Intent(this, BucketActivity::class.java).apply {
						putExtra(Konstant.Companion.Konstant.BUCKET_KEY.name, data as String)
						startActivity(this)
					}
				}
			}
			Action.LONG_CLICK_BUCKET -> {
				data as String
				activityState.isSelected.value = true
				val selectedItemList = activityState.selectedItemList
				selectedItemList.add(data)
			}
			Action.NEW_NOTEBOOK -> {
				viewModel.insertNotebook(data as NotebookDbEntry)
				activityState.scope.launch { activityState.bottomSheetState.hide() }
			}
			Action.CHANGE_COMPONENT -> {
				if (!activityState.isSelected.value) {
					activityState.componentType.value = ComponentType.values()[data as Int]
				}
			}
			Action.BUCKET_FILTER_CHIP -> {
				data as BucketItemType
				if (data in activityState.bucketFilter) activityState.bucketFilter.remove(data)
				else activityState.bucketFilter.add(data)
			}
			Action.FAB -> {
				val openSheet: (BottomSheetType) -> Unit = { bottomSheetType ->
					activityState.bottomSheetType.value = bottomSheetType
					activityState.scope.launch { activityState.bottomSheetState.show() }
				}
				when (data as String?) {
					BottomNavigationItem.Momento.route -> {
						when (activityState.componentType.value) {
							ComponentType.NOTE -> if (viewModel.defaultNotebookKey != null) {
								startActivity(
									Intent(this@MainActivity, NoteActivity::class.java).apply {
										putExtra(
											Konstant.Companion.Konstant.NOTEBOOK_KEY.name,
											viewModel.defaultNotebookKey
										)
										putStringArrayListExtra(
											Konstant.Companion.Konstant.CHAPTER_KEY.name,
											ArrayList()
										)
										putExtra(Konstant.Companion.Konstant.TITLE.name, "")
									}
								)
							}
							ComponentType.NOTEBOOK -> openSheet(BottomSheetType.NotebookBottomSheet)
						}
					}
					BottomNavigationItem.Bucket.route -> openSheet(BottomSheetType.BucketBottomSheet)
				}
			}
			Action.MENU -> {
				BottomSheetType.MenuBottomSheet
				val openSheet: (BottomSheetType) -> Unit = { bottomSheetType ->
					activityState.bottomSheetType.value = bottomSheetType
					activityState.scope.launch { activityState.bottomSheetState.show() }
				}
				openSheet(BottomSheetType.MenuBottomSheet)
			}
			Action.SEARCH -> startActivity(Intent(this, SearchActivity::class.java))
			Action.ATTACHMENT -> {
				Intent(this, AttachmentActivity::class.java).apply {
					putExtra(Konstant.Companion.Konstant.IS_NOTE.name, false)
					putExtra(
						Konstant.Companion.Konstant.NOTEBOOK_KEY.name,
						viewModel.defaultNotebookKey
					)
					startActivity(this)
				}
				activityState.scope.launch { activityState.bottomSheetState.hide() }
			}
			Action.TAGS -> logger("open tags")
			Action.VAULT -> logger("open vault")
			Action.SETTINGS -> startActivity(Intent(this, SettingsActivity::class.java))
			Action.TOGGLE_FAVOURITE -> {
				activityState.showFavourite.value = !activityState.showFavourite.value
				activityState.scope.launch { activityState.bottomSheetState.hide() }
			}
			Action.TOGGLE_ARCHIVED -> {
				activityState.showArchived.value = !activityState.showArchived.value
				activityState.scope.launch { activityState.bottomSheetState.hide() }
			}
		}
	}

	override fun onBackPressed() {
		var vaultState by viewModel.activityState.vaultState
		var showArchived by viewModel.activityState.showArchived
		var showFavourite by viewModel.activityState.showFavourite
		var showLocked by viewModel.activityState.showLocked
		var isSelected by viewModel.activityState.isSelected

		if (vaultState == Momento.Companion.VaultState.TRY_OPEN) {
			vaultState = Momento.Companion.VaultState.NOT_OPENED
		} else {
			if (isSelected) {
				isSelected = false
				viewModel.activityState.selectedItemList.clear()
			} else if (showArchived || showFavourite || showLocked
			) {
				showArchived = false
				showFavourite = false
				showLocked = false
			} else {
				super.onBackPressed()
			}
		}
	}

	@OptIn(ExperimentalAnimatedInsets::class, ExperimentalMaterialApi::class)
	@Preview
	@ExperimentalPagerApi
	@ExperimentalFoundationApi
	@ExperimentalMaterial3Api
	@Composable
	fun Screen() {
		val systemUiController = rememberSystemUiController()
		systemUiController
			.setNavigationBarColor(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1))

		val activityState = viewModel.activityState
		val navBackStackEntry by activityState.navController.currentBackStackEntryAsState()
		val currentRoute = navBackStackEntry?.destination?.route

		Crossfade(
			targetState = activityState.vaultState.value,
			animationSpec = tween(durationMillis = 600)
		) {
			when (it) {
				Momento.Companion.VaultState.TRY_OPEN -> {
					systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)

					VaultScreen(
						evokeReason = EvokeReason.UNLOCK_VAULT,
						onSuccess = {
							activityState.vaultState.value = Momento.Companion.VaultState.OPENED
							activityState.showLocked.value = true
						}
					) {}
				}
				else -> {
					when (currentRoute) {
						"momento" -> systemUiController
							.setStatusBarColor(MaterialTheme.colorScheme.background)
						"bucket" -> systemUiController
							.setStatusBarColor(MaterialTheme.colorScheme.background)
						"calendar" -> systemUiController
							.setStatusBarColor(MaterialTheme.colorScheme.surface)
						"atlas" -> systemUiController
							.setStatusBarColor(MaterialTheme.colorScheme.surface)
						else -> systemUiController
							.setStatusBarColor(MaterialTheme.colorScheme.surface)
					}

					ModalBottomSheetLayout(
						sheetState = activityState.bottomSheetState,
						sheetElevation = 0.dp,
						sheetBackgroundColor = Color.Transparent,
						sheetShape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
						sheetContent = {
							SheetLayout { action, data -> onPerformAction(action, data) }
						},
					) {
						Image(
							painter = painterResource(id = R.drawable.background_1),
							contentDescription = null,
							contentScale = ContentScale.Crop,
							modifier = Modifier.fillMaxSize()
						)

						Scaffold(
							bottomBar = {
								BottomNavigationBar(
									currentRoute = currentRoute,
								) { action, data -> onPerformAction(action, data) }
							},
							floatingActionButtonPosition = FabPosition.End,
							floatingActionButton = { FloatingActionButton(currentRoute = currentRoute) },
							containerColor = MaterialTheme.colorScheme.background,
							topBar = {
								TopBar(
									isSelected = activityState.isSelected.value,
									selectedItemSize = activityState.selectedItemList.size,
									componentType = activityState.componentType.value,
									currentRoute = currentRoute,
									showFavorite = activityState.showFavourite.value,
									showArchived = activityState.showArchived.value,
									showLocked = false,
									bucketFilter = activityState.bucketFilter
								) { action, data -> onPerformAction(action, data) }
							}
						) {
							val viewModelStoreOwner =
								checkNotNull(LocalViewModelStoreOwner.current) {
									"No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
								}

							CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
								MainNavigation(
									navController = activityState.navController,
									viewModelStoreOwner = viewModelStoreOwner,
								) { click, data -> onPerformAction(action = click, data = data) }
							}
						}
						DeleteDialog(
							showDeleteDialog = activityState.showDeleteDialog.value,
							selectedItemSize = activityState.selectedItemList.size,
							onDismiss = { activityState.showDeleteDialog.value = false },
							onDelete = {
								val selectedItemList = activityState.selectedItemList.toList()
								viewModel.delete(keyList = selectedItemList)
								Toast.makeText(
									this,
									"${if (selectedItemList.size == 1) "1 entry" else "${selectedItemList.size} entries"} deleted",
									Toast.LENGTH_SHORT
								).show()
								activityState.selectedItemList.clear()
								activityState.isSelected.value = false
								activityState.showDeleteDialog.value = false
							},
						)
					}
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	private fun FloatingActionButton(currentRoute: String?) {
		when (currentRoute) {
			BottomNavigationItem.Calendar.route -> Box(modifier = Modifier)
			BottomNavigationItem.Atlas.route -> Box(modifier = Modifier)
			else -> {
				FloatingActionButton(
					onClick = { onPerformAction(Action.FAB, currentRoute) },
					modifier = Modifier.navigationBarsPadding(),
				) {
					Crossfade(targetState = currentRoute) { route ->
						when (route) {
							BottomNavigationItem.Momento.route -> when (viewModel.activityState.componentType.value) {
								ComponentType.NOTE -> Icon(
									painter = painterResource(id = R.drawable.ic_pencil),
									contentDescription = null,
									modifier = Modifier.requiredSize(24.dp)
								)
								ComponentType.NOTEBOOK -> Icon(
									painter = painterResource(id = R.drawable.ic_notebook),
									contentDescription = null,
									modifier = Modifier.requiredSize(24.dp)
								)
							}
							BottomNavigationItem.Bucket.route -> Icon(
								painter = painterResource(id = R.drawable.ic_bucket),
								contentDescription = null,
								modifier = Modifier.requiredSize(24.dp)
							)
						}
					}
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	inner class ActivityState(
		val scope: CoroutineScope,
		val navController: NavHostController,
		val bottomSheetState: ModalBottomSheetState,
	) {
		var vaultState = (application as Momento).vaultState
		var bottomSheetType: MutableState<BottomSheetType> =
			mutableStateOf(BottomSheetType.MenuBottomSheet)
		var componentType: MutableState<ComponentType> = mutableStateOf(ComponentType.NOTE)
		var selectedItemList: SnapshotStateList<String> = mutableStateListOf()
		var showDeleteDialog: MutableState<Boolean> = mutableStateOf(false)

		var isSelected = mutableStateOf(false)
		var showArchived = mutableStateOf(false)
		var showFavourite = mutableStateOf(false)
		var showLocked = mutableStateOf(false)
		var showTrash = mutableStateOf(false)
		val bucketFilter: SnapshotStateList<BucketItemType> =
			mutableStateListOf(BucketItemType.TODO, BucketItemType.BOOKS, BucketItemType.SHOWS)
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	private fun rememberActivityState(
		coroutineScope: CoroutineScope = rememberCoroutineScope(),
		navController: NavHostController = rememberNavController(),
		bottomSheetState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
	) = remember {
		ActivityState(coroutineScope, navController, bottomSheetState)
	}

	enum class Action {
		OPEN_LOGIN_SCREEN,
		LOGIN,
		TRY_FIRST,
		NAVIGATION,
		SHOW_DELETE,
		CLICK_NOTE,
		LONG_CLICK_NOTE,
		CLICK_NOTEBOOK,
		LONG_CLICK_NOTEBOOK,
		CLICK_BUCKET,
		LONG_CLICK_BUCKET,
		NEW_NOTEBOOK,
		CHANGE_COMPONENT,
		BUCKET_FILTER_CHIP,
		FAB,
		MENU,
		SEARCH,
		ATTACHMENT,
		TAGS,
		VAULT,
		SETTINGS,
		TOGGLE_FAVOURITE,
		TOGGLE_ARCHIVED,
		LOCKED
	}

	enum class ComponentType {
		NOTE,
		NOTEBOOK
	}
}
