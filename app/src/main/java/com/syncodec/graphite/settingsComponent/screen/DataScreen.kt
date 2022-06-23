package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syncodec.graphite.settingsComponent.SettingsActivity
import com.syncodec.graphite.settingsComponent.miscellaneous.SettingButton
import com.syncodec.graphite.R


@Composable
fun DataScreen(
	onAction: (SettingsActivity.Action, Any?) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item {
			SettingButton(
				title = "Import",
				leadingIcon = R.drawable.ic_import,
			) {
				onAction(
					SettingsActivity.Action.NAVIGATION,
					SettingsActivity.Companion.Path.IMPORT
				)
			}
		}
		item {
			SettingButton(
				title = "Export",
				leadingIcon = R.drawable.ic_export,
			) {
				onAction(
					SettingsActivity.Action.NAVIGATION,
					SettingsActivity.Companion.Path.EXPORT
				)
			}
		}
	}
}
