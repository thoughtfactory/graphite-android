package com.syncodec.graphite.presentation.settings.composable.screen

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingButton
import com.syncodec.graphite.presentation.sync.dropbox.DropboxSyncActivity
import com.syncodec.graphite.presentation.sync.googleDrive.GoogleDriveSyncActivity
import com.syncodec.graphite.utils.dataStore.SyncDataStoreInstance


@Preview
@Composable
fun BackupAndSyncScreen(
	navigateTo: (SettingsActivity.Companion.SettingsScreen) -> Unit = {},
) {
	val context = LocalContext.current
	val syncDataStoreInstance = remember { SyncDataStoreInstance(context) }

	val syncProvider by syncDataStoreInstance.syncProvider.collectAsState(initial = null)

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
		SettingButton(
			text = "Dropbox",
			icon = R.drawable.ic_logo_dropbox,
			subIcon = if (syncProvider == SyncDataStoreInstance.Companion.SyncProvider.Dropbox) R.drawable.ic_checkmark else null,
			subText = "Manage",
			tint = Color.Unspecified,
			subIconTint =  Color.Unspecified,
		) {
			context.startActivity(Intent(context, DropboxSyncActivity::class.java))
		}
		SettingButton(
			text = "Google Drive",
			icon = R.drawable.ic_logo_google_drive,
			subIcon = if (syncProvider == SyncDataStoreInstance.Companion.SyncProvider.GoogleDrive) R.drawable.ic_checkmark else null,
			subText = "Manage",
			tint = Color.Unspecified,
			subIconTint =  Color.Unspecified,
		) {
			context.startActivity(Intent(context, GoogleDriveSyncActivity::class.java))
		}
		SettingButton(
			text = "One Drive",
			icon = R.drawable.ic_logo_onedrive,
			subIcon = if (syncProvider == SyncDataStoreInstance.Companion.SyncProvider.OneDrive) R.drawable.ic_checkmark else null,
			subText = "Coming soon",
			tint = Color.Unspecified,
			subIconTint =  Color.Unspecified,
		)
	}
}
