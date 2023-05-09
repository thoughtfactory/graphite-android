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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.LogInCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.notification.WriteNoteNotification
import com.syncodec.graphite.presentation.common.bar.GenericTopBar
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.settings.composable.bottomSheet.SettingsBottomSheetType
import com.syncodec.graphite.presentation.settings.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.settings.composable.dialog.SettingsDialog
import com.syncodec.graphite.presentation.settings.composable.dialog.SettingsDialogType
import com.syncodec.graphite.presentation.settings.composable.screen.BackupAndSyncScreen
import com.syncodec.graphite.presentation.settings.composable.screen.SettingsScreen
import com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen.ImportDataScreen
import com.syncodec.graphite.presentation.settings.composable.screen.localBackupScreen.LocalBackupScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.alice.Alice
import kotlinx.coroutines.Dispatchers
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

		setContent {
			BaseContent {
				val scope = rememberCoroutineScope()
				val _firebaseUser by this.firebaseUser
				val dataStoreInstance = remember { DataStoreInstance(context = this) }

				val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
				var bottomSheetType : SettingsBottomSheetType by remember { mutableStateOf(SettingsBottomSheetType.Profile) }

				fun openSheet(sheetType : SettingsBottomSheetType) {
					scope.launch { bottomSheetType = sheetType; modalBottomSheetState.show() }
				}

				fun closeSheet() {
					scope.launch { modalBottomSheetState.hide() }
				}

				var showBiometricDialog by remember { mutableStateOf(false) }
				var showExportDataDialog by remember { mutableStateOf(false) }
				var showClearDataDialog by remember { mutableStateOf(false) }
				var showNotificationPermissionDialog by remember { mutableStateOf(false) }
				var showDeleteAccountDialog by remember { mutableStateOf(false) }
				var showManageSubscriptionDialog by remember { mutableStateOf(false) }

				var settingsScreen by remember { mutableStateOf(SettingsScreen.Settings) }

				fun openDialog(dialogType : SettingsDialogType) = when (dialogType) {
					SettingsDialogType.Biometric -> showBiometricDialog = true
					SettingsDialogType.ExportData -> showExportDataDialog = true
					SettingsDialogType.ClearData -> showClearDataDialog = true
					SettingsDialogType.NotificationPermission -> showNotificationPermissionDialog = true
					SettingsDialogType.DeleteAccount -> showDeleteAccountDialog = true
					SettingsDialogType.ManageSubscription -> showManageSubscriptionDialog = true
				}

				fun closeDialog(dialogType : SettingsDialogType) = when (dialogType) {
					SettingsDialogType.Biometric -> showBiometricDialog = false
					SettingsDialogType.ExportData -> showExportDataDialog = false
					SettingsDialogType.ClearData -> showClearDataDialog = false
					SettingsDialogType.NotificationPermission -> showNotificationPermissionDialog = false
					SettingsDialogType.DeleteAccount -> showDeleteAccountDialog = false
					SettingsDialogType.ManageSubscription -> showManageSubscriptionDialog = false
				}

				this.onBackPressedDispatcher.addCallback(
					this, object : OnBackPressedCallback(true) {
						override fun handleOnBackPressed() {
							if (authenticatorScreen == AuthenticatorScreen.None) {
								when (settingsScreen) {
									SettingsScreen.Settings -> finish()
									SettingsScreen.BackupAndSync -> settingsScreen = SettingsScreen.Settings
									SettingsScreen.LocalBackup -> settingsScreen = SettingsScreen.BackupAndSync
									SettingsScreen.ImportData -> settingsScreen = SettingsScreen.Settings
								}
							} else {
								authenticatorScreen = AuthenticatorScreen.None
							}
						}
					}
				)

				GenericScaffold(
					topBar = {
						GenericTopBar(
							title = when (settingsScreen) {
								Companion.SettingsScreen.Settings -> "Settings"
								Companion.SettingsScreen.BackupAndSync -> "Backup & Sync"
								Companion.SettingsScreen.LocalBackup -> "Local Backup"
								Companion.SettingsScreen.ImportData -> "Import Data"
							}
						)
					},
					modalBottomSheetState = modalBottomSheetState,
					sheetContent = {
						SheetLayout(
							bottomSheetType = bottomSheetType,
							firebaseUser = _firebaseUser,
							signOut = ::onClickSignOut,
							closeSheet = ::closeSheet,
						)
					},
					dialogContent = {
						SettingsDialog(
							showBiometricDialog = showBiometricDialog,
							showExportDataDialog = showExportDataDialog,
							showClearDataDialog = showClearDataDialog,
							onAddBiometricAuth = {
								dataStoreInstance.putUseBiometric(true)
								closeDialog(SettingsDialogType.Biometric)
							},
							showNotificationPermissionDialog = showNotificationPermissionDialog,
							showDeleteAccountDialog = showDeleteAccountDialog,
							showManageSubscriptionDialog = showManageSubscriptionDialog,
							onNotificationPermissionAvailable = {
								if (BaseApplication.isPro.value) WriteNoteNotification.showSimpleNotification(applicationContext)
								else Toast.makeText(applicationContext, "Join Graphite Pro to access this feature", Toast.LENGTH_SHORT).show()
							},
							onDeleteAccount = {
								deleteAccount()
								closeDialog(SettingsDialogType.DeleteAccount)
							},
							closeDialog = ::closeDialog,
						)
					},
				) {
					AnimatedContent(
						targetState = settingsScreen,
						transitionSpec = {
							scaleIn(tween(300), initialScale = 0.71f) + fadeIn(tween(300)) with
									scaleOut(tween(300), targetScale = 0.71f) + fadeOut(tween(300))
						},
						label = "settings_screen_transition"
					) {
						when (it) {
							SettingsScreen.Settings -> SettingsScreen(
								firebaseUser = _firebaseUser,
								onClickSignIn = ::onClickSignIn,
								onClickSignOut = ::onClickSignOut,
								onClickDeleteAccount = { openDialog(SettingsDialogType.DeleteAccount) },
								navigateTo = { settingsScreen = it },
								openDialog = ::openDialog,
							)

							SettingsScreen.BackupAndSync -> BackupAndSyncScreen { settingsScreen = it }
							SettingsScreen.LocalBackup -> LocalBackupScreen(
								openDialog = ::openDialog,
								closeDialog = ::closeDialog,
							)

							SettingsScreen.ImportData -> ImportDataScreen(
								openDialog = ::openDialog,
							)
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

	private fun onClickSignIn() {
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

	private fun onClickSignOut() {
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

	private fun deleteAccount() {
		val auth = Firebase.auth
		val data = hashMapOf("uId" to auth.currentUser?.uid)
		FirebaseFunctions
			.getInstance()
			.getHttpsCallable("deleteAccount")
			.call(data)
			.addOnSuccessListener {
				lifecycleScope.launch(Dispatchers.Main) {
					val data = it.data as String
					if (data == "Ok") {
						Toast.makeText(this@SettingsActivity, "Your account is scheduled for deletion.", Toast.LENGTH_SHORT).show()
						onClickSignOut()
					} else if (data == "Error") {
						Toast.makeText(this@SettingsActivity, "Error deleting account. You can write us a mail about account deletion.", Toast.LENGTH_SHORT)
							.show()
					}
				}
			}
			.addOnFailureListener {
				lifecycleScope.launch(Dispatchers.Main) {
					Toast.makeText(this@SettingsActivity, "Error deleting account. You can write us a mail about account deletion.", Toast.LENGTH_SHORT).show()
				}
			}
	}

	companion object {
		enum class SettingsScreen {
			Settings,
			BackupAndSync,
			LocalBackup,
			ImportData,
		}

		enum class DarkTheme {
			SyncWithSystem,
			AlwaysOn,
			AlwaysOff
		}
	}
}
