package com.syncodec.graphite.presentation.settings.composable.screen

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScreen
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton


@Preview
@Composable
fun LocalBackupScreen() {

	val context = LocalContext.current

	val scrollState = SettingsActivity.scrollState.current
	val onNavigate = SettingsActivity.onNavigate.current

	val setupLocalBackupFolder = SettingsActivity.setupLocalBackupFolder.current
	val removeLocalBackupFolder = SettingsActivity.removeLocalBackupFolder.current

	GenericSettingsScreen(
		title = "Local Backup",
		scrollState = scrollState
	) {
		SettingsButton(
			title = "Automatic Backup",
			icon = R.drawable.ic_hourglass,
			subTitle = "Setup a folder to store your backup files",
		) { Toast.makeText(context, "We are still working on this. Stay tuned!", Toast.LENGTH_LONG).show() }

		SettingsButton(
			title = "Setup Backup Folder",
			icon = R.drawable.ic_folder,
			subTitle = "Setup a folder to store your backup files",
		) { setupLocalBackupFolder() }

		SettingsButton(
			title = "Remove Backup Folder",
			icon = R.drawable.ic_folder_remove,
			subTitle = "Remove the backup folder",
			enabled = context.contentResolver.persistedUriPermissions.isNotEmpty()
		) { removeLocalBackupFolder() }

		SettingsButton(
			title = "Snapshot Warehouse",
			icon = R.drawable.ic_warehouse,
			subTitle = "View available backup files",
			enabled = context.contentResolver.persistedUriPermissions.isNotEmpty()
		) { onNavigate(SettingsActivity.Companion.Navigator.SNAPSHOT_WAREHOUSE) }
	}
}
