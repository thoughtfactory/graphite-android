package com.syncodec.graphite.presentation.settings.composable.screen

import androidx.compose.runtime.Composable
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScreen
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton


@Composable
fun DataScreen() {

	val scrollState = SettingsActivity.LocalScrollState.current
	val onNavigate = SettingsActivity.LocalOnNavigate.current

	val exportData = SettingsActivity.LocalExportData.current

	GenericSettingsScreen(
		title = "Data",
		scrollState = scrollState
	) {
		SettingsButton(
			title = "Import",
			icon = R.drawable.ic_import,
			subTitle = "Import data from various sources",
		) { onNavigate(SettingsActivity.Companion.Navigator.IMPORT) }

		SettingsButton(
			title = "Export",
			icon = R.drawable.ic_export,
			subTitle = "Export data",
			onClick = exportData
		)
	}
}
