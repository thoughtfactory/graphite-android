package com.syncodec.graphite.presentation.settings.composable.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingButton


@Preview
@Composable
fun BackupAndRestoreScreen(
	navigateTo : (SettingsActivity.Companion.SettingsScreen) -> Unit = {},
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
	) {
		SettingButton(
			text = "Local Backup",
			icon = R.drawable.ic_snapshot,
			subText = "Manage"
		) { navigateTo(SettingsActivity.Companion.SettingsScreen.LOCAL_BACKUP) }
		SettingButton(text = "Dropbox", icon = R.drawable.ic_logo_dropbox, subText = "Coming soon", tint = Color.Unspecified)
		SettingButton(text = "Google Drive", icon = R.drawable.ic_logo_google_drive, subText = "Coming soon", tint = Color.Unspecified)
		SettingButton(text = "One Drive", icon = R.drawable.ic_logo_onedrive, subText = "Coming soon",tint = Color.Unspecified)
	}
}
