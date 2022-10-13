package com.syncodec.graphite.presentation.ui.authentication

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import com.syncodec.graphite.utils.Authenticator


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AuthenticationContent(
	authenticator: Authenticator,
	onAuthentication: () -> Unit,
	onAddPasscode: () -> Unit,
	onChangePasscode: () -> Unit,
	onRemovePasscode: () -> Unit,
	content: @Composable () -> Unit
) {
	AnimatedContent(targetState = authenticator) { target ->
		when (target) {
			Authenticator.AUTHENTICATOR -> content()
			Authenticator.ADD_PASSCODE -> AddPasscodeScreen{}
			Authenticator.CHANGE_PASSCODE -> content()
			Authenticator.REMOVE_PASSCODE -> content()
			Authenticator.NONE -> content()
		}
	}
}
