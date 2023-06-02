package com.syncodec.graphite.presentation.settings.composable.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScaffold
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButtonDefaults


@Preview
@Composable
fun BackUpAndSyncScreen(
	onClickBack: () -> Unit = {},
) {
	GenericSettingsScaffold(
		title = "Back up & sync",
		onClickBack = onClickBack,
	) {
		LazyColumn(
			modifier = Modifier.fillMaxSize()
		) {
			item {
				SettingsButton(
					title = " Local backup",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_flat_local_storage),
					onClick = { },
				)
			}
			item {
				SettingsButton(
					title = "Dropbox",
					subTitle = "Manage",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_logo_dropbox, color = Color.Unspecified, size = 28.dp),
					onClick = { },
				)
			}
			item {
				SettingsButton(
					title = "Google Drive",
					subTitle = "Manage",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_logo_google_drive, color = Color.Unspecified, size = 28.dp),
					onClick = { },
				)
			}
			item {
				SettingsButton(
					title = "OneDrive",
					subTitle = "Coming soon",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_logo_onedrive, color = Color.Unspecified, size = 28.dp),
					onClick = { },
				)
			}
		}
	}
}
