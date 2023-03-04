package com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsContentTitle
import com.syncodec.graphite.presentation.settings.composable.dialog.SettingsDialogType
import com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen.dialog.googleKeep.ImportDataGoogleKeepDialog
import com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen.dialog.graphite.ImportDataGraphiteDialog
import com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen.dialog.journey.ImportDataJourneyDialog


@Preview
@Composable
fun ImportDataScreen(
	openDialog : (SettingsDialogType) -> Unit = {},
) {
	var showImportDataGraphiteDialog by remember { mutableStateOf(false) }
	var showImportDataJourneyDialog by remember { mutableStateOf(false) }
	var showImportDataGoogleKeepDialog by remember { mutableStateOf(false) }

	Box(
		modifier = Modifier.fillMaxSize()
	) {
		Column(
			modifier = Modifier.fillMaxSize()
		) {
			SettingsContentTitle(title = "Import from")
			SettingButton(text = "Graphite", icon = R.drawable.ic_state) { showImportDataGraphiteDialog = true }
			SettingButton(text = "Journey", icon = R.drawable.ic_state) { showImportDataJourneyDialog = true }
			SettingButton(text = "Google Keep", icon = R.drawable.ic_state) { showImportDataGoogleKeepDialog = true }

			SettingButton(text = "Learn how to import data", icon = R.drawable.ic_import) {
			}
		}

		ImportDataGraphiteDialog(showDialog = showImportDataGraphiteDialog) { showImportDataGraphiteDialog = false }
		ImportDataJourneyDialog(showDialog = showImportDataJourneyDialog) { showImportDataJourneyDialog = false }
		ImportDataGoogleKeepDialog(showDialog = showImportDataGoogleKeepDialog) { showImportDataGoogleKeepDialog = false }
	}
}
