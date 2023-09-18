package com.syncodec.graphite.presentation.settings.composable.screen.dataScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScaffold
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButtonDefaults
import com.syncodec.graphite.presentation.settings.composable.dialog.ClearDataDialog
import com.syncodec.graphite.presentation.settings.composable.screen.dataScreen.dialog.ExportDataDialog


@Preview
@Composable
fun DataScreen(
	onNavigate: (SettingsActivity.Companion.SettingsScreen) -> Unit = {}
) {

	var isExportDataDialogVisible by remember { mutableStateOf(false) }
	var isClearDataDialogVisible by remember { mutableStateOf(false) }

	GenericSettingsScaffold(
		title = stringResource(id = R.string.data),
	) {
		LazyColumn(
			modifier = Modifier.fillMaxSize()
		) {
			item {
				SettingsButton(
					title = stringResource(id = R.string.import_data),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_import),
					onClick = { onNavigate(SettingsActivity.Companion.SettingsScreen.ImportData) },
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.export_data),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_export),
					onClick = { isExportDataDialogVisible = true },
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.clear_data),
					subTitle = stringResource(id = R.string.clear_data_sub),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_flat_file_delete, color = SettingsButtonDefaults.warningSettingsButtonColors().contentColor),
					colors = SettingsButtonDefaults.warningSettingsButtonColors(),
					onClick = { isClearDataDialogVisible = true },
				)
			}
		}
	}

	ExportDataDialog(
		isDialogVisible = isExportDataDialogVisible,
		onDismissRequest = { isExportDataDialogVisible = false }
	)

	ClearDataDialog(
		showDialog = isClearDataDialogVisible,
		onConfirmDelete = {
			isClearDataDialogVisible = false
		},
		onDismissRequest = { isClearDataDialogVisible = false },
	)
}
