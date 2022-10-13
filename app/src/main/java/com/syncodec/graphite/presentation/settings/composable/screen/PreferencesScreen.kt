package com.syncodec.graphite.presentation.settings.composable.screen

import android.widget.Toast
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScreen
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsSwitch
import com.syncodec.graphite.utils.DataStoreInstance


@Composable
fun PreferencesScreen(scrollState: ScrollState) {

	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val isFollowSystemDarkTheme by dataStoreInstance.getFollowSystemDarkTheme.collectAsState(initial = null)
	val isForceDarkTheme by dataStoreInstance.getForceDarkTheme.collectAsState(initial = null)
	val isTintFavourite by dataStoreInstance.getTintFavorite.collectAsState(initial = null)
	val isGeolocationEnabled by dataStoreInstance.getGeolocation.collectAsState(initial = null)
	val isYearProgressEnabled by dataStoreInstance.getYearProgress.collectAsState(initial = null)

	GenericSettingsScreen(
		title = "Preferences",
		scrollState = scrollState
	) {
		SettingsButton(
			title = "Theme",
			subTitle = "Graphite",
			icon = R.drawable.ic_theme
		) { Toast.makeText(context, "We are still working on this. Stay tuned!", Toast.LENGTH_LONG).show() }

		SettingsButton(
			title = "Font Style",
			subTitle = "Ubuntu",
			icon = R.drawable.ic_font_family
		) { Toast.makeText(context, "We are still working on this. Stay tuned!", Toast.LENGTH_LONG).show() }

		SettingsSwitch(
			title = "Tint Favourite Notes",
			subTitle = "Show a tint on favourite notes",
			icon = R.drawable.ic_color_picker,
			isChecked = isTintFavourite != false
		) { dataStoreInstance.putTintFavorite(isTintFavourite == false) }

		SettingsSwitch(
			title = "Auto Dark Mode",
			subTitle = "Follow system settings",
			icon = R.drawable.ic_system_theme,
			isChecked = isFollowSystemDarkTheme != false
		) { dataStoreInstance.putFollowSystemDarkTheme(isFollowSystemDarkTheme == false) }

		SettingsSwitch(
			title = "Enable dark mode",
			subTitle = "Enable dark mode regardless of system settings",
			icon = R.drawable.ic_bulb,
			isChecked = isForceDarkTheme == true
		) { dataStoreInstance.putForceDarkTheme(isForceDarkTheme != true) }

		SettingsSwitch(
			title = "Year Progress Bar",
			subTitle = "Show year progress bar on home screen",
			icon = R.drawable.ic_advance,
			isChecked = isYearProgressEnabled == true
		) { dataStoreInstance.putYearProgress(isYearProgressEnabled != true) }

		SettingsSwitch(
			title = "Geo Location",
			subTitle = "Keep your notes connected to location",
			icon = R.drawable.ic_map_marker,
			isChecked = isGeolocationEnabled == true
		) { dataStoreInstance.putGeolocation(isGeolocationEnabled != true) }

		SettingsButton(
			title = "Language",
			subTitle = "English",
			icon = R.drawable.ic_language
		) { Toast.makeText(context, "We are still working on this. Stay tuned!", Toast.LENGTH_LONG).show() }
	}
}
