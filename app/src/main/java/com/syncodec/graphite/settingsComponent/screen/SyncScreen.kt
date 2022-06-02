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
fun SyncScreen(
	onAction: (SettingsActivity.Action, Any?) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item {
			SettingButton(
				title = "Google Drive",
				subTitle = "Coming soon...",
				icon = R.drawable.ic_state,
				tint = Color(0xFF87A7B3)
			) { onAction(SettingsActivity.Action.GOOGLE_DRIVE, null) }
		}
		item {
			SettingButton(
				title = "One Drive",
				subTitle = "Coming soon...",
				icon = R.drawable.ic_state,
				tint = Color(0xFF125D98)
			) { onAction(SettingsActivity.Action.ONE_DRIVE, null) }
		}
		item {
			SettingButton(
				title = "Dropbox",
				subTitle = "Coming soon...",
				icon = R.drawable.ic_state,
				tint = Color(0xFF564A4A)
			) { onAction(SettingsActivity.Action.DROPBOX, null) }
		}
		item {
			SettingButton(
				title = "WebDAV",
				subTitle = "Coming soon...",
				icon = R.drawable.ic_state,
				tint = Color(0xFF00917C)
			) { onAction(SettingsActivity.Action.WEBDAV, null) }
		}
	}
}
