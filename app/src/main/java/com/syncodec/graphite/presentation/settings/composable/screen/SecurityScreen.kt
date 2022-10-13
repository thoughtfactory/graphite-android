package com.syncodec.graphite.presentation.settings.composable.screen

import android.widget.Toast
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScreen
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsSwitch
import com.syncodec.graphite.utils.Authenticator
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.LocalAuthenticatorAction


@Composable
fun SecurityScreen(scrollState: ScrollState) {

	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val useBiometric = dataStoreInstance.getUseBiometric().collectAsState(initial = null).value

	val authenticatorAction = LocalAuthenticatorAction.current

	GenericSettingsScreen(
		title = "Security",
		scrollState = scrollState
	) {
		SettingsButton(
			title = "Add Passcode",
			icon = R.drawable.ic_passcode
		) {
			authenticatorAction(Authenticator.ADD_PASSCODE)
		}

		SettingsButton(
			title = "Change Passcode",
			icon = R.drawable.ic_change_passcode
		) {}

		SettingsButton(
			title = "Remove Passcode",
			icon = R.drawable.ic_remove_passcode
		) {}

		SettingsSwitch(
			title = "Biometric Authentication",
			subTitle = "Unlock vault with your fingerprint",
			icon = R.drawable.ic_biometric,
			isChecked = useBiometric == true,
		) { Toast.makeText(context, "We are still working on this. Stay tuned!", Toast.LENGTH_LONG).show() }
	}
}
