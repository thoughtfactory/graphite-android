package com.syncodec.graphite.presentation.base

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.presentation.base.secureComposable.SecureComposable
import com.syncodec.graphite.presentation.common.shape.AbsoluteSmoothCornerShape
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.dataStore.DataStoreInstance


val LocalIsPro = compositionLocalOf { false }
val LocalIsDarkTheme = compositionLocalOf { false }
val LocalAppDataStore = compositionLocalOf<DataStoreInstance> { error("no instance provided") }

@Composable
fun sortOn() : State<SortOn?> {
	return LocalAppDataStore.current.getSortOn.collectAsState(initial = null)
}

@Composable
fun sortBy() : State<SortBy?> {
	return LocalAppDataStore.current.getSortBy.collectAsState(initial = null)
}

@SuppressLint("NewApi")
@Composable
fun BaseComposable(
	vararg providerValues : ProvidedValue<*>,
	isDarkTheme : Boolean = isSystemInDarkTheme(),
	isDynamicColor : Boolean = false,
	content : @Composable () -> Unit
) {
	val context = LocalContext.current

	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val darkTheme by dataStoreInstance.getDarkTheme.collectAsState(initial = null)
	val typography by dataStoreInstance.getTypography.collectAsState(initial = null)

	val dynamicColor = isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
	val appColorScheme = when (darkTheme) {
		SettingsActivity.Companion.DarkTheme.SyncWithSystem -> if (isDarkTheme) darkColorScheme0 else lightColorScheme0
		SettingsActivity.Companion.DarkTheme.AlwaysOn -> darkColorScheme0
		SettingsActivity.Companion.DarkTheme.AlwaysOff -> lightColorScheme0
		else -> null
	}
	val appTypography = when (typography) {
		"PT Mono" -> PTMonoTypography
		"Ubuntu" -> UbuntuTypography
		"Montserrat" -> MontserratTypography
		"Roboto" -> RobotoTypography
		"Tilt Neon" -> TiltNeonTypography
		else -> Defaults.DefaultTypagrophy
	}

	val appShapes = Shapes(
		extraSmall = AbsoluteSmoothCornerShape(4.dp, 100),
		small = AbsoluteSmoothCornerShape(8.dp, 100),
		medium = AbsoluteSmoothCornerShape(12.dp, 100),
		large = AbsoluteSmoothCornerShape(16.dp, 100),
		extraLarge = AbsoluteSmoothCornerShape(28.dp, 100)
	)


	val isPro by BaseApplication.isPro.collectAsState()

	appColorScheme?.let { colorScheme ->
		MaterialTheme(
			colorScheme = colorScheme,
			shapes = appShapes,
			typography = appTypography,
		) {
			val systemUiController = rememberSystemUiController()
			systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
			systemUiController.setNavigationBarColor(Color.Black)

			// TODO (M3): MaterialTheme doesn't provide LocalIndication, remove when it does
			val rippleIndication = rememberRipple()

			CompositionLocalProvider(
				*providerValues,
				LocalIndication provides rippleIndication,
				LocalIsPro provides isPro,
				LocalAppDataStore provides dataStoreInstance,
				LocalIsDarkTheme provides when (colorScheme) {
					darkColorScheme0 -> true
					lightColorScheme0 -> false
					else -> true
				},
			) {
				SecureComposable(
					content = content
				)
			}
		}
	}
}
