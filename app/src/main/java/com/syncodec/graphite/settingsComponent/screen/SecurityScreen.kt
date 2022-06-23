package com.syncodec.graphite.settingsComponent.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.miscellaneous.DataStoreInstance
import com.syncodec.graphite.settingsComponent.SettingsActivity
import com.syncodec.graphite.settingsComponent.miscellaneous.SettingButton
import com.syncodec.graphite.R


@Composable
fun SecurityScreen(
	onClick: (SettingsActivity.Action, Any?) -> Unit
) {
	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }
	val passcode by dataStoreInstance.getPasscode.collectAsState(initial = null)

	Crossfade(targetState = passcode != null) {
		if (it) {
			LazyColumn(
				modifier = Modifier.fillMaxSize()
			) {
				item {
					SettingButton(
						title = "Add Passcode",
						enabled = passcode == "",
						leadingIcon = R.drawable.ic_passcode
					) { onClick(SettingsActivity.Action.ADD_PASSCODE, null) }
				}
				item {
					SettingButton(
						title = "Change Passcode",
						enabled = passcode != "",
						leadingIcon = R.drawable.ic_change_passcode
					) { onClick(SettingsActivity.Action.CHANGE_PASSCODE, null) }
				}
				item {
					SettingButton(
						title = "Remove Passcode",
						enabled = passcode != "",
						leadingIcon = R.drawable.ic_remove_passcode
					) { onClick(SettingsActivity.Action.REMOVE_PASSCODE, null) }
				}
				item {
					SettingButton(
						title = "Biometric Unlock",
						subTitle = "Unlock vault with biometric",
						enabled = true,
						leadingIcon = R.drawable.ic_biometric
					) { onClick(SettingsActivity.Action.BIOMETRIC_UNLOCK, SettingsActivity.Action.BIOMETRIC_UNLOCK) }
				}
			}
		}
	}
}
