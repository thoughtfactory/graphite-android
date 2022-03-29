package com.syncodec.momento.settings.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syncodec.momento.settings.SettingsActivity
import com.syncodec.momento.settings.miscellaneous.SettingButton


@Composable
fun DataScreen(
	onClick: (SettingsActivity.Click, Any?) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item { SettingButton(title = "Import") { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.IMPORT) } }
		item { SettingButton(title = "Export") { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.EXPORT) } }
	}
}
