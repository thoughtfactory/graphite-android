package com.syncodec.graphite.presentation.main

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.repository.repository.Repository
import com.syncodec.graphite.di.sync.dropbox.DBox
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.main.composable.screen.FirstTimeScreen
import com.syncodec.graphite.presentation.main.composable.screen.MainScreen
import com.syncodec.graphite.presentation.main.composable.screen.RepositoryLockedScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.service.syncService.DropboxServiceConnectionManager
import com.syncodec.graphite.service.syncService.SyncerService
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.alice.Alice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : ComponentActivity() {

	private val viewModel by viewModel<MainViewModel>()

	private lateinit var auth : FirebaseAuth
	private lateinit var oneTapClient : SignInClient

	private var cancellationSignal : CancellationSignal? = null

	private var biometricErrorMessage : MutableState<String?> = mutableStateOf(null)

	private val syncStatus = MutableStateFlow<SyncerService.Companion.SyncStatus>(SyncerService.Companion.SyncStatus.Init)

	@OptIn(ExperimentalAnimationApi::class)
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)
		isInStack = true

		val dataStoreInstance = DataStoreInstance(this)

		auth = Firebase.auth
		oneTapClient = Identity.getSignInClient(this)

		setContent {
			BaseContent {
				val isFirstTime by dataStoreInstance.getIsFirstTime.collectAsState(initial = null)
				val isBiometricsEnabled by dataStoreInstance.getUseBiometric().collectAsState(initial = null)
				var isBiometricUsed by remember { mutableStateOf(false) }

				val systemUiController = rememberSystemUiController()
				if (isFirstTime == true) systemUiController.setStatusBarColor(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
				else systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)

				LaunchedEffect(key1 = isBiometricsEnabled) {
					if (isBiometricsEnabled != null && ! isBiometricUsed) {
						if (isBiometricsEnabled == true) {
							viewModel.setRepositoryState(Repository.Companion.RepositoryState.Locked)
							launchBiometric()
						} else viewModel.onAuthenticate(applicationContext)
						isBiometricUsed = true
					}
				}

				val repositoryState by viewModel.repositoryState.collectAsState(initial = null)

				val biometricErrorMessage by this.biometricErrorMessage

				val syncStatus by syncStatus.collectAsState()
				val testConnectionResponse by viewModel.testConnectionResponse.collectAsState(initial = DBox.Companion.TestConnectionResponse.Error(Exception("Test Connection Error"), ""))

				LaunchedEffect(key1 = testConnectionResponse) {
					if (testConnectionResponse is DBox.Companion.TestConnectionResponse.Success) {
						startSyncService()
					}
				}

				LaunchedEffect(key1 = null) {
					viewModel.testDropboxConnection()
				}

				AnimatedContent(
					targetState = isFirstTime,
					transitionSpec = { fadeIn(tween(300)) with fadeOut(animationSpec = tween(300)) },
					modifier = Modifier.fillMaxSize(),
					label = "isFirstTime"
				) {
					when (it) {
						true -> FirstTimeScreen(onClickLogin = this@MainActivity::signIn)
						false -> AnimatedContent(
							targetState = repositoryState,
							transitionSpec = { fadeIn(tween(300)) with fadeOut(animationSpec = tween(300)) },
							modifier = Modifier.fillMaxSize(),
							label = "repositoryState"
						) {
							when (it) {
								Repository.Companion.RepositoryState.Locked -> RepositoryLockedScreen(
									errorMessage = biometricErrorMessage,
									onUnlock = { this@MainActivity.launchBiometric() }
								)

								Repository.Companion.RepositoryState.Success -> MainScreen(
									syncStatus = syncStatus,
									testConnectionResponse = testConnectionResponse,
									testDropboxConnection = { viewModel.testDropboxConnection() },
									onClickSyncNow = { dropboxServiceConnectionManager?.service?.onClickSyncNow() },
									onClickForceSync = { dropboxServiceConnectionManager?.service?.onClickForceSync() },
								)

								Repository.Companion.RepositoryState.Error -> RepositoryLockedScreen(
									errorMessage = biometricErrorMessage,
									onUnlock = { this@MainActivity.launchBiometric() }
								)

								else -> LoadingView()
							}
						}

						else -> LoadingView()
					}
				}
			}
		}
	}

	private fun checkBiometricSupport() : Boolean {
		val keyGuardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

		if (! keyGuardManager.isDeviceSecure) {
			return true
		}

		if (checkSelfPermission(android.Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED) {
			return false
		}

		return packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
	}

	private fun launchBiometric() {
		if (checkBiometricSupport()) {
			val executor = ContextCompat.getMainExecutor(applicationContext)
			val biometricPrompt = BiometricPrompt
				.Builder(applicationContext)
				.setTitle("Graphite")
				.setSubtitle("Unlock repository")
				.setDescription("Repository is locked behind biometric authentication. Please authenticate to unlock.")
				.setConfirmationRequired(false)
				.setNegativeButton("Cancel", executor) { _, _ ->
					Toast.makeText(this@MainActivity, "Authentication Cancelled", Toast.LENGTH_SHORT).show()
				}.build()

			biometricPrompt.authenticate(
				getCancellationSignal(),
				executor,
				object : BiometricPrompt.AuthenticationCallback() {
					override fun onAuthenticationSucceeded(result : BiometricPrompt.AuthenticationResult?) {
						super.onAuthenticationSucceeded(result)
						Toast.makeText(this@MainActivity, "Authentication Succeeded", Toast.LENGTH_SHORT).show()
						viewModel.onAuthenticate(applicationContext)
					}

					override fun onAuthenticationError(errorCode : Int, errString : CharSequence?) {
						super.onAuthenticationError(errorCode, errString)
						Toast.makeText(this@MainActivity, "Authentication Error", Toast.LENGTH_SHORT).show()

						when (errorCode) {
							BiometricPrompt.BIOMETRIC_ERROR_CANCELED -> null
							BiometricPrompt.BIOMETRIC_ERROR_HW_NOT_PRESENT -> null
							BiometricPrompt.BIOMETRIC_ERROR_HW_UNAVAILABLE -> null
							BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT -> null
							BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT_PERMANENT -> null
							BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS -> {
								viewModel.onAuthenticate(applicationContext)
							}

							BiometricPrompt.BIOMETRIC_ERROR_NO_DEVICE_CREDENTIAL -> Toast.makeText(
								this@MainActivity,
								"It seems like your device don't have pin, password or pattern setup. Try setting up credentials and unlocking again",
								Toast.LENGTH_SHORT
							).show()

							BiometricPrompt.BIOMETRIC_ERROR_NO_SPACE -> null
							BiometricPrompt.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> null
							BiometricPrompt.BIOMETRIC_ERROR_TIMEOUT -> null
							BiometricPrompt.BIOMETRIC_ERROR_UNABLE_TO_PROCESS -> null
							BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED -> null
							BiometricPrompt.BIOMETRIC_ERROR_VENDOR -> null
						}

						biometricErrorMessage.value = errString.toString()

						viewModel.onAuthFailure()
					}

					override fun onAuthenticationFailed() {
						super.onAuthenticationFailed()
						viewModel.onAuthFailure()
					}

					override fun onAuthenticationHelp(helpCode : Int, helpString : CharSequence?) {
						super.onAuthenticationHelp(helpCode, helpString)
					}
				}
			)
		}
	}

	private fun getCancellationSignal() : CancellationSignal {
		cancellationSignal = CancellationSignal()
		cancellationSignal?.setOnCancelListener {
			Toast.makeText(this, "Authentication Cancelled Signal", Toast.LENGTH_SHORT).show()
		}

		return cancellationSignal as CancellationSignal
	}

	private fun signIn() {
		val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
			.setFilterByAuthorizedAccounts(true)
			.setServerClientId(Alice.decrypt(BuildConfig.CLIENT_KEY, "lt3(3x4R7M^107!&4E74Z%*o8cp2i7y@") ?: "")
			.setAutoSelectEnabled(true)
//			.setNonce(<nonce string to use when generating a Google ID token>)
			.build()

		val request: GetCredentialRequest = GetCredentialRequest.Builder()
			.addCredentialOption(googleIdOption)
			.build()

		lifecycleScope.launch {
			try {
				val credentialManager = CredentialManager.create(this@MainActivity)

				val result = credentialManager.getCredential(
					request = request,
					context = this@MainActivity,
				)
				handleSignIn(result)
			} catch (e: GetCredentialException) {
//				handleFailure(e)
				e.printStackTrace()
			}
		}
	}


	fun handleSignIn(result: GetCredentialResponse) {
		val credential = result.credential
		if(credential is CustomCredential) {
			if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
				try {

					val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
					val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

					auth.signInWithCredential(authCredential)
						.addOnCompleteListener(this) { task ->
							if (task.isSuccessful) {
								val user = auth.currentUser
								updateUI(user)
							} else {
								updateUI(null)
							}
						}
				} catch (e: GoogleIdTokenParsingException) {
					e.printStackTrace()
				}
			} else {
				Log.e(TAG, "Unexpected type of credential")
			}
		} else {
			Log.e(TAG, "Unexpected type of credential")
		}
	}

	private fun updateUI(user : FirebaseUser?) {
		if (user != null) {
			val dataStoreInstance = DataStoreInstance(this)
			dataStoreInstance.putIsFirstTime(false)

			Toast.makeText(this, "Hi ${user.displayName}", Toast.LENGTH_SHORT).show()
		} else {
			Toast.makeText(this, "Error signing in. Please try again later.", Toast.LENGTH_SHORT).show()
		}
	}

	private var dropboxServiceConnectionManager : DropboxServiceConnectionManager? = null

	private fun startSyncService() {
		if (dropboxServiceConnectionManager == null) {
			dropboxServiceConnectionManager = DropboxServiceConnectionManager(this) { dropboxService ->
				lifecycleScope.launch(Dispatchers.Default) {
					dropboxService.syncStatus.collect { syncStatus.tryEmit(it) }
				}
			}
		}
	}

	override fun onDestroy() {
		dropboxServiceConnectionManager?.unbindFromService()
		super.onDestroy()
	}

	companion object {
		const val TAG = "MainActivity"
		var isInStack = false
	}
}
