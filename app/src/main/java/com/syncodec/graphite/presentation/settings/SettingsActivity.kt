package com.syncodec.graphite.presentation.settings

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.LogInCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.settings.composable.bottomSheet.SettingsBottomSheetType
import com.syncodec.graphite.presentation.settings.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.settings.composable.dialog.SettingsDialog
import com.syncodec.graphite.presentation.settings.composable.dialog.SettingsDialogType
import com.syncodec.graphite.presentation.settings.composable.screen.SettingsScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Authenticator
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.alice.Alice
import com.syncodec.graphite.utils.share
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject


@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

	private val viewModel by viewModels<SettingsViewModel>()

	private lateinit var auth : FirebaseAuth
	private lateinit var oneTapClient : SignInClient
	private lateinit var signInRequest : BeginSignInRequest

	private var firebaseUser : MutableState<FirebaseUser?> = mutableStateOf(null)

	private var backupFolderPath : MutableState<String?> = mutableStateOf(null)

	val showTakeSnapshotDialog = mutableStateOf(false)
	val showRestoreSnapshotDialog = mutableStateOf(false)
	val showRestoringSnapshotDialog = mutableStateOf(false)
	val showImportingDataDialog = mutableStateOf(false)
	val showImportingJourneyDataDialog = mutableStateOf(false)
	val showDeleteAccountDialog = mutableStateOf(false)

	@OptIn(ExperimentalMaterialApi::class)
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		auth = Firebase.auth

		this.firebaseUser.value = auth.currentUser
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
					val importDataCount by viewModel.importDataCount
					val importDataProcessed by viewModel.importDataProcessed

					val _firebaseUser by this.firebaseUser

					val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
					var bottomSheetType : SettingsBottomSheetType by remember { mutableStateOf(SettingsBottomSheetType.PROFILE) }

					fun openSheet(_bottomSheetType : SettingsBottomSheetType) {
						scope.launch { bottomSheetType = _bottomSheetType; modalBottomSheetState.show() }
					}

					fun closeSheet() {
						scope.launch { modalBottomSheetState.hide() }
					}

					var _showTakeSnapshotDialog by this.showTakeSnapshotDialog
					var _showRestoreSnapshotDialog by this.showRestoreSnapshotDialog
					var _showRestoringSnapshotDialog by this.showRestoringSnapshotDialog
					var _showImportingDataDialog by this.showImportingDataDialog
					var _showImportingJourneyDataDialog by this.showImportingJourneyDataDialog
					var _showDeleteAccountDialog by this.showDeleteAccountDialog

					var restoreSnapshotFile by remember { mutableStateOf<DocumentFile?>(null) }

					fun openDialog(dialogType : SettingsDialogType, data : Any?) {
						when (dialogType) {
							SettingsDialogType.TAKE_SNAPSHOT -> _showTakeSnapshotDialog = true
							SettingsDialogType.RESTORE_SNAPSHOT -> {
								try {
									restoreSnapshotFile = data as DocumentFile
									_showRestoreSnapshotDialog = true
								} catch (e : Exception) {
//									e.printStackTrace()
									Toast.makeText(this@SettingsActivity, "Error reading snapshot file", Toast.LENGTH_SHORT).show()
								}
							}

							SettingsDialogType.RESTORING_SNAPSHOT -> _showRestoringSnapshotDialog = true
							SettingsDialogType.DELETE_ACCOUNT -> _showDeleteAccountDialog = true
						}
					}

					fun closeDialog(dialogType : SettingsDialogType) {
						when (dialogType) {
							SettingsDialogType.TAKE_SNAPSHOT -> _showTakeSnapshotDialog = false
							SettingsDialogType.RESTORE_SNAPSHOT -> _showRestoreSnapshotDialog = false
							SettingsDialogType.RESTORING_SNAPSHOT -> _showRestoringSnapshotDialog = false
							SettingsDialogType.DELETE_ACCOUNT -> _showDeleteAccountDialog = false
						}
					}

					this.onBackPressedDispatcher.addCallback(
						this, object : OnBackPressedCallback(true) {
							override fun handleOnBackPressed() {
								if (authenticator == Authenticator.NONE) {
									if (_showDeleteAccountDialog || _showRestoreSnapshotDialog) {
										closeDialog(SettingsDialogType.DELETE_ACCOUNT)
										closeDialog(SettingsDialogType.RESTORE_SNAPSHOT)
									} else if (_showTakeSnapshotDialog || _showRestoringSnapshotDialog) {
										Toast.makeText(this@SettingsActivity, "Please wait for the current operation to finish", Toast.LENGTH_SHORT).show()
									} else if (modalBottomSheetState.isVisible) {
										closeSheet()
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
						LocalOnNavigate provides {
							navigatorPath.add(it)
							scope.launch { scrollState.scrollTo(0) }
						},
						LocalNavigatorPath provides navigatorPath,
						LocalScrollState provides scrollState,
						LocalFirebaseUser provides _firebaseUser,
						LocalSignIn provides ::signIn,
						LocalSignOut provides ::signOut,
						LocalDeleteAccount provides {
							closeSheet()
							openDialog(SettingsDialogType.DELETE_ACCOUNT, null)
							viewModel.deleteAccount()
						},
						LocalSetupLocalBackupFolder provides {
							try {
								Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
									localBackupDirPicker.launch(this)
								}
							} catch (e : Exception) {
								Toast.makeText(this@SettingsActivity, "Error setting backup folder", Toast.LENGTH_SHORT).show()
							}
						},
						LocalRemoveLocalBackupFolder provides {
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
						LocalBackupFolderPath provides backupFolderPath,
						LocalSnapshotList provides snapshotList,
						LocalTakeSnapshot provides {
							try {
								val uri = contentResolver.persistedUriPermissions.firstOrNull()?.uri
								if (uri == null) Toast.makeText(this, "Error generating snapshot. Try setting backup folder again.", Toast.LENGTH_SHORT).show()
								else viewModel.takeSnapshot(uri) {
									scope.launch(Dispatchers.Main) {
										if (it) Toast.makeText(this@SettingsActivity, "Snapshot saved", Toast.LENGTH_SHORT).show()
										else Toast.makeText(this@SettingsActivity, "Error taking snapshot", Toast.LENGTH_SHORT).show()
										closeDialog(SettingsDialogType.TAKE_SNAPSHOT)

										viewModel.getSnapshot(uri)
									}
								}
								openDialog(SettingsDialogType.TAKE_SNAPSHOT, null)
							} catch (e : Exception) {
								Toast.makeText(this@SettingsActivity, "Error taking snapshot", Toast.LENGTH_SHORT).show()
							}
						},
						LocalGetSnapshot provides {
							val uri = contentResolver.persistedUriPermissions.firstOrNull()?.uri
							if (uri == null) Toast.makeText(this, "Error getting snapshot. Try setting backup folder again.", Toast.LENGTH_SHORT).show()
							else viewModel.getSnapshot(uri)
						},
						LocalRestoreSnapshot provides {
							openDialog(SettingsDialogType.RESTORING_SNAPSHOT, null)
							try {
								restoreSnapshotFile?.let {
									viewModel.restoreSnapshot(contentResolver.openInputStream(it.uri) !!, true) {
										CoroutineScope(Dispatchers.Main).launch {
											if (it) Toast.makeText(this@SettingsActivity, "Snapshot restored", Toast.LENGTH_SHORT).show()
											else Toast.makeText(this@SettingsActivity, "Error restoring snapshot", Toast.LENGTH_SHORT).show()
											closeDialog(SettingsDialogType.RESTORING_SNAPSHOT)
										}
									}
								} ?: Toast.makeText(this, "Error restoring snapshot. Try again.", Toast.LENGTH_SHORT)
							} catch (e : Exception) {
								Toast.makeText(this@SettingsActivity, "Error restoring snapshot", Toast.LENGTH_SHORT).show()
							}
						},
						LocalExportData provides {
							openDialog(SettingsDialogType.TAKE_SNAPSHOT, null)
							CoroutineScope(Dispatchers.IO).launch {
								viewModel.generateSnapshot { _, file ->
									CoroutineScope(Dispatchers.Main).launch { closeDialog(SettingsDialogType.TAKE_SNAPSHOT) }
									file?.share(this@SettingsActivity)
								}
							}
						},
						LocalImportData provides {
							when (it) {
								ImportType.GRAPHITE -> importGraphiteFilePicker.launch(arrayOf("application/x-7z-compressed"))
								ImportType.GOOGLE_KEEP -> null
								ImportType.JOURNEY -> importJourneyFilePicker.launch(arrayOf("application/zip"))
								ImportType.NOTESNOOK -> null
							}
						},
						LocalShowTakeSnapshotDialog provides _showTakeSnapshotDialog,
						LocalShowRestoreSnapshotDialog provides _showRestoreSnapshotDialog,
						LocalShowRestoringSnapshotDialog provides _showRestoringSnapshotDialog,
						LocalShowImportingDataDialog provides _showImportingDataDialog,
						LocalShowImportingJourneyDataDialog provides _showImportingJourneyDataDialog,
						LocalShowDeleteAccountDialog provides _showDeleteAccountDialog,
						LocalAttachmentCount provides attachmentCount,
						LocalAttachmentProcessed provides attachmentProcessed,
						LocalBucketItemCount provides bucketItemCount,
						LocalBucketItemProcessed provides bucketItemProcessed,
						LocalBucketCount provides bucketCount,
						LocalBucketProcessed provides bucketProcessed,
						LocalChapterCount provides chapterCount,
						LocalChapterProcessed provides chapterProcessed,
						LocalNoteCount provides noteCount,
						LocalNoteProcessed provides noteProcessed,
						LocalTagCount provides tagCount,
						LocalTagProcessed provides tagProcessed,
						LocalPackageCount provides packageCount,
						LocalPackageProcessed provides packageProcessed,
						LocalImportDataCount provides importDataCount,
						LocalImportDataProcessed provides importDataProcessed,
						LocalOpenBottomSheet provides ::openSheet,
						LocalCloseBottomSheet provides ::closeSheet,
						LocalOpenDialog provides ::openDialog,
						LocalCloseDialog provides ::closeDialog,
						LocalOnBackPressed provides { this.onBackPressedDispatcher.onBackPressed() }
					) {
						ModalBottomSheetLayout(
							sheetContent = { SheetLayout(bottomSheetType = bottomSheetType) { closeSheet() } },
							sheetState = modalBottomSheetState,
							sheetElevation = 0.dp,
							sheetBackgroundColor = Color.Transparent,
							modifier = Modifier.fillMaxSize(),
						) {
							SettingsScreen()
							SettingsDialog()
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
												BaseApplication.isPro.value = customerInfo.entitlements["pro"]?.isActive == true
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
					BaseApplication.isPro.value = customerInfo.entitlements["pro"]?.isActive == true
				}
			}
		)
	}

	private val localBackupDirPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
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

	private fun richTextEditor() = RichTextEditor(this, Color.Transparent, Color.Transparent, 0, null).apply {
		setGetTextListener(
			object : RichTextEditor.GetTextListener {
				override fun onGetData(extra : String?, data : String?) {
					CoroutineScope(Dispatchers.Default).launch {
						try {
							if (data == null) {
								viewModel.lock = false
							} else {
								val dataObject = JSONObject(data)
								val dataJson = dataObject.optJSONObject("dataJson")
								val dataText = dataObject.optString("dataText")
								val importData = dataObject.optJSONObject("importData") ?: JSONObject()
								val noteId = try {
									RealmUUID.from(dataObject.optString("noteId"))
								} catch (e : Exception) {
									RealmUUID.random()
								}
								NoteObject().apply {
									this.id = noteId
									this.createdTimestamp = importData.optLong("date_journal").let { if (it == 0L) System.currentTimeMillis() else it }
									this.modifiedTimestamp = importData.optLong("date_modified").let { if (it == 0L) System.currentTimeMillis() else it }
									this.userTimestamp = this.createdTimestamp
									this.title
									this.color
									val lat = importData.optDouble("lat")
									val lng = importData.optDouble("lng")
									this.setLatLng(LatLng(lat, lng))
									this.address = importData.optString("address")
									this.contentThumbnail = dataText.substring(0, minOf(256, dataText.length))
									this.content = dataJson?.toString()
									this.thumbnail
									this.thumbnailType
									this.isFavourite = importData.optBoolean("favorite")
									this.isLocked

									this.parentId = viewModel.defaultChapterId

									viewModel.putNote(this)
								}
							}
						} catch (e : Exception) {
//							e.printStackTrace()
							viewModel.lock = false
						}
					}
				}
			}
		)
	}

	private val importGraphiteFilePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
		try {
			it?.let { it1 ->
				contentResolver.openInputStream(it1)?.let { inputStream ->
					showImportingDataDialog.value = true
					try {
						viewModel.restoreSnapshot(inputStream = inputStream, clearAll = false) {
							CoroutineScope(Dispatchers.Main).launch {
								if (it) Toast.makeText(this@SettingsActivity, "Data imported", Toast.LENGTH_SHORT).show()
								else Toast.makeText(this@SettingsActivity, "Error importing data", Toast.LENGTH_SHORT).show()
								showImportingDataDialog.value = false
							}
						}
					} catch (e : Exception) {
						CoroutineScope(Dispatchers.Main).launch {
							Toast.makeText(this@SettingsActivity, "Error importing data", Toast.LENGTH_SHORT).show()
							showImportingDataDialog.value = false
						}
					}
				} ?: CoroutineScope(Dispatchers.Main).launch {
					Toast.makeText(this@SettingsActivity, "Error importing data", Toast.LENGTH_SHORT).show()
					showImportingDataDialog.value = false
				}
			}
		} catch (e : Exception) {
			CoroutineScope(Dispatchers.Main).launch {
				Toast.makeText(this@SettingsActivity, "Error importing data", Toast.LENGTH_SHORT).show()
				showImportingDataDialog.value = false
			}
		}
	}

	private val importJourneyFilePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
		try {
			it?.let { it1 ->
				contentResolver.openInputStream(it1)?.let { inputStream ->
					showImportingJourneyDataDialog.value = true
					try {
						viewModel.importFromJourney(inputStream = inputStream, richTextEditor()) {
							CoroutineScope(Dispatchers.Main).launch {
								if (it) Toast.makeText(this@SettingsActivity, "Data imported", Toast.LENGTH_SHORT).show()
								else Toast.makeText(this@SettingsActivity, "Error importing data", Toast.LENGTH_SHORT).show()
								showImportingJourneyDataDialog.value = false
							}
						}
					} catch (e : Exception) {
						CoroutineScope(Dispatchers.Main).launch {
							Toast.makeText(this@SettingsActivity, "Error importing data", Toast.LENGTH_SHORT).show()
							showImportingJourneyDataDialog.value = false
						}
					}
				} ?: CoroutineScope(Dispatchers.Main).launch {
					Toast.makeText(this@SettingsActivity, "Error importing data", Toast.LENGTH_SHORT).show()
					showImportingJourneyDataDialog.value = false
				}
			}
		} catch (e : Exception) {
			CoroutineScope(Dispatchers.Main).launch {
				Toast.makeText(this@SettingsActivity, "Error importing data", Toast.LENGTH_SHORT).show()
				showImportingJourneyDataDialog.value = false
			}
		}
	}

	companion object {
		enum class Navigator {
			BASE,
			PREFERENCES,
			THEME,
			SECURITY,
			EXTENSIONS,
			BACKUP,
			DATA,
			IMPORT,
			LOCAL_BACKUP,
			SNAPSHOT_WAREHOUSE,
			SYNC,
			ABOUT_US
		}

		enum class ImportType {
			GRAPHITE,
			GOOGLE_KEEP,
			JOURNEY,
			NOTESNOOK
		}

		val LocalOnNavigate = compositionLocalOf<(Navigator) -> Unit> { {} }
		val LocalNavigatorPath = compositionLocalOf<SnapshotStateList<Navigator>> { mutableStateListOf() }
		val LocalScrollState = compositionLocalOf { ScrollState(0) }
		val LocalFirebaseUser = compositionLocalOf<FirebaseUser?> { null }
		val LocalSignIn = compositionLocalOf { {} }
		val LocalSignOut = compositionLocalOf { {} }
		val LocalDeleteAccount = compositionLocalOf { {} }
		val LocalSetupLocalBackupFolder = compositionLocalOf { {} }
		val LocalRemoveLocalBackupFolder = compositionLocalOf { {} }
		val LocalBackupFolderPath = compositionLocalOf<String?> { null }
		val LocalSnapshotList = compositionLocalOf<SnapshotStateList<DocumentFile>> { mutableStateListOf() }
		val LocalTakeSnapshot = compositionLocalOf { {} }
		val LocalGetSnapshot = compositionLocalOf { {} }
		val LocalRestoreSnapshot = compositionLocalOf { {} }
		val LocalExportData = compositionLocalOf { {} }
		val LocalImportData = compositionLocalOf<(ImportType) -> Unit> { {} }

		val LocalAttachmentCount = compositionLocalOf { 0 }
		val LocalAttachmentProcessed = compositionLocalOf { 0 }
		val LocalBucketItemCount = compositionLocalOf { 0 }
		val LocalBucketItemProcessed = compositionLocalOf { 0 }
		val LocalBucketCount = compositionLocalOf { 0 }
		val LocalBucketProcessed = compositionLocalOf { 0 }
		val LocalChapterCount = compositionLocalOf { 0 }
		val LocalChapterProcessed = compositionLocalOf { 0 }
		val LocalNoteCount = compositionLocalOf { 0 }
		val LocalNoteProcessed = compositionLocalOf { 0 }
		val LocalTagCount = compositionLocalOf { 0 }
		val LocalTagProcessed = compositionLocalOf { 0 }
		val LocalPackageCount = compositionLocalOf { 0 }
		val LocalPackageProcessed = compositionLocalOf { 0 }
		val LocalImportDataCount = compositionLocalOf { 0 }
		val LocalImportDataProcessed = compositionLocalOf { 0 }

		val LocalShowTakeSnapshotDialog = compositionLocalOf { false }
		val LocalShowRestoreSnapshotDialog = compositionLocalOf { false }
		val LocalShowRestoringSnapshotDialog = compositionLocalOf { false }
		val LocalShowImportingDataDialog = compositionLocalOf { false }
		val LocalShowImportingJourneyDataDialog = compositionLocalOf { false }
		val LocalShowDeleteAccountDialog = compositionLocalOf { false }

		val LocalOpenBottomSheet = compositionLocalOf<(SettingsBottomSheetType) -> Unit> { {} }
		val LocalCloseBottomSheet = compositionLocalOf { { } }
		val LocalOpenDialog = compositionLocalOf<(SettingsDialogType, Any?) -> Unit> { { _, _ -> } }
		val LocalCloseDialog = compositionLocalOf<(SettingsDialogType) -> Unit> { {} }

		val LocalOnBackPressed = compositionLocalOf { {} }
	}
}
