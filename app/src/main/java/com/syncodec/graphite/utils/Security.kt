package com.syncodec.graphite.utils

import androidx.compose.runtime.compositionLocalOf
import com.syncodec.graphite.presentation.ui.authentication.AuthenticatorScreen
import com.syncodec.graphite.presentation.ui.authentication.AddPasscodeScreen
import com.syncodec.graphite.presentation.ui.BaseContent


val LocalAuthenticatorAction = compositionLocalOf<(AuthenticatorScreen) -> Unit> { {} }
val LocalIsAuthenticated = compositionLocalOf { false }


/**
 * Show screen according to the [AuthenticatorScreen].
 *
 * [Authenticate] Shows [AuthenticatorScreen] to authenticate user using passcode.
 *
 * [AddPasscode] Shows [AddPasscodeScreen] to add a new passcode.
 *
 * [ChangePasscode] Shows [ChangePasscode] to change the passcode.
 *
 * [RemovePasscode] Shows [RemovePasscode] to remove the passcode.
 *
 * [None] Shows actual content passed to [BaseContent]
 */
enum class AuthenticatorScreen {
	Authenticate,
	AddPasscode,
	ChangePasscode,
	RemovePasscode,
	None
}
