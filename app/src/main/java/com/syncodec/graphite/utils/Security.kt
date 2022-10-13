package com.syncodec.graphite.utils

import androidx.compose.runtime.compositionLocalOf


val LocalAuthenticatorAction = compositionLocalOf<(Authenticator) -> Unit> { error("No authenticator found") }


enum class Authenticator {
	AUTHENTICATOR,
	ADD_PASSCODE,
	CHANGE_PASSCODE,
	REMOVE_PASSCODE,
	NONE
}
