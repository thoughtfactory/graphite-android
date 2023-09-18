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
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScaffold
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButtonDefaults
import com.syncodec.graphite.presentation.settings.composable.screen.dataScreen.dialog.ImportDataGraphiteDialog


@Preview
@Composable
fun ImportDataScreen() {

	var isImportDataGraphiteDialogVisible by remember { mutableStateOf(false) }

	GenericSettingsScaffold(
		title = stringResource(id = R.string.import_data)
	) {
		LazyColumn(
			modifier = Modifier.fillMaxSize()
		) {
			item {
				SettingsButton(
					title = "Graphite",
					subTitle = stringResource(id = R.string.import_data_from) + " Graphite",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_import),
					onClick = { isImportDataGraphiteDialogVisible = true },
				)
			}
			item {
				SettingsButton(
					title = "Journey",
					subTitle = stringResource(id = R.string.import_data_from) + " Journey",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_import),
					onClick = {
					},
				)
			}
			item {
				SettingsButton(
					title = "Google Keep",
					subTitle = stringResource(id = R.string.import_data_from) + " Google Keep",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_import),
					onClick = {
					},
				)
			}
		}
	}

	ImportDataGraphiteDialog(
		isDialogVisible = isImportDataGraphiteDialogVisible,
		onDismissRequest = { isImportDataGraphiteDialogVisible = false }
	)
}
