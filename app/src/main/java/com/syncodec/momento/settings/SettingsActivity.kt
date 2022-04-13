package com.syncodec.momento.settings

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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.custom.LoadingView
import com.syncodec.momento.custom.richText.RichTextEditor
import com.syncodec.momento.custom.richText.rememberRichTextEditorWithLifecycle
import com.syncodec.momento.miscellaneous.DataStore
import com.syncodec.momento.miscellaneous.FileUtils.Companion.getFileFromUri
import com.syncodec.momento.miscellaneous.logger
import com.syncodec.momento.settings.miscellaneous.ImportDialog
import com.syncodec.momento.settings.miscellaneous.TopBar
import com.syncodec.momento.settings.screen.*
import com.syncodec.momento.ui.theme.MomentoTheme
import com.syncodec.momento.vaultComponent.EvokeReason
import com.syncodec.momento.vaultComponent.VaultScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.zip.ZipFile


class SettingsActivity : ComponentActivity() {

	private val viewModel by viewModels<SettingsViewModel>()
	private var showVaultScreen: MutableState<Boolean> = mutableStateOf(false)

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

					MomentoTheme {
						val systemUiController = rememberSystemUiController()
						systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
						systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.background)
						Screen()
					}
				}
			}
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

	private fun onClick(click: Click, data: Any? = null) {
		val activityState = viewModel.activityState

		when (click) {
			Click.NAVIGATION -> activityState.currentPath.add(data as Path)
			Click.CHANGE_FONT_FAMILY -> DataStore(context = this).putTypography(data as Int)
			Click.CHANGE_THEME -> DataStore(context = this).putTheme(data as Int)
			Click.ADD_PASSCODE -> {
				activityState.evokeReason.value = EvokeReason.NEW_PASSCODE
				showVaultScreen.value = true
			}
			Click.CHANGE_PASSCODE -> {
				activityState.evokeReason.value = EvokeReason.CHANGE_PASSCODE
				showVaultScreen.value = true
			}
			Click.REMOVE_PASSCODE -> {
				activityState.evokeReason.value = EvokeReason.REMOVE_PASSCODE
				showVaultScreen.value = true
			}
			Click.BIOMETRIC_UNLOCK -> {
				activityState.evokeReason.value = EvokeReason.REMOVE_PASSCODE
				showVaultScreen.value = true
			}
			Click.JOURNEY -> {
				activityState.dataExchange.value = DataExchange.IMPORT
				selectDocumentToImport.launch(arrayOf("application/zip"))
			}
			Click.EXPORT_NOTEBOOK -> {
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
			Click.EXPORT_BUCKET -> {
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

		val notebookMap = viewModel.notebookMap

		AnimatedContent(targetState = showVaultScreen) {
			if (it) {
				VaultScreen(
					evokeReason = evokeReason,
					onSuccess = { showVaultScreen = false }
				) {

				}
			} else {
				Scaffold(
					topBar = { TopBar(currentPath = activityState.currentPath) }
				) {
					AnimatedContent(targetState = activityState.currentPath.last()) {
						when (it) {
							Path.BASE -> BaseScreen { click, path -> onClick(click, path) }
							Path.LOGIN -> {}
							Path.PREFERENCE -> PreferenceScreen { click, path ->
								onClick(click, path)
							}
							Path.THEME -> ThemeScreen { click, theme -> onClick(click, theme) }
							Path.FONT_FAMILY -> FontFamilyScreen { click, typography ->
								onClick(click, typography)
							}
							Path.SECURITY -> SecurityScreen { click, data -> onClick(click, data) }
							Path.DATA -> DataScreen { click, data -> onClick(click, data) }
							Path.IMPORT -> ImportScreen { click, data -> onClick(click, data) }
							Path.EXPORT -> ExportScreen { click, data -> onClick(click, data) }
							Path.SELECT_NOTEBOOK -> SelectNotebookScreen(notebookMap = notebookMap) { click, data ->
								onClick(click, data)
							}
							Path.PRIVACY_POLICY -> logger("TODO")
							Path.TERMS -> logger("TODO")
							Path.ABOUT_US -> logger("TODO")
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

	enum class Click {
		NAVIGATION,
		CHANGE_FONT_FAMILY,
		CHANGE_THEME,
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
			PRIVACY_POLICY,
			TERMS,
			ABOUT_US
		}

		val PathMap: Map<Path, String> = mapOf(
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
			Path.PRIVACY_POLICY to "Privacy Policy",
			Path.TERMS to "Terms Of Service",
			Path.ABOUT_US to "About Us",
		)
	}
}
