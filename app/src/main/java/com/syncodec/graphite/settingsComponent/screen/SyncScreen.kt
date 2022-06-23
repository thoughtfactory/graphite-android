package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syncodec.graphite.settingsComponent.SettingsActivity
import com.syncodec.graphite.settingsComponent.miscellaneous.SettingButton
import com.syncodec.graphite.R


@Composable
fun SyncScreen(
	onAction: (SettingsActivity.Action, Any?) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item {
			SettingButton(
				title = "Local Backup",
				leadingIcon = R.drawable.ic_local_backup,
			) {
				onAction(
					SettingsActivity.Action.NAVIGATION,
					SettingsActivity.Companion.Path.LOCAL_BACKUP
				)
			}
		}
		item {
			SettingButton(
				title = "Google Drive",
				subTitle = "Coming soon...",
				leadingIcon = R.drawable.ic_state,
			) { onAction(SettingsActivity.Action.GOOGLE_DRIVE, null) }
		}
		item {
			SettingButton(
				title = "One Drive",
				subTitle = "Coming soon...",
				leadingIcon = R.drawable.ic_state,
			) { onAction(SettingsActivity.Action.ONE_DRIVE, null) }
		}
		item {
			SettingButton(
				title = "Dropbox",
				subTitle = "Coming soon...",
				leadingIcon = R.drawable.ic_state,
			) { onAction(SettingsActivity.Action.DROPBOX, null) }
		}
		item {
			SettingButton(
				title = "WebDAV",
				subTitle = "Coming soon...",
				leadingIcon = R.drawable.ic_state,
			) { onAction(SettingsActivity.Action.WEBDAV, null) }
		}
	}
}
