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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
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
import com.syncodec.momento.premiumComponent.PremiumActivity
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
				viewModel.getQuote()
			}
		} else {
			Toast.makeText(this, "Sign in failed", Toast.LENGTH_LONG).show()
		}
	}


	@OptIn(ExperimentalMaterialApi::class)
	private fun onPerformAction(action: Action, data: Any? = null) {
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
				viewModel.getQuote()
			}
			Action.TRY_PREMIUM -> {
				Intent(this, PremiumActivity::class.java).apply {
					startActivity(this)
				}
			}
			Action.SHOW_DELETE -> viewModel.showDeleteDialog.value = true
			Action.ON_DELETE -> {
				val selectedItemList = viewModel.selectedItemList.toMutableList()
				if (selectedItemList.contains(viewModel.defaultNotebookKey)) {
					Toast.makeText(
						this,
						"Cannot delete default notebook",
						Toast.LENGTH_SHORT
					).show()
				}
				selectedItemList.remove(viewModel.defaultNotebookKey)

				viewModel.delete(keyList = selectedItemList)
				Toast.makeText(
					this,
					"${if (selectedItemList.size == 1) "1 entry" else "${selectedItemList.size} entries"} deleted",
					Toast.LENGTH_SHORT
				).show()
				viewModel.selectedItemList.clear()
				viewModel.isSelected.value = false
				viewModel.showDeleteDialog.value = false
			}
			Action.CLICK_NOTE -> {
				data as String
				val selectedItemList = viewModel.selectedItemList

				if (viewModel.isSelected.value) {
					viewModel.isSelected.value = true
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
						putExtra(Konstant.Companion.Konstant.IS_NEW.name, false)
						startActivity(this)
					}
				}
			}
			Action.LONG_CLICK_NOTE -> {
				data as String
				viewModel.isSelected.value = true
				val selectedItemList = viewModel.selectedItemList
				if (data in selectedItemList) selectedItemList.remove(data)
				else selectedItemList.add(data)
			}
			Action.CLICK_NOTEBOOK -> {
				data as String
				val selectedItemList = viewModel.selectedItemList

				if (viewModel.isSelected.value) {
					viewModel.isSelected.value = true
					if (data in selectedItemList) selectedItemList.remove(data)
					else selectedItemList.add(data)
				} else {
					Intent(this, NotebookActivity::class.java).apply {
						putExtra(Konstant.Companion.Konstant.NOTEBOOK_KEY.name, data)
						startActivity(this)
					}
				}
			}
			Action.LONG_CLICK_NOTEBOOK -> {
				data as String
				viewModel.isSelected.value = true
				val selectedItemList = viewModel.selectedItemList
				if (data in selectedItemList) selectedItemList.remove(data)
				else selectedItemList.add(data)
			}
			Action.CLICK_BUCKET -> {
				data as String
				val selectedItemList = viewModel.selectedItemList

				if (viewModel.isSelected.value) {
					viewModel.isSelected.value = true
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
				viewModel.isSelected.value = true
				val selectedItemList = viewModel.selectedItemList
				selectedItemList.add(data)
			}
			Action.NEW_NOTEBOOK -> {
				viewModel.insertNotebook(data as NotebookDbEntry)
			}
			Action.NEW_BUCKET -> {
				data as Pair<*, *>
				viewModel.createNewBucket(
					bucketType = data.second as BucketItemType,
					title = data.first as String
				)
			}
			Action.CHANGE_COMPONENT -> {
				if (!viewModel.isSelected.value) {
					viewModel.componentType.value = ComponentType.values()[data as Int]
				}
			}
			Action.BUCKET_FILTER_CHIP -> {
				data as BucketItemType
				if (data in viewModel.bucketFilter) viewModel.bucketFilter.remove(data)
				else viewModel.bucketFilter.add(data)
			}
			Action.NEW_NOTE -> {
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
						putExtra(Konstant.Companion.Konstant.IS_NEW.name, true)
					}
				)

			}
			Action.MENU -> {
				BottomSheetType.MenuBottomSheet
				val openSheet: (BottomSheetType) -> Unit = { bottomSheetType ->
					viewModel.bottomSheetType.value = bottomSheetType
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
			}
			Action.TAGS -> logger("open tags")
			Action.VAULT -> logger("open vault")
			Action.SETTINGS -> startActivity(Intent(this, SettingsActivity::class.java))
			Action.TOGGLE_FAVOURITE -> {
				viewModel.showFavourite.value = !viewModel.showFavourite.value
			}
			Action.TOGGLE_ARCHIVED -> {
				viewModel.showArchived.value = !viewModel.showArchived.value
			}
		}
	}

	override fun onBackPressed() {
		var vaultState by viewModel.vaultState
		var showArchived by viewModel.showArchived
		var showFavourite by viewModel.showFavourite
		var showLocked by viewModel.showLocked
		var isSelected by viewModel.isSelected

		if (vaultState == Momento.Companion.VaultState.TRY_OPEN) {
			vaultState = Momento.Companion.VaultState.NOT_OPENED
		} else {
			if (isSelected) {
				isSelected = false
				viewModel.selectedItemList.clear()
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

		val scope = rememberCoroutineScope()
		val navController = rememberNavController()
		val navBackStackEntry by navController.currentBackStackEntryAsState()
		val currentRoute = navBackStackEntry?.destination?.route
		val bottomSheetState =
			rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

		Crossfade(
			targetState = viewModel.vaultState.value,
			animationSpec = tween(durationMillis = 600)
		) {
			when (it) {
				Momento.Companion.VaultState.TRY_OPEN -> {
					systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)

					VaultScreen(
						evokeReason = EvokeReason.UNLOCK_VAULT,
						onSuccess = {
							viewModel.vaultState.value = Momento.Companion.VaultState.OPENED
							viewModel.showLocked.value = true
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
						sheetState = bottomSheetState,
						sheetElevation = 0.dp,
						sheetBackgroundColor = Color.Transparent,
						sheetShape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
						sheetContent = {
							SheetLayout(bottomSheetType = viewModel.bottomSheetType.value) { action, data ->
								onPerformAction(action, data)
								scope.launch { bottomSheetState.hide() }
							}
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
									onNavigation = {
										if (!viewModel.isSelected.value || viewModel.selectedItemList.size == 0) {
											if (currentRoute == it) {
												if (currentRoute == "momento") {
													val currentComponent =
														viewModel.componentType.value.ordinal
													onPerformAction(
														Action.CHANGE_COMPONENT,
														if (currentComponent == 0) 1 else 0
													)
												}
											} else {
												navController.navigate(it) {
													popUpTo(navController.graph.findStartDestination().id) {
														saveState = true
													}
													launchSingleTop = true
													restoreState = true
												}
											}
										}
									}
								)
							},
							floatingActionButtonPosition = FabPosition.End,
							floatingActionButton = {
								FloatingActionButton(currentRoute = currentRoute) { action, data ->
									if (action == Action.OPEN_BOTTOM_SHEET) {
										viewModel.bottomSheetType.value = data as BottomSheetType
										scope.launch { bottomSheetState.show() }
									} else {
										scope.launch { bottomSheetState.hide() }
										onPerformAction(action, data)
									}
								}
							},
							containerColor = MaterialTheme.colorScheme.background,
							topBar = {
								TopBar(
									isSelected = viewModel.isSelected.value,
									selectedItemSize = viewModel.selectedItemList.size,
									componentType = viewModel.componentType.value,
									currentRoute = currentRoute,
									showFavorite = viewModel.showFavourite.value,
									showArchived = viewModel.showArchived.value,
									showLocked = false,
									bucketFilter = viewModel.bucketFilter
								) { action, data ->
									if (action == Action.OPEN_BOTTOM_SHEET) {
										viewModel.bottomSheetType.value = data as BottomSheetType
										scope.launch { bottomSheetState.show() }
									} else {
										scope.launch { bottomSheetState.hide() }
									}
									onPerformAction(action, data)
								}
							}
						) {
							val viewModelStoreOwner =
								checkNotNull(LocalViewModelStoreOwner.current) {
									"No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
								}

							CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
								MainNavigation(
									navController = navController,
									viewModelStoreOwner = viewModelStoreOwner,
								) { action, data ->
									if (action == Action.OPEN_BOTTOM_SHEET) {
										viewModel.bottomSheetType.value = data as BottomSheetType
										scope.launch { bottomSheetState.show() }
									} else {
										scope.launch { bottomSheetState.hide() }
									}
									onPerformAction(action = action, data = data)
								}
							}
						}
						DeleteDialog(
							showDeleteDialog = viewModel.showDeleteDialog.value,
							selectedItemSize = viewModel.selectedItemList.size,
							onDismiss = { viewModel.showDeleteDialog.value = false },
							onDelete = { onPerformAction(Action.ON_DELETE) },
						)
					}
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	@Composable
	private fun FloatingActionButton(
		currentRoute: String?,
		onAction: (Action, Any?) -> Unit
	) {
		when (currentRoute) {
			BottomNavigationItem.Calendar.route -> Box(modifier = Modifier)
			BottomNavigationItem.Atlas.route -> Box(modifier = Modifier)
			else -> {
				FloatingActionButton(
					onClick = {
						when (currentRoute) {
							BottomNavigationItem.Momento.route -> {
								when (viewModel.componentType.value) {
									ComponentType.NOTE -> onAction(Action.NEW_NOTE, null)
									ComponentType.NOTEBOOK -> onAction(
										Action.OPEN_BOTTOM_SHEET,
										BottomSheetType.NotebookBottomSheet
									)
								}
							}
							BottomNavigationItem.Bucket.route -> onAction(
								Action.OPEN_BOTTOM_SHEET,
								BottomSheetType.BucketBottomSheet
							)

						}
					},
					modifier = Modifier.navigationBarsPadding(),
				) {
					Crossfade(targetState = currentRoute) { route ->
						when (route) {
							BottomNavigationItem.Momento.route -> when (viewModel.componentType.value) {
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

	enum class Action {
		OPEN_LOGIN_SCREEN,
		LOGIN,
		TRY_FIRST,
		TRY_PREMIUM,
		SHOW_DELETE,
		ON_DELETE,
		CLICK_NOTE,
		LONG_CLICK_NOTE,
		CLICK_NOTEBOOK,
		LONG_CLICK_NOTEBOOK,
		CLICK_BUCKET,
		LONG_CLICK_BUCKET,
		NEW_NOTE,
		NEW_NOTEBOOK,
		NEW_BUCKET,
		CHANGE_COMPONENT,
		BUCKET_FILTER_CHIP,
		OPEN_BOTTOM_SHEET,
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
