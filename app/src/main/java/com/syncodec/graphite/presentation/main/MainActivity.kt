package com.syncodec.graphite.presentation.main

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.main.composable.screen.FirstTimeScreen
import com.syncodec.graphite.presentation.main.composable.screen.MainScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.DataStoreInstance


class MainActivity : ComponentActivity() {

	private lateinit var auth: FirebaseAuth
	private lateinit var oneTapClient: SignInClient
	private lateinit var signInRequest: BeginSignInRequest
	@OptIn(ExperimentalAnimationApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		auth = Firebase.auth
		oneTapClient = Identity.getSignInClient(this)
		signInRequest = BeginSignInRequest.builder()
			.setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
				.setSupported(true)
				.build())
			.setGoogleIdTokenRequestOptions(
				BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
					.setSupported(true)
					.setServerClientId(BuildConfig.CLIENT_KEY)
					.setFilterByAuthorizedAccounts(false)
					.build())
			.setAutoSelectEnabled(false)
			.build()

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)
				systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

				val dataStoreInstance = DataStoreInstance(this)
				val isFirstTime by dataStoreInstance.getIsFirstTime.collectAsState(initial = null)

				AnimatedContent(targetState = isFirstTime) {
					when(it) {
						true -> FirstTimeScreen(onClickLogin = this@MainActivity::signIn)
						false -> MainScreen()
						else -> LoadingView()
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

					if (idToken==null) {
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
					Log.e("npr71", "Couldn't start One Tap UI: ${e.localizedMessage}")
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
}
