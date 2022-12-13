package com.syncodec.graphite.presentation.settings.composable.screen

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScreen
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsSwitch
import com.syncodec.graphite.utils.DataStoreInstance


@Composable
fun PreferencesScreen() {

	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val isFollowSystemDarkTheme by dataStoreInstance.getFollowSystemDarkTheme.collectAsState(initial = null)
	val isForceDarkTheme by dataStoreInstance.getForceDarkTheme.collectAsState(initial = null)
	val isTintFavourite by dataStoreInstance.getTintFavorite.collectAsState(initial = null)

	val scrollState = SettingsActivity.LocalScrollState.current

	val onNavigate = SettingsActivity.LocalOnNavigate.current

	GenericSettingsScreen(
		title = "Preferences",
		scrollState = scrollState
	) {
		SettingsButton(
			title = "Theme",
			subTitle = "Graphite",
			icon = R.drawable.ic_theme
		) { onNavigate(SettingsActivity.Companion.Navigator.THEME) }

		SettingsButton(
			title = "Font Style",
			subTitle = "Ubuntu",
			icon = R.drawable.ic_font_family
		) { Toast.makeText(context, "We are still working on this. Stay tuned!", Toast.LENGTH_LONG).show() }

		SettingsSwitch(
			title = "Tint Favourite Notes",
			subTitle = "Show a tint on favourite notes",
			icon = R.drawable.ic_color_picker,
			isExperimental = true,
			isChecked = isTintFavourite != false,
		) { dataStoreInstance.putTintFavorite(isTintFavourite == false) }

		SettingsSwitch(
			title = "Auto Dark Mode",
			subTitle = "Follow system settings",
			icon = R.drawable.ic_system_theme,
			isExperimental = true,
			isChecked = isFollowSystemDarkTheme != false
		) { dataStoreInstance.putFollowSystemDarkTheme(isFollowSystemDarkTheme == false) }

		SettingsSwitch(
			title = "Enable dark mode",
			subTitle = "Enable dark mode regardless of system settings",
			icon = R.drawable.ic_bulb,
			isExperimental = true,
			isChecked = isForceDarkTheme == true
		) { dataStoreInstance.putForceDarkTheme(isForceDarkTheme != true) }

		SettingsButton(
			title = "Language",
			subTitle = "English",
			icon = R.drawable.ic_language
		) { Toast.makeText(context, "We are still working on this. Stay tuned!", Toast.LENGTH_LONG).show() }
	}
}
