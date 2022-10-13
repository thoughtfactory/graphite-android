package com.syncodec.graphite.presentation.ui

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.presentation.ui.authentication.AuthenticationContent
import com.syncodec.graphite.utils.Authenticator


@SuppressLint("NewApi")
@Composable
fun BaseContent(
	isDarkTheme: Boolean = isSystemInDarkTheme(),
	isDynamicColor: Boolean = false,
	authenticator: Authenticator = Authenticator.NONE,
	content: @Composable () -> Unit
) {
	val dynamicColor = isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
	val appColorScheme = when {
		dynamicColor && isDarkTheme -> dynamicDarkColorScheme(LocalContext.current)
		dynamicColor && !isDarkTheme -> dynamicLightColorScheme(LocalContext.current)
		else -> if (isDarkTheme) darkColorScheme0 else lightColorScheme0
	}

	val appTypography = UbuntuTypography


	androidx.compose.material3.MaterialTheme(
		colorScheme = appColorScheme,
		typography = appTypography
	) {
		// TODO (M3): MaterialTheme doesn't provide LocalIndication, remove when it does
		val rippleIndication = rememberRipple()

		CompositionLocalProvider(
			LocalIndication provides rippleIndication,
			content = {
				AuthenticationContent(
					authenticator = authenticator,
					onAuthentication = {},
					onAddPasscode = {},
					onChangePasscode = {},
					onRemovePasscode = {},
					content = content
				)
			}
		)
	}
}
