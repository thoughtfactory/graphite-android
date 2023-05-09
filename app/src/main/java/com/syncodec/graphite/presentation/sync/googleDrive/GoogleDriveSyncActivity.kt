package com.syncodec.graphite.presentation.sync.googleDrive

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.model.About
import com.syncodec.graphite.di.cloud.googleDrive.GDrive
import com.syncodec.graphite.presentation.sync.googleDrive.composable.screen.GoogleDriveSyncScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.dataStore.SyncDataStoreInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class GoogleDriveSyncActivity : ComponentActivity() {

	val gDrive : GDrive by inject()

	private val aboutStateFlow : MutableStateFlow<AboutState> = MutableStateFlow(AboutState.Init)
	private lateinit var syncDataStoreInstance : SyncDataStoreInstance

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		syncDataStoreInstance = SyncDataStoreInstance(this)

		connectWithDrive()

		setContent {
			BaseContent {
				val aboutState by aboutStateFlow.collectAsState()
				GoogleDriveSyncScreen(
					aboutState = aboutState,
					onClickConnect = { signInWithDrivePermission() },
				)
			}
		}
	}

	private val signInActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
		try {
			task.result.serverAuthCode?.let {
				Log.d("GoogleDriveSyncActivity", "serverAuthCode: $it")
			}
			task.result.account?.let {
				Toast.makeText(this, "Connection successful.", Toast.LENGTH_SHORT).show()
				connectWithDrive()
			}
		} catch (exception : Exception) {
			exception.printStackTrace()
			Toast.makeText(this, "Connection unsuccessful.", Toast.LENGTH_SHORT).show()
		}
	}

	private fun signInWithDrivePermission() {
		val serverClientId = "948547440986-h3ckomagfcehf7mj7e2uelt7jltca9km.apps.googleusercontent.com"
		val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
			.requestServerAuthCode(serverClientId)
			.requestEmail()
			.requestScopes(Scope(Scopes.DRIVE_APPFOLDER))
			.build()

		val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
		signInActivityLauncher.launch(googleSignInClient.signInIntent)
	}

	private fun connectWithDrive() {
		lifecycleScope.launch(Dispatchers.IO) {
			val googleAccount = GoogleSignIn.getLastSignedInAccount(this@GoogleDriveSyncActivity)
			if (googleAccount == null) {
				aboutStateFlow.tryEmit(AboutState.NotLoggedIn)
			} else {
				aboutStateFlow.tryEmit(AboutState.Loading)
				gDrive.getDrive()?.let {
					try {
						it.about().get().setFields("user, storageQuota").execute().let {
							aboutStateFlow.tryEmit(AboutState.Success(it))
							syncDataStoreInstance.setSyncProvider(SyncDataStoreInstance.Companion.SyncProvider.GoogleDrive)
							Log.d("npr71", "connectWithDrive: ${it.user.displayName}")
						}
					} catch (exception : Exception) {
						exception.printStackTrace()
						aboutStateFlow.tryEmit(AboutState.Error(exception.message ?: "Unknown error"))
					}
				}
			}
		}
	}

	companion object {
		sealed class AboutState {
			object Init : AboutState()
			object Loading : AboutState()
			data class Success(val about : About) : AboutState()
			data class Error(val message : String) : AboutState()
			object NotLoggedIn : AboutState()
		}
	}
}
