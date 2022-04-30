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
fun DataScreen(
	onClick: (SettingsActivity.Action, Any?) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item {
			SettingButton(
			title = "Import",
			icon = R.drawable.ic_import,
			tint = Color(0xFF2785BD)
		) { onClick(SettingsActivity.Action.NAVIGATION, SettingsActivity.Companion.Path.IMPORT) } }
		item {
			SettingButton(
				title = "Export",
				icon = R.drawable.ic_export,
				tint = Color(0xFF4B778D)
			) { onClick(SettingsActivity.Action.NAVIGATION, SettingsActivity.Companion.Path.EXPORT) } }
	}
}
