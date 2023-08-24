package com.syncodec.graphite.presentation.main.composable.screen

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.di.cloud.dropbox.DBox
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.explorer.ExplorerActivity
import com.syncodec.graphite.presentation.main.composable.bar.BottomNavigationBar
import com.syncodec.graphite.presentation.main.composable.bar.BottomNavigationItem
import com.syncodec.graphite.presentation.main.composable.bar.MainNavigation
import com.syncodec.graphite.presentation.main.composable.bar.TopBar
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MainBottomSheetType
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MenuBottomSheet
import com.syncodec.graphite.presentation.main.composable.dialog.MainDialogType
import com.syncodec.graphite.service.syncInator.SyncInatorService
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch


enum class ComponentType {
	Note,
	Bucket,
	Notebook
}

@OptIn(
	ExperimentalMaterialApi::class,
	ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
)
@Composable
fun MainScreen(
	syncStatus: SyncInatorService.Companion.SyncStatus = SyncInatorService.Companion.SyncStatus.Init,
	testConnectionResponse: DBox.Companion.TestConnectionResponse? = null,
	testDropboxConnection: () -> Unit = {},
	onClickSyncNow: () -> Unit = {},
	onClickForceSync: () -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	var currentRoute by remember { mutableStateOf<BottomNavigationItem>(BottomNavigationItem.Home) }
	var currentComponentType: ComponentType by remember { mutableStateOf(ComponentType.Note) }

	fun openSheet(sheetType: MainBottomSheetType) = scope.launch { }

	var isSelecting: Boolean by remember { mutableStateOf(false) }
	var selectedIdList: List<RealmUUID> by remember { mutableStateOf(listOf()) }

	var showNotificationPermissionDialog: Boolean by remember { mutableStateOf(false) }
	var showDeleteDialog: Boolean by remember { mutableStateOf(false) }

	fun openDialog(dialogType: MainDialogType) = when (dialogType) {
		MainDialogType.NotificationPermission -> showNotificationPermissionDialog = true
		MainDialogType.Delete -> showDeleteDialog = true
	}

	BackHandler(enabled = currentRoute != BottomNavigationItem.Home) { currentRoute = BottomNavigationItem.Home }

	BackHandler(enabled = isSelecting) {
		isSelecting = false
		selectedIdList = listOf()
	}

	val bottomSheetState = rememberModalBottomSheetState()
	var isMenuBottomSheetVisible by remember { mutableStateOf(false) }

	GenericScaffold2(
		topBar = {
			TopBar(
				currentRoute = currentRoute.route,
				isSelecting = isSelecting,
				selectedSize = selectedIdList.size,
				syncStatus = syncStatus,
				onClickMenu = { isMenuBottomSheetVisible = true },
				onClickCancelSelect = {
					isSelecting = false
					selectedIdList = listOf()
				},
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
					openSheet(MainBottomSheetType.Sync)
				},
				onClickSearch = {
					Intent(context, ExplorerActivity::class.java).apply {
						putExtra(Extra.Companion.Extra.ExplorerType.name, Extra.Companion.ExplorerType.Search.name)
						context.startActivity(this)
					}
				}
			) { openDialog(MainDialogType.Delete) }
		},
		bottomBar = {
			BottomNavigationBar(
				currentRoute = currentRoute.route,
				onNavigation = {
					when {
						currentRoute == BottomNavigationItem.Home && it == BottomNavigationItem.Home -> currentComponentType = ComponentType.values()[(currentComponentType.ordinal + 1) % 3]
						currentRoute != it -> currentRoute = it
					}
				}
			)
		},
		isBottomBarVisible = !isSelecting,
	) {
		MainNavigation(
			currentRoute = currentRoute,
			isSelecting = isSelecting,
			onSelect = {
				isSelecting = true
				selectedIdList.toMutableList()
					.apply {
						if (it in this) remove(it) else add(it)
						selectedIdList = this
					}
			},
			selectedIdList = selectedIdList,
		)
	}

	MenuBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isMenuBottomSheetVisible,
		onDismissRequest = { isMenuBottomSheetVisible = false },
	)
}
