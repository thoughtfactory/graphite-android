package com.syncodec.graphite.presentation.settings.composable.screen

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.dropbox.DropboxActivity
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScreen
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton


@Composable
fun SynchronizationScreen() {

	val context = LocalContext.current

	val scrollState = SettingsActivity.LocalScrollState.current

	GenericSettingsScreen(
		title = "Synchronization",
		scrollState = scrollState
	) {
		SettingsButton(
			title = "Dropbox",
			icon = R.drawable.ic_dropbox,
			subTitle = "Sync your data with Dropbox",
		) {
			Intent(context, DropboxActivity::class.java).apply {
				context.startActivity(this)
			}
		}
	}
}
