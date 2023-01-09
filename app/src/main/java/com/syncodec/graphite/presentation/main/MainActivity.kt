package com.syncodec.graphite.presentation.main

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
import com.syncodec.graphite.di.repository.RepositoryState
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.main.composable.LocalCompositionCloseDialog
import com.syncodec.graphite.presentation.main.composable.LocalCompositionIsBucketRefreshing
import com.syncodec.graphite.presentation.main.composable.LocalCompositionIsNoteRefreshing
import com.syncodec.graphite.presentation.main.composable.LocalCompositionIsNotebookRefreshing
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnAddDebugData
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnDelete
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnExit
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnForceSync
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnRefresh
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnReorderBucketList
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnSyncNow
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.main.composable.LocalCompositionPutBucket
import com.syncodec.graphite.presentation.main.composable.LocalCompositionShowDeleteDialog
import com.syncodec.graphite.presentation.main.composable.LocalCompositionShowExitDialog
import com.syncodec.graphite.presentation.main.composable.LocalCompositionShowNotificationPermissionDialog
import com.syncodec.graphite.presentation.main.composable.LocalCompositionSyncStatus
import com.syncodec.graphite.presentation.main.composable.LocalCompositionTagList
import com.syncodec.graphite.presentation.main.composable.bar.BottomNavigationItem
import com.syncodec.graphite.presentation.main.composable.dialog.MainDialogType
import com.syncodec.graphite.presentation.main.composable.screen.FirstTimeScreen
import com.syncodec.graphite.presentation.main.composable.screen.MainScreen
import com.syncodec.graphite.presentation.main.composable.screen.RepositoryLockedScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.service.DropboxSyncService
import com.syncodec.graphite.service.DropboxSyncServiceConnectionManager
import com.syncodec.graphite.service.DropboxSyncStatus
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.alice.Alice
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

	private val viewModel by viewModels<MainViewModel>()

	private lateinit var auth : FirebaseAuth
	private lateinit var oneTapClient : SignInClient
	private lateinit var signInRequest : BeginSignInRequest

	private var cancellationSignal : CancellationSignal? = null

	private var biometricErrorMessage : MutableState<String?> = mutableStateOf(null)

	private var syncStatus: MutableState<DropboxSyncStatus> = mutableStateOf(DropboxSyncStatus.INIT)

	@OptIn(ExperimentalAnimationApi::class)
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)
		isInStack = true

//		startSyncService()

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
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
				systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

				val isFirstTime by dataStoreInstance.getIsFirstTime.collectAsState(initial = null)
				val useBiometric by dataStoreInstance.getUseBiometric().collectAsState(initial = null)
				var isUsedBiometric by remember { mutableStateOf(false) }

				LaunchedEffect(key1 = useBiometric) {
					if (useBiometric != null && ! isUsedBiometric) {
						if (useBiometric == true) launchBiometric()
						else viewModel.onAuthenticate()
						isUsedBiometric = true
					}
				}

				val repositoryState by viewModel.repositoryState.collectAsState(initial = null)

				val tagList = viewModel.tagList

				val isNoteRefreshing by viewModel.isNoteRefreshing
				val isBucketRefreshing by viewModel.isBucketRefreshing
				val isNotebookRefreshing by viewModel.isNotebookRefreshing

				var isSelected by viewModel.isSelected
				val selectedRealmUUIDList = viewModel.selectedObjectIdList

				var showNotificationPermissionDialog by remember { mutableStateOf(false) }
				var showDeleteDialog by viewModel.showDeleteDialog
				var showExitDialog by viewModel.showExitDialog

				val navController = rememberNavController()
				val navBackStackEntry by navController.currentBackStackEntryAsState()
				val currentRoute = navBackStackEntry?.destination?.route

				val biometricErrorMessage by this.biometricErrorMessage
				val _syncStatus by this.syncStatus

				fun openDialog(_mainDialogType : MainDialogType) {
					when (_mainDialogType) {
						MainDialogType.NOTIFICATION_PERMISSION -> showNotificationPermissionDialog = true
						MainDialogType.DELETE -> showDeleteDialog = true
						MainDialogType.EXIT -> showExitDialog = true
					}
				}

				fun closeDialog(_mainDialogType : MainDialogType) {
					when (_mainDialogType) {
						MainDialogType.NOTIFICATION_PERMISSION -> showNotificationPermissionDialog = false
						MainDialogType.DELETE -> showDeleteDialog = false
						MainDialogType.EXIT -> showExitDialog = false
					}
				}

				this.onBackPressedDispatcher.addCallback(
					this, object : OnBackPressedCallback(true) {
						override fun handleOnBackPressed() {
							if (isSelected) {
								selectedRealmUUIDList.clear()
								isSelected = false
							} else {
								if (currentRoute == BottomNavigationItem.Home.route) {
									openDialog(MainDialogType.EXIT)
								} else {
									navController.popBackStack(route = BottomNavigationItem.Home.route, inclusive = false, saveState = true)
								}
							}
						}
					}
				)

				CompositionLocalProvider(
					LocalCompositionSyncStatus provides _syncStatus,
					LocalCompositionTagList provides tagList,
					LocalCompositionIsNoteRefreshing provides isNoteRefreshing,
					LocalCompositionIsBucketRefreshing provides isBucketRefreshing,
					LocalCompositionIsNotebookRefreshing provides isNotebookRefreshing,
					LocalCompositionOnRefresh provides { viewModel.refresher.value = viewModel.refresher.value + 1 },
					LocalCompositionOnSyncNow provides {
//						dropboxSyncService?.onSync()
													   },
					LocalCompositionOnForceSync provides { },
					LocalCompositionIsSelected provides isSelected,
					LocalCompositionOnSelect provides { isSelected = it },
					LocalCompositionSelectedObjectIdList provides selectedRealmUUIDList,
					LocalCompositionPutBucket provides viewModel::putBucket,
					LocalCompositionOnReorderBucketList provides viewModel::onReorderBucketList,
					LocalCompositionOpenDialog provides ::openDialog,
					LocalCompositionCloseDialog provides ::closeDialog,
					LocalCompositionShowNotificationPermissionDialog provides showNotificationPermissionDialog,
					LocalCompositionShowDeleteDialog provides showDeleteDialog,
					LocalCompositionShowExitDialog provides showExitDialog,
					LocalCompositionOnDelete provides viewModel::delete,
					LocalCompositionOnExit provides {
						finishAndRemoveTask()
						viewModel.onDeauthenticate()
					},
					LocalCompositionOnAddDebugData provides {},
				) {
					AnimatedContent(
						targetState = isFirstTime,
						transitionSpec = { fadeIn(tween(300)) with fadeOut(animationSpec = tween(300)) },
						modifier = Modifier.fillMaxSize()
					) {
						when (it) {
							true -> FirstTimeScreen(onClickLogin = this@MainActivity::signIn)
							false -> AnimatedContent(
								targetState = repositoryState,
								transitionSpec = { fadeIn(tween(300)) with fadeOut(animationSpec = tween(300)) },
								modifier = Modifier.fillMaxSize()
							) {
								when (it) {
									RepositoryState.LOCKED -> RepositoryLockedScreen(
										errorMessage = biometricErrorMessage,
										onUnlock = { this@MainActivity.launchBiometric() }
									)

									RepositoryState.SUCCESS -> MainScreen(
										viewModel = viewModel,
										currentRoute = currentRoute,
										navController = navController,
									)

									RepositoryState.ERROR -> RepositoryLockedScreen(
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
	}

	override fun onResume() {
		super.onResume()

		viewModel.refresher.value = viewModel.refresher.value + 1
	}

	override fun onDestroy() {
		isInStack = false

//		dropboxServiceConnection.unbindFromService()
		super.onDestroy()
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
						viewModel.onAuthenticate()
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
								viewModel.onAuthenticate()
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


	private val signInIntentResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
		if (result.data != null) {
			try {

				val googleCredential = oneTapClient.getSignInCredentialFromIntent(result.data)
				val displayName = googleCredential.displayName
				val username = googleCredential.id
				val password = googleCredential.password
				val idToken = googleCredential.googleIdToken
				val profilePictureUri = googleCredential.profilePictureUri

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
			} catch (e : ApiException) {
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
				} catch (e : IntentSender.SendIntentException) {
				}
			}
			.addOnFailureListener(this) { e ->
				Toast.makeText(this, "Error signing in. Please try again later.", Toast.LENGTH_SHORT).show()
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

//	var dropboxSyncService : DropboxSyncService? = null
//	private val dropboxServiceConnection = DropboxSyncServiceConnectionManager(this) {
//		dropboxSyncService = it
//		it?.let {
//			CoroutineScope(Dispatchers.Main).launch {
//				it.dropboxSyncStatus.collect {
//					syncStatus.value = it
//				}
//			}
//		}
//	}

//	private fun startSyncService() {
//		Intent(this.applicationContext, DropboxSyncService::class.java).apply {
//			dropboxServiceConnection.bindToService()
//		}
//	}

	companion object {
		var isInStack = false
	}
}
