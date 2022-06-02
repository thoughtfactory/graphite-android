package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.syncodec.graphite.R
import com.syncodec.graphite.settingsComponent.SettingsActivity
import com.syncodec.graphite.settingsComponent.miscellaneous.SettingButton


@Composable
fun PreferenceScreen(
	onAction: (SettingsActivity.Action, SettingsActivity.Companion.Path) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item {
			SettingButton(
				title = "Theme",
				subTitle = "Material you",
				icon = R.drawable.ic_theme,
				tint = MaterialTheme.colorScheme.primary
			) {
				onAction(
					SettingsActivity.Action.NAVIGATION,
					SettingsActivity.Companion.Path.THEME
				)
			}
		}
		item {
			SettingButton(
				title = "Font Family",
				subTitle = "Roboto",
				icon = R.drawable.ic_font_family,
				tint = Color(0xFFBFA2DB)
			) {
				onAction(
					SettingsActivity.Action.NAVIGATION,
					SettingsActivity.Companion.Path.FONT_FAMILY
				)
			}
		}
	}
}
