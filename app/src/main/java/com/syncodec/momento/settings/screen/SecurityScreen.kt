package com.syncodec.momento.settings.screen

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.syncodec.momento.miscellaneous.DataStore
import com.syncodec.momento.settings.SettingsActivity
import com.syncodec.momento.settings.miscellaneous.SettingButton


@Composable
fun SecurityScreen(
	onClick: (SettingsActivity.Click) -> Unit
) {
	val context = LocalContext.current
	val dataStore = DataStore(context = context)
	val passcode by dataStore.getPasscode.collectAsState(initial = null)

	Log.i("npr71", "passcode : $passcode")

	Crossfade(targetState = passcode != null) {
		if (it) {
			LazyColumn(
				modifier = Modifier
					.fillMaxSize()
			) {
				item { SettingButton(title = "Add Passcode", enabled = passcode == "") { onClick(SettingsActivity.Click.ADD_PASSCODE) } }
				item { SettingButton(title = "Change Passcode", enabled = passcode != "") { onClick(SettingsActivity.Click.CHANGE_PASSCODE) } }
				item { SettingButton(title = "Remove Passcode", enabled = passcode != "") { onClick(SettingsActivity.Click.REMOVE_PASSCODE) } }
				item { SettingButton(title = "Biometric Unlock", subTitle = "Unlock vault with biometric", enabled = passcode != "") { onClick(SettingsActivity.Click.BIOMETRIC_UNLOCK) } }
			}
		}
	}
}
