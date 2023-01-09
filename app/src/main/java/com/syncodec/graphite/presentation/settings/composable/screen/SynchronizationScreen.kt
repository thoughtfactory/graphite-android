package com.syncodec.graphite.presentation.settings.composable.screen

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
			icon = R.drawable.ic_logo_dropbox,
			subTitle = "Sync your data with Dropbox",
			iconColor = Color.Unspecified,
		) {
//			Intent(context, DropboxActivity::class.java).apply {
//				context.startActivity(this)
//			}
			Toast.makeText(context, "We are still working on this. Stay tuned!", Toast.LENGTH_LONG).show()
		}
	}
}
