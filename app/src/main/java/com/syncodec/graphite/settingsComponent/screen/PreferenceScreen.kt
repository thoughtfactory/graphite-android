package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
				subTitle = "Graphite",
				leadingIcon = R.drawable.ic_theme,
			) {
				onAction(
					SettingsActivity.Action.COMING_SOON,
					SettingsActivity.Companion.Path.THEME
				)
			}
		}
		item {
			SettingButton(
				title = "Font Family",
				subTitle = "Ubuntu",
				leadingIcon = R.drawable.ic_font_family,
			) {
				onAction(
					SettingsActivity.Action.COMING_SOON,
					SettingsActivity.Companion.Path.FONT_FAMILY
				)
			}
		}
	}
}
