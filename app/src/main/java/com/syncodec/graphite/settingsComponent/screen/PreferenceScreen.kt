package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.miscellaneous.logger
import com.syncodec.graphite.settingsComponent.SettingsActivity
import com.syncodec.graphite.settingsComponent.miscellaneous.SettingButton
import com.syncodec.graphite.R


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
