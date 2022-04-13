package com.syncodec.momento.settings.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.syncodec.momento.settings.SettingsActivity
import com.syncodec.momento.settings.miscellaneous.SettingButton
import com.syncodec.momento.R


@Composable
fun BaseScreen(
	onClick: (SettingsActivity.Click, SettingsActivity.Companion.Path) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item { SettingButton(title = "Login") { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.LOGIN) } }
		item { SettingButton(title = "Preference") { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.PREFERENCE) } }
		item { SettingButton(
			title = "Security",
			icon = R.drawable.ic_security,
			iconTint = Color(0xFF28527A)
		) { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.SECURITY) } }
		item { SettingButton(
			title = "Data",
			icon = R.drawable.ic_data,
			iconTint = Color(0xFF5EAAA8)
		) { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.DATA) } }
		item { SettingButton(title = "Privacy policy") { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.PRIVACY_POLICY) } }
		item { SettingButton(title = "Terms of service") { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.TERMS) } }
		item { SettingButton(title = "About us") { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.ABOUT_US) } }
	}
}
