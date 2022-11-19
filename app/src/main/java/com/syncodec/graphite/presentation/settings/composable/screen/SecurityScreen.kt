package com.syncodec.graphite.presentation.settings.composable.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScreen
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsSwitch
import com.syncodec.graphite.utils.Authenticator
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.LocalAuthenticatorAction


@Composable
fun SecurityScreen() {

	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val useBiometric = dataStoreInstance.getUseBiometric().collectAsState(initial = null).value

	val authenticatorAction = LocalAuthenticatorAction.current

	val scrollState = SettingsActivity.scrollState.current
	val onNavigate = SettingsActivity.onNavigate.current

	GenericSettingsScreen(
		title = "Security",
		scrollState = scrollState
	) {
		SettingsButton(
			title = "Add Passcode",
			icon = R.drawable.ic_passcode
		) { authenticatorAction(Authenticator.ADD_PASSCODE) }

		SettingsButton(
			title = "Change Passcode",
			icon = R.drawable.ic_change_passcode
		) { authenticatorAction(Authenticator.CHANGE_PASSCODE) }

//		SettingsButton(
//			title = "Remove Passcode",
//			icon = R.drawable.ic_remove_passcode
//		) {}

		SettingsSwitch(
			title = "Biometric Authentication",
			subTitle = "Add an extra layer of authentication while opening the app",
			icon = R.drawable.ic_biometric,
			isChecked = useBiometric == true,
		) { dataStoreInstance.putUseBiometric(useBiometric != true) }
	}
}
