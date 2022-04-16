package com.syncodec.momento.ui.theme

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.syncodec.momento.custom.LoadingView
import com.syncodec.momento.miscellaneous.DataStore


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

val lightColorScheme1 = lightColorScheme(
	primary = Color(0xFF789395),
	onPrimary = Color(0xFFDCE2E2),
	primaryContainer = Color(0xFF789395),
	onPrimaryContainer = Color(0xFFDCE2E2),
	secondary = Color(0xFF94B49F),
	onSecondary = Color(0xFFE8EDEA),
	secondaryContainer = Color(0xFF6C9A7C),
	onSecondaryContainer = Color(0xFFC4D6CA),
	surface = Color(0xFFB4CFB0),
	onSurface = Color(0xFF2B6522),
	background = Color(0xFFE5E3C9),
	onBackground = Color(0xFF66622B)
)

val darkColorScheme1 = darkColorScheme(
	primary = Color(0xFF3B676B),
	onPrimary = Color(0xFFE2E7E8),
	primaryContainer = Color(0xFF789395),
	onPrimaryContainer = Color(0xFFDCE2E2),
	secondary = Color(0xFF6C9A7C),
	onSecondary = Color(0xFFE8EDEA),
	secondaryContainer = Color(0xFF326844),
	onSecondaryContainer = Color(0xFFC5D7CB),
	surface = Color(0xFF02241C),
	onSurface = Color(0xFF9BB8B1),
	background = Color(0xFF00191B),
	onBackground = Color(0xFFA4B0B1)
)

val lightColorScheme2 = lightColorScheme(
	primary = Color(0xFF3C4245),
	onPrimary = Color(0xFFCACCCC),
	primaryContainer = Color(0xFF1C1F21),
	onPrimaryContainer = Color(0xFF9A9EA1),
	secondary = Color(0xFF5F6769),
	onSecondary = Color(0xFFFFFFFF),
	secondaryContainer = Color(0xFF5F6769),
	onSecondaryContainer = Color(0xFF9FA3A4),
	surface = Color(0xFF719192),
	onSurface = Color(0xFFD6DEDE),
	background = Color(0xFFDFCDC3),
	onBackground = Color(0xFF66622B)
)

val darkColorScheme2 = darkColorScheme(
	primary = Color(0xFF3B676B),
	onPrimary = Color(0xFFE2E7E8),
	primaryContainer = Color(0xFF789395),
	onPrimaryContainer = Color(0xFFDCE2E2),
	secondary = Color(0xFF6C9A7C),
	onSecondary = Color(0xFFE8EDEA),
	secondaryContainer = Color(0xFF326844),
	onSecondaryContainer = Color(0xFFC5D7CB),
	surface = Color(0xFF02241C),
	onSurface = Color(0xFF9BB8B1),
	background = Color(0xFF00191B),
	onBackground = Color(0xFFA4B0B1)
)

val lightColorScheme3 = lightColorScheme(
	primary = Color(0xFF056676),
	onPrimary = Color(0xFF7FC2CF),
	primaryContainer = Color(0xFF013B46),
	onPrimaryContainer = Color(0xFF92C7D1),
	secondary = Color(0xFF5EAAA8),
	onSecondary = Color(0xFFEDFBFA),
	secondaryContainer = Color(0xFF237976),
	onSecondaryContainer = Color(0xFFCCEBEA),
	surface = Color(0xFFA3D2CA),
	onSurface = Color(0xFF313131),
	background = Color(0xFFE8DED2),
	onBackground = Color(0xFF4D5F69)
)

val darkColorScheme3 = darkColorScheme(
	primary = Color(0xFF3B676B),
	onPrimary = Color(0xFFE2E7E8),
	primaryContainer = Color(0xFF789395),
	onPrimaryContainer = Color(0xFFDCE2E2),
	secondary = Color(0xFF6C9A7C),
	onSecondary = Color(0xFFE8EDEA),
	secondaryContainer = Color(0xFF326844),
	onSecondaryContainer = Color(0xFFC5D7CB),
	surface = Color(0xFF02241C),
	onSurface = Color(0xFF9BB8B1),
	background = Color(0xFF00191B),
	onBackground = Color(0xFFA4B0B1)
)

val lightColorScheme4 = lightColorScheme(
	primary = Color(0xFF456268),
	onPrimary = Color(0xFFE7E9E9),
	primaryContainer = Color(0xFF1C3D43),
	onPrimaryContainer = Color(0xFF8B9EA1),
	secondary = Color(0xFF79A3B1),
	onSecondary = Color(0xFF073747),
	secondaryContainer = Color(0xFF2E6577),
	onSecondaryContainer = Color(0xFFDDEAEF),
	surface = Color(0xFFD0E8F2),
	onSurface = Color(0xFFFFCE97),
	background = Color(0xFFFCF8EC),
	onBackground = Color(0xFF65657F)
)

val darkColorScheme4 = darkColorScheme(
	primary = Color(0xFF3B676B),
	onPrimary = Color(0xFFE2E7E8),
	primaryContainer = Color(0xFF789395),
	onPrimaryContainer = Color(0xFFDCE2E2),
	secondary = Color(0xFF6C9A7C),
	onSecondary = Color(0xFFE8EDEA),
	secondaryContainer = Color(0xFF326844),
	onSecondaryContainer = Color(0xFFC5D7CB),
	surface = Color(0xFF02241C),
	onSurface = Color(0xFF9BB8B1),
	background = Color(0xFF00191B),
	onBackground = Color(0xFFA4B0B1)
)

val lightColorScheme5 = lightColorScheme(
	primary = Color(0xFF769FCD),
	onPrimary = Color(0xFFD5E5F7),
	primaryContainer = Color(0xFF346398),
	onPrimaryContainer = Color(0xFFDBE8F7),
	secondary = Color(0xFFB9D7EA),
	onSecondary = Color(0xFF245D82),
	secondaryContainer = Color(0xFF5C8DAD),
	onSecondaryContainer = Color(0xFFBFD9E9),
	surface = Color(0xFFD6E6F2),
	onSurface = Color(0xFF325F82),
	background = Color(0xFFFCF8EC),
	onBackground = Color(0xFF4C818D)
)

val darkColorScheme5 = darkColorScheme(
	primary = Color(0xFF3B676B),
	onPrimary = Color(0xFFE2E7E8),
	primaryContainer = Color(0xFF789395),
	onPrimaryContainer = Color(0xFFDCE2E2),
	secondary = Color(0xFF6C9A7C),
	onSecondary = Color(0xFFE8EDEA),
	secondaryContainer = Color(0xFF326844),
	onSecondaryContainer = Color(0xFFC5D7CB),
	surface = Color(0xFF02241C),
	onSurface = Color(0xFF9BB8B1),
	background = Color(0xFF00191B),
	onBackground = Color(0xFFA4B0B1)
)

val lightColorScheme6 = lightColorScheme(
	primary = Color(0xFF132238),
	onPrimary = Color(0xFFCACFD5),
	primaryContainer = Color(0xFF213149),
	onPrimaryContainer = Color(0xFF97A0AE),
	secondary = Color(0xFF364E68),
	onSecondary = Color(0xFFD1D6DC),
	secondaryContainer = Color(0xFF223A55),
	onSecondaryContainer = Color(0xFFA5B1BE),
	surface = Color(0xFF98CCD3),
	onSurface = Color(0xFF155B65),
	background = Color(0xFFEBF0F6),
	onBackground = Color(0xFF010E1F)
)

val darkColorScheme6 = darkColorScheme(
	primary = Color(0xFF3B676B),
	onPrimary = Color(0xFFE2E7E8),
	primaryContainer = Color(0xFF789395),
	onPrimaryContainer = Color(0xFFDCE2E2),
	secondary = Color(0xFF6C9A7C),
	onSecondary = Color(0xFFE8EDEA),
	secondaryContainer = Color(0xFF326844),
	onSecondaryContainer = Color(0xFFC5D7CB),
	surface = Color(0xFF02241C),
	onSurface = Color(0xFF9BB8B1),
	background = Color(0xFF00191B),
	onBackground = Color(0xFFA4B0B1)
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

val lightBackground1 = Color(0xFFF4F3F3)
val darkBackground1 = Color(0xFF02060A)

val lightBackground2 = Color(0xFFFBF0F0)
val darkBackground2 = Color(0xFF02060A)

val lightBackground3 = Color(0xFFE3FDFD)
val darkBackground3 = Color(0xFF02060A)

val lightBackground4 = Color(0xFFEBEBE3)
val darkBackground4 = Color(0xFF02060A)

val lightBackground5 = Color(0xFFD7F2F7)
val darkBackground5 = Color(0xFF02060A)

val lightBackground6 = Color(0xFFDDDDDD)
val darkBackground6 = Color(0xFF02060A)

val lightBackground7 = Color(0xFFF1FDF3)
val darkBackground7 = Color(0xFF02060A)

val lightBackground8 = Color(0xFFFBF8F1)
val darkBackground8 = Color(0xFF02060A)

val lightBackground9 = Color(0xFFD3E0DC)
val darkBackground9 = Color(0xFF02060A)

@SuppressLint("NewApi")
@Composable
fun MomentoTheme(
	isDarkTheme: Boolean = isSystemInDarkTheme(),
	isDynamicColor: Boolean = false,
	content: @Composable () -> Unit
) {
	val context = LocalContext.current

	val dataStore = DataStore(context = context)
	val currentTheme by dataStore.getTheme.collectAsState(initial = null)
	val currentBackground by dataStore.getBackground.collectAsState(initial = null)
	val currentTypography by dataStore.getTypography.collectAsState(initial = null)

	val dynamicColor = isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
	val appColorScheme = when {
		dynamicColor && isDarkTheme -> {
			dynamicDarkColorScheme(LocalContext.current)
		}
		dynamicColor && !isDarkTheme -> {
			dynamicLightColorScheme(LocalContext.current)
		}
		else -> {
			if (isDarkTheme) {
				when (currentTheme) {
					1 -> darkColorScheme1.copy(
						background = getBackground(
							currentBackground = currentBackground,
							isDarkTheme = true
						)
					)
					2 -> darkColorScheme2.copy(
						background = getBackground(
							currentBackground = currentBackground,
							isDarkTheme = true
						)
					)
					3 -> darkColorScheme3.copy(
						background = getBackground(
							currentBackground = currentBackground,
							isDarkTheme = true
						)
					)
					4 -> darkColorScheme4.copy(
						background = getBackground(
							currentBackground = currentBackground,
							isDarkTheme = true
						)
					)
					5 -> darkColorScheme5.copy(
						background = getBackground(
							currentBackground = currentBackground,
							isDarkTheme = true
						)
					)
					6 -> darkColorScheme6.copy(
						background = getBackground(
							currentBackground = currentBackground,
							isDarkTheme = true
						)
					)
					7 -> darkColorScheme0.copy(
						background = getBackground(
							currentBackground = currentBackground,
							isDarkTheme = true
						)
					)
					else -> darkColorScheme0.copy(
						background = getBackground(
							currentBackground = currentBackground,
							isDarkTheme = true
						)
					)
				}
			} else {
				when (currentTheme) {
					1 -> lightColorScheme1.copy(
						background = getBackground(
							currentBackground = currentBackground,
							isDarkTheme = false
						)
					)
					2 -> lightColorScheme2.copy(
						background = getBackground(
							currentBackground = currentBackground,
							isDarkTheme = false
						)
					)
					3 -> lightColorScheme3.copy(
						background = getBackground(
							currentBackground = currentBackground,
							isDarkTheme = false
						)
					)
					4 -> lightColorScheme4.copy(
						background = getBackground(
							currentBackground = currentBackground,
							isDarkTheme = false
						)
					)
					5 -> lightColorScheme5.copy(
						background = getBackground(
							currentBackground = currentBackground,
							isDarkTheme = false
						)
					)
					6 -> lightColorScheme6.copy(
						background = getBackground(
							currentBackground = currentBackground,
							isDarkTheme = false
						)
					)
					7 -> lightColorScheme0.copy(
						background = getBackground(
							currentBackground = currentBackground,
							isDarkTheme = false
						)
					)
					else -> lightColorScheme0.copy(
						background = getBackground(
							currentBackground = currentBackground,
							isDarkTheme = false
						)
					)
				}
			}
		}
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
		Crossfade(targetState = currentTheme != null && currentTypography != null) {
			if (it) {
				CompositionLocalProvider(
					LocalIndication provides rippleIndication,
					content = content
				)
			} else {
				LoadingView()
			}
		}
	}
}

private fun getBackground(
	currentBackground: Int?,
	isDarkTheme: Boolean
): Color {
	return if (isDarkTheme) {
		when (currentBackground) {
			0 -> darkBackground0
			1 -> darkBackground1
			2 -> darkBackground2
			3 -> darkBackground3
			4 -> darkBackground4
			5 -> darkBackground5
			6 -> darkBackground6
			7 -> darkBackground7
			8 -> darkBackground8
			9 -> darkBackground9
			else -> darkBackground0
		}
	} else {
		when (currentBackground) {
			0 -> lightBackground0
			1 -> lightBackground1
			2 -> lightBackground2
			3 -> lightBackground3
			4 -> lightBackground4
			5 -> lightBackground5
			6 -> lightBackground6
			7 -> lightBackground7
			8 -> lightBackground8
			9 -> lightBackground9
			else -> lightBackground0
		}
	}
}
