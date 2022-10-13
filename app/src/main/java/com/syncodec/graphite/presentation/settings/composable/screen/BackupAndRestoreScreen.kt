package com.syncodec.graphite.presentation.settings.composable.screen

import android.widget.Toast
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScreen
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton


@Composable
fun BackupAndRestoreScreen(scrollState: ScrollState) {
	val context = LocalContext.current
	GenericSettingsScreen(
		title = "Backup & Restore",
		scrollState = scrollState
	) {
		SettingsButton(
			title = "Local Backup",
		) {}

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
