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
fun ImportScreen(
	onAction: (SettingsActivity.Action, Any?) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item {
			SettingButton(
				title = "Graphite",
				subTitle = "Import your entries from Graphite",
				icon = R.drawable.ic_state,
				tint = Color(0xFF87A7B3)
			) { onAction(SettingsActivity.Action.IMPORT_GRAPHITE, null) }
		}
		item {
			SettingButton(
				title = "Journey",
				subTitle = "Import your entries from Journey",
				icon = R.drawable.ic_state,
				tint = Color(0xFFCC9B6D)
			) { onAction(SettingsActivity.Action.IMPORT_JOURNEY, null) }
		}
//		item {
//			SettingButton(
//				title = "Day One",
//				subTitle = "Import your entries from DayOne™"
//			) { onAction(SettingsActivity.Action.IMPORT_JOURNEY, null) }
//		}
	}
}
