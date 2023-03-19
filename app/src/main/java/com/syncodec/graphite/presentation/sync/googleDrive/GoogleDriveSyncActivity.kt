package com.syncodec.graphite.presentation.sync.googleDrive

import android.R.attr.data
import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.syncodec.graphite.presentation.sync.googleDrive.screen.GoogleDriveSyncScreen
import com.syncodec.graphite.presentation.ui.BaseContent


class GoogleDriveSyncActivity : ComponentActivity() {

//	private val JSON_FACTORY : JsonFactory = GsonFactory.getDefaultInstance()
//	private val SCOPES = listOf(DriveScopes.DRIVE_METADATA_READONLY, DriveScopes.DRIVE_APPDATA)
//	private val CREDENTIALS_FILE_PATH = "/credentials.json"


	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			BaseContent {
				GoogleDriveSyncScreen(
					onClickConnect = { signIn() }
				)
			}
		}
	}

	val signInActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		if (result.resultCode == Activity.RESULT_OK && result.data != null) {

		}
	}
//	val signInActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//		if (result.resultCode == Activity.RESULT_OK && result.data != null) {
//			GoogleSignIn.getSignedInAccountFromIntent(result.data)
//				.addOnSuccessListener { googleAccount : GoogleSignInAccount ->
//					Log.d("npr71", "Signed in as " + googleAccount.email)
//
//					// Use the authenticated account to sign in to the Drive service.
//					val credential : GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(this, setOf(DriveScopes.DRIVE_FILE))
//					credential.selectedAccount = googleAccount.account
//					val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
//					val googleDriveService : Drive = Drive.Builder(
//						HTTP_TRANSPORT,
//						GsonFactory(),
//						credential
//					)
//						.setApplicationName("Graphene")
//						.build()
//
//				}
//				.addOnFailureListener { exception : Exception? -> Log.e("npr71", "Unable to sign in.", exception) }
//		}
//	}

	fun signIn() {
		val serverClientId = "CLIENT_ID_OF_WEB_BROWSER_API"
		val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
			.requestScopes(Scope(Scopes.DRIVE_APPFOLDER))
			.requestServerAuthCode(serverClientId)
			.requestEmail()
			.build()

		val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
		signInActivity.launch(mGoogleSignInClient.signInIntent)
	}

//	fun signIn() {
//		val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//			.requestEmail()
//			.requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
//			.build()
//		val client = GoogleSignIn.getClient(this, signInOptions)
//		signInActivity.launch(client.signInIntent)
//	}

//	@Throws(IOException::class)
//	private fun getCredentials(HTTP_TRANSPORT : NetHttpTransport) : Credential? {
////		// Load client secrets.
////		val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))
////		GoogleClientSecrets.load()
////
////		// Build flow and trigger user authorization request.
////		val flow = GoogleAuthorizationCodeFlow.Builder(
////			HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES
////		)
////			.setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
////			.setAccessType("offline")
////			.build()
////		val receiver = LocalServerReceiver.Builder().setPort(8888).build()
////		//returns an authorized Credential object.
////		return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
//	}
}
