package com.syncodec.graphite.presentation.settings.composable.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.SettingsActivity.Companion.LocalDropboxSignIn
import com.syncodec.graphite.presentation.settings.SettingsActivity.Companion.LocalTestDropboxConnection
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScreen
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton


@Composable
fun DropboxSyncScreen() {

	val context = LocalContext.current

	val scrollState = SettingsActivity.LocalScrollState.current

	val dropboxSignIn = LocalDropboxSignIn.current
	val testDropboxConnection = LocalTestDropboxConnection.current

	GenericSettingsScreen(
		title = "Dropbox",
		scrollState = scrollState
	) {
		SettingsButton(
			title = "Connect with Dropbox",
			icon = R.drawable.ic_dropbox,
			subTitle = "Connect with Dropbox to sync your data",
		) { dropboxSignIn() }

		SettingsButton(
			title = "Test Connection",
			icon = R.drawable.ic_test_connection,
			subTitle = "Test your connection with Google Drive",
		) { testDropboxConnection() }
	}
}
