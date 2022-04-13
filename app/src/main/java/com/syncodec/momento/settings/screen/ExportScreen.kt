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
fun ExportScreen(
	onClick: (SettingsActivity.Click, Any?) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item {
			SettingButton(
				title = "Notebook",
				subTitle = "Export multiple notebook",
				icon = R.drawable.ic_notebook,
				iconTint = Color(0xFFFED049)
			) { onClick(SettingsActivity.Click.NAVIGATION, SettingsActivity.Companion.Path.SELECT_NOTEBOOK) }
		}
		item {
			SettingButton(
				title = "Bucket",
				subTitle = "Export single or multiple bucket",
				icon = R.drawable.ic_bucket,
				iconTint = Color(0xFFFF6464)
			) { onClick(SettingsActivity.Click.EXPORT_BUCKET, null) }
		}
	}
}
