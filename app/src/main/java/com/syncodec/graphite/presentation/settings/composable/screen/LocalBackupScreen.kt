package com.syncodec.graphite.presentation.settings.composable.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScreen
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.ui.DeleteContainer
import com.syncodec.graphite.presentation.ui.DeleteContent


@Preview
@Composable
fun LocalBackupScreen() {

	val context = LocalContext.current

	val scrollState = SettingsActivity.LocalScrollState.current
	val onNavigate = SettingsActivity.LocalOnNavigate.current

	val setupLocalBackupFolder = SettingsActivity.LocalSetupLocalBackupFolder.current
	val removeLocalBackupFolder = SettingsActivity.LocalRemoveLocalBackupFolder.current

	GenericSettingsScreen(
		title = "Local Backup",
		scrollState = scrollState
	) {
		ExperimentalWarning()
		Spacer(modifier = Modifier.height(4.dp))

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

@Composable
private fun ExperimentalWarning() {
	Box(
		modifier = Modifier
			.padding(12.dp, 0.dp)
			.background(Color.DeleteContainer.copy(alpha = 0.71f), RoundedCornerShape(12.dp))
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth()
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_warning),
					contentDescription = "Warning",
					tint = Color.DeleteContent,
					modifier = Modifier.requiredSize(32.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = "Warning",
					color = Color.DeleteContent,
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Bold
				)
			}
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = "This is an experimental feature. We are still testing and removing potential bugs.",
				color = Color.DeleteContent,
				style = MaterialTheme.typography.bodyMedium
			)
		}
	}
}
