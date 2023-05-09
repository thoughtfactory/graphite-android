package com.syncodec.graphite.presentation.settings.composable.screen

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingButton
import com.syncodec.graphite.presentation.sync.dropbox.DropboxSyncActivity
import com.syncodec.graphite.presentation.sync.googleDrive.GoogleDriveSyncActivity


@Preview
@Composable
fun BackupAndSyncScreen(
	navigateTo : (SettingsActivity.Companion.SettingsScreen) -> Unit = {},
) {
	val context = LocalContext.current

	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
	) {
		SettingButton(
			text = "Local Backup",
			icon = R.drawable.ic_snapshot,
			subText = "Manage"
		) { navigateTo(SettingsActivity.Companion.SettingsScreen.LocalBackup) }
		SettingButton(text = "Dropbox", icon = R.drawable.ic_logo_dropbox, subText = "Manage", tint = Color.Unspecified){
			context.startActivity(Intent(context, DropboxSyncActivity::class.java))
		}
		SettingButton(text = "Google Drive", icon = R.drawable.ic_logo_google_drive, subText = "Manage", tint = Color.Unspecified) {
			context.startActivity(Intent(context, GoogleDriveSyncActivity::class.java))
		}
		SettingButton(text = "One Drive", icon = R.drawable.ic_logo_onedrive, subText = "Coming soon",tint = Color.Unspecified)
	}
}
