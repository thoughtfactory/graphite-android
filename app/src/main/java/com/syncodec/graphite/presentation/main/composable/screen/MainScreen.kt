package com.syncodec.graphite.presentation.main.composable.screen

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.explorer.ExplorerActivity
import com.syncodec.graphite.presentation.main.MainViewModel
import com.syncodec.graphite.presentation.main.composable.bar.BottomNavigationBar
import com.syncodec.graphite.presentation.main.composable.bar.BottomNavigationItem
import com.syncodec.graphite.presentation.main.composable.bar.MainNavigation
import com.syncodec.graphite.presentation.main.composable.bar.TopBar
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MainBottomSheetType
import com.syncodec.graphite.presentation.main.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.main.composable.dialog.MainDialog
import com.syncodec.graphite.presentation.main.composable.dialog.MainDialogType
import com.syncodec.graphite.service.DropboxService
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


enum class ComponentType {
	Note,
	Bucket,
	Notebook
}

@OptIn(
	ExperimentalMaterialApi::class,
	ExperimentalPagerApi::class,
	ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class
)
@Composable
fun MainScreen(
	syncStatus : DropboxService.Companion.DropboxSyncStatus = DropboxService.Companion.DropboxSyncStatus.Init,
	onClickSyncNow : () -> Unit = {},
	onClickForceSync : () -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val viewModel : MainViewModel = koinViewModel()

	var currentRoute by remember { mutableStateOf<BottomNavigationItem>(BottomNavigationItem.Home) }

	val keyboardController = LocalSoftwareKeyboardController.current
	val focusManager = LocalFocusManager.current

	var bottomSheetType : MainBottomSheetType by remember { mutableStateOf(MainBottomSheetType.Menu) }
	var currentComponentType : ComponentType by remember { mutableStateOf(ComponentType.Note) }

	val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

	LaunchedEffect(key1 = modalBottomSheetState.currentValue) {
		if (modalBottomSheetState.currentValue == ModalBottomSheetValue.Hidden) {
			try {
				keyboardController?.hide()
				focusManager.clearFocus()
			} catch (e : Exception) {
			}
		}
	}

	fun openSheet(sheetType : MainBottomSheetType) = scope.launch { bottomSheetType = sheetType; modalBottomSheetState.show() }

	fun closeSheet() = scope.launch { modalBottomSheetState.hide() }

	var isSelecting : Boolean by remember { mutableStateOf(false) }
	var selectedIdList : List<RealmUUID> by remember { mutableStateOf(listOf()) }

	var showNotificationPermissionDialog : Boolean by remember { mutableStateOf(false) }
	var showDeleteDialog : Boolean by remember { mutableStateOf(false) }

	fun openDialog(dialogType : MainDialogType) = when (dialogType) {
		MainDialogType.NotificationPermission -> showNotificationPermissionDialog = true
		MainDialogType.Delete -> showDeleteDialog = true
	}

	fun closeDialog(dialogType : MainDialogType) = when (dialogType) {
		MainDialogType.NotificationPermission -> showNotificationPermissionDialog = false
		MainDialogType.Delete -> showDeleteDialog = false
	}

	BackHandler(enabled = currentRoute != BottomNavigationItem.Home) { currentRoute = BottomNavigationItem.Home }

	BackHandler(enabled = isSelecting) {
		isSelecting = false
		selectedIdList = listOf()
	}

	GenericScaffold(
		topBar = {
			TopBar(
				currentRoute = currentRoute.route,
				componentType = currentComponentType,
				isSelecting = isSelecting,
				selectedSize = selectedIdList.size,
				syncStatus = syncStatus,
				onComponentChange = { currentComponentType = ComponentType.values()[it] },
				onClickFilter = { openSheet(MainBottomSheetType.Filter) },
				onClickMenu = { openSheet(MainBottomSheetType.Menu) },
				onClickCancelSelect = {
					isSelecting = false
					selectedIdList = listOf()
				},
				onClickCloud = { openSheet(MainBottomSheetType.Sync) },
				onClickSearch = {
					Intent(context, ExplorerActivity::class.java).apply {
						putExtra(Extra.Companion.Extra.ExplorerType.name, Extra.Companion.ExplorerType.Search.name)
						context.startActivity(this)
					}
				},
				onClickDelete = { openDialog(MainDialogType.Delete) }
			)
		},
		bottomBar = {
			BottomNavigationBar(
				currentRoute = currentRoute.route,
				onNavigation = {
					when {
						currentRoute == BottomNavigationItem.Home && it == BottomNavigationItem.Home -> currentComponentType =
							ComponentType.values()[(currentComponentType.ordinal + 1) % 3]

						currentRoute != it -> currentRoute = it

					}
				}
			)
		},
		isBottomBarVisible = ! isSelecting,
		modalBottomSheetState = modalBottomSheetState,
		sheetContent = {
			SheetLayout(
				bottomSheetType = bottomSheetType,
				syncStatus = syncStatus,
				putBucket = viewModel::putBucket,
				putNotebook = viewModel::putNotebook,
				onClickSyncNow = onClickSyncNow,
				onClickForceSync = onClickForceSync,
				closeSheet = ::closeSheet
			)
		},
		dialogContent = {
			MainDialog(
				showNotificationPermissionDialog = showNotificationPermissionDialog,
				showDeleteDialog = showDeleteDialog,
				onDelete = { viewModel.delete(idList = selectedIdList.toList()); isSelecting = false; selectedIdList = listOf() },
				closeDialog = ::closeDialog,
			)
		}
	) {
		MainNavigation(
			currentRoute = currentRoute,
			componentType = currentComponentType,
			isSelecting = isSelecting,
			onSelect = {
				isSelecting = true
				selectedIdList.toMutableList().apply {
					if (it in this) remove(it) else add(it)
					selectedIdList = this
				}
			},
			selectedIdList = selectedIdList,
			openSheet = ::openSheet,
		)
	}
}
