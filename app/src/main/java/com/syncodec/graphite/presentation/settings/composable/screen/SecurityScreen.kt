package com.syncodec.graphite.presentation.settings.composable.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.LocalAppDataStore
import com.syncodec.graphite.presentation.base.secureComposable.AuthenticationState
import com.syncodec.graphite.presentation.base.secureComposable.LocalAuthenticatorAction
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScaffold
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButtonDefaults
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsSwitch
import com.syncodec.graphite.presentation.settings.composable.dialog.BiometricEnableDialog


@Preview
@Composable
fun SecurityScreen(
	onClickBack: () -> Unit = {},
) {
	val appDataStore = LocalAppDataStore.current

	val authenticatorAction = LocalAuthenticatorAction.current

	val isBiometricEnabled by appDataStore.isBiometricEnabled.collectAsState(initial = null)

	var isBiometricEnableDialogVisible by remember { mutableStateOf(false) }

	GenericSettingsScaffold(
		title = stringResource(id = R.string.security),
		onClickBack = onClickBack,
	) {
		LazyColumn(
			modifier = Modifier.fillMaxSize()
		) {
			item {
				SettingsButton(
					title = stringResource(id = R.string.add_change_passcode),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_key),
					onClick = { authenticatorAction(AuthenticationState.AddChangePasscode) },
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.remove_passcode),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_lock_open),
					onClick = { authenticatorAction(AuthenticationState.RemovePasscode) },
				)
			}
			item {
				SettingsSwitch(
					title = stringResource(id = R.string.biometric_authentication),
					subTitle = stringResource(id = R.string.biometric_authentication_sub),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_fingerprint),
					checked = isBiometricEnabled == true,
					onCheckChanged = {
						if (isBiometricEnabled == true) appDataStore.putUseBiometric(false)
						else isBiometricEnableDialogVisible = true
					},
				)
			}
		}
	}

	BiometricEnableDialog(
		isDialogVisible = isBiometricEnableDialogVisible,
		onDismissRequest = { isBiometricEnableDialogVisible = false },
		onClickEnable = {
			appDataStore.putUseBiometric(true)
			isBiometricEnableDialogVisible = false
		}
	)
}
