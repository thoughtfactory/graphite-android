package com.syncodec.graphite.presentation.base.secureComposable

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.utils.alice.AliceRequestResult
import com.syncodec.graphite.utils.alice.getSecretData
import com.syncodec.graphite.utils.alice.putSecretData
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject


@Preview
@Composable
fun SecureComposable(
	content: @Composable () -> Unit = {}
) {
	val context = LocalContext.current

	val repository = koinInject<LockableRepo>()

	val authenticationState by BaseApplication.authenticationState.collectAsState()
	val isUnlocked by repository.isUnlocked.collectAsState()

	BackHandler(enabled = authenticationState != AuthenticationState.None) { BaseApplication.authenticationState.tryEmit(AuthenticationState.None) }

	CompositionLocalProvider(
		LocalIsRepoUnlocked provides isUnlocked,
		LocalAuthenticatorAction provides { newAuthenticatorState ->
			val alice = context.getSecretData("passcode")

			when {
				newAuthenticatorState == AuthenticationState.Authenticate && isUnlocked -> {
					repository.lockRepo()
					AuthenticationState.None
				}

				newAuthenticatorState == AuthenticationState.Authenticate && !isUnlocked && alice.result == AliceRequestResult.SUCCESS -> AuthenticationState.Authenticate
				newAuthenticatorState == AuthenticationState.Authenticate && !isUnlocked && alice.result != AliceRequestResult.SUCCESS -> AuthenticationState.AddChangePasscode
				newAuthenticatorState == AuthenticationState.AddChangePasscode -> AuthenticationState.AddChangePasscode
				newAuthenticatorState == AuthenticationState.RemovePasscode && alice.result == AliceRequestResult.KEY_NOT_FOUND -> {
					Toast.makeText(context, "No passcode set", Toast.LENGTH_SHORT).show()
					AuthenticationState.None
				}

				newAuthenticatorState == AuthenticationState.RemovePasscode && alice.result != AliceRequestResult.KEY_NOT_FOUND -> AuthenticationState.RemovePasscode
				newAuthenticatorState == AuthenticationState.None -> AuthenticationState.None
				else -> AuthenticationState.None
			}.let {
				BaseApplication.authenticationState.tryEmit(it)
			}
		}
	) {
		content()
		AnimatedVisibility(
			visible = authenticationState == AuthenticationState.AddChangePasscode,
			enter = slideInVertically(tween(470)) { it / 2 } + fadeIn(tween(470)),
			exit = slideOutVertically(tween(470)) { it / 2 } + fadeOut(tween(470))
		) {

			val alice = context.getSecretData("passcode")

			if (alice.result == AliceRequestResult.SUCCESS) {
				ChangePasscode(
					onUpdatePasscode = { BaseApplication.authenticationState.tryEmit(AuthenticationState.None); repository.unlockRepo(); context.putSecretData("passcode", it.toByteArray()) },
					onClose = { BaseApplication.authenticationState.tryEmit(AuthenticationState.None) }
				)
			} else {
				AddPasscodeScreen(
					onAddPasscode = { BaseApplication.authenticationState.tryEmit(AuthenticationState.None); repository.unlockRepo(); context.putSecretData("passcode", it.toByteArray()) },
					onClose = { BaseApplication.authenticationState.tryEmit(AuthenticationState.None) }
				)
			}
		}

		AnimatedVisibility(
			visible = authenticationState == AuthenticationState.RemovePasscode,
			enter = slideInVertically(tween(470)) { it / 2 } + fadeIn(tween(470)),
			exit = slideOutVertically(tween(470)) { it / 2 } + fadeOut(tween(470))
		) {
			RemovePasscode(
				onClose = { repository.lockRepo(); BaseApplication.authenticationState.tryEmit(AuthenticationState.None) }
			)
		}

		AnimatedVisibility(
			visible = authenticationState == AuthenticationState.Authenticate,
			enter = slideInVertically(tween(470)) { it / 2 } + fadeIn(tween(470)),
			exit = slideOutVertically(tween(470)) { it / 2 } + fadeOut(tween(470))
		) {
			AuthenticatorScreen(
				onAuthenticate = { BaseApplication.authenticationState.tryEmit(AuthenticationState.None); repository.unlockRepo() },
				onClose = { BaseApplication.authenticationState.tryEmit(AuthenticationState.None) }
			)
		}
	}
}

@Serializable
data class LastFailedAttempt(
	val nextAttemptAt: Long,
	val attemptCount: Int,
)
