package com.syncodec.graphite.presentation.main.composable.screen

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.di.cloud.dropbox.DBox
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.main.composable.bar.BottomBar
import com.syncodec.graphite.presentation.main.composable.bar.BottomNavigationItem
import com.syncodec.graphite.presentation.main.composable.bar.TopBar
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MenuBottomSheet
import com.syncodec.graphite.presentation.main.composable.screen.explorerScreen.AtlasScreen
import com.syncodec.graphite.presentation.main.composable.screen.explorerScreen.CalendarScreen
import com.syncodec.graphite.presentation.search.SearchActivity
import com.syncodec.graphite.service.syncInator.SyncInatorService
import io.realm.kotlin.types.RealmUUID


enum class ComponentType {
	Note,
	Bucket,
	Notebook
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
	syncStatus: SyncInatorService.Companion.SyncStatus = SyncInatorService.Companion.SyncStatus.Init,
	testConnectionResponse: DBox.Companion.TestConnectionResponse? = null,
	testDropboxConnection: () -> Unit = {},
	locationFilteredNoteList: List<NoteObjectLite> = listOf(),
	onClickSyncNow: () -> Unit = {},
	onClickForceSync: () -> Unit = {},
) {
	val context = LocalContext.current

	var currentRoute by remember { mutableStateOf<BottomNavigationItem>(BottomNavigationItem.Home) }
	var currentComponentType: ComponentType by remember { mutableStateOf(ComponentType.Note) }

	var isSelecting: Boolean by remember { mutableStateOf(false) }
	var selectedIdList: List<RealmUUID> by remember { mutableStateOf(listOf()) }

	val bottomSheetState = rememberModalBottomSheetState()
	var isMenuBottomSheetVisible by remember { mutableStateOf(false) }

	BackHandler(enabled = currentRoute != BottomNavigationItem.Home) { currentRoute = BottomNavigationItem.Home }
	BackHandler(enabled = isSelecting) { isSelecting = false; selectedIdList = listOf() }

	GenericScaffold2(
		topBar = {
			TopBar(
				syncStatus = syncStatus,
				onClickMenu = { isMenuBottomSheetVisible = true },
				onClickCloud = {
					if (syncStatus is SyncInatorService.Companion.SyncStatus.Init
						|| syncStatus is SyncInatorService.Companion.SyncStatus.AutoSyncDisabled
						|| syncStatus is SyncInatorService.Companion.SyncStatus.Locked
						|| syncStatus is SyncInatorService.Companion.SyncStatus.CredentialError
						|| syncStatus is SyncInatorService.Companion.SyncStatus.Idle
						|| syncStatus is SyncInatorService.Companion.SyncStatus.Failed
					) {
						testDropboxConnection()
					}
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
					isSelecting = isSelecting,
//					onSelect = onSelect,
					selectedIdList = selectedIdList,
				)

				BottomNavigationItem.Calendar -> CalendarScreen()
				BottomNavigationItem.Atlas -> AtlasScreen()
			}
		}

	}

	MenuBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isMenuBottomSheetVisible,
		onDismissRequest = { isMenuBottomSheetVisible = false },
	)
}
