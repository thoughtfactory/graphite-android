package com.syncodec.momento.settings.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.syncodec.momento.settings.SettingsActivity
import com.syncodec.momento.settings.miscellaneous.SettingButton


@Composable
fun ImportScreen(
	onClick: (SettingsActivity.Click, Any?) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item {
			SettingButton(
				title = "Journey",
				subTitle = "Import your entries from Journey™"
			) { onClick(SettingsActivity.Click.JOURNEY, null) }
		}
	}
}
