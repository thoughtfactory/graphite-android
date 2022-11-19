package com.syncodec.graphite.presentation.settings

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.documentfile.provider.DocumentFile
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
import com.syncodec.graphite.presentation.settings.composable.bottomSheet.SettingsBottomSheetType
import com.syncodec.graphite.presentation.settings.composable.dialog.SettingsDialogType
import com.syncodec.graphite.presentation.settings.composable.screen.SettingsScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Authenticator
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

	private val viewModel by viewModels<SettingsViewModel>()

	private lateinit var auth : FirebaseAuth
	private lateinit var oneTapClient : SignInClient
	private lateinit var signInRequest : BeginSignInRequest

	private var backupFolderPath : MutableState<String?> = mutableStateOf(null)

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

		var authenticator by mutableStateOf(Authenticator.NONE)
		val authenticatorAction : (Authenticator) -> Unit = { authenticator = it }

		setContent {
			CompositionLocalProvider(
				LocalAuthenticatorAction provides authenticatorAction
			) {
				BaseContent {
					val scope = rememberCoroutineScope()
					val systemUiController = rememberSystemUiController()
					systemUiController.setStatusBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)
					systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

					val scrollState = rememberScrollState()

					val navigatorPath = remember { mutableStateListOf(Navigator.BASE) }

					var backupFolderPath by this.backupFolderPath

					val snapshotList = viewModel.snapshotList

					val attachmentCount by viewModel.attachmentCount
					val attachmentProcessed by viewModel.attachmentProcessed
					val bucketItemCount by viewModel.bucketItemCount
					val bucketItemProcessed by viewModel.bucketItemProcessed
					val bucketCount by viewModel.bucketCount
					val bucketProcessed by viewModel.bucketProcessed
					val chapterCount by viewModel.chapterCount
					val chapterProcessed by viewModel.chapterProcessed
					val noteCount by viewModel.noteCount
					val noteProcessed by viewModel.noteProcessed
					val tagCount by viewModel.tagCount
					val tagProcessed by viewModel.tagProcessed
					val packageCount by viewModel.packageCount
					val packageProcessed by viewModel.packageProcessed

					var showTakeSnapshotDialog by remember { mutableStateOf(false) }
					var showRestoreSnapshotDialog by remember { mutableStateOf(false) }
					var showRestoringSnapshotDialog by remember { mutableStateOf(false) }
					var showDeleteAccountDialog by remember { mutableStateOf(false) }

					var restoreSnapshotFile by remember { mutableStateOf<DocumentFile?>(null) }

					fun openDialog(dialogType : SettingsDialogType, data : Any?) {
						when (dialogType) {
							SettingsDialogType.TAKE_SNAPSHOT -> showTakeSnapshotDialog = true
							SettingsDialogType.RESTORE_SNAPSHOT -> {
								try {
									restoreSnapshotFile = data as DocumentFile
									showRestoreSnapshotDialog = true
								} catch (e : Exception) {
									Toast.makeText(this@SettingsActivity, "Error reading snapshot file", Toast.LENGTH_SHORT).show()
								}
							}

							SettingsDialogType.RESTORING_SNAPSHOT -> showRestoringSnapshotDialog = true
							SettingsDialogType.DELETE_ACCOUNT -> showDeleteAccountDialog = true
						}
					}

					fun closeDialog(dialogType : SettingsDialogType) {
						when (dialogType) {
							SettingsDialogType.TAKE_SNAPSHOT -> showTakeSnapshotDialog = false
							SettingsDialogType.RESTORE_SNAPSHOT -> showRestoreSnapshotDialog = false
							SettingsDialogType.RESTORING_SNAPSHOT -> showRestoringSnapshotDialog = false
							SettingsDialogType.DELETE_ACCOUNT -> showDeleteAccountDialog = false
						}
					}

					this.onBackPressedDispatcher.addCallback(
						this, object : OnBackPressedCallback(true) {
							override fun handleOnBackPressed() {
								if (authenticator == Authenticator.NONE) {
									if(showDeleteAccountDialog || showRestoreSnapshotDialog) {
										closeDialog(SettingsDialogType.DELETE_ACCOUNT)
										closeDialog(SettingsDialogType.RESTORE_SNAPSHOT)
									} else if (showTakeSnapshotDialog || showRestoringSnapshotDialog) {
										Toast.makeText(this@SettingsActivity, "Please wait for the current operation to finish", Toast.LENGTH_SHORT).show()
									} else {
										if (navigatorPath.size > 1) {
											navigatorPath.removeLast()
											scope.launch { scrollState.scrollTo(0) }
										} else {
											finish()
										}
									}
								} else {
									authenticator = Authenticator.NONE
								}
							}
						}
					)

					CompositionLocalProvider(
						onNavigate provides {
							navigatorPath.add(it)
							scope.launch { scrollState.scrollTo(0) }
						},
						Companion.navigatorPath provides navigatorPath,
						SettingsActivity.scrollState provides scrollState,
						SettingsActivity.firebaseUser provides auth.currentUser,
						SettingsActivity.signIn provides ::signIn,
						setupLocalBackupFolder provides {
							try {
								Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
									localBackupDirSelector.launch(this)
								}
							} catch (e : Exception) {
								Toast.makeText(this@SettingsActivity, "Error setting backup folder", Toast.LENGTH_SHORT).show()
							}
						},
						removeLocalBackupFolder provides {
							try {
								contentResolver.persistedUriPermissions.forEach {
									contentResolver.releasePersistableUriPermission(it.uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
								}
								backupFolderPath = contentResolver.persistedUriPermissions.firstOrNull()?.uri?.path
								Toast.makeText(this, "Local backup folder removed", Toast.LENGTH_SHORT).show()
							} catch (e : Exception) {
								Toast.makeText(this@SettingsActivity, "Error removing backup folder", Toast.LENGTH_SHORT).show()
							}
						},
						Companion.backupFolderPath provides backupFolderPath,
						Companion.snapshotList provides snapshotList,
						takeSnapshot provides {
							try {
								val uri = contentResolver.persistedUriPermissions.firstOrNull()?.uri
								if (uri == null) Toast.makeText(this, "Error generating snapshot. Try setting backup folder again.", Toast.LENGTH_SHORT).show()
								else viewModel.takeSnapshot(uri) {
									scope.launch(Dispatchers.Main) {
										if (it) Toast.makeText(this@SettingsActivity, "Snapshot saved", Toast.LENGTH_SHORT).show()
										else Toast.makeText(this@SettingsActivity, "Error taking snapshot", Toast.LENGTH_SHORT).show()
										closeDialog(SettingsDialogType.TAKE_SNAPSHOT)
									}
								}
								openDialog(SettingsDialogType.TAKE_SNAPSHOT, null)
							} catch (e : Exception) {
								Toast.makeText(this@SettingsActivity, "Error taking snapshot", Toast.LENGTH_SHORT).show()
							}
						},
						getSnapshot provides {
							val uri = contentResolver.persistedUriPermissions.firstOrNull()?.uri
							if (uri == null) Toast.makeText(this, "Error getting snapshot. Try setting backup folder again.", Toast.LENGTH_SHORT).show()
							else viewModel.getSnapshot(uri)
						},
						restoreSnapshot provides {
							try {
								if (restoreSnapshotFile == null) Toast.makeText(this, "Error restoring snapshot. Try again.", Toast.LENGTH_SHORT).show()
								else viewModel.restoreSnapshot(restoreSnapshotFile !!)
							} catch (e : Exception) {
								Toast.makeText(this@SettingsActivity, "Error restoring snapshot", Toast.LENGTH_SHORT).show()
							}
						},
						Companion.showTakeSnapshotDialog provides showTakeSnapshotDialog,
						Companion.showRestoreSnapshotDialog provides showRestoreSnapshotDialog,
						Companion.showRestoringSnapshotDialog provides showRestoringSnapshotDialog,
						Companion.showDeleteAccountDialog provides showDeleteAccountDialog,
						Companion.attachmentCount provides attachmentCount,
						Companion.attachmentProcessed provides attachmentProcessed,
						Companion.bucketItemCount provides bucketItemCount,
						Companion.bucketItemProcessed provides bucketItemProcessed,
						Companion.bucketCount provides bucketCount,
						Companion.bucketProcessed provides bucketProcessed,
						Companion.chapterCount provides chapterCount,
						Companion.chapterProcessed provides chapterProcessed,
						Companion.noteCount provides noteCount,
						Companion.noteProcessed provides noteProcessed,
						Companion.tagCount provides tagCount,
						Companion.tagProcessed provides tagProcessed,
						Companion.packageCount provides packageCount,
						Companion.packageProcessed provides packageProcessed,
						openDialog provides ::openDialog,
						closeDialog provides ::closeDialog,
						onBackPressed provides { this.onBackPressedDispatcher.onBackPressed() }
					) {
						SettingsScreen()
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
				e.printStackTrace()
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
					e.printStackTrace()
					Log.e("npr71", "Couldn't start One Tap UI: ${e.localizedMessage}")
				}
			}
			.addOnFailureListener(this) { e ->
				e.printStackTrace()
				Toast.makeText(this, "Error signing in. Please try again later.", Toast.LENGTH_SHORT).show()
			}
	}

	private fun updateUI(user : FirebaseUser?) {
	}

	val localBackupDirSelector = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
		try {
			it.data?.data.apply {
				this?.let { it1 ->
					contentResolver.takePersistableUriPermission(this, Intent.FLAG_GRANT_READ_URI_PERMISSION)
					backupFolderPath.value = contentResolver.persistedUriPermissions.firstOrNull()?.uri?.path
				}
			}
			Toast.makeText(this, "Backup folder set to ${backupFolderPath.value}", Toast.LENGTH_SHORT).show()
		} catch (e : Exception) {
			Toast.makeText(this, "Error setting backup folder", Toast.LENGTH_SHORT).show()
		}
	}


	companion object {
		enum class Navigator {
			BASE,
			PREFERENCES,
			SECURITY,
			EXTENSIONS,
			BACKUP,
			LOCAL_BACKUP,
			SNAPSHOT_WAREHOUSE,
			SYNC,
			ABOUT
		}

		val onNavigate = compositionLocalOf<(Navigator) -> Unit> { {} }
		val navigatorPath = compositionLocalOf<SnapshotStateList<Navigator>> { mutableStateListOf() }
		val scrollState = compositionLocalOf<ScrollState> { ScrollState(0) }
		val firebaseUser = compositionLocalOf<FirebaseUser?> { null }
		val signIn = compositionLocalOf { {} }
		val signOut = compositionLocalOf { {} }
		val deleteAccount = compositionLocalOf { {} }
		val setupLocalBackupFolder = compositionLocalOf<() -> Unit> { {} }
		val removeLocalBackupFolder = compositionLocalOf<() -> Unit> { {} }
		val backupFolderPath = compositionLocalOf<String?> { null }
		val snapshotList = compositionLocalOf<SnapshotStateList<DocumentFile>> { mutableStateListOf() }
		val takeSnapshot = compositionLocalOf<() -> Unit> { {} }
		val getSnapshot = compositionLocalOf<() -> Unit> { {} }
		val restoreSnapshot = compositionLocalOf<() -> Unit> { {} }

		val attachmentCount = compositionLocalOf { 0 }
		val attachmentProcessed = compositionLocalOf { 0 }
		val bucketItemCount = compositionLocalOf { 0 }
		val bucketItemProcessed = compositionLocalOf { 0 }
		val bucketCount = compositionLocalOf { 0 }
		val bucketProcessed = compositionLocalOf { 0 }
		val chapterCount = compositionLocalOf { 0 }
		val chapterProcessed = compositionLocalOf { 0 }
		val noteCount = compositionLocalOf { 0 }
		val noteProcessed = compositionLocalOf { 0 }
		val tagCount = compositionLocalOf { 0 }
		val tagProcessed = compositionLocalOf { 0 }
		val packageCount = compositionLocalOf { 0 }
		val packageProcessed = compositionLocalOf { 0 }

		val showTakeSnapshotDialog = compositionLocalOf<Boolean> { false }
		val showRestoreSnapshotDialog = compositionLocalOf<Boolean> { false }
		val showRestoringSnapshotDialog = compositionLocalOf<Boolean> { false }
		val showDeleteAccountDialog = compositionLocalOf<Boolean> { false }

		val openBottomSheet = compositionLocalOf<(SettingsBottomSheetType) -> Unit> { {} }
		val closeBottomSheet = compositionLocalOf<() -> Unit> { { } }
		val openDialog = compositionLocalOf<(SettingsDialogType, Any?) -> Unit> { { _, _ -> } }
		val closeDialog = compositionLocalOf<(SettingsDialogType) -> Unit> { {} }

		val onBackPressed = compositionLocalOf<() -> Unit> { {} }
	}
}
