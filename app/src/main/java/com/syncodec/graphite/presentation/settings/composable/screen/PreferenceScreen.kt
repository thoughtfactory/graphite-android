package com.syncodec.graphite.presentation.settings.composable.screen

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.custom.topBarColumn.TopBarColumn
import com.syncodec.graphite.presentation.settings.composable.SettingsNavigator
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton


@Composable
fun PreferenceScreen(navController: NavHostController) {
	TopBarColumn(
		title = "Preferences"
	) {
		SettingsButton(
			title = "Theme",
			icon = R.drawable.ic_theme,
			isEnabled = true
		) { navController.navigate(SettingsNavigator.THEME.name) }
	}
}
