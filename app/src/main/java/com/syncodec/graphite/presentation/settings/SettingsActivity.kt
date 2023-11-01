package com.syncodec.graphite.presentation.settings

import android.content.IntentSender
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import com.syncodec.graphite.presentation.settings.composable.screen.backupAndSyncScreen.BackUpAndSyncScreen
import com.syncodec.graphite.presentation.settings.composable.screen.dataScreen.DataScreen
import com.syncodec.graphite.presentation.settings.composable.screen.PreferenceScreen
import com.syncodec.graphite.presentation.settings.composable.screen.SecurityScreen
import com.syncodec.graphite.presentation.settings.composable.screen.SettingsScreen
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.presentation.settings.composable.screen.AboutScreen
import com.syncodec.graphite.presentation.settings.composable.screen.ExtensionScreen
import com.syncodec.graphite.presentation.settings.composable.screen.backupAndSyncScreen.LocalBackupScreen
import com.syncodec.graphite.presentation.settings.composable.screen.dataScreen.ImportDataScreen
import com.syncodec.graphite.utils.alice.Alice
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingsActivity : ComponentActivity() {

	private lateinit var auth: FirebaseAuth
	private lateinit var oneTapClient: SignInClient
	private lateinit var signInRequest: BeginSignInRequest

	private var firebaseUser: MutableState<FirebaseUser?> = mutableStateOf(null)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

//		window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

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

		val hasExtrasScreen = intent.hasExtra(Extras.SettingsScreen.name)
		var extrasScreen: SettingsScreen? = null
		if (hasExtrasScreen) {
			val extrasScreenString = intent.getStringExtra(Extras.SettingsScreen.name)
			if (extrasScreenString.isNullOrBlank()) Toast.makeText(this, "Error navigating to screen", Toast.LENGTH_SHORT).show()
			else extrasScreen = SettingsScreen.entries.find { it.name == extrasScreenString }
		}

		setContent {
			BaseComposable {

				val firebaseUser by this.firebaseUser

				val navController = rememberNavController()
				NavHost(
					navController = navController,
					startDestination = SettingsScreen.Settings.name,
					modifier = Modifier.background(MaterialTheme.colorScheme.background)
				) {
					composable(SettingsScreen.Settings.name) {
						SettingsScreen(
							firebaseUser = firebaseUser,
							onClickSignIn = { onClickSignIn() },
							onClickSignOut = { onClickSignOut() },
							onClickDeleteAccount = { deleteAccount() },
							onNavigate = { navController.navigate(it.name) }
						)
					}
					composable(SettingsScreen.Preferences.name) { PreferenceScreen() }
					composable(SettingsScreen.Security.name) { SecurityScreen() }
					composable(SettingsScreen.Data.name) { DataScreen { navController.navigate(it.name) } }
					composable(SettingsScreen.ImportData.name) { ImportDataScreen() }
					composable(SettingsScreen.BackUpAndSync.name) { BackUpAndSyncScreen { navController.navigate(it.name) } }
					composable(SettingsScreen.LocalBackup.name) { LocalBackupScreen() }
					composable(SettingsScreen.Extension.name) { ExtensionScreen() }
					composable(SettingsScreen.About.name) { AboutScreen() }
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
										newAppUserID = auth.currentUser!!.uid,
										callback = object : LogInCallback {
											override fun onError(error: PurchasesError) {
											}

											override fun onReceived(customerInfo: CustomerInfo, created: Boolean) {
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
			} catch (e: ApiException) {
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
				} catch (e: IntentSender.SendIntentException) {
					e.printStackTrace()
				}
			}
			.addOnFailureListener(this) { e ->
				e.printStackTrace()
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
				override fun onError(error: PurchasesError) {
				}

				override fun onReceived(customerInfo: CustomerInfo) {
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

		enum class Extras {
			SettingsScreen,
		}

		enum class SettingsScreen {
			Settings,
			Account,
			Preferences,
			Security,
			Data,
			ImportData,
			ExportData,
			BackUpAndSync,
			LocalBackup,
			Extension,
			About,
		}

		enum class DarkTheme {
			SyncWithSystem,
			AlwaysOn,
			AlwaysOff
		}
	}
}
