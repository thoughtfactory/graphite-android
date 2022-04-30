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
fun ExportScreen(
	onClick: (SettingsActivity.Action, Any?) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item {
			SettingButton(
				title = "Notebook",
				subTitle = "Export multiple notebook",
				icon = R.drawable.ic_notebook,
				tint = Color(0xFFFED049)
			) { onClick(SettingsActivity.Action.NAVIGATION, SettingsActivity.Companion.Path.SELECT_NOTEBOOK) }
		}
		item {
			SettingButton(
				title = "Bucket",
				subTitle = "Export single or multiple bucket",
				icon = R.drawable.ic_bucket,
				tint = Color(0xFFFF6464)
			) { onClick(SettingsActivity.Action.EXPORT_BUCKET, null) }
		}
	}
}
