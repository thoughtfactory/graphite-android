package com.syncodec.momento.settings.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syncodec.momento.settings.SettingsActivity
import com.syncodec.momento.settings.miscellaneous.SettingButton


@Composable
fun BaseScreen(
	onClick: (SettingsActivity.Click) -> Unit
) {
	LazyColumn(
		modifier = Modifier
			.fillMaxSize()
	) {
		item { SettingButton(title = "Login") { onClick(SettingsActivity.Click.LOGIN) } }
		item { SettingButton(title = "Preference") { onClick(SettingsActivity.Click.PREFERENCE) } }
		item { SettingButton(title = "Security") { onClick(SettingsActivity.Click.SECURITY) } }
		item { SettingButton(title = "Privacy policy") { onClick(SettingsActivity.Click.PRIVACY_POLICY) } }
		item { SettingButton(title = "Terms of service") { onClick(SettingsActivity.Click.TERMS_OF_SERVICE) } }
		item { SettingButton(title = "About us") { onClick(SettingsActivity.Click.ABOUT_US) } }
	}
}
