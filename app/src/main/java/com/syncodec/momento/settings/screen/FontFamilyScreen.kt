package com.syncodec.momento.settings.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.syncodec.momento.miscellaneous.DataStore
import com.syncodec.momento.settings.SettingsActivity
import com.syncodec.momento.settings.miscellaneous.SettingButton


@Composable
fun FontFamilyScreen(
	onClick: (SettingsActivity.Click, Int) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item { SettingButton(title = "Overlock") { onClick(SettingsActivity.Click.CHANGE_FONT_FAMILY, 0) } }
		item { SettingButton(title = "Source Sans Pro") { onClick(SettingsActivity.Click.CHANGE_FONT_FAMILY, 1) } }
		item { SettingButton(title = "Ubuntu") { onClick(SettingsActivity.Click.CHANGE_FONT_FAMILY, 2) } }
		item { SettingButton(title = "ATWriter") { onClick(SettingsActivity.Click.CHANGE_FONT_FAMILY, 3) } }
	}
}
