package com.syncodec.graphite.presentation.settings.composable

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.custom.topBarColumn.TopBarColumn
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton


@Composable
fun BaseScreen(navController: NavHostController) {
	TopBarColumn(
		title = "Settings"
	) {
		SettingsButton(
			title = "Subscription",
			icon = R.drawable.ic_subscription,
			isEnabled = true
		) {}
		SettingsButton(
			title = "Preference",
			icon = R.drawable.ic_preference,
			isEnabled = true
		) { navController.navigate(SettingsNavigator.PREFERENCE.name) }
		SettingsButton(title = "Security", icon = R.drawable.ic_lock_close, isEnabled = true) {}
		SettingsButton(title = "Data", icon = R.drawable.ic_data, isEnabled = true) {}
		SettingsButton(
			title = "Backup and Sync",
			icon = R.drawable.ic_sync,
			isEnabled = true
		) {navController.navigate(SettingsNavigator.BACKUP_AND_SYNC.name)}
		SettingsButton(title = "About Us", icon = R.drawable.ic_about_us, isEnabled = true) {}
	}
}
