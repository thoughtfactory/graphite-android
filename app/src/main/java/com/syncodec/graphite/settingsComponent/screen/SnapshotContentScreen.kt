package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.documentfile.provider.DocumentFile
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.ErrorView
import com.syncodec.graphite.settingsComponent.SettingsActivity
import com.syncodec.graphite.settingsComponent.miscellaneous.SettingButton


@Composable
fun SnapshotContentScreen(
	documentFile: DocumentFile?,
	onAction: (SettingsActivity.Action, Any?) -> Unit
) {
	if (documentFile == null || !documentFile.isDirectory) {
		ErrorView()
	} else {
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			documentFile.listFiles().forEach { documentFile1 ->
				item {
					SettingButton(
						title = documentFile1.name ?: "Untitled",
						leadingIcon = R.drawable.ic_attachment,
					) { onAction(SettingsActivity.Action.ACTION_VIEW, documentFile1) }
				}
			}
		}
	}
}
