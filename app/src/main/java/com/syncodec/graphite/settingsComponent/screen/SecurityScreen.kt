package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.miscellaneous.DataStore
import com.syncodec.graphite.miscellaneous.logger
import com.syncodec.graphite.settingsComponent.SettingsActivity
import com.syncodec.graphite.settingsComponent.miscellaneous.SettingButton


@Composable
fun SecurityScreen(
	onClick: (SettingsActivity.Action, Any?) -> Unit
) {
	val context = LocalContext.current
	val dataStore = remember { DataStore(context = context) }
	val passcode by dataStore.getPasscode.collectAsState(initial = null)

	Crossfade(targetState = passcode != null) {
		if (it) {
			LazyColumn(
				modifier = Modifier.fillMaxSize()
			) {
				item {
					SettingButton(
						title = "Add Passcode",
						enabled = passcode == ""
					) { onClick(SettingsActivity.Action.ADD_PASSCODE, null) }
				}
				item {
					SettingButton(
						title = "Change Passcode",
						enabled = passcode != ""
					) { onClick(SettingsActivity.Action.CHANGE_PASSCODE, null) }
				}
				item {
					SettingButton(
						title = "Remove Passcode",
						enabled = passcode != ""
					) { onClick(SettingsActivity.Action.REMOVE_PASSCODE, null) }
				}
				item {
					SettingButton(
						title = "Biometric Unlock",
						subTitle = "Unlock vault with biometric",
						enabled = passcode != ""
					) { onClick(SettingsActivity.Action.BIOMETRIC_UNLOCK, null) }
				}
			}
		}
	}
}
