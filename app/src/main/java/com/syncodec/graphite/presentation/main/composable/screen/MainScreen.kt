package com.syncodec.graphite.presentation.main.composable.screen

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.syncodec.graphite.di.cloud.dropbox.DropboxApi
import com.syncodec.graphite.di.network.NetworkRequest
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.common.biometric.BiometricComposable
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.main.HomeComponent
import com.syncodec.graphite.presentation.main.MainComponent
import com.syncodec.graphite.presentation.main.composable.bar.BottomBar
import com.syncodec.graphite.presentation.main.composable.bar.TopBar
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MenuBottomSheet
import com.syncodec.graphite.presentation.main.composable.bottomSheet.syncBottomSheet2.SyncBottomSheet2
import com.syncodec.graphite.presentation.main.composable.screen.bucketScreen.BucketScreen
import com.syncodec.graphite.presentation.main.composable.screen.explorerScreen.AtlasScreen
import com.syncodec.graphite.presentation.main.composable.screen.explorerScreen.CalendarScreen
import com.syncodec.graphite.presentation.main.composable.screen.noteScreen.NoteScreen
import com.syncodec.graphite.presentation.main.composable.screen.notebookScreen.NotebookScreen
import com.syncodec.graphite.presentation.search.SearchActivity
import com.syncodec.graphite.service.syncInator.SyncStat
import com.syncodec.graphite.utils.xor
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MainScreen(
	syncStat: SyncStat = SyncStat.Init,
	dropboxAccountInfo: NetworkRequest<DropboxApi.Companion.DropboxAccountInfo> = NetworkRequest.Init,
	onClickTestConnection: () -> Unit = {},
	onClickForceSync: () -> Unit = {},
	onClickSyncNow: () -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val bottomSheetState = rememberModalBottomSheetState()
	var isMenuBottomSheetVisible by remember { mutableStateOf(false) }
	var isSyncBottomSheetVisible by remember { mutableStateOf(false) }

	var isSelecting: Boolean by remember { mutableStateOf(false) }
	var selectedIdList: Set<RealmUUID> by remember { mutableStateOf(setOf()) }
	fun onSelect(id: RealmUUID) {
		isSelecting = true
		selectedIdList.toMutableSet().apply {
			xor(id)
			selectedIdList = toSet()
		}
	}

	var currentMainRoute by remember { mutableStateOf<MainComponent>(MainComponent.Home) }
	val homeNavController = rememberNavController()
	val homeNavBackStackEntry by homeNavController.currentBackStackEntryAsState()
	val currentHomeRoute by remember(homeNavBackStackEntry?.destination?.route) { derivedStateOf { HomeComponent.fromRoute(homeNavBackStackEntry?.destination?.route) ?: HomeComponent.Note } }

	BackHandler(enabled = !isSelecting && homeNavController.currentBackStackEntry?.destination?.route != HomeComponent.Note.route && currentMainRoute != MainComponent.Home) {
		homeNavController.popBackStack(route = HomeComponent.Note.route, inclusive = false, saveState = true)
	}
	BackHandler(enabled = currentMainRoute != MainComponent.Home) {
		currentMainRoute = MainComponent.Home
	}

	GenericScaffold2(
		topBar = {
			Column {
				TopBar(
					onClickMenu = { isMenuBottomSheetVisible = true },
					onClickCloud = { isSyncBottomSheetVisible = true },
					onClickSearch = { context.startActivity(Intent(context, SearchActivity::class.java)) }
				)
			}
		},
		bottomBar = {
			BottomBar(
				currentRoute = currentMainRoute,
				onNavigation = { currentMainRoute = it }
			)
		},
		isTopBarVisible = !isSelecting,
		isBottomBarVisible = !isSelecting
	) {
		BiometricComposable {
			AnimatedContent(
				targetState = currentMainRoute,
				transitionSpec = { fadeIn(tween(ANIMATION_DURATION_MILLIS)) + scaleIn(tween(ANIMATION_DURATION_MILLIS), 0.80f) togetherWith fadeOut(tween(ANIMATION_DURATION_MILLIS)) + scaleOut(tween(ANIMATION_DURATION_MILLIS), 0.80f) },
				label = "currentMainRoute_animation"
			) { currentMainRoute1 ->
				when (currentMainRoute1) {
					is MainComponent.Home -> Column {
						HomeTabNavigator(
							currentRoute = currentHomeRoute,
							isVisible = !isSelecting,
							onNavigate = { homeNavController.navigate(route = it.route) { this.popUpTo(HomeComponent.Note.route) } }
						)

						NavHost(
							navController = homeNavController,
							startDestination = HomeComponent.Note.route,
							enterTransition = { scaleIn(tween(ANIMATION_DURATION_MILLIS), 0.69f) + fadeIn(tween(ANIMATION_DURATION_MILLIS)) },
							exitTransition = { scaleOut(tween(ANIMATION_DURATION_MILLIS), 0.69f) + fadeOut(tween(ANIMATION_DURATION_MILLIS)) },
							modifier = Modifier.fillMaxSize(),
						) {
							composable(HomeComponent.Note.route) {
								NoteScreen(
									isSelecting = isSelecting,
									selectedIdList = selectedIdList,
									onSelect = ::onSelect,
									onUnSelectAll = { selectedIdList = setOf(); isSelecting = false },
								)
							}
							composable(HomeComponent.Bucket.route) {
								BucketScreen(
									isSelecting = isSelecting,
									selectedIdList = selectedIdList,
									onSelect = ::onSelect,
									onUnSelectAll = { selectedIdList = setOf(); isSelecting = false },
								)
							}
							composable(HomeComponent.Notebook.route) {
								NotebookScreen(
									isSelecting = isSelecting,
									selectedIdList = selectedIdList,
									onSelect = ::onSelect,
									onUnSelectAll = { selectedIdList = setOf(); isSelecting = false },
								)
							}
						}
					}

					is MainComponent.Calendar -> CalendarScreen(
						isSelecting = isSelecting,
						selectedIdList = selectedIdList,
						onSelect = ::onSelect,
						onUnSelectAll = { selectedIdList = setOf(); isSelecting = false },
					)

					is MainComponent.Atlas -> AtlasScreen(
						isSelecting = isSelecting,
						selectedIdList = selectedIdList,
						onSelect = ::onSelect,
						onUnSelectAll = { selectedIdList = setOf(); isSelecting = false },
					)
				}
			}
		}
	}

	MenuBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isMenuBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isMenuBottomSheetVisible = false } },
	)

	SyncBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isSyncBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isSyncBottomSheetVisible = false } },
		syncStat = syncStat,
		dropboxAccountInfo = dropboxAccountInfo,
		onClickTestConnection = onClickTestConnection,
		onClickForceSync = onClickForceSync,
		onClickSyncNow = onClickSyncNow,
	)
}
