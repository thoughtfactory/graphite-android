package com.syncodec.graphite.presentation.settings

import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import com.syncodec.graphite.presentation.main.composable.bottomSheet.MainBottomSheetType
import com.syncodec.graphite.presentation.settings.composable.bar.TopBar
import com.syncodec.graphite.presentation.settings.composable.bottomSheet.SettingsBottomSheetType
import com.syncodec.graphite.presentation.settings.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.settings.composable.screen.BackupAndRestoreScreen
import com.syncodec.graphite.presentation.settings.composable.screen.PreferencesScreen
import com.syncodec.graphite.presentation.settings.composable.screen.SecurityScreen
import com.syncodec.graphite.presentation.settings.composable.screen.SettingsScreen
import com.syncodec.graphite.presentation.settings.composable.screen.SynchronizationScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Authenticator
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import kotlinx.coroutines.launch


class SettingsActivity : ComponentActivity() {

	private lateinit var auth : FirebaseAuth
	private lateinit var oneTapClient : SignInClient
	private lateinit var signInRequest : BeginSignInRequest

	val authenticator : MutableState<Authenticator> = mutableStateOf(Authenticator.NONE)

	val navigator : MutableState<Navigator> = mutableStateOf(Navigator.BASE)

	@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

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
					.setServerClientId(BuildConfig.CLIENT_KEY)
					.setFilterByAuthorizedAccounts(false)
					.build()
			)
			.setAutoSelectEnabled(false)
			.build()

		var authenticator by this.authenticator
		val authenticatorAction : (Authenticator) -> Unit = { authenticator = it }

		setContent {
			CompositionLocalProvider(
				LocalAuthenticatorAction provides authenticatorAction
			) {
				BaseContent(
					authenticator = authenticator,
				) {
					val scope = rememberCoroutineScope()
					val systemUiController = rememberSystemUiController()
					systemUiController.setStatusBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)
					systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

					val baseScrollState = rememberScrollState()
					val preferencesScrollState = rememberScrollState()
					val securityScrollState = rememberScrollState()
					val backupScrollState = rememberScrollState()

					val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
					var bottomSheetType : SettingsBottomSheetType by remember { mutableStateOf(SettingsBottomSheetType.PROFILE) }
					val closeSheet = { scope.launch { modalBottomSheetState.hide() } }
					val openSheet = { scope.launch { modalBottomSheetState.show() } }

					val navigator by this.navigator

					val title = when (navigator) {
						Navigator.BASE -> "Settings"
						Navigator.PREFERENCES -> "Preferences"
						Navigator.SECURITY -> "Security"
						Navigator.EXTENSIONS -> "Extensions"
						Navigator.BACKUP -> "Backup & Restore"
						Navigator.SYNC -> "Synchronization"
						Navigator.ABOUT -> "About Us"
					}

					ModalBottomSheetLayout(
						sheetContent = {
							SheetLayout(bottomSheetType = bottomSheetType) { closeSheet() }
						},
						sheetState = modalBottomSheetState,
						sheetElevation = 0.dp,
						sheetBackgroundColor = Color.Transparent,
						modifier = Modifier.fillMaxSize(),
					) {
						Scaffold(
							modifier = Modifier.fillMaxSize(),
							topBar = {
								TopBar(
									title = title,
									scrollState = when (navigator) {
										Navigator.BASE -> baseScrollState
										Navigator.PREFERENCES -> preferencesScrollState
										Navigator.SECURITY -> securityScrollState
										Navigator.BACKUP -> backupScrollState
										else -> baseScrollState
									},
								) { this.onBackPressed() }
							}
						) {
							Box(
								modifier = Modifier
									.fillMaxSize()
									.padding(it)
							) {
								AnimatedContent(targetState = navigator) {
									when (it) {
										Navigator.BASE -> SettingsScreen(
											scrollState = baseScrollState,
											currentUser = auth.currentUser,
											onClickLogin = this@SettingsActivity::signIn,
										) {
											bottomSheetType = SettingsBottomSheetType.PROFILE; openSheet()
										}

										Navigator.PREFERENCES -> PreferencesScreen(scrollState = preferencesScrollState)
										Navigator.SECURITY -> SecurityScreen(scrollState = securityScrollState)
										Navigator.EXTENSIONS -> null
										Navigator.BACKUP -> BackupAndRestoreScreen(scrollState = backupScrollState)
										Navigator.SYNC -> SynchronizationScreen()
										Navigator.ABOUT -> null
									}
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
								Log.d("npr71", "signInWithCredential:success")
								val user = auth.currentUser
								updateUI(user)
							} else {
								Log.w("npr71", "signInWithCredential:failure", task.exception)
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
					Log.e("npr71", "Couldn't start One Tap UI: ${e.localizedMessage}")
				}
			}
			.addOnFailureListener(this) { e ->
				Toast.makeText(this, "Error signing in. Please try again later.", Toast.LENGTH_SHORT).show()
			}
	}

	private fun updateUI(user : FirebaseUser?) {
	}

	@Deprecated("Must be removed in the next release")
	override fun onBackPressed() {
		if (authenticator.value == Authenticator.NONE) {
			if (navigator.value != Navigator.BASE) {
				navigator.value = Navigator.BASE
			} else {
				super.onBackPressed()
			}
		} else {
			authenticator.value = Authenticator.NONE
		}
	}

	companion object {
		enum class Navigator {
			BASE,
			PREFERENCES,
			SECURITY,
			EXTENSIONS,
			BACKUP,
			SYNC,
			ABOUT
		}
	}
}
