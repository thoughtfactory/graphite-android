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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.bar.TopBar


@OptIn(
	ExperimentalMaterial3Api::class,
	ExperimentalAnimationApi::class
)
@Composable
fun SettingsScreen() {

	val navigatorPath = SettingsActivity.LocalNavigatorPath.current
	val scrollState = SettingsActivity.LocalScrollState.current

	val onBackPressed = SettingsActivity.LocalOnBackPressed.current

	val title = when (navigatorPath.last()) {
		SettingsActivity.Companion.Navigator.BASE -> "Settings"
		SettingsActivity.Companion.Navigator.PREFERENCES -> "Preferences"
		SettingsActivity.Companion.Navigator.THEME -> "Theme"
		SettingsActivity.Companion.Navigator.SECURITY -> "Security"
		SettingsActivity.Companion.Navigator.EXTENSIONS -> "Extensions"
		SettingsActivity.Companion.Navigator.BACKUP -> "Backup & Restore"
		SettingsActivity.Companion.Navigator.DATA -> "Data"
		SettingsActivity.Companion.Navigator.IMPORT -> "Import"
		SettingsActivity.Companion.Navigator.LOCAL_BACKUP -> "Local Backup"
		SettingsActivity.Companion.Navigator.SNAPSHOT_WAREHOUSE -> "Snapshot Warehouse"
		SettingsActivity.Companion.Navigator.SYNC -> "Synchronization"
		SettingsActivity.Companion.Navigator.ABOUT_US -> "About Us"
	}

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
					SettingsActivity.Companion.Navigator.THEME -> ThemeScreen()
					SettingsActivity.Companion.Navigator.SECURITY -> SecurityScreen()
					SettingsActivity.Companion.Navigator.EXTENSIONS -> ExtensionsScreen()
					SettingsActivity.Companion.Navigator.BACKUP -> BackupAndRestoreScreen()
					SettingsActivity.Companion.Navigator.DATA -> DataScreen()
					SettingsActivity.Companion.Navigator.IMPORT -> ImportScreen()
					SettingsActivity.Companion.Navigator.LOCAL_BACKUP -> LocalBackupScreen()
					SettingsActivity.Companion.Navigator.SNAPSHOT_WAREHOUSE -> SnapshotWarehouseScreen()
					SettingsActivity.Companion.Navigator.SYNC -> SynchronizationScreen()
					SettingsActivity.Companion.Navigator.ABOUT_US -> AboutUsScreen( )
				}
			}
		}
	}
}
