package com.syncodec.graphite.settingsComponent

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
import androidx.compose.material.ExperimentalMaterialApi
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.LoadingView
import com.syncodec.graphite.custom.richText.RichTextEditor
import com.syncodec.graphite.custom.richText.rememberRichTextEditorWithLifecycle
import com.syncodec.graphite.miscellaneous.DataStore
import com.syncodec.graphite.miscellaneous.FileUtils.Companion.getFileFromUri
import com.syncodec.graphite.premiumComponent.PremiumActivity
import com.syncodec.graphite.settingsComponent.miscellaneous.ImportDialog
import com.syncodec.graphite.settingsComponent.miscellaneous.TopBar
import com.syncodec.graphite.settingsComponent.screen.*
import com.syncodec.graphite.ui.theme.GraphiteTheme
import com.syncodec.graphite.vaultComponent.EvokeReason
import com.syncodec.graphite.vaultComponent.VaultScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.zip.ZipFile


class SettingsActivity : ComponentActivity() {

	private val viewModel by viewModels<SettingsViewModel>()
	private var showVaultScreen: MutableState<Boolean> = mutableStateOf(false)
	val firebaseAuth = FirebaseAuth.getInstance()

	@OptIn(ExperimentalMaterialApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			viewModel.activityState = rememberActivityState()
			val dataStore = DataStore(this)
			val defaultNotebookKey by dataStore.getDefaultNotebookKey.collectAsState(initial = null)

			Crossfade(targetState = defaultNotebookKey) {
				if (it == null) {
					LoadingView()
				} else {
					viewModel.activityState.richTextEditor.setOnSaveData(
						object : RichTextEditor.OnSaveDataListener {
							override fun onSaveData(data: String) {
								CoroutineScope(Dispatchers.IO).launch {
									viewModel.insertNote(
										notebookKey = defaultNotebookKey!!,
										data = data
									)
									viewModel.activityState.isDataSaving.value = false
								}
							}
						}
					)

					GraphiteTheme {
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
			if (firebaseAuth.currentUser != null) {
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

	private val selectDocumentToImport =
		registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
			CoroutineScope(Dispatchers.Main).launch {
				val activityState = viewModel.activityState
				val file = getFileFromUri(uri = uri)
				if (file != null) {
					try {
						val zipFile = ZipFile(file)
						val fileList = zipFile.entries().toList()
							.filter { it.name.split(".").lastOrNull() == "json" }
						activityState.exchangeDataSize.value = fileList.size

						var currentIndex = 0

						while (true) {
							if (currentIndex < fileList.size) {
								if (activityState.isDataSaving.value) {
									delay(10)
								} else {
									val jsonString = String(
										zipFile.getInputStream(fileList[currentIndex]).readBytes()
									)
									activityState.richTextEditor.exec("editor.importData($jsonString);")
									activityState.isDataSaving.value = true
									activityState.currentImportFileIndex.value = currentIndex
									currentIndex++
								}
							} else {
								break
							}
						}

						activityState.dataExchange.value = DataExchange.NONE
						activityState.exchangeDataSize.value = 0
						activityState.currentImportFileIndex.value = 1
						CoroutineScope(Dispatchers.Main).launch {
							Toast.makeText(
								this@SettingsActivity,
								"${fileList.size} entries imported",
								Toast.LENGTH_LONG
							).show()
						}
					} catch (exception: Exception) {
						activityState.dataExchange.value = DataExchange.NONE
						activityState.exchangeDataSize.value = 0
						activityState.currentImportFileIndex.value = 1

						exception.printStackTrace()

						CoroutineScope(Dispatchers.Main).launch {
							Toast.makeText(
								this@SettingsActivity,
								"Sorry, can't process selected file",
								Toast.LENGTH_LONG
							).show()
						}
					}
				} else {
					activityState.dataExchange.value = DataExchange.NONE
					activityState.exchangeDataSize.value = 0
					activityState.currentImportFileIndex.value = 1
				}
			}
		}

	private fun onPerformAction(action: Action, data: Any? = null) {
		val activityState = viewModel.activityState

		when (action) {
			Action.BACK -> onBackPressed()
			Action.NAVIGATION -> activityState.currentPath.add(data as Path)
			Action.SUBSCRIPTION -> {
				Intent(this, PremiumActivity::class.java).apply { startActivity(this) }
			}
			Action.INSTAGRAM -> null
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
			Action.CHANGE_FONT_FAMILY -> DataStore(context = this).putTypography(data as Int)
			Action.CHANGE_THEME -> DataStore(context = this).putTheme(data as Int)
			Action.CHANGE_BACKGROUND -> DataStore(context = this).putBackground(data as Int)
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
			Action.JOURNEY -> {
				activityState.dataExchange.value = DataExchange.IMPORT
				selectDocumentToImport.launch(arrayOf("application/zip"))
			}
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
							.setChooserTitle("Choose bar")
							.createChooserIntent()
							.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

						startActivity(intent)
					}
				}
			}
			Action.EXPORT_BUCKET -> {
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

		val notebookList by viewModel.notebookList.collectAsState(initial = listOf())

		AnimatedContent(targetState = showVaultScreen) {
			if (it) {
				VaultScreen(
					evokeReason = evokeReason,
					onSuccess = { showVaultScreen = false }
				) {

				}
			} else {
				Scaffold(
					topBar = {
						TopBar(currentPath = activityState.currentPath) {
							onPerformAction(
								Action.BACK
							)
						}
					}
				) {
					AnimatedContent(targetState = activityState.currentPath.last()) {
						when (it) {
							Path.BASE -> BaseScreen { action, path ->
								onPerformAction(action, path)
							}
							Path.LOGIN -> {}
							Path.PREFERENCE -> PreferenceScreen { action, path ->
								onPerformAction(action, path)
							}
							Path.THEME -> ThemeScreen { action, theme ->
								onPerformAction(action, theme)
							}
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
							Path.SELECT_NOTEBOOK -> SelectNotebookScreen(notebookList = notebookList) { action, data ->
								onPerformAction(action, data)
							}
							Path.ABOUT_US -> AboutUsScreen { action, data ->
								onPerformAction(action, data)
							}
						}
					}
					ImportDialog(
						dataExchange = activityState.dataExchange.value,
						dataExchangeSize = activityState.exchangeDataSize.value,
						currentImportFileIndex = activityState.currentImportFileIndex.value
					)
				}
			}
		}
	}

	@OptIn(ExperimentalMaterialApi::class)
	inner class ActivityState @OptIn(ExperimentalPermissionsApi::class) constructor(
		val currentPath: SnapshotStateList<Path> = mutableStateListOf(Path.BASE),
		val evokeReason: MutableState<EvokeReason> = mutableStateOf(EvokeReason.UNLOCK_VAULT),
		val dataExchange: MutableState<DataExchange> = mutableStateOf(DataExchange.NONE),
		val exchangeDataSize: MutableState<Int> = mutableStateOf(0),
		val currentImportFileIndex: MutableState<Int> = mutableStateOf(0),
		val isDataSaving: MutableState<Boolean> = mutableStateOf(false),
		val richTextEditor: RichTextEditor,
	)

	@OptIn(ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
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
		INSTAGRAM,
		LOGIN,
		CHANGE_FONT_FAMILY,
		CHANGE_THEME,
		CHANGE_BACKGROUND,
		JOURNEY,
		EXPORT_NOTEBOOK,
		EXPORT_BUCKET,
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
			Path.SELECT_NOTEBOOK to "Select Notebook",
			Path.ABOUT_US to "About Us",
		)
		val PathIconMap: Map<Path, Int> = mapOf(
			Path.BASE to R.drawable.ic_settings,
			Path.LOGIN to R.drawable.ic_login,
			Path.PREFERENCE to R.drawable.ic_preference,
			Path.THEME to R.drawable.ic_theme,
			Path.FONT_FAMILY to R.drawable.ic_font_family,
			Path.SECURITY to R.drawable.ic_security,
			Path.DATA to R.drawable.ic_data,
			Path.IMPORT to R.drawable.ic_import,
			Path.EXPORT to R.drawable.ic_export,
			Path.SELECT_NOTEBOOK to R.drawable.ic_notebook,
			Path.ABOUT_US to R.drawable.ic_about_us,
		)
	}
}
