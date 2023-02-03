package com.syncodec.graphite.presentation.settings

import android.content.IntentSender
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.LogInCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.settings.composable.bar.TopBar
import com.syncodec.graphite.presentation.settings.composable.bottomSheet.SettingsBottomSheetType
import com.syncodec.graphite.presentation.settings.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.settings.composable.dialog.SettingsDialog
import com.syncodec.graphite.presentation.settings.composable.dialog.SettingsDialogType
import com.syncodec.graphite.presentation.settings.composable.screen.BackupAndRestoreScreen
import com.syncodec.graphite.presentation.settings.composable.screen.localBackup.LocalBackupScreen
import com.syncodec.graphite.presentation.settings.composable.screen.SettingsScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.alice.Alice
import kotlinx.coroutines.launch


class SettingsActivity : ComponentActivity() {

	private lateinit var auth : FirebaseAuth
	private lateinit var oneTapClient : SignInClient
	private lateinit var signInRequest : BeginSignInRequest

	private var firebaseUser : MutableState<FirebaseUser?> = mutableStateOf(null)

	@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		auth = Firebase.auth

		this.firebaseUser.value = auth.currentUser
		oneTapClient = Identity.getSignInClient(this)
		signInRequest = BeginSignInRequest
			.builder()
			.setPasswordRequestOptions(
				BeginSignInRequest
					.PasswordRequestOptions
					.builder()
					.setSupported(true)
					.build()
			)
			.setGoogleIdTokenRequestOptions(
				GoogleIdTokenRequestOptions
					.builder()
					.setSupported(true)
					.setServerClientId(Alice.decrypt(BuildConfig.CLIENT_KEY, "lt3(3x4R7M^107!&4E74Z%*o8cp2i7y@") ?: "")
					.setFilterByAuthorizedAccounts(false)
					.build()
			)
			.setAutoSelectEnabled(false)
			.build()

		var authenticatorScreen by mutableStateOf(AuthenticatorScreen.None)
		val authenticatorAction : (AuthenticatorScreen) -> Unit = { authenticatorScreen = it }

		setContent {
			CompositionLocalProvider(
				LocalAuthenticatorAction provides authenticatorAction
			) {
				BaseContent {
					val scope = rememberCoroutineScope()
					val systemUiController = rememberSystemUiController()
					systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
					systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

					val _firebaseUser by this.firebaseUser

					val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
					var bottomSheetType : SettingsBottomSheetType by remember { mutableStateOf(SettingsBottomSheetType.PROFILE) }

					fun openSheet(_bottomSheetType : SettingsBottomSheetType) {
						scope.launch { bottomSheetType = _bottomSheetType; modalBottomSheetState.show() }
					}

					fun closeSheet() {
						scope.launch { modalBottomSheetState.hide() }
					}

					var showImportDataDialog by remember { mutableStateOf(false) }
					var showExportDataDialog by remember { mutableStateOf(false) }
					var showClearDataDialog by remember { mutableStateOf(false) }
					var showNotificationPermissionDialog by remember { mutableStateOf(false) }
					var _showDeleteAccountDialog by remember { mutableStateOf(false) }

					var settingsScreen by remember { mutableStateOf(SettingsScreen.SETTINGS) }

					fun openDialog(dialogType : SettingsDialogType, data : Any?) {
						when (dialogType) {
							SettingsDialogType.IMPORT_DATA -> showImportDataDialog = true
							SettingsDialogType.EXPORT_DATA -> showExportDataDialog = true
							SettingsDialogType.CLEAR_DATA -> showClearDataDialog = true
							SettingsDialogType.NOTIFICATION_PERMISSION -> showNotificationPermissionDialog = true
							SettingsDialogType.DELETE_ACCOUNT -> _showDeleteAccountDialog = true
						}
					}

					fun closeDialog(dialogType : SettingsDialogType) {
						when (dialogType) {
							SettingsDialogType.IMPORT_DATA -> showImportDataDialog = false
							SettingsDialogType.EXPORT_DATA -> showExportDataDialog = false
							SettingsDialogType.CLEAR_DATA -> showClearDataDialog = false
							SettingsDialogType.NOTIFICATION_PERMISSION -> showNotificationPermissionDialog = false
							SettingsDialogType.DELETE_ACCOUNT -> _showDeleteAccountDialog = false
						}
					}

					this.onBackPressedDispatcher.addCallback(
						this, object : OnBackPressedCallback(true) {
							override fun handleOnBackPressed() {
								if (authenticatorScreen == AuthenticatorScreen.None) {
									when (settingsScreen) {
										SettingsScreen.SETTINGS -> finish()
										SettingsScreen.BACKUP_AND_RESTORE -> settingsScreen = SettingsScreen.SETTINGS
										SettingsScreen.LOCAL_BACKUP -> settingsScreen = SettingsScreen.BACKUP_AND_RESTORE
									}
								} else {
									authenticatorScreen = AuthenticatorScreen.None
								}
							}
						}
					)

					CompositionLocalProvider(
						LocalFirebaseUser provides _firebaseUser,
						LocalSignIn provides ::signIn,
						LocalSignOut provides ::signOut,
						LocalDeleteAccount provides {
							closeSheet()
							openDialog(SettingsDialogType.DELETE_ACCOUNT, null)
						},
						LocalShowImportDataDialog provides showImportDataDialog,
						LocalShowExportDataDialog provides showExportDataDialog,
						LocalShowClearDataDialog provides showClearDataDialog,
						LocalShowNotificationPermissionDialog provides showNotificationPermissionDialog,
						LocalShowDeleteAccountDialog provides _showDeleteAccountDialog,
						LocalOpenBottomSheet provides ::openSheet,
						LocalCloseBottomSheet provides ::closeSheet,
						LocalOpenDialog provides ::openDialog,
						LocalCloseDialog provides ::closeDialog,
					) {
						GenericScaffold(
							modalBottomSheetState = modalBottomSheetState,
							sheetContent = { SheetLayout(bottomSheetType = bottomSheetType) { closeSheet() } },
							topBar = { TopBar { super.getOnBackPressedDispatcher().onBackPressed() } },
							dialogContent = { SettingsDialog() },
						) {
							AnimatedContent(
								targetState = settingsScreen,
								transitionSpec = { scaleIn(tween(300), initialScale = 0.71f) + fadeIn(tween(300)) with scaleOut(tween(300), targetScale = 0.71f) + fadeOut(tween(300)) }
							) {
								when (it) {
									SettingsScreen.SETTINGS -> SettingsScreen { settingsScreen = it }
									SettingsScreen.BACKUP_AND_RESTORE -> BackupAndRestoreScreen { settingsScreen = it }
									SettingsScreen.LOCAL_BACKUP -> LocalBackupScreen()
								}
							}
						}
					}
				}
			}
		}
	}

	private val signInIntentResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
		if (result.data != null) {
			try {
				val googleCredential = oneTapClient.getSignInCredentialFromIntent(result.data)
				val displayName = googleCredential.displayName
				val idToken = googleCredential.googleIdToken

				if (idToken == null) {
					Toast.makeText(this, "Error signing in. Please try again later.", Toast.LENGTH_SHORT).show()
				} else {
					val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
					auth.signInWithCredential(firebaseCredential)
						.addOnSuccessListener {
							this.firebaseUser.value = auth.currentUser
							Toast.makeText(this, "Signed in as $displayName", Toast.LENGTH_SHORT).show()

							Purchases
								.sharedInstance
								.apply {
									setAttributes(mapOf("\$email" to auth.currentUser?.email))
									logIn(
										newAppUserID = auth.currentUser !!.uid,
										callback = object : LogInCallback {
											override fun onError(error : PurchasesError) {
											}

											override fun onReceived(customerInfo : CustomerInfo, created : Boolean) {
												BaseApplication.isPro.tryEmit(customerInfo.entitlements["pro"]?.isActive == true)
											}
										}
									)
								}
						}
						.addOnFailureListener {
							Toast.makeText(this, "Error signing in. Please try again later.", Toast.LENGTH_SHORT).show()
						}
				}
			} catch (e : ApiException) {
//				e.printStackTrace()
				Toast.makeText(this, "Error signing in. Please try again later.", Toast.LENGTH_SHORT).show()
			}
		}
	}

	private fun signIn() {
		Toast.makeText(this, "Signing in...", Toast.LENGTH_SHORT).show()
		oneTapClient.beginSignIn(signInRequest)
			.addOnSuccessListener(this) { result ->
				try {
					IntentSenderRequest.Builder(result.pendingIntent.intentSender).build().let {
						signInIntentResultLauncher.launch(it)
					}
				} catch (e : IntentSender.SendIntentException) {
//					e.printStackTrace()
				}
			}
			.addOnFailureListener(this) { e ->
//				e.printStackTrace()
				Toast.makeText(this, "Error signing in. Please try again later.", Toast.LENGTH_SHORT).show()
			}
	}

	private fun signOut() {
		val dataStoreInstance = DataStoreInstance(this)
		auth.signOut()
		this.firebaseUser.value = null
		dataStoreInstance.putSuperExpiryTime(0)
		Purchases.sharedInstance.logOut(
			callback = object : ReceiveCustomerInfoCallback {
				override fun onError(error : PurchasesError) {
				}

				override fun onReceived(customerInfo : CustomerInfo) {
					BaseApplication.isPro.tryEmit(customerInfo.entitlements["pro"]?.isActive == true)
				}
			}
		)
	}

	companion object {
		val LocalFirebaseUser = compositionLocalOf<FirebaseUser?> { null }
		val LocalSignIn = compositionLocalOf { {} }
		val LocalSignOut = compositionLocalOf { {} }
		val LocalDeleteAccount = compositionLocalOf { {} }

		val LocalShowImportDataDialog = compositionLocalOf { false }
		val LocalShowExportDataDialog = compositionLocalOf { false }
		val LocalShowClearDataDialog = compositionLocalOf { false }
		val LocalShowNotificationPermissionDialog = compositionLocalOf { false }
		val LocalShowDeleteAccountDialog = compositionLocalOf { false }

		val LocalOpenBottomSheet = compositionLocalOf<(SettingsBottomSheetType) -> Unit> { {} }
		val LocalCloseBottomSheet = compositionLocalOf { { } }
		val LocalOpenDialog = compositionLocalOf<(SettingsDialogType, Any?) -> Unit> { { _, _ -> } }
		val LocalCloseDialog = compositionLocalOf<(SettingsDialogType) -> Unit> { {} }

		enum class SettingsScreen {
			SETTINGS,
			BACKUP_AND_RESTORE,
			LOCAL_BACKUP
		}

		enum class DarkTheme {
			SYNC_WITH_SYSTEM,
			ALWAYS_ON,
			ALWAYS_OFF
		}
	}
}
