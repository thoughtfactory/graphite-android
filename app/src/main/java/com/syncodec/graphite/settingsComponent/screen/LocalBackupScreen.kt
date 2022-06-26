package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.info.InfoCard
import com.syncodec.graphite.settingsComponent.SettingsActivity
import com.syncodec.graphite.settingsComponent.miscellaneous.SettingButton


@Composable
fun LocalBackupScreen(
	backFolderPath: String?,
	onAction: (SettingsActivity.Action, Any?) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item {
			InfoCard(
				title = "WARNING",
				icon = R.drawable.ic_warning,
				color = Color(0xAAF87474),
				text = "This is an experimental feature. We are still testing and removing potential bugs"
			)
		}
		item {
			InfoCard(
				title = "DISCLAIMER",
				icon = R.drawable.ic_info,
				color = Color(0xAAFFB562),
				text = "Data saved in Local Backup folder is not encrypted and is also visible to other applications if given required permissions. This data will not be deleted when the app is uninstalled."
			)
		}
		item {
			SettingButton(
				title = "Setup Backup Folder",
				subTitle = backFolderPath,
				leadingIcon = R.drawable.ic_folder,
			) { onAction(SettingsActivity.Action.SETUP_LOCAL_BACKUP_FOLDER, null) }
		}
		item {
			SettingButton(
				title = "Remove Backup Folder",
				leadingIcon = R.drawable.ic_folder_remove,
				enabled = backFolderPath != null
			) { onAction(SettingsActivity.Action.REMOVE_LOCAL_BACKUP_FOLDER, null) }
		}
		item {
			SettingButton(
				title = "Snapshot Warehouse",
				leadingIcon = R.drawable.ic_warehouse,
				enabled = backFolderPath != null
			) {
				onAction(
					SettingsActivity.Action.NAVIGATION,
					SettingsActivity.Companion.Path.SNAPSHOT_WAREHOUSE
				)
				onAction(SettingsActivity.Action.REFRESH_SNAPSHOT, null)
			}
		}
	}
}
