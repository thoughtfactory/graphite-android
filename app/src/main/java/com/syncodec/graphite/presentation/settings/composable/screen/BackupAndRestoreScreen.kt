package com.syncodec.graphite.presentation.settings.composable.screen

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScreen
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton


@Composable
fun BackupAndRestoreScreen() {

	val context = LocalContext.current

	val scrollState = SettingsActivity.scrollState.current
	val onNavigate = SettingsActivity.onNavigate.current

	GenericSettingsScreen(
		title = "Backup & Restore",
		scrollState = scrollState
	) {
		SettingsButton(
			title = "Local Backup",
			icon = R.drawable.ic_snapshot,
			subTitle = "Backup your data to your local storage",
		) { onNavigate(SettingsActivity.Companion.Navigator.LOCAL_BACKUP) }

		SettingsButton(
			title = "Google Drive™",
			subTitle = "We are still working on this. Stay tuned!",
		) { Toast.makeText(context, "We are still working on this. Stay tuned!", Toast.LENGTH_LONG).show() }

		SettingsButton(
			title = "OneDrive™",
			subTitle = "We are still working on this. Stay tuned!",
		) { Toast.makeText(context, "We are still working on this. Stay tuned!", Toast.LENGTH_LONG).show() }

		SettingsButton(
			title = "WebDAV",
			subTitle = "We are still working on this. Stay tuned!",
		) { Toast.makeText(context, "We are still working on this. Stay tuned!", Toast.LENGTH_LONG).show() }
	}
}
