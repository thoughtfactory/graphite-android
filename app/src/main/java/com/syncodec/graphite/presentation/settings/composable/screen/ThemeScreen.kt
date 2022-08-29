package com.syncodec.graphite.presentation.settings.composable.screen

import androidx.compose.runtime.Composable
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.custom.topBarColumn.TopBarColumn
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton


@Composable
fun ThemeScreen() {
	TopBarColumn(
		title = "Theme"
	) {
		SettingsButton(
			title = "Graphite",
			icon = R.drawable.ic_theme,
			isEnabled = true
		) {}

	}

}
