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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.LoadingView
import com.syncodec.graphite.custom.richText.RichTextEditor
import com.syncodec.graphite.custom.richText.rememberRichTextEditorWithLifecycle
import com.syncodec.graphite.miscellaneous.DataStoreInstance
import com.syncodec.graphite.premiumComponent.PremiumActivity
import com.syncodec.graphite.settingsComponent.miscellaneous.DataExchangeDialog
import com.syncodec.graphite.settingsComponent.miscellaneous.TopBar
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
	private var showVaultScreen: MutableState<Boolean> = mutableStateOf(false)

	private var showToast: Boolean = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

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
			val dataStoreInstance = DataStoreInstance(this)
			val defaultNotebookKey by dataStoreInstance.getDefaultNotebookKey.collectAsState(initial = null)

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
		if (showVaultScreen.value) {
			showVaultScreen.value = false
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
				val dataStoreInstance = DataStoreInstance(this)
				viewModel.firebaseAuth.signOut()
				dataStoreInstance.putSuperExpiryTime(0)
				dataStoreInstance.putExpiryTime(0)
			}
			Action.CHANGE_FONT_FAMILY -> DataStoreInstance(context = this).putTypography(data as Int)
			Action.CHANGE_THEME -> DataStoreInstance(context = this).putTheme(data as Int)
			Action.CHANGE_BACKGROUND -> DataStoreInstance(context = this).putBackground(data as Int)
			Action.ADD_PASSCODE -> {
				activityState.evokeReason.value = EvokeReason.NEW_PASSCODE
				showVaultScreen.value = true
			}
			Action.CHANGE_PASSCODE -> {
				activityState.evokeReason.value = EvokeReason.CHANGE_PASSCODE
				showVaultScreen.value = true
			}
			Action.REMOVE_PASSCODE -> {
				activityState.evokeReason.value = EvokeReason.REMOVE_PASSCODE
				showVaultScreen.value = true
			}
			Action.BIOMETRIC_UNLOCK -> {
				activityState.evokeReason.value = EvokeReason.REMOVE_PASSCODE
				showVaultScreen.value = true
			}
			Action.IMPORT_GRAPHITE -> {
				activityState.dataExchange.value = DataExchange.IMPORT
				importFromGraphite.launch(arrayOf("application/zip"))
			}
			Action.IMPORT_JOURNEY -> {
				activityState.dataExchange.value = DataExchange.IMPORT
				importFromJourney.launch(arrayOf("application/zip"))
			}
			Action.IMPORT_DAY_ONE -> Toast.makeText(
				this,
				"This functionality is under development. Stay tuned...",
				Toast.LENGTH_SHORT
			).show()
			Action.IMPORT_GOOGLE_KEEP -> Toast.makeText(
				this,
				"This functionality is under development. Stay tuned...",
				Toast.LENGTH_SHORT
			).show()
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
			Action.EXPORT_BUCKET -> {
				Toast.makeText(
					this,
					"This functionality is under development. Stay tuned...",
					Toast.LENGTH_SHORT
				).show()
			}
			Action.GOOGLE_DRIVE -> {
				Toast.makeText(
					this,
					"This functionality is under development. Stay tuned...",
					Toast.LENGTH_SHORT
				).show()
			}
			Action.ONE_DRIVE -> {
				Toast.makeText(
					this,
					"This functionality is under development. Stay tuned...",
					Toast.LENGTH_SHORT
				).show()
			}
			Action.DROPBOX -> {
				Toast.makeText(
					this,
					"This functionality is under development. Stay tuned...",
					Toast.LENGTH_SHORT
				).show()
			}
			Action.WEBDAV -> {
				Toast.makeText(
					this,
					"This functionality is under development. Stay tuned...",
					Toast.LENGTH_SHORT
				).show()
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
		}
	}

	@OptIn(
		ExperimentalMaterial3Api::class,
		androidx.compose.animation.ExperimentalAnimationApi::class
	)
	@Composable
	private fun Screen() {
		val activityState = viewModel.activityState
		var showVaultScreen by showVaultScreen
		val evokeReason by activityState.evokeReason
		val email by viewModel.email

		val notebookList by viewModel.notebookList.collectAsState(initial = listOf())

		AnimatedContent(targetState = showVaultScreen) {
			if (it) {
				VaultScreen(
					evokeReason = evokeReason,
					onSuccess = { showVaultScreen = false }
				) {}
			} else {
				Scaffold(
					topBar = {
						TopBar(currentPath = activityState.currentPath) {
							onPerformAction(Action.BACK)
						}
					}
				) {
					AnimatedContent(targetState = activityState.currentPath.last()) {
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
							Path.SELECT_NOTEBOOK -> SelectNotebookScreen(notebookList = notebookList) { action, data ->
								onPerformAction(action, data)
							}
							Path.GRAPHITE -> GraphiteScreen { action, data ->
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
		ADD_PASSCODE,
		CHANGE_PASSCODE,
		REMOVE_PASSCODE,
		BIOMETRIC_UNLOCK,
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
			SELECT_NOTEBOOK,
			GRAPHITE
		}

		val PathNameMap: Map<Path, String> = mapOf(
			Path.BASE to "Settings",
			Path.LOGIN to "Login",
			Path.PREFERENCE to "Preference",
			Path.THEME to "Theme",
			Path.FONT_FAMILY to "Font Family",
			Path.SECURITY to "Security",
			Path.DATA to "Data",
			Path.SYNC to "Backup and Sync",
			Path.IMPORT to "Import",
			Path.EXPORT to "Export",
			Path.SELECT_NOTEBOOK to "Export Notebook",
			Path.GRAPHITE to "Graphite",
		)
		val PathIconMap: Map<Path, Int> = mapOf(
			Path.BASE to R.drawable.ic_settings,
			Path.LOGIN to R.drawable.ic_login,
			Path.PREFERENCE to R.drawable.ic_preference,
			Path.THEME to R.drawable.ic_theme,
			Path.FONT_FAMILY to R.drawable.ic_font_family,
			Path.SECURITY to R.drawable.ic_security,
			Path.DATA to R.drawable.ic_data,
			Path.SYNC to R.drawable.ic_sync,
			Path.IMPORT to R.drawable.ic_import,
			Path.EXPORT to R.drawable.ic_export,
			Path.SELECT_NOTEBOOK to R.drawable.ic_notebook,
			Path.GRAPHITE to R.drawable.ic_icon,
		)
	}
}
