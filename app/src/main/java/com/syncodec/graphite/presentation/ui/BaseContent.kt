package com.syncodec.graphite.presentation.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.presentation.common.shape.AbsoluteSmoothCornerShape
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.ui.authentication.AddPasscodeScreen
import com.syncodec.graphite.presentation.ui.authentication.AuthenticatorScreen
import com.syncodec.graphite.presentation.ui.authentication.ChangePasscode
import com.syncodec.graphite.presentation.ui.authenticator2.AuthController
import com.syncodec.graphite.presentation.ui.authenticator2.AuthState
import com.syncodec.graphite.presentation.ui.authenticator2.AuthenticatorScreen2
import com.syncodec.graphite.presentation.ui.authenticator2.LocalAuthController
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated
import com.syncodec.graphite.utils.alice.AliceRequestResult
import com.syncodec.graphite.utils.alice.getSecretData
import com.syncodec.graphite.utils.alice.putSecretData
import org.koin.compose.koinInject


val LocalIsPro = compositionLocalOf { false }
val LocalIsDarkTheme = compositionLocalOf { false }

@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("NewApi")
@Composable
fun BaseContent(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    isDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val dataStoreInstance = remember { DataStoreInstance(context = context) }

    val systemTheme by dataStoreInstance.getDarkTheme.collectAsState(initial = null)
    val typography by dataStoreInstance.getTypography.collectAsState(initial = null)

    val dynamicColor = isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val appColorScheme = when (systemTheme) {
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
        else -> PTMonoTypography
    }

    val isAuthenticated by BaseApplication.isAuthenticated.collectAsState(initial = false)
    val authenticatorState by BaseApplication.authenticatorScreen.collectAsState()

    fun onClose() = BaseApplication.authenticatorScreen.tryEmit(AuthenticatorScreen.None)

    var noTry by remember { mutableIntStateOf(0) }

    val isPro by BaseApplication.isPro.collectAsState()

    BackHandler(enabled = authenticatorState != AuthenticatorScreen.None) {
        BaseApplication.authenticatorScreen.tryEmit(AuthenticatorScreen.None)
    }

    val appShapes = Shapes(
        extraSmall = AbsoluteSmoothCornerShape(4.dp, 100),
        small = AbsoluteSmoothCornerShape(8.dp, 100),
        medium = AbsoluteSmoothCornerShape(12.dp, 100),
        large = AbsoluteSmoothCornerShape(16.dp, 100),
        extraLarge = AbsoluteSmoothCornerShape(28.dp, 100)
    )

    appColorScheme?.let { colorScheme ->
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = appShapes,
            typography = appTypography
        ) {
            // TODO (M3): MaterialTheme doesn't provide LocalIndication, remove when it does
//			val rippleIndication = rememberRipple()

            CompositionLocalProvider(
//				LocalIndication provides rippleIndication,
                LocalIsPro provides isPro,
                LocalIsDarkTheme provides (((systemTheme == SettingsActivity.Companion.DarkTheme.SyncWithSystem) && isDarkTheme) || systemTheme == SettingsActivity.Companion.DarkTheme.AlwaysOn),
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
                }
            ) {
                content()

                AnimatedContent(
                    targetState = authenticatorState,
                    transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(300)) }
                ) {
                    when (it) {
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
                                    noTry++
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

                        AuthenticatorScreen.RemovePasscode -> null
                        AuthenticatorScreen.None -> null
                    }
                }
            }
        }
    }
}

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}


@Composable
fun BaseComposable2(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val isPro by BaseApplication.isPro.collectAsState()
    val dataStoreInstance = remember { DataStoreInstance(context = context) }

    val systemTheme by dataStoreInstance.getDarkTheme.collectAsState(initial = null)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val typography by dataStoreInstance.getTypography.collectAsState(initial = null)

    val appColorScheme = when (systemTheme) {
        SettingsActivity.Companion.DarkTheme.SyncWithSystem -> if (isSystemInDarkTheme) darkColorScheme0 else lightColorScheme0
        SettingsActivity.Companion.DarkTheme.AlwaysOn -> darkColorScheme0
        SettingsActivity.Companion.DarkTheme.AlwaysOff -> lightColorScheme0
        else -> return
    }
    val isDarkTheme = ((systemTheme == SettingsActivity.Companion.DarkTheme.SyncWithSystem) && isSystemInDarkTheme) || systemTheme == SettingsActivity.Companion.DarkTheme.AlwaysOn

    val appTypography = when (typography) {
        "PT Mono" -> PTMonoTypography
        "Ubuntu" -> UbuntuTypography
        "Montserrat" -> MontserratTypography
        "Roboto" -> RobotoTypography
        "Tilt Neon" -> TiltNeonTypography
        else -> PTMonoTypography
    }

    val appShapes = Shapes(
        extraSmall = AbsoluteSmoothCornerShape(cornerRadius = 4.dp, smoothnessAsPercent = 100),
        small = AbsoluteSmoothCornerShape(cornerRadius = 8.dp, smoothnessAsPercent = 100),
        medium = AbsoluteSmoothCornerShape(cornerRadius = 12.dp, smoothnessAsPercent = 100),
        large = AbsoluteSmoothCornerShape(cornerRadius = 16.dp, smoothnessAsPercent = 100),
        extraLarge = AbsoluteSmoothCornerShape(cornerRadius = 28.dp, smoothnessAsPercent = 100)
    )

    val authController: AuthController = koinInject()
    val authState by authController.authStateFlow.collectAsState()

    MaterialTheme(
        colorScheme = appColorScheme,
        shapes = appShapes,
        typography = appTypography
    ) {
        CompositionLocalProvider(
            values = arrayOf(
                LocalIsPro provides isPro,
                LocalIsDarkTheme provides isDarkTheme,
                LocalAuthController provides authController
            ),
        ) {

            context
                .getActivity()
                ?.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(
                    lightScrim = MaterialTheme.colorScheme.onBackground.toArgb(),
                    darkScrim = MaterialTheme.colorScheme.onBackground.toArgb(),
                    detectDarkMode = { isDarkTheme }
                ),
            )

            content()
            AnimatedVisibility(
                visible = authState == AuthState.Authenticate,
                enter = AnimationDefaults.scaleAndFadeEnter(scale = 0.8f),
                exit = AnimationDefaults.scaleAndFadeExit(scale = 0.8f),
                modifier = Modifier
            ) {
                AuthenticatorScreen2()
            }
        }
    }
}
