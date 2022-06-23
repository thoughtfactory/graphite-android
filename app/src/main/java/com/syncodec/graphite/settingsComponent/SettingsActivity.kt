package com.syncodec.graphite.settingsComponent

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.custom.LoadingView
import com.syncodec.graphite.custom.richText.RichTextEditor
import com.syncodec.graphite.custom.richText.rememberRichTextEditorWithLifecycle
import com.syncodec.graphite.database.snapshot.Snapshot
import com.syncodec.graphite.premiumComponent.PremiumActivity
import com.syncodec.graphite.settingsComponent.miscellaneous.*
import com.syncodec.graphite.settingsComponent.screen.*
import com.syncodec.graphite.ui.theme.GraphiteBase
import com.syncodec.graphite.vaultComponent.EvokeReason
import com.syncodec.graphite.vaultComponent.VaultScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class SettingsActivity : ComponentActivity() {

	private val viewModel by viewModels<SettingsViewModel>()

	private var backupFolderPath: MutableState<String?> = mutableStateOf(null)

	private var showToast: Boolean = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		backupFolderPath.value = contentResolver.persistedUriPermissions.firstOrNull()?.uri?.path

		Firebase.auth.addAuthStateListener {
			viewModel.email.value = it.currentUser?.email
			if (showToast) {
				if (it.currentUser?.email == null) {
					Toast.makeText(
						this,
						"Logging out successful...",
						Toast.LENGTH_SHORT
					).show()
				} else {
					Toast.makeText(
						this,
						"Welcome ${it.currentUser?.email}",
						Toast.LENGTH_SHORT
					).show()
				}
			}
		}

		setContent {
			viewModel.activityState = rememberActivityState()
			val defaultNotebookKey by viewModel.dataStoreInstance.getDefaultNotebookKey.collectAsState(
				initial = null
			)

			Crossfade(targetState = defaultNotebookKey) {
				if (it == null) {
					LoadingView()
				} else {
					viewModel.activityState.richTextEditor.setOnSaveData(
						object : RichTextEditor.OnSaveDataListener {
							override fun onSaveData(data: String) {
								CoroutineScope(Dispatchers.IO).launch {
									try {
										viewModel.importNotes(
											notebookKey = defaultNotebookKey!!,
											data = data
										)
									} catch (exception: Exception) {

									}
									viewModel.activityState.isDataSaving.value = false
								}
							}
						}
					)

					GraphiteBase {
						val systemUiController = rememberSystemUiController()
						systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
						systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.background)
						Screen()
					}
				}
			}
		}
	}

	private val signInLauncher =
		registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
			onSignInResult(result = result)
		}

	private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
		if (result.resultCode == RESULT_OK) {
			if (viewModel.firebaseAuth.currentUser != null) {
			}
		} else {
			Toast.makeText(this, "Sign in failed", Toast.LENGTH_LONG).show()
		}
	}

	override fun onBackPressed() {
		if (viewModel.activityState.showVaultScreen.value) {
			viewModel.activityState.showVaultScreen.value = false
		} else {
			if (viewModel.activityState.currentPath.last() == Path.BASE) super.onBackPressed() else viewModel.activityState.currentPath.removeLast()
		}
	}

	private val importFromGraphite =
		registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
			viewModel.importFromGraphite(uri)
		}

	private val importFromJourney =
		registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
			viewModel.importFromJourney(uri)
		}

	private val localBackupDirSelector =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
			it.data?.data.apply {
				this?.let { it1 ->
					contentResolver.takePersistableUriPermission(
						this,
						Intent.FLAG_GRANT_READ_URI_PERMISSION
					)
					backupFolderPath.value =
						contentResolver.persistedUriPermissions?.firstOrNull()?.uri?.path
				}
			}
		}

	private fun onPerformAction(action: Action, data: Any? = null) {
		val activityState = viewModel.activityState

		when (action) {
			Action.BACK -> onBackPressed()
			Action.NAVIGATION -> activityState.currentPath.add(data as Path)
			Action.SUBSCRIPTION -> startActivity(Intent(this, PremiumActivity::class.java))
			Action.LOGIN -> {
				val providers =
					arrayListOf(
						AuthUI.IdpConfig.GoogleBuilder().setScopes(listOf("profile")).build()
					)

				val signInIntent = AuthUI.getInstance()
					.createSignInIntentBuilder()
					.setAvailableProviders(providers)
					.setAlwaysShowSignInMethodScreen(false)
					.build()
				signInLauncher.launch(signInIntent)
			}
			Action.LOGOUT -> {
				viewModel.firebaseAuth.signOut()
				viewModel.dataStoreInstance.putSuperExpiryTime(0)
				viewModel.dataStoreInstance.putExpiryTime(0)
			}
			Action.CHANGE_FONT_FAMILY -> viewModel.dataStoreInstance.putTypography(data as Int)
			Action.CHANGE_THEME -> viewModel.dataStoreInstance.putTheme(data as Int)
			Action.CHANGE_BACKGROUND -> viewModel.dataStoreInstance.putBackground(data as Int)
			Action.ADD_PASSCODE -> {
				activityState.evokeReason.value = EvokeReason.NEW_PASSCODE
				viewModel.activityState.showVaultScreen.value = true
			}
			Action.CHANGE_PASSCODE -> {
				activityState.evokeReason.value = EvokeReason.CHANGE_PASSCODE
				viewModel.activityState.showVaultScreen.value = true
			}
			Action.REMOVE_PASSCODE -> {
				activityState.evokeReason.value = EvokeReason.REMOVE_PASSCODE
				viewModel.activityState.showVaultScreen.value = true
			}
			Action.BIOMETRIC_UNLOCK -> onPerformAction(Action.COMING_SOON)
			Action.IMPORT_GRAPHITE -> {
				activityState.dataExchange.value = DataExchange.IMPORT
				importFromGraphite.launch(arrayOf("application/zip"))
			}
			Action.IMPORT_JOURNEY -> {
				activityState.dataExchange.value = DataExchange.IMPORT
				importFromJourney.launch(arrayOf("application/zip"))
			}
			Action.IMPORT_DAY_ONE -> onPerformAction(Action.COMING_SOON)
			Action.IMPORT_GOOGLE_KEEP -> onPerformAction(Action.COMING_SOON)
			Action.EXPORT_NOTEBOOK -> {
				activityState.dataExchange.value = DataExchange.EXPORT
				CoroutineScope(Dispatchers.IO).launch {
					val exportDirPath = viewModel.exportNotes(data as String)
					if (exportDirPath != null) {
						val file = File(exportDirPath)
						val uri: Uri = FileProvider.getUriForFile(
							this@SettingsActivity,
							"com.syncodec.fileprovider",
							file
						)

						val intent = ShareCompat.IntentBuilder.from(this@SettingsActivity)
							.setType("application/zip")
							.setStream(uri)
							.createChooserIntent()
							.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

						startActivity(intent)
					}
				}
			}
			Action.EXPORT_BUCKET -> onPerformAction(Action.COMING_SOON)
			Action.GOOGLE_DRIVE -> onPerformAction(Action.COMING_SOON)
			Action.ONE_DRIVE -> onPerformAction(Action.COMING_SOON)
			Action.DROPBOX -> onPerformAction(Action.COMING_SOON)
			Action.WEBDAV -> onPerformAction(Action.COMING_SOON)
			Action.SETUP_LOCAL_BACKUP_FOLDER -> {
				Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
					localBackupDirSelector.launch(this)
				}
			}
			Action.REMOVE_LOCAL_BACKUP_FOLDER -> {
				contentResolver.persistedUriPermissions.forEach {
					contentResolver.releasePersistableUriPermission(
						it.uri,
						Intent.FLAG_GRANT_READ_URI_PERMISSION
					)
				}
				backupFolderPath.value =
					contentResolver.persistedUriPermissions.firstOrNull()?.uri?.path
			}
			Action.TAKE_SNAPSHOT -> {
				val uri = contentResolver.persistedUriPermissions.firstOrNull()?.uri
				if (uri == null) {
					Toast.makeText(
						this,
						"Error generating snapshot. Try setting up backup folder again",
						Toast.LENGTH_SHORT
					).show()
				} else {
					viewModel.takeSnapshot(uri = uri)
				}
			}
			Action.REFRESH_SNAPSHOT -> {
				val uri = contentResolver.persistedUriPermissions.firstOrNull()?.uri
				if (uri == null) {
					Toast.makeText(
						this,
						"Error refreshing snapshots",
						Toast.LENGTH_SHORT
					).show()
				} else {
					viewModel.refreshSnapshot(uri = uri)
				}
			}
			Action.OPEN_SNAPSHOT -> {
				viewModel.currentSnapshot.value = data as Snapshot
				onPerformAction(Action.NAVIGATION, Path.RESTORE_SNAPSHOT)
			}
			Action.RESTORE_SNAPSHOT_CONSENT -> activityState.showRestoreSnapshotConsent.value = true
			Action.RESTORE_SNAPSHOT -> {
				viewModel.restoreSnapshot()
				activityState.showRestoreSnapshotConsent.value = false
			}
			Action.ON_SNAPSHOT_COMPLETE -> {
				Toast.makeText(this, "Snapshot created successfully", Toast.LENGTH_SHORT).show()

				val uri = contentResolver.persistedUriPermissions.firstOrNull()?.uri
				if (uri == null) {
					Toast.makeText(
						this,
						"Error refreshing snapshots",
						Toast.LENGTH_SHORT
					).show()
				} else {
					viewModel.refreshSnapshot(uri)
				}
			}
			Action.ON_RESTORE_COMPLETE -> {
				Toast.makeText(this, "Snapshot restoration complete", Toast.LENGTH_SHORT).show()
			}
			Action.DONT_RESTORE_SNAPSHOT -> activityState.showRestoreSnapshotConsent.value = false
			Action.ACTION_VIEW -> {
				viewModel.currentSnapshotFile.value = data as DocumentFile
				onPerformAction(Action.NAVIGATION, Path.SNAPSHOT_PREVIEW)
			}
			Action.POLICY -> {
				val url = "https://graphite.syncodec.com/policy.html"
				Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { startActivity(this) }
			}
			Action.TERMS -> {
				val url = "https://graphite.syncodec.com/terms.html"
				Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { startActivity(this) }
			}
			Action.SHARE_A_WORD -> {
				try {
					val shareIntent = Intent(Intent.ACTION_SEND)
					shareIntent.type = "text/plain"
					shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Graphite")
					var shareMessage =
						"\nHey... Check out Graphite, an everyday diary and bucket list\n\n"
					shareMessage =
						"""
						${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
						""".trimIndent()
					shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
					startActivity(Intent.createChooser(shareIntent, "choose one"))
				} catch (e: Exception) {
				}
			}
			Action.RATE_US -> startActivity(
				Intent(
					Intent.ACTION_VIEW,
					Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
				)
			)
			Action.OPEN_SOURCE_LICENSES -> startActivity(
				Intent(
					this,
					OssLicensesMenuActivity::class.java
				)
			)
			Action.INSTAGRAM -> {
				val uri = Uri.parse("https://www.instagram.com/graphite.diary/?hl=en")
				val likeIng = Intent(Intent.ACTION_VIEW, uri)

				likeIng.setPackage("com.instagram.android")

				try {
					startActivity(likeIng)
				} catch (e: ActivityNotFoundException) {
					startActivity(
						Intent(
							Intent.ACTION_VIEW,
							Uri.parse("https://www.instagram.com/graphite.diary/?hl=en")
						)
					)
				}
			}
			Action.COMING_SOON -> Toast.makeText(
				this,
				"This functionality is under development. Stay tuned...",
				Toast.LENGTH_SHORT
			).show()
		}
	}

	@OptIn(
		ExperimentalMaterial3Api::class,
		ExperimentalAnimationApi::class
	)
	@Composable
	private fun Screen() {
		val activityState = viewModel.activityState
		var showVaultScreen by viewModel.activityState.showVaultScreen
		val evokeReason by activityState.evokeReason
		val email by viewModel.email

		val notebookList by viewModel.notebookList.collectAsState(initial = listOf())
		val currentSnapshot by viewModel.currentSnapshot
		val currentSnapshotFile by viewModel.currentSnapshotFile

		val decayAnimationSpec = rememberSplineBasedDecay<Float>()
		val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
			decayAnimationSpec,
			rememberTopAppBarScrollState()
		)

		AnimatedContent(targetState = showVaultScreen) {
			if (it) {
				VaultScreen(
					evokeReason = evokeReason,
					onSuccess = { showVaultScreen = false }
				) {}
			} else {
				Scaffold(
					topBar = {
						TopBar(
							title = PathNameMap[activityState.currentPath.last()]!!,
							scrollBehavior = scrollBehavior
						) {
							onPerformAction(Action.BACK)
						}
					},
					modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
				) {
					AnimatedContent(
						targetState = activityState.currentPath.last(),
						modifier = Modifier.padding(it)
					) {
						when (it) {
							Path.BASE -> BaseScreen(
								email = email
							) { action, path -> onPerformAction(action, path) }
							Path.LOGIN -> {}
							Path.PREFERENCE -> PreferenceScreen { action, path ->
								onPerformAction(action, path)
							}
//							Path.THEME -> ThemeScreen { action, theme ->
//								onPerformAction(action, theme)
//							}
							Path.FONT_FAMILY -> FontFamilyScreen { action, typography ->
								onPerformAction(action, typography)
							}
							Path.SECURITY -> SecurityScreen { action, data ->
								onPerformAction(action, data)
							}
							Path.DATA -> DataScreen { action, data ->
								onPerformAction(action, data)
							}
							Path.IMPORT -> ImportScreen { action, data ->
								onPerformAction(action, data)
							}
							Path.EXPORT -> ExportScreen { action, data ->
								onPerformAction(action, data)
							}
							Path.SYNC -> SyncScreen { action, data ->
								onPerformAction(action, data)
							}
							Path.LOCAL_BACKUP -> LocalBackupScreen(
								backFolderPath = backupFolderPath.value
							) { action, data -> onPerformAction(action, data) }
							Path.SNAPSHOT_WAREHOUSE -> SnapshotWarehouseScreen(
								snapshotList = viewModel.snapshotList,
								isRefreshing = viewModel.isSnapshotRefreshing.value,
							) { action, data -> onPerformAction(action, data) }
							Path.RESTORE_SNAPSHOT -> RestoreSnapshotScreen(
								snapshot = viewModel.currentSnapshot.value,
							) { action, data -> onPerformAction(action, data) }
							Path.SNAPSHOT_NOTEBOOK -> SnapshotContentScreen(
								documentFile = currentSnapshot?.documentFile?.findFile("Notebooks"),
							) { action, data -> onPerformAction(action, data) }
							Path.SNAPSHOT_CHAPTER -> SnapshotContentScreen(
								documentFile = currentSnapshot?.documentFile?.findFile("Chapters"),
							) { action, data -> onPerformAction(action, data) }
							Path.SNAPSHOT_NOTE -> SnapshotContentScreen(
								documentFile = currentSnapshot?.documentFile?.findFile("Notes"),
							) { action, data -> onPerformAction(action, data) }
							Path.SNAPSHOT_ATTACHMENT -> SnapshotContentScreen(
								documentFile = currentSnapshot?.documentFile?.findFile("Attachments"),
							) { action, data -> onPerformAction(action, data) }
							Path.SNAPSHOT_BUCKET -> SnapshotContentScreen(
								documentFile = currentSnapshot?.documentFile?.findFile("Buckets"),
							) { action, data -> onPerformAction(action, data) }
							Path.SNAPSHOT_BUCKET_ITEM -> SnapshotContentScreen(
								documentFile = currentSnapshot?.documentFile?.findFile("Bucket Items"),
							) { action, data -> onPerformAction(action, data) }
							Path.SNAPSHOT_TAG -> SnapshotPreviewScreen(
								documentFile = currentSnapshot?.documentFile?.findFile("tags.json"),
							) { action, data -> onPerformAction(action, data) }
							Path.SNAPSHOT_CONNECTION -> SnapshotPreviewScreen(
								documentFile = currentSnapshot?.documentFile?.findFile("connections.json"),
							) { action, data -> onPerformAction(action, data) }
							Path.SNAPSHOT_PREVIEW -> SnapshotPreviewScreen(
								documentFile = currentSnapshotFile,
							) { action, data -> onPerformAction(action, data) }
							Path.SELECT_NOTEBOOK -> SelectNotebookScreen(notebookList = notebookList) { action, data ->
								onPerformAction(action, data)
							}
							Path.ABOUT_US -> AboutUsScreen { action, data ->
								onPerformAction(action, data)
							}
						}
					}
					DataExchangeDialog(
						dataExchange = activityState.dataExchange.value,
						dataExchangeSize = activityState.exchangeDataSize.value,
						currentImportFileIndex = activityState.currentImportFileIndex.value,
						currentImportFileName = activityState.currentImportFileName.value,
					)
					TakeSnapshotDialog(
						timestamp = System.currentTimeMillis(),
						totalNotebook = activityState.totalNotebook.value,
						processedNotebook = activityState.processedNotebook.value,
						totalChapter = activityState.totalChapter.value,
						processedChapter = activityState.processedChapter.value,
						totalNote = activityState.totalNote.value,
						processedNote = activityState.processedNote.value,
						totalAttachment = activityState.totalAttachment.value,
						processedAttachment = activityState.processedAttachment.value,
						totalBucket = activityState.totalBucket.value,
						processedBucket = activityState.processedBucket.value,
						totalBucketItem = activityState.totalBucketItem.value,
						processedBucketItem = activityState.processedBucketItem.value,
						totalTag = activityState.totalTag.value,
						processedTag = activityState.processedTag.value,
						isSnapshotting = activityState.isSnapshotting.value
					) { onPerformAction(Action.ON_SNAPSHOT_COMPLETE) }
					RestoreSnapshotDialog(
						timestamp = System.currentTimeMillis(),
						totalNotebook = activityState.totalNotebook.value,
						processedNotebook = activityState.processedNotebook.value,
						totalChapter = activityState.totalChapter.value,
						processedChapter = activityState.processedChapter.value,
						totalNote = activityState.totalNote.value,
						processedNote = activityState.processedNote.value,
						totalAttachment = activityState.totalAttachment.value,
						processedAttachment = activityState.processedAttachment.value,
						totalBucket = activityState.totalBucket.value,
						processedBucket = activityState.processedBucket.value,
						totalBucketItem = activityState.totalBucketItem.value,
						processedBucketItem = activityState.processedBucketItem.value,
						totalTag = activityState.totalTag.value,
						processedTag = activityState.processedTag.value,
						isRestoring = activityState.isRestoring.value
					) { onPerformAction(Action.ON_RESTORE_COMPLETE) }
					RestoreSnapshotConsentDialog(
						showDialog = activityState.showRestoreSnapshotConsent.value
					) { action, data -> onPerformAction(action, data) }
				}
			}
		}
	}

	inner class ActivityState constructor(
		val currentPath: SnapshotStateList<Path> = mutableStateListOf(Path.BASE),
		val evokeReason: MutableState<EvokeReason> = mutableStateOf(EvokeReason.UNLOCK_VAULT),
		val dataExchange: MutableState<DataExchange> = mutableStateOf(DataExchange.NONE),
		val exchangeDataSize: MutableState<Int> = mutableStateOf(0),
		val currentImportFileIndex: MutableState<Int> = mutableStateOf(0),
		val currentImportFileName: MutableState<String?> = mutableStateOf(null),
		val isDataSaving: MutableState<Boolean> = mutableStateOf(false),
		val richTextEditor: RichTextEditor,
		val showVaultScreen: MutableState<Boolean> = mutableStateOf(false),
		val totalNotebook: MutableState<Int> = mutableStateOf(0),
		val processedNotebook: MutableState<Int> = mutableStateOf(0),
		val totalChapter: MutableState<Int> = mutableStateOf(0),
		val processedChapter: MutableState<Int> = mutableStateOf(0),
		val totalNote: MutableState<Int> = mutableStateOf(0),
		val processedNote: MutableState<Int> = mutableStateOf(0),
		val totalAttachment: MutableState<Int> = mutableStateOf(0),
		val processedAttachment: MutableState<Int> = mutableStateOf(0),
		val totalBucket: MutableState<Int> = mutableStateOf(0),
		val processedBucket: MutableState<Int> = mutableStateOf(0),
		val totalBucketItem: MutableState<Int> = mutableStateOf(0),
		val processedBucketItem: MutableState<Int> = mutableStateOf(0),
		val totalTag: MutableState<Int> = mutableStateOf(0),
		val processedTag: MutableState<Int> = mutableStateOf(0),
		val isSnapshotting: MutableState<Boolean> = mutableStateOf(false),
		val isRestoring: MutableState<Boolean> = mutableStateOf(false),
		val showRestoreSnapshotConsent: MutableState<Boolean> = mutableStateOf(false)
	)

	@Composable
	fun rememberActivityState(
		richTextEditor: RichTextEditor = rememberRichTextEditorWithLifecycle()
	) = remember { ActivityState(richTextEditor = richTextEditor) }

	enum class Action {
		BACK,
		NAVIGATION,
		SUBSCRIPTION,
		POLICY,
		TERMS,
		SHARE_A_WORD,
		RATE_US,
		OPEN_SOURCE_LICENSES,
		INSTAGRAM,
		LOGIN,
		LOGOUT,
		CHANGE_FONT_FAMILY,
		CHANGE_THEME,
		CHANGE_BACKGROUND,
		IMPORT_GRAPHITE,
		IMPORT_JOURNEY,
		IMPORT_DAY_ONE,
		IMPORT_GOOGLE_KEEP,
		EXPORT_NOTEBOOK,
		EXPORT_BUCKET,
		GOOGLE_DRIVE,
		ONE_DRIVE,
		DROPBOX,
		WEBDAV,
		SETUP_LOCAL_BACKUP_FOLDER,
		REMOVE_LOCAL_BACKUP_FOLDER,
		TAKE_SNAPSHOT,
		REFRESH_SNAPSHOT,
		OPEN_SNAPSHOT,
		RESTORE_SNAPSHOT_CONSENT,
		RESTORE_SNAPSHOT,
		ON_SNAPSHOT_COMPLETE,
		ON_RESTORE_COMPLETE,
		DONT_RESTORE_SNAPSHOT,
		ACTION_VIEW,
		ADD_PASSCODE,
		CHANGE_PASSCODE,
		REMOVE_PASSCODE,
		BIOMETRIC_UNLOCK,
		COMING_SOON
	}

	enum class DataExchange {
		IMPORT,
		EXPORT,
		NONE
	}

	companion object {
		enum class Path {
			BASE,
			LOGIN,
			PREFERENCE,
			THEME,
			FONT_FAMILY,
			SECURITY,
			DATA,
			IMPORT,
			EXPORT,
			SYNC,
			LOCAL_BACKUP,
			SNAPSHOT_WAREHOUSE,
			RESTORE_SNAPSHOT,
			SNAPSHOT_NOTEBOOK,
			SNAPSHOT_CHAPTER,
			SNAPSHOT_NOTE,
			SNAPSHOT_ATTACHMENT,
			SNAPSHOT_BUCKET,
			SNAPSHOT_BUCKET_ITEM,
			SNAPSHOT_TAG,
			SNAPSHOT_CONNECTION,
			SNAPSHOT_PREVIEW,
			SELECT_NOTEBOOK,
			ABOUT_US
		}

		val PathNameMap: Map<Path, String> = mapOf(
			Path.BASE to "Settings",
			Path.LOGIN to "Login",
			Path.PREFERENCE to "Preference",
			Path.THEME to "Theme",
			Path.FONT_FAMILY to "Font Family",
			Path.SECURITY to "Security",
			Path.DATA to "Data",
			Path.IMPORT to "Import",
			Path.EXPORT to "Export",
			Path.SYNC to "Backup and Sync",
			Path.LOCAL_BACKUP to "Local Backup",
			Path.SNAPSHOT_WAREHOUSE to "Snapshot Warehouse",
			Path.RESTORE_SNAPSHOT to "Restore Snapshot",
			Path.SNAPSHOT_NOTEBOOK to "Notebook Snapshot",
			Path.SNAPSHOT_CHAPTER to "Chapter Snapshot",
			Path.SNAPSHOT_NOTE to "Note Snapshot",
			Path.SNAPSHOT_ATTACHMENT to "Attachment Snapshot",
			Path.SNAPSHOT_BUCKET to "Bucket Snapshot",
			Path.SNAPSHOT_BUCKET_ITEM to "Bucket Item Snapshot",
			Path.SNAPSHOT_TAG to "Tag Snapshot",
			Path.SNAPSHOT_CONNECTION to "Connection Snapshot",
			Path.SNAPSHOT_PREVIEW to "Preview",
			Path.SELECT_NOTEBOOK to "Export Notebook",
			Path.ABOUT_US to "About Us",
		)
	}
}
