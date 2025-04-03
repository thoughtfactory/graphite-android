package com.syncodec.graphite.presentation.ui.authenticator2

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.syncodec.graphite.presentation.common.v2.button.GraIconButton
import com.syncodec.graphite.utils.alice.getSecretData
import com.syncodec.graphite.utils.alice2.Alice2.Companion.putSecretData
import com.syncodec.graphite.utils.onlyIf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


enum class AuthState {
    UnAuthenticated,
    Authenticate,
    Authenticated
}

class AuthController {
    private val _authStateFlow: MutableStateFlow<AuthState> = MutableStateFlow(value = AuthState.UnAuthenticated)
    val authStateFlow: StateFlow<AuthState> = this._authStateFlow.asStateFlow()

    fun authenticate() = this._authStateFlow.tryEmit(value = AuthState.Authenticate)
    fun unauthenticate() = this._authStateFlow.tryEmit(value = AuthState.UnAuthenticated)

    fun authenticationSuccessful() = this._authStateFlow.tryEmit(value = AuthState.Authenticated)
}

val LocalAuthController: ProvidableCompositionLocal<AuthController> = compositionLocalOf { error("data not provided") }

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AuthenticatorScreen2() {
    val configuration = LocalConfiguration.current
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val authController = LocalAuthController.current
    BackHandler(enabled = true) { authController.unauthenticate() }

    val context = LocalContext.current
    var currentPasscode by remember { mutableStateOf(value = context.getSecretData("passcode").data?.decodeToString()) }

    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { authController.unauthenticate() }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    navigationIcon = { GraIconButton.BackButton()},
                    title = {},
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            },
            bottomBar = { BottomAppBar(content = {}, containerColor = MaterialTheme.colorScheme.background) }
        ) { paddingValues ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValues)
            ) {
                AnimatedContent(
                    targetState = currentPasscode,
                    modifier = Modifier
                        .onlyIf(predicate = { configuration.orientation == Configuration.ORIENTATION_PORTRAIT }) { fillMaxWidth() }
                        .onlyIf(predicate = { configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }) { widthIn(max = 512.dp) }
                ) { currentPasscode1 ->
                    if (currentPasscode1 == null) AddPasscodeScreen {
                        context.putSecretData("passcode", it.encodeToByteArray())
                        currentPasscode = context.getSecretData("passcode").data?.decodeToString()
                    } else AuthenticatePasscodeScreen(
                        currentPasscode = currentPasscode1,
                        onConfirmPasscode = { authController.authenticationSuccessful() }
                    )
                }
            }
        }
    }
}
