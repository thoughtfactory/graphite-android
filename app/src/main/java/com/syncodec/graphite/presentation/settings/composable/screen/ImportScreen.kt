package com.syncodec.graphite.presentation.settings.composable.screen

import androidx.compose.runtime.Composable
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScreen
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton


@Composable
fun ImportScreen() {
	val scrollState = SettingsActivity.LocalScrollState.current

	val importData = SettingsActivity.LocalImportData.current

	GenericSettingsScreen(
		title = "Import",
		scrollState = scrollState
	) {
		SettingsButton(
			title = "Graphite",
			icon = R.drawable.ic_import,
			subTitle = "Import data from Graphite",
		) { importData(SettingsActivity.Companion.ImportType.GRAPHITE) }

		SettingsButton(
			title = "Google Keep",
			icon = R.drawable.ic_import,
			subTitle = "Import data from Google Keep",
		) { importData(SettingsActivity.Companion.ImportType.GOOGLE_KEEP) }

		SettingsButton(
			title = "Journey",
			icon = R.drawable.ic_import,
			subTitle = "Import data from Journey",
		) { importData(SettingsActivity.Companion.ImportType.JOURNEY) }

		SettingsButton(
			title = "Notesnook",
			icon = R.drawable.ic_import,
			subTitle = "Import data from Notesnook",
		) { importData(SettingsActivity.Companion.ImportType.NOTESNOOK) }
	}
}
