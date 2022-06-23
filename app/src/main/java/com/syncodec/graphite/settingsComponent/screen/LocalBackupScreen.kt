package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
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
		item { InfoCard() }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoCard() {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp),
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(Color(0xAAF87474))
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
		) {
			Row(
				modifier = Modifier,
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_warning),
					contentDescription = "Warning",
					tint = Color.White,
					modifier = Modifier.requiredSize(32.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = "WARNING",
					style = MaterialTheme.typography.titleMedium,
					color = Color.White
				)
			}
			Spacer(modifier = Modifier.width(4.dp))
			Row(modifier = Modifier) {
				Spacer(modifier = Modifier.width(40.dp))
				Text(
					text = "This is an experimental feature. We are still testing and removing potential bugs",
					style = MaterialTheme.typography.titleSmall,
					color = Color.White
				)
			}
		}
	}
}
