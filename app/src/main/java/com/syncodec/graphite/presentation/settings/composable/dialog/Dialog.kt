package com.syncodec.graphite.presentation.settings.composable.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.presentation.settings.composable.dialog.clearData.ClearDataDialog
import com.syncodec.graphite.presentation.settings.composable.dialog.exportData.ExportDataDialog
import com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen.dialog.ImportDataDialog
import com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen.dialog.journey.ImportDataJourneyDialog


enum class SettingsDialogType {
	ImportData,
	ImportDataJourney,
	ExportData,
	ClearData,
	NotificationPermission,
	DeleteAccount
}

@Preview
@Composable
fun SettingsDialog(
	showImportDataDialog : Boolean = false,
	showImportDataJourneyDialog : Boolean = false,
	showExportDataDialog : Boolean = false,
	showClearDataDialog : Boolean = false,
	closeDialog : (SettingsDialogType) -> Unit = { }
) {
	ExportDataDialog(showDialog = showExportDataDialog) { closeDialog(SettingsDialogType.ExportData) }
	ImportDataDialog(showDialog = showImportDataDialog) { closeDialog(SettingsDialogType.ImportData) }
	ImportDataJourneyDialog(showDialog = showImportDataJourneyDialog) { closeDialog(SettingsDialogType.ImportDataJourney) }
	ClearDataDialog(showDialog = showClearDataDialog) { closeDialog(SettingsDialogType.ClearData) }
}
