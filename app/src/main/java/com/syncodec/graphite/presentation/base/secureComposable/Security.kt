package com.syncodec.graphite.presentation.base.secureComposable

import androidx.compose.runtime.compositionLocalOf
import com.syncodec.graphite.presentation.base.BaseComposable


val LocalAuthenticatorAction = compositionLocalOf<(AuthenticationState) -> Unit> { {} }
val LocalIsRepoUnlocked = compositionLocalOf { false }


/**
 * Show screen according to the [AuthenticationState].
 *
 * [Authenticate] Shows [AuthenticationState] to authenticate user using passcode.
 *
 * [AddChangePasscode] Shows [AddChangePasscode] to change the passcode.
 *
 * [RemovePasscode] Shows [RemovePasscode] to remove the passcode.
 *
 * [None] Shows actual content passed to [BaseComposable]
 */
enum class AuthenticationState {
	Authenticate,
	AddChangePasscode,
	RemovePasscode,
	None
}
