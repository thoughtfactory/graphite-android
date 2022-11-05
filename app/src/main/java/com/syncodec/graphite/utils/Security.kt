package com.syncodec.graphite.utils

import androidx.compose.runtime.compositionLocalOf


val LocalAuthenticatorAction = compositionLocalOf<(Authenticator) -> Unit> { error("No authenticator found") }
val LocalVaultIsOpened = compositionLocalOf<Boolean> { error("No vault found") }

enum class Authenticator {
	AUTHENTICATE,
	ADD_PASSCODE,
	CHANGE_PASSCODE,
	REMOVE_PASSCODE,
	NONE
}
