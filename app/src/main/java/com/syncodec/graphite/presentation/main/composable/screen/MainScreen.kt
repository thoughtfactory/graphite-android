package com.syncodec.graphite.presentation.main.composable.screen

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.di.cloud.dropbox.DBox
import com.syncodec.graphite.di.model.local.NoteObjectLite
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.main.composable.bar.BottomBar
import com.syncodec.graphite.presentation.main.composable.bar.BottomNavigationItem
import com.syncodec.graphite.presentation.main.composable.bar.TopBar
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MenuBottomSheet
import com.syncodec.graphite.presentation.main.composable.screen.explorerScreen.AtlasScreen
import com.syncodec.graphite.presentation.main.composable.screen.explorerScreen.CalendarScreen
import com.syncodec.graphite.presentation.search.SearchActivity
import com.syncodec.graphite.utils.xor
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch


enum class ComponentType {
	Note,
	Bucket,
	Notebook
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
//	syncStatus: SyncInatorService.Companion.SyncStatus = SyncInatorService.Companion.SyncStatus.Init,
	testConnectionResponse: DBox.Companion.TestConnectionResponse? = null,
	testDropboxConnection: () -> Unit = {},
	locationFilteredNoteList: List<NoteObjectLite> = listOf(),
	onClickSyncNow: () -> Unit = {},
	onClickForceSync: () -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	var currentRoute by remember { mutableStateOf<BottomNavigationItem>(BottomNavigationItem.Home) }
	var currentScreen by rememberSaveable { mutableIntStateOf(0) }
	var currentComponentType: ComponentType by remember { mutableStateOf(ComponentType.Note) }

	val bottomSheetState = rememberModalBottomSheetState()
	var isMenuBottomSheetVisible by remember { mutableStateOf(false) }

	var isSelecting: Boolean by remember { mutableStateOf(false) }
	var selectedIdList: Set<RealmUUID> by remember { mutableStateOf(setOf()) }
	fun onSelect(id: RealmUUID) {
		isSelecting = true
		selectedIdList.toMutableSet().apply {
			xor(id)
			selectedIdList = toSet()
		}
	}

	BackHandler(enabled = currentScreen != 0) { currentScreen = 0 }
	BackHandler(enabled = currentRoute != BottomNavigationItem.Home) { currentRoute = BottomNavigationItem.Home }
	BackHandler(enabled = isSelecting) { isSelecting = false; selectedIdList = setOf() }

	GenericScaffold2(
		topBar = {
			TopBar(
				onClickMenu = { isMenuBottomSheetVisible = true },
				onClickCloud = {
//					if (syncStatus is SyncInatorService.Companion.SyncStatus.Init
//						|| syncStatus is SyncInatorService.Companion.SyncStatus.AutoSyncDisabled
//						|| syncStatus is SyncInatorService.Companion.SyncStatus.Locked
//						|| syncStatus is SyncInatorService.Companion.SyncStatus.CredentialError
//						|| syncStatus is SyncInatorService.Companion.SyncStatus.Idle
//						|| syncStatus is SyncInatorService.Companion.SyncStatus.Failed
//					) {
//						testDropboxConnection()
//					}
				},
				onClickSearch = {
					Intent(context, SearchActivity::class.java).apply {
						context.startActivity(this)
					}
				}
			)
		},
		bottomBar = {
			BottomBar(
				currentRoute = currentRoute.route,
				onNavigation = {
					when {
						currentRoute == BottomNavigationItem.Home && it == BottomNavigationItem.Home -> currentComponentType = ComponentType.values()[(currentComponentType.ordinal + 1) % 3]
						currentRoute != it -> currentRoute = it
					}
				}
			)
		},
		isTopBarVisible = !isSelecting,
		isBottomBarVisible = !isSelecting
	) {
		Crossfade(
			targetState = currentRoute,
			label = "currentRoute_animation",
		) {
			when (it) {
				BottomNavigationItem.Home -> HomeScreen(
					currentScreen = currentScreen,
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onChangeScreen = { currentScreen = it },
					onSelect = ::onSelect,
					onUnSelectAll = { selectedIdList = setOf() },
				)

				BottomNavigationItem.Calendar -> CalendarScreen(
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onSelect = ::onSelect,
					onUnSelectAll = { selectedIdList = setOf() },
				)

				BottomNavigationItem.Atlas -> AtlasScreen(
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onSelect = ::onSelect,
					onUnSelectAll = { selectedIdList = setOf() },
				)
			}
		}

	}

	MenuBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isMenuBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isMenuBottomSheetVisible = false } },
	)
}
