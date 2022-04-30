package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syncodec.graphite.settingsComponent.SettingsActivity
import com.syncodec.graphite.settingsComponent.miscellaneous.SettingButton


@Composable
fun FontFamilyScreen(
	onClick: (SettingsActivity.Action, Int) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item { SettingButton(title = "Overlock") { onClick(SettingsActivity.Action.CHANGE_FONT_FAMILY, 0) } }
		item { SettingButton(title = "Source Sans Pro") { onClick(SettingsActivity.Action.CHANGE_FONT_FAMILY, 1) } }
		item { SettingButton(title = "Ubuntu") { onClick(SettingsActivity.Action.CHANGE_FONT_FAMILY, 2) } }
		item { SettingButton(title = "ATWriter") { onClick(SettingsActivity.Action.CHANGE_FONT_FAMILY, 3) } }
	}
}
