package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
				subTitle = "Export notebook",
				leadingIcon = R.drawable.ic_notebook,
			) { onClick(SettingsActivity.Action.NAVIGATION, SettingsActivity.Companion.Path.SELECT_NOTEBOOK) }
		}
		item {
			SettingButton(
				title = "Bucket",
				subTitle = "Export bucket",
				leadingIcon = R.drawable.ic_bucket,
			) { onClick(SettingsActivity.Action.EXPORT_BUCKET, null) }
		}
	}
}
