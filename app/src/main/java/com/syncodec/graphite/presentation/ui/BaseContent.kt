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
import com.syncodec.graphite.utils.Authenticator
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalVaultIsOpened
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

	var authenticator by remember { mutableStateOf(Authenticator.NONE) }

	fun onClose() {
		authenticator = Authenticator.NONE
	}

	var noTry by remember { mutableStateOf(0) }

	var isVaultOpened by remember { mutableStateOf(false) }

	val isPro by BaseApplication.isPro.collectAsState()

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
				LocalVaultIsOpened provides isVaultOpened,
				LocalAuthenticatorAction provides {
					if (isVaultOpened) {
						isVaultOpened = false
						noTry = 0
						Toast.makeText(context, "Vault closed", Toast.LENGTH_SHORT).show()
					} else {
						val alice = context.getSecretData("passcode")

						authenticator = when (it) {
							Authenticator.AUTHENTICATE -> if (alice.result == AliceRequestResult.SUCCESS) Authenticator.AUTHENTICATE else Authenticator.ADD_PASSCODE
							Authenticator.ADD_PASSCODE -> it
							Authenticator.CHANGE_PASSCODE -> if (alice.result == AliceRequestResult.SUCCESS) Authenticator.CHANGE_PASSCODE else Authenticator.ADD_PASSCODE
							Authenticator.REMOVE_PASSCODE -> if (alice.result == AliceRequestResult.SUCCESS) Authenticator.CHANGE_PASSCODE else Authenticator.ADD_PASSCODE
							Authenticator.NONE -> it
						}
					}
				},
				content = {
					AnimatedContent(targetState = authenticator) { target ->
						when (target) {
							Authenticator.AUTHENTICATE -> AuthenticatorScreen(
								noTry = noTry,
								onAuthenticate = {
									val passcode = context.getSecretData("passcode").data?.decodeToString()

									if (passcode == it) {
										authenticator = Authenticator.NONE
										isVaultOpened = true
										noTry = 0
										Toast.makeText(context, "Vault opened", Toast.LENGTH_SHORT).show()
									} else {
										Toast.makeText(context, "Wrong passcode", Toast.LENGTH_SHORT).show()
										noTry ++
									}

									if (noTry >= 3) {
										noTry = 0
										authenticator = Authenticator.NONE
									}
								},
								onClose = ::onClose
							)

							Authenticator.ADD_PASSCODE -> AddPasscodeScreen(
								onPasscodeAdded = {
									context.putSecretData("passcode", it.toByteArray())
									authenticator = Authenticator.NONE
									Toast.makeText(context, "Passcode added", Toast.LENGTH_SHORT).show()
								},
								onClose = ::onClose
							)

							Authenticator.CHANGE_PASSCODE -> ChangePasscode(
								onPasscodeAdded = {
									context.putSecretData("passcode", it.toByteArray())
									authenticator = Authenticator.NONE
									Toast.makeText(context, "Passcode updated", Toast.LENGTH_SHORT).show()
								},
								onClose = ::onClose
							)

							Authenticator.REMOVE_PASSCODE -> content()
							Authenticator.NONE -> content()
						}
					}
				}
			)
		}
	}
}
