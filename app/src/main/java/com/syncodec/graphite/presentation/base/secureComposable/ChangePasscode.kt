package com.syncodec.graphite.presentation.base.secureComposable

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext


@Composable
fun ChangePasscode(
	onUpdatePasscode: (String) -> Unit,
	onClose: () -> Unit
) {
	val context = LocalContext.current

	var isOldPasscodeValidated by remember { mutableStateOf(false) }

	if (isOldPasscodeValidated) AddPasscodeScreen(
		onAddPasscode = {
			onUpdatePasscode(it)
			Toast.makeText(context, "Passcode removed", Toast.LENGTH_SHORT).show()
		},
		onClose = onClose
	) else {
		AuthenticatorScreen(
			onAuthenticate = { isOldPasscodeValidated = true },
			onClose = onClose
		)
	}
}
