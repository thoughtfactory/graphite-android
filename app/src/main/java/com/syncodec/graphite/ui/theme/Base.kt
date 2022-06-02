package com.syncodec.graphite.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.custom.LoadingView
import com.syncodec.graphite.miscellaneous.DataStore


private val DarkColorScheme = darkColorScheme(
	primary = Blue80,
	onPrimary = Blue20,
	primaryContainer = Blue30,
	onPrimaryContainer = Blue90,
	inversePrimary = Blue40,
	secondary = DarkBlue80,
	onSecondary = DarkBlue20,
	secondaryContainer = DarkBlue30,
	onSecondaryContainer = DarkBlue90,
	tertiary = Yellow80,
	onTertiary = Yellow20,
	tertiaryContainer = Yellow30,
	onTertiaryContainer = Yellow90,
	error = Red80,
	onError = Red20,
	errorContainer = Red30,
	onErrorContainer = Red90,
	background = Grey10,
	onBackground = Grey90,
	surface = Grey10,
	onSurface = Grey80,
	inverseSurface = Grey90,
	inverseOnSurface = Grey20,
	surfaceVariant = BlueGrey30,
	onSurfaceVariant = BlueGrey80,
	outline = BlueGrey60
)

private val LightColorScheme = lightColorScheme(
	primary = Blue40,
	onPrimary = Color.White,
	primaryContainer = Blue90,
	onPrimaryContainer = Blue10,
	inversePrimary = Blue80,
	secondary = DarkBlue40,
	onSecondary = Color.White,
	secondaryContainer = DarkBlue90,
	onSecondaryContainer = DarkBlue10,
	tertiary = Yellow40,
	onTertiary = Color.White,
	tertiaryContainer = Yellow90,
	onTertiaryContainer = Yellow10,
	error = Red40,
	onError = Color.White,
	errorContainer = Red90,
	onErrorContainer = Red10,
	background = Grey99,
	onBackground = Grey10,
	surface = Grey99,
	onSurface = Grey10,
	inverseSurface = Grey20,
	inverseOnSurface = Grey95,
	surfaceVariant = BlueGrey90,
	onSurfaceVariant = BlueGrey30,
	outline = BlueGrey50
)

val lightColorScheme0 = lightColorScheme(
	primary = Color(0xFF1E2022),
	onPrimary = Color(0xFFC0CEDD),
	primaryContainer = Color(0xFF0F1316),
	onPrimaryContainer = Color(0xFF9BAEC3),
	secondary = Color(0xFF242D34),
	onSecondary = Color(0xFFA2A9AE),
	secondaryContainer = Color(0xFF1C3648),
	onSecondaryContainer = Color(0xFFCED8DF),
	surface = Color(0xFFC9D6DF),
	onSurface = Color(0xFF1E2A34),
	background = Color(0xFFECF1F4),
	onBackground = Color(0xFF02060A)
)

val darkColorScheme0 = darkColorScheme(
	primary = Color(0xFFC0CEDD),
	onPrimary = Color(0xFF1E2022),
	primaryContainer = Color(0xFF9BAEC3),
	onPrimaryContainer = Color(0xFF0F1316),
	secondary = Color(0xFFA2A9AE),
	onSecondary = Color(0xFF242D34),
	secondaryContainer = Color(0xFFCED8DF),
	onSecondaryContainer = Color(0xFF1C3648),
	surface = Color(0xFF1E2A34),
	onSurface = Color(0xFFC9D6DF),
	background = Color(0xFF02060A),
	onBackground = Color(0xFFCEDBE6)
)

val lightBackground0 = Color(0xFFECF1F4)
val darkBackground0 = Color(0xFF02060A)

@SuppressLint("NewApi")
@Composable
fun GraphiteBase(
	isDarkTheme: Boolean = isSystemInDarkTheme(),
	isDynamicColor: Boolean = false,
	content: @Composable () -> Unit
) {
	val context = LocalContext.current

	val dataStore = DataStore(context = context)
	val currentTypography by dataStore.getTypography.collectAsState(initial = null)
	val expiryTimestamp by dataStore.getExpiryTime.collectAsState(initial = null)
	val currentTimestamp = System.currentTimeMillis()

	val dynamicColor = isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
	val appColorScheme = when {
		dynamicColor && isDarkTheme -> dynamicDarkColorScheme(LocalContext.current)
		dynamicColor && !isDarkTheme -> dynamicLightColorScheme(LocalContext.current)
		else -> if (isDarkTheme) darkColorScheme0 else lightColorScheme0
	}
	val appTypography = when (currentTypography) {
		0 -> UbuntuTypography
		1 -> SourceSansProTypography
		2 -> OverlockTypography
		3 -> ATWriterTypography
		else -> UbuntuTypography
	}

	androidx.compose.material3.MaterialTheme(
		colorScheme = appColorScheme,
		typography = appTypography
	) {
		// TODO (M3): MaterialTheme doesn't provide LocalIndication, remove when it does
		val rippleIndication = rememberRipple()
		Crossfade(targetState = currentTypography != null) {
			if (it) {
				CompositionLocalProvider(
					PremiumCompositionLocal provides
							((expiryTimestamp ?: 0L) > currentTimestamp)
				) {
					CompositionLocalProvider(
						LocalIndication provides rippleIndication,
						content = content
					)
				}
			} else {
				LoadingView()
			}
		}
	}
}

val PremiumCompositionLocal = staticCompositionLocalOf<Boolean> {
	error("No Color provided")
}
