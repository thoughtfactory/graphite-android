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
fun DataScreen(
	onClick: (SettingsActivity.Click, Any?) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item {
			SettingButton(
			title = "Import",
			icon = R.drawable.ic_import,
			iconTint = Color(0xFF2785BD)
		) { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.IMPORT) } }
		item {
			SettingButton(
				title = "Export",
				icon = R.drawable.ic_export,
				iconTint = Color(0xFF4B778D)
			) { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.EXPORT) } }
	}
}
