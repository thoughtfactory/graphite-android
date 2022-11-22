package com.syncodec.graphite.presentation.settings.composable.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.bar.TopBar
import com.syncodec.graphite.presentation.settings.composable.bottomSheet.SettingsBottomSheetType
import com.syncodec.graphite.presentation.settings.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.settings.composable.dialog.SettingsDialog
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SettingsScreen() {

	val scope = rememberCoroutineScope()

	val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
	var bottomSheetType : SettingsBottomSheetType by remember { mutableStateOf(SettingsBottomSheetType.PROFILE) }

	fun openSheet(_bottomSheetType : SettingsBottomSheetType) {
		scope.launch { bottomSheetType = _bottomSheetType; modalBottomSheetState.show() }
	}

	fun closeSheet() {
		scope.launch { modalBottomSheetState.hide() }
	}

	val navigatorPath = SettingsActivity.navigatorPath.current
	val scrollState = SettingsActivity.scrollState.current

	val onBackPressed = SettingsActivity.onBackPressed.current

	val title = when (navigatorPath.last()) {
		SettingsActivity.Companion.Navigator.BASE -> "Settings"
		SettingsActivity.Companion.Navigator.PREFERENCES -> "Preferences"
		SettingsActivity.Companion.Navigator.SECURITY -> "Security"
		SettingsActivity.Companion.Navigator.EXTENSIONS -> "Extensions"
		SettingsActivity.Companion.Navigator.BACKUP -> "Backup & Restore"
		SettingsActivity.Companion.Navigator.LOCAL_BACKUP -> "Local Backup"
		SettingsActivity.Companion.Navigator.SNAPSHOT_WAREHOUSE -> "Snapshot Warehouse"
		SettingsActivity.Companion.Navigator.SYNC -> "Synchronization"
		SettingsActivity.Companion.Navigator.ABOUT_US -> "About Us"
	}

	CompositionLocalProvider(
		SettingsActivity.openBottomSheet provides ::openSheet,
		SettingsActivity.closeBottomSheet provides ::closeSheet,
	) {
		ModalBottomSheetLayout(
			sheetContent = {
				SheetLayout(bottomSheetType = bottomSheetType) { closeSheet() }
			},
			sheetState = modalBottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			modifier = Modifier.fillMaxSize(),
		) {
			Scaffold(
				modifier = Modifier.fillMaxSize(),
				topBar = {
					TopBar(
						title = title,
						scrollState = scrollState,
					) { onBackPressed() }
				}
			) {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.padding(it)
				) {
					AnimatedContent(
						targetState = navigatorPath.last(),
						transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(300)) },
						modifier = Modifier.fillMaxSize()
					) {
						when (it) {
							SettingsActivity.Companion.Navigator.BASE -> BaseScreen()
							SettingsActivity.Companion.Navigator.PREFERENCES -> PreferencesScreen()
							SettingsActivity.Companion.Navigator.SECURITY -> SecurityScreen()
							SettingsActivity.Companion.Navigator.EXTENSIONS -> ExtensionsScreen()
							SettingsActivity.Companion.Navigator.BACKUP -> BackupAndRestoreScreen()
							SettingsActivity.Companion.Navigator.LOCAL_BACKUP -> LocalBackupScreen()
							SettingsActivity.Companion.Navigator.SNAPSHOT_WAREHOUSE -> SnapshotWarehouseScreen()
							SettingsActivity.Companion.Navigator.SYNC -> SynchronizationScreen()
							SettingsActivity.Companion.Navigator.ABOUT_US -> AboutUsScreen( )
						}
					}
				}
			}

			SettingsDialog()
		}
	}
}
