package com.syncodec.graphite.presentation.sync.googleDrive

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.About
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.presentation.sync.googleDrive.composable.screen.GoogleDriveSyncScreen
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.utils.alice.Alice
import com.syncodec.graphite.utils.dataStore.SyncDataStoreInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class GoogleDriveSyncActivity : ComponentActivity() {
	private val viewModel by viewModel<GoogleDriveSyncViewModel>()

	private val aboutStateFlow: MutableStateFlow<AboutState> = MutableStateFlow(AboutState.Init)
	private lateinit var syncDataStoreInstance: SyncDataStoreInstance

	private var drive: Drive? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
//
//		syncDataStoreInstance = SyncDataStoreInstance(this)
//
//		connectWithDrive()
//
//		setContent {
//			BaseComposable {
//
//				val aboutState by aboutStateFlow.collectAsState()
//				val snapshotList by viewModel.snapshotList.collectAsState()
//				var isGeneratingSnapshot by remember { mutableStateOf(false) }
//				var isRestoringSnapshot by remember { mutableStateOf(false) }
//
//				LaunchedEffect(key1 = snapshotList) {
//					if (snapshotList == null) Log.d("npr71", "snapshotList: null")
//					else when (snapshotList) {
//						is GDriveSyncInatorService.Companion.ListFiles.Success -> Log.d("npr71", "snapshotList: ${(snapshotList as GDriveSyncInatorService.Companion.ListFiles.Success).fileList.size}")
//						else -> Log.d("npr71", "snapshotList: ${snapshotList!!::class.java.simpleName}")
//					}
//				}
//
//				GoogleDriveSyncScreen(
//					aboutState = aboutState,
//					snapshotList = snapshotList,
//					isGeneratingSnapshot = isGeneratingSnapshot,
//					isRestoringSnapshot = isRestoringSnapshot,
//					onTestConnection = { connectWithDrive() },
//					onClickConnect = { signInWithDrivePermission() },
//					onClickDisconnect = { disconnectFromDrive() },
//					onClickGenerateSnapshot = { drive?.let { viewModel.generateSnapshot(it) { isGeneratingSnapshot = it } } },
//					onClickShareSnapshot = {},
//					onClickRestoreSnapshot = { file ->
//						drive?.let {
//							isRestoringSnapshot = true
//							viewModel.downloadSnapshot(drive = it, file = file) { downloadSnapshotResponse ->
//								if (downloadSnapshotResponse is GDriveSyncInatorService.Companion.DownloadResult.Success) {
//									viewModel.restore(downloadSnapshotResponse.fileContent.inputStream()) {
//										if (!it) {
//											lifecycleScope.launch(Dispatchers.Main) {
//												Toast.makeText(this@GoogleDriveSyncActivity, "Failed to restore snapshot", Toast.LENGTH_SHORT).show()
//												isRestoringSnapshot = false
//											}
//										}
//									}
//								}
//							}
//						}
//					},
//					onClickDeleteSnapshot = { file -> drive?.let { viewModel.deleteSnapshot(it, file) { _ -> viewModel.refreshSnapshot(it) } } },
//					refreshSnapshot = { drive?.let { viewModel.refreshSnapshot(it) } },
//				)
//			}
//		}
	}

//	private val signInActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//		val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//		try {
//			task.result.account?.let {
//				Toast.makeText(this, "Connection successful.", Toast.LENGTH_SHORT).show()
//				connectWithDrive()
//			}
//		} catch (e: RuntimeExecutionException) {
//			if (e.cause is ApiException) {
//				val apiException = e.cause as ApiException
//				if (apiException.statusCode == 7) Toast.makeText(this, "Connection unsuccessful. Check your internet connection and try again.", Toast.LENGTH_SHORT).show()
//				else Toast.makeText(this, "Connection unsuccessful.", Toast.LENGTH_SHORT).show()
//			}
//			else {
//				Toast.makeText(this, "Connection unsuccessful.", Toast.LENGTH_SHORT).show()
//			}
//		} catch (exception: Exception) {
//			Toast.makeText(this, "Connection unsuccessful.", Toast.LENGTH_SHORT).show()
//		}
//	}

//	private fun signInWithDrivePermission() {
//		val serverClientId = Alice.decrypt(BuildConfig.CLIENT_KEY, "lt3(3x4R7M^107!&4E74Z%*o8cp2i7y@") ?: run{
//			Toast.makeText(this, "Connection unsuccessful.", Toast.LENGTH_SHORT).show()
//			return
//		}
//		val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//			.requestServerAuthCode(serverClientId)
//			.requestEmail()
//			.requestScopes(Scope(Scopes.DRIVE_APPFOLDER))
//			.build()
//
//		val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
//		signInActivityLauncher.launch(googleSignInClient.signInIntent)
//	}
//
//	private fun connectWithDrive() {
//		lifecycleScope.launch(Dispatchers.IO) {
//			val googleAccount = GoogleSignIn.getLastSignedInAccount(this@GoogleDriveSyncActivity)
//			if (googleAccount == null) {
//				aboutStateFlow.tryEmit(AboutState.NotLoggedIn)
//				Log.d("npr71", "connectWithDrive: NotLoggedIn")
//			}
//			else {
//				aboutStateFlow.tryEmit(AboutState.Loading)
//				drive = viewModel.gDrive.getDrive()
//				drive?.let {
//					try {
//						it.about().get().setFields("user, storageQuota").execute().let {
//							aboutStateFlow.tryEmit(AboutState.Success(it))
//							syncDataStoreInstance.setSyncProvider(SyncDataStoreInstance.Companion.SyncProvider.GoogleDrive)
//							Log.d("npr71", "connectWithDrive: ${it.user.displayName}")
//						}
//					} catch (exception: Exception) {
//						exception.printStackTrace()
//						aboutStateFlow.tryEmit(AboutState.Error(exception.message ?: "Unknown error"))
//					}
//					viewModel.refreshSnapshot(it)
//				}
//			}
//		}
//	}
//
//	private fun disconnectFromDrive() {
//		lifecycleScope.launch(Dispatchers.IO) {
//			GoogleSignIn
//				.getClient(this@GoogleDriveSyncActivity, GoogleSignInOptions.DEFAULT_SIGN_IN)
//				.signOut()
//				.addOnSuccessListener {
//					aboutStateFlow.tryEmit(AboutState.NotLoggedIn)
//					lifecycleScope.launch(Dispatchers.Main) {
//						Toast.makeText(this@GoogleDriveSyncActivity, "Disconnected from Google Drive.", Toast.LENGTH_SHORT).show()
//					}
//					val syncDataStoreInstance = SyncDataStoreInstance(this@GoogleDriveSyncActivity)
//					syncDataStoreInstance.setSyncProvider(SyncDataStoreInstance.Companion.SyncProvider.NotConfigured)
//				}
//				.addOnFailureListener {
//					lifecycleScope.launch(Dispatchers.Main) {
//						Toast.makeText(this@GoogleDriveSyncActivity, "Failed to disconnect from Google Drive.", Toast.LENGTH_SHORT).show()
//					}
//				}
//		}
//	}

	companion object {
		sealed class AboutState {
			object Init : AboutState()
			object Loading : AboutState()
			data class Success(val about: About) : AboutState()
			data class Error(val message: String) : AboutState()
			object NotLoggedIn : AboutState()
		}
	}
}
