package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.syncodec.graphite.settingsComponent.SettingsActivity
import com.syncodec.graphite.settingsComponent.miscellaneous.SettingButton
import com.syncodec.graphite.R


@Composable
fun BaseScreen(
	onAction: (SettingsActivity.Action, Any?) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item {
			SettingButton(
				title = "Login",
				icon = R.drawable.ic_login,
				tint = Color(0xFF5463FF)
			) { onAction(SettingsActivity.Action.LOGIN, null) }
		}
		item {
			SettingButton(
				title = "Subscription",
				icon = R.drawable.ic_subscription,
				tint = Color(0xFF66BFBF)
			) { onAction(SettingsActivity.Action.SUBSCRIPTION, null) }
		}
		item {
			SettingButton(
				title = "Preference",
				icon = R.drawable.ic_preference,
				tint = Color(0xFFFE7E6D)
			) {
				onAction(
					SettingsActivity.Action.NAVIGATION,
					SettingsActivity.Companion.Path.PREFERENCE
				)
			}
		}
		item {
			SettingButton(
				title = "Security",
				icon = R.drawable.ic_security,
				tint = Color(0xFF28527A)
			) {
				onAction(
					SettingsActivity.Action.NAVIGATION,
					SettingsActivity.Companion.Path.SECURITY
				)
			}
		}
		item {
			SettingButton(
				title = "Data",
				icon = R.drawable.ic_data,
				tint = Color(0xFF5EAAA8)
			) { onAction(SettingsActivity.Action.NAVIGATION, SettingsActivity.Companion.Path.DATA) }
		}
		item {
			SettingButton(
				title = "Privacy policy",
				icon = R.drawable.ic_privacy,
				tint = Color(0xFF886F6F)
			) { onAction(SettingsActivity.Action.POLICY, null)
			}
		}
		item {
			SettingButton(
				title = "Terms of service",
				icon = R.drawable.ic_terms,
				tint = Color(0xFF316B83)
			) { onAction(SettingsActivity.Action.TERMS, null) }
		}
		item {
			SettingButton(
				title = "About us",
				icon = R.drawable.ic_about_us,
				tint = Color(0xFF39A2DB)
			) {
				onAction(
					SettingsActivity.Action.NAVIGATION,
					SettingsActivity.Companion.Path.ABOUT_US
				)
			}
		}
	}
}
