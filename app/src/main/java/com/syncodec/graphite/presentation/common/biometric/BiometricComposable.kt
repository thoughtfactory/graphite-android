package com.syncodec.graphite.presentation.common.biometric

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.base.LocalAppDataStore
import com.syncodec.graphite.presentation.common.LoadingView
import org.koin.compose.koinInject


@Preview
@Composable
fun BiometricComposable(
	content: @Composable () -> Unit = {}
) {
	val context = LocalContext.current

	val appDataStore = LocalAppDataStore.current

	val lockableRepo = koinInject<LockableRepo>()

	val repositoryStatus by lockableRepo.repositoryStatusFlow.collectAsState()

	val isBiometricsEnabled by appDataStore.isBiometricEnabled.collectAsState(initial = null)

	var isBiometricPromptVisible by remember { mutableStateOf(false) }

	LaunchedEffect(key1 = isBiometricsEnabled) {
		when (isBiometricsEnabled) {
			true -> lockableRepo.repositoryStatusFlow.tryEmit(Repository.Companion.RepositoryStatus.Locked)
			false -> lockableRepo.decryptRepository()
			null -> lockableRepo.repositoryStatusFlow.tryEmit(Repository.Companion.RepositoryStatus.Loading)
		}
	}

	AnimatedContent(
		targetState = repositoryStatus,
		transitionSpec = { fadeIn(tween(ANIMATION_DURATION_MILLIS)) togetherWith fadeOut(tween(ANIMATION_DURATION_MILLIS)) },
		label = "repositoryStatus_animation"
	) {
		when (it) {
			is Repository.Companion.RepositoryStatus.Init -> LoadingView()
			is Repository.Companion.RepositoryStatus.Loading -> LoadingView()
			is Repository.Companion.RepositoryStatus.Locked -> LockedRepositoryView(onClickUnlock = { isBiometricPromptVisible = true })
			is Repository.Companion.RepositoryStatus.Success -> content()
			is Repository.Companion.RepositoryStatus.Error -> LoadingView()
		}
	}

	if (isBiometricPromptVisible) {
		Authenticator(
			onAuthenticated = {
				Toast.makeText(context, "onAuthenticated", Toast.LENGTH_SHORT).show()
				lockableRepo.decryptRepository()
			},
			onCancel = { Toast.makeText(context, "onCancel", Toast.LENGTH_SHORT).show() },
			onAuthenticationError = { Toast.makeText(context, "onAuthenticationError", Toast.LENGTH_SHORT).show() },
			onAuthenticationSoftError = { Toast.makeText(context, "onAuthenticationSoftError", Toast.LENGTH_SHORT).show() },
		)
	}
}

@Preview
@Composable
private fun Authenticator(
	onAuthenticated: () -> Unit = {},
	onCancel: () -> Unit = {},
	onAuthenticationError: () -> Unit = {},
	onAuthenticationSoftError: () -> Unit = {}
) {
	val context = LocalContext.current
	val activity = context as FragmentActivity

	val biometricManager = remember { BiometricManager.from(context) }

	val callback = remember {
		object : BiometricPrompt.AuthenticationCallback() {
			override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
				super.onAuthenticationError(errorCode, errString)
				when (errorCode) {
					BiometricPrompt.ERROR_USER_CANCELED -> onCancel()
					BiometricPrompt.ERROR_NEGATIVE_BUTTON -> onCancel()
					BiometricPrompt.ERROR_CANCELED -> onCancel()
					else -> onAuthenticationError()
				}
			}

			override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
				super.onAuthenticationSucceeded(result)
				onAuthenticated()
			}

			override fun onAuthenticationFailed() {
				super.onAuthenticationFailed()
				onAuthenticationSoftError()
			}
		}
	}


	val promptInfo = remember {
		val secureOption = bestSecureOption(biometricManager)

		BiometricPrompt.PromptInfo.Builder()
			.setTitle("title")
			.setDescription("description")
			.apply {
				if ((secureOption and BiometricManager.Authenticators.DEVICE_CREDENTIAL) == 0) {
					setNegativeButtonText("negativeButton")
				}
			}.setAllowedAuthenticators(
				secureOption
			)
			.build()
	}

	val biometricPrompt = remember { BiometricPrompt(activity, callback) }

	DisposableEffect(biometricPrompt) {
		biometricPrompt.authenticate(promptInfo)

		onDispose {
			biometricPrompt.cancelAuthentication()
		}
	}
}

private fun bestSecureOption(biometricManager: BiometricManager): Int {
	when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
		BiometricManager.BIOMETRIC_SUCCESS,
		BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> return BiometricManager.Authenticators.BIOMETRIC_STRONG
	}
	when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
		BiometricManager.BIOMETRIC_SUCCESS,
		BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> return BiometricManager.Authenticators.BIOMETRIC_WEAK
	}
	when (biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
		BiometricManager.BIOMETRIC_SUCCESS,
		BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> return BiometricManager.Authenticators.DEVICE_CREDENTIAL
	}
	return if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
		BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_WEAK
	} else {
		BiometricManager.Authenticators.DEVICE_CREDENTIAL
	}
}
