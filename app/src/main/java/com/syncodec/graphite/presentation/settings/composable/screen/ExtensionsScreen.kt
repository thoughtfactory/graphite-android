package com.syncodec.graphite.presentation.settings.composable.screen

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.R
import com.syncodec.graphite.notification.WriteNoteNotification
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScreen
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsSwitch
import com.syncodec.graphite.utils.DataStoreInstance


@Composable
fun ExtensionsScreen() {

	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val isGeolocationEnabled by dataStoreInstance.getGeolocation.collectAsState(initial = null)
	val isYearProgressEnabled by dataStoreInstance.getYearProgress.collectAsState(initial = null)
	val isNoteNotificationEnabled by dataStoreInstance.getNoteFromNotification.collectAsState(initial = null)

	val scrollState = SettingsActivity.LocalScrollState.current

	val isPro by BaseApplication.isPro.collectAsState()

	GenericSettingsScreen(
		title = "Extensions",
		scrollState = scrollState
	) {
		SettingsSwitch(
			title = "Year Progress Bar",
			subTitle = "Show year progress bar on home screen",
			icon = R.drawable.ic_advance,
			isChecked = isYearProgressEnabled == true
		) { dataStoreInstance.putYearProgress(isYearProgressEnabled != true) }

		SettingsSwitch(
			title = "Auto Geo Tagging",
			subTitle = "Automatically detect and add your location when writing a note",
			icon = R.drawable.ic_map_marker,
			isProFeature = true,
			isChecked = if (isPro) isGeolocationEnabled == true else false
		) {
			if (isPro) {
				dataStoreInstance.putGeolocation(isGeolocationEnabled != true)
			} else {
				Toast.makeText(context, "Join Graphite Pro to enable auto geo tagging your notes", Toast.LENGTH_SHORT).show()
			}
		}

		SettingsSwitch(
			title = "Note from notification",
			subTitle = "Directly add a note from notification",
			icon = R.drawable.ic_note_notification,
			isProFeature = false,
			isChecked = isNoteNotificationEnabled == true
		) {
			isNoteNotificationEnabled?.not()?.let {
				dataStoreInstance.putNoteFromNotification(it)
				if (it) WriteNoteNotification.showSimpleNotification(context = context)
			} ?: dataStoreInstance.putNoteFromNotification(false)
		}
	}
}
