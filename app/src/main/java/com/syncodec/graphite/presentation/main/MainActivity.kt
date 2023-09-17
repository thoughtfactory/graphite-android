package com.syncodec.graphite.presentation.main

import android.app.KeyguardManager
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.cloud.dropbox.DBox
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.main.composable.screen.FirstTimeScreen
import com.syncodec.graphite.presentation.main.composable.screen.MainScreen
import com.syncodec.graphite.presentation.main.composable.screen.RepositoryLockedScreen
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.service.syncInator.DropboxSyncServiceConnectionManager
import com.syncodec.graphite.service.syncInator.GDriveSyncServiceConnectionManager
import com.syncodec.graphite.service.syncInator.SyncInatorService
import com.syncodec.graphite.utils.alice.Alice
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.dataStore.SyncDataStoreInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : ComponentActivity() {

	private val viewModel by viewModel<MainViewModel>()

	private lateinit var auth: FirebaseAuth
	private lateinit var oneTapClient: SignInClient
	private lateinit var signInRequest: BeginSignInRequest

	private var cancellationSignal: CancellationSignal? = null

	private var biometricErrorMessage: MutableState<String?> = mutableStateOf(null)

	private val syncStatus = MutableStateFlow<SyncInatorService.Companion.SyncStatus>(SyncInatorService.Companion.SyncStatus.Init)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		isInStack = true

		val dataStoreInstance = DataStoreInstance(this)

		auth = Firebase.auth
		oneTapClient = Identity.getSignInClient(this)
		signInRequest = BeginSignInRequest.builder()
			.setPasswordRequestOptions(
				BeginSignInRequest.PasswordRequestOptions.builder()
					.setSupported(true)
					.build()
			)
			.setGoogleIdTokenRequestOptions(
				BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
					.setSupported(true)
					.setServerClientId(Alice.decrypt(BuildConfig.CLIENT_KEY, "lt3(3x4R7M^107!&4E74Z%*o8cp2i7y@") ?: "")
					.setFilterByAuthorizedAccounts(false)
					.build()
			)
			.setAutoSelectEnabled(false)
			.build()

		setContent {
			BaseComposable {
				val isFirstTime by dataStoreInstance.getIsFirstTime.collectAsState(initial = null)
				val isBiometricsEnabled by dataStoreInstance.getUseBiometric().collectAsState(initial = null)
				var isBiometricUsed by remember { mutableStateOf(false) }

				val systemUiController = rememberSystemUiController()
				if (isFirstTime == true) systemUiController.setStatusBarColor(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
				else systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)

				LaunchedEffect(key1 = isBiometricsEnabled) {
					if (isBiometricsEnabled != null && !isBiometricUsed) {
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
				val testConnectionResponse by viewModel.testConnectionResponse.collectAsState(initial = DBox.Companion.TestConnectionResponse.Loading)

				val syncDataStore = remember { SyncDataStoreInstance(this@MainActivity) }
				val syncProvider by syncDataStore.syncProvider.collectAsState(initial = null)
				LaunchedEffect(key1 = testConnectionResponse) { startSyncService(syncProvider) }
				LaunchedEffect(key1 = syncProvider) { syncProvider?.let { viewModel.testRemoteConnection(it) } }

				AnimatedContent(
					targetState = isFirstTime,
					transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(animationSpec = tween(300)) },
					modifier = Modifier.fillMaxSize(),
					label = "isFirstTime"
				) {
					when (it) {
						true -> FirstTimeScreen(onClickLogin = this@MainActivity::signIn)
						false -> AnimatedContent(
							targetState = repositoryState,
							transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(animationSpec = tween(300)) },
							modifier = Modifier.fillMaxSize(),
							label = "repositoryState_animation"
						) {
							when (it) {
								Repository.Companion.RepositoryState.Locked -> RepositoryLockedScreen(
									errorMessage = biometricErrorMessage,
									onUnlock = { this@MainActivity.launchBiometric() }
								)

								Repository.Companion.RepositoryState.Success -> MainScreen(
									syncStatus = syncStatus,
									testConnectionResponse = testConnectionResponse,
									testDropboxConnection = { viewModel.testRemoteConnection(syncProvider) },
									onClickSyncNow = { syncProvider?.onClickSyncNow() },
									onClickForceSync = { syncProvider?.onClickForceSync() },
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

	private fun checkBiometricSupport(): Boolean {
		val keyGuardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

		if (!keyGuardManager.isDeviceSecure) {
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
					override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
						super.onAuthenticationSucceeded(result)
						Toast.makeText(this@MainActivity, "Authentication Succeeded", Toast.LENGTH_SHORT).show()
						viewModel.onAuthenticate(applicationContext)
					}

					override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
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

					override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
						super.onAuthenticationHelp(helpCode, helpString)
					}
				}
			)
		}
	}

	private fun getCancellationSignal(): CancellationSignal {
		cancellationSignal = CancellationSignal()
		cancellationSignal?.setOnCancelListener {
			Toast.makeText(this, "Authentication Cancelled Signal", Toast.LENGTH_SHORT).show()
		}

		return cancellationSignal as CancellationSignal
	}


	private val signInIntentResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
		if (result.data != null) {
			try {

				val googleCredential = oneTapClient.getSignInCredentialFromIntent(result.data)
//				val displayName = googleCredential.displayName
//				val username = googleCredential.id
//				val password = googleCredential.password
				val idToken = googleCredential.googleIdToken
//				val profilePictureUri = googleCredential.profilePictureUri

				if (idToken == null) {
					Toast.makeText(this, "Error signing in. Please try again later.", Toast.LENGTH_SHORT).show()
				} else {
					val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
					auth.signInWithCredential(firebaseCredential)
						.addOnCompleteListener(this) { task ->
							if (task.isSuccessful) {
								val user = auth.currentUser
								updateUI(user)
							} else {
								updateUI(null)
							}
						}
				}
			} catch (e: ApiException) {
//					e.printStackTrace()
				Toast.makeText(this, "Error signing in. Please try again later.", Toast.LENGTH_SHORT).show()
			}
		}
	}

	private fun signIn() {
		oneTapClient.beginSignIn(signInRequest)
			.addOnSuccessListener(this) { result ->
				try {
					IntentSenderRequest.Builder(result.pendingIntent.intentSender).build().let {
						signInIntentResultLauncher.launch(it)
					}
				} catch (e: IntentSender.SendIntentException) {
				}
			}
			.addOnFailureListener(this) { e ->
				Toast.makeText(this, "Error signing in. Please try again later.", Toast.LENGTH_SHORT).show()
			}
	}

	private fun updateUI(user: FirebaseUser?) {
		if (user != null) {
			val dataStoreInstance = DataStoreInstance(this)
			dataStoreInstance.putIsFirstTime(false)

			Toast.makeText(this, "Hi ${user.displayName}", Toast.LENGTH_SHORT).show()
		} else {
			Toast.makeText(this, "Error signing in. Please try again later.", Toast.LENGTH_SHORT).show()
		}
	}

	private var dropboxServiceConnectionManager: DropboxSyncServiceConnectionManager? = null
	private var gDriveSyncServiceConnectionManager: GDriveSyncServiceConnectionManager? = null

	private fun startSyncService(syncProvider: SyncDataStoreInstance.Companion.SyncProvider?) {
		when (syncProvider) {
			SyncDataStoreInstance.Companion.SyncProvider.Dropbox -> {
				if (dropboxServiceConnectionManager == null) {
					dropboxServiceConnectionManager = DropboxSyncServiceConnectionManager(applicationContext) { syncInator ->
						lifecycleScope.launch(Dispatchers.Default) { syncInator.syncStatus.collect { syncStatus.tryEmit(it) } }
					}
					gDriveSyncServiceConnectionManager?.service?.hardCutOff()
					gDriveSyncServiceConnectionManager?.unbindFromService()
					gDriveSyncServiceConnectionManager = null
				}
			}

			SyncDataStoreInstance.Companion.SyncProvider.GoogleDrive -> {
				if (gDriveSyncServiceConnectionManager == null) {
					gDriveSyncServiceConnectionManager = GDriveSyncServiceConnectionManager(applicationContext) { syncInator ->
						lifecycleScope.launch(Dispatchers.Default) { syncInator.syncStatus.collect { syncStatus.tryEmit(it) } }
					}
					dropboxServiceConnectionManager?.service?.hardCutOff()
					dropboxServiceConnectionManager?.unbindFromService()
					dropboxServiceConnectionManager = null
				}
			}

			else -> {
				dropboxServiceConnectionManager?.service?.hardCutOff()
				dropboxServiceConnectionManager?.unbindFromService()
				dropboxServiceConnectionManager = null

				gDriveSyncServiceConnectionManager?.service?.hardCutOff()
				gDriveSyncServiceConnectionManager?.unbindFromService()
				gDriveSyncServiceConnectionManager = null
			}
		}
	}

	private fun SyncDataStoreInstance.Companion.SyncProvider.onClickSyncNow() {
//		when (this) {
//			SyncDataStoreInstance.Companion.SyncProvider.Dropbox -> dropboxServiceConnectionManager?.service?.onClickSyncNow()
//			SyncDataStoreInstance.Companion.SyncProvider.GoogleDrive -> gDriveSyncServiceConnectionManager?.service?.onClickSyncNow()
//			else -> null
//		}
	}

	private fun SyncDataStoreInstance.Companion.SyncProvider.onClickForceSync() {
//		when (this) {
//			SyncDataStoreInstance.Companion.SyncProvider.Dropbox -> dropboxServiceConnectionManager?.service?.onClickForceSync()
//			SyncDataStoreInstance.Companion.SyncProvider.GoogleDrive -> gDriveSyncServiceConnectionManager?.service?.onClickForceSync()
//			else -> null
//		}
	}

	override fun onDestroy() {
		dropboxServiceConnectionManager?.unbindFromService()
		gDriveSyncServiceConnectionManager?.unbindFromService()
		super.onDestroy()
	}

	companion object {
		var isInStack = false
	}
}
