package com.syncodec.graphite.presentation.ui

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.presentation.ui.authentication.AddPasscodeScreen
import com.syncodec.graphite.presentation.ui.authentication.AuthenticatorScreen
import com.syncodec.graphite.presentation.ui.authentication.ChangePasscode
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated
import com.syncodec.graphite.utils.alice.AliceRequestResult
import com.syncodec.graphite.utils.alice.getSecretData
import com.syncodec.graphite.utils.alice.putSecretData


val LocalIsPro = compositionLocalOf { false }

@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("NewApi")
@Composable
fun BaseContent(
	isDarkTheme : Boolean = isSystemInDarkTheme(),
	isDynamicColor : Boolean = false,
	content : @Composable () -> Unit
) {
	val context = LocalContext.current

	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val isFollowSystemDarkTheme by dataStoreInstance.getFollowSystemDarkTheme.collectAsState(initial = null)
	val isForceDarkTheme by dataStoreInstance.getForceDarkTheme.collectAsState(initial = null)

	val dynamicColor = isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
	val appColorScheme = if (isFollowSystemDarkTheme == true) {
		when {
			dynamicColor && isDarkTheme -> dynamicDarkColorScheme(LocalContext.current)
			dynamicColor && ! isDarkTheme -> dynamicLightColorScheme(LocalContext.current)
			else -> if (isDarkTheme) darkColorScheme0 else lightColorScheme0
		}
	} else {
		if (isForceDarkTheme == true) darkColorScheme0 else lightColorScheme0
	}

	val appTypography = UbuntuTypography

	val isAuthenticated by BaseApplication.isAuthenticated.collectAsState(initial = false)
	val authenticatorState by BaseApplication.authenticatorScreen.collectAsState()

	fun onClose() = BaseApplication.authenticatorScreen.tryEmit(AuthenticatorScreen.None)

	var noTry by remember { mutableStateOf(0) }

	val isPro by BaseApplication.isPro.collectAsState()

	BackHandler(enabled = authenticatorState != AuthenticatorScreen.None) {
		BaseApplication.authenticatorScreen.tryEmit(AuthenticatorScreen.None)
	}

	if (isFollowSystemDarkTheme != null && isForceDarkTheme != null) {
		androidx.compose.material3.MaterialTheme(
			colorScheme = appColorScheme,
			typography = appTypography
		) {
			// TODO (M3): MaterialTheme doesn't provide LocalIndication, remove when it does
			val rippleIndication = rememberRipple()

			CompositionLocalProvider(
				LocalIndication provides rippleIndication,
				LocalIsPro provides isPro,
				LocalIsAuthenticated provides isAuthenticated,
				LocalAuthenticatorAction provides { newAuthenticatorState ->
					if (isAuthenticated) {
						BaseApplication.isAuthenticated.tryEmit(false)
						noTry = 0
						Toast.makeText(context, "Vault closed", Toast.LENGTH_SHORT).show()
					} else {
						val alice = context.getSecretData("passcode")

						when (newAuthenticatorState) {
							AuthenticatorScreen.Authenticate -> if (alice.result == AliceRequestResult.SUCCESS) AuthenticatorScreen.Authenticate else AuthenticatorScreen.AddPasscode
							AuthenticatorScreen.AddPasscode -> newAuthenticatorState
							AuthenticatorScreen.ChangePasscode -> if (alice.result == AliceRequestResult.SUCCESS) AuthenticatorScreen.ChangePasscode else AuthenticatorScreen.AddPasscode
							AuthenticatorScreen.RemovePasscode -> if (alice.result == AliceRequestResult.SUCCESS) AuthenticatorScreen.ChangePasscode else AuthenticatorScreen.AddPasscode
							AuthenticatorScreen.None -> newAuthenticatorState
						}.let { BaseApplication.authenticatorScreen.tryEmit(it) }
					}
				},
				content = {
					AnimatedContent(targetState = authenticatorState) { target ->
						when (target) {
							AuthenticatorScreen.Authenticate -> AuthenticatorScreen(
								noTry = noTry,
								onAuthenticate = {
									val passcode = context.getSecretData("passcode").data?.decodeToString()

									if (passcode == it) {
										BaseApplication.isAuthenticated.tryEmit(true)
										BaseApplication.authenticatorScreen.tryEmit(AuthenticatorScreen.None)
										noTry = 0
										Toast.makeText(context, "Vault opened", Toast.LENGTH_SHORT).show()
									} else {
										Toast.makeText(context, "Wrong passcode", Toast.LENGTH_SHORT).show()
										noTry ++
									}

									if (noTry >= 3) {
										noTry = 0
										BaseApplication.authenticatorScreen.tryEmit(AuthenticatorScreen.None)
									}
								},
								onClose = ::onClose
							)

							AuthenticatorScreen.AddPasscode -> AddPasscodeScreen(
								onPasscodeAdded = {
									context.putSecretData("passcode", it.toByteArray())
									BaseApplication.authenticatorScreen.tryEmit(AuthenticatorScreen.None)
									Toast.makeText(context, "Passcode added", Toast.LENGTH_SHORT).show()
								},
								onClose = ::onClose
							)

							AuthenticatorScreen.ChangePasscode -> ChangePasscode(
								onPasscodeAdded = {
									context.putSecretData("passcode", it.toByteArray())
									BaseApplication.authenticatorScreen.tryEmit(AuthenticatorScreen.None)
									Toast.makeText(context, "Passcode updated", Toast.LENGTH_SHORT).show()
								},
								onClose = ::onClose
							)

							AuthenticatorScreen.RemovePasscode -> content()
							AuthenticatorScreen.None -> content()
						}
					}
				}
			)
		}
	}
}
