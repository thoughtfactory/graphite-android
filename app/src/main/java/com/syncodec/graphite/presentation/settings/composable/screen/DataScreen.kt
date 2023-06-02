package com.syncodec.graphite.presentation.settings.composable.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScaffold
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButtonDefaults
import com.syncodec.graphite.presentation.settings.composable.dialog.ClearDataDialog


@Preview
@Composable
fun DataScreen(
	onNavigate: (SettingsActivity.Companion.SettingsScreen) -> Unit = {}
) {
	var showClearDataDialog by remember { mutableStateOf(false) }

	GenericSettingsScaffold(
		title = "Data",
		onClickBack = { /*TODO*/ }
	) {
		LazyColumn(
			modifier = Modifier.fillMaxSize()
		) {
			item {
				SettingsButton(
					title = "Import data",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_flat_import),
					onClick = { onNavigate(SettingsActivity.Companion.SettingsScreen.ImportData) },
				)
			}
			item {
				SettingsButton(
					title = "Export data",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_flat_export),
					onClick = { },
				)
			}
			item {
				SettingsButton(
					title = "Clear data",
					subTitle = "Clear all local data from this device",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_flat_file_delete, color = SettingsButtonDefaults.warningSettingsButtonColors().contentColor),
					colors = SettingsButtonDefaults.warningSettingsButtonColors(),
					onClick = { showClearDataDialog = true },
				)
			}
		}
	}

	ClearDataDialog(
		showDialog = showClearDataDialog,
		onConfirmDelete = {
			showClearDataDialog = false
		},
		onDismiss = { showClearDataDialog = false },
	)
}
