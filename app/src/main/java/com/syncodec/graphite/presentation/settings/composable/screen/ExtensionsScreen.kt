package com.syncodec.graphite.presentation.settings.composable.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScreen
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsSwitch
import com.syncodec.graphite.utils.DataStoreInstance


@Composable
fun ExtensionsScreen() {

	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val isGeolocationEnabled by dataStoreInstance.getGeolocation.collectAsState(initial = null)
	val isYearProgressEnabled by dataStoreInstance.getYearProgress.collectAsState(initial = null)

	val scrollState = SettingsActivity.scrollState.current

	GenericSettingsScreen(
		title = "Extensions",
		scrollState = scrollState
	) {
		SettingsSwitch(
			title = "Year Progress Bar",
			subTitle = "Show year progress bar on home screen",
			icon = R.drawable.ic_advance,
			isChecked = isYearProgressEnabled == true
		) { dataStoreInstance.putYearProgress(isYearProgressEnabled != true) }

		SettingsSwitch(
			title = "Auto Geo Tagging",
			subTitle = "Automatically detect and add your location when writing a note",
			icon = R.drawable.ic_map_marker,
			isChecked = isGeolocationEnabled == true
		) { dataStoreInstance.putGeolocation(isGeolocationEnabled != true) }
	}
}
