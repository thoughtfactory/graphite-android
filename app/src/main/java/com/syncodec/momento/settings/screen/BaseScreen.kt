package com.syncodec.momento.settings.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syncodec.momento.settings.SettingsActivity
import com.syncodec.momento.settings.miscellaneous.SettingButton


@Composable
fun BaseScreen(
	onClick: (SettingsActivity.Click, SettingsActivity.Companion.Path) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item { SettingButton(title = "Login") { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.LOGIN) } }
		item { SettingButton(title = "Preference") { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.PREFERENCE) } }
		item { SettingButton(title = "Security") { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.SECURITY) } }
		item { SettingButton(title = "Data") { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.DATA) } }
		item { SettingButton(title = "Privacy policy") { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.PRIVACY_POLICY) } }
		item { SettingButton(title = "Terms of service") { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.TERMS) } }
		item { SettingButton(title = "About us") { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.ABOUT_US) } }
	}
}
