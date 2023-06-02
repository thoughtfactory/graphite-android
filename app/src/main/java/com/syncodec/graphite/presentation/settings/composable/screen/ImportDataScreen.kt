package com.syncodec.graphite.presentation.settings.composable.screen

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScaffold
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButtonDefaults
import com.syncodec.graphite.presentation.settings.composable.dialog.ImportDataDialog
import com.syncodec.graphite.presentation.settings.composable.dialog.ImportDataDialogState
import com.syncodec.graphite.presentation.settings.composable.dialog.ImportingDialog
import com.syncodec.graphite.utils.importData.ImportDataUtil.Companion.validateGoogleKeepFile
import com.syncodec.graphite.utils.importData.ImportDataUtil.Companion.validateGraphiteExportFile
import com.syncodec.graphite.utils.importData.ImportDataUtil.Companion.validateJourneyFile
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.koin.androidx.compose.koinViewModel
import java.io.Closeable


@Preview
@Composable
fun ImportDataScreen(
	onClickBack: () -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val journeyImporterScope = rememberCoroutineScope()
	val viewModel: ImportDataViewModel = koinViewModel()

	val getTextListener = remember {
		object : RichTextEditor.GetTextListener {
			override fun onGetData(
				requestData : RichTextEditor.Companion.RequestData,
				dataObject : JSONObject,
				dataJson : JSONObject?,
				dataText : String?,
				dataHtml : String?,
				dataMarkdown : String?,
				dataTitle : String?
			) {
				Log.d("ImportDataScreen", "onGetData: $requestData")
				try {
					val journeyData = dataObject.optJSONObject("importData") ?: JSONObject()
					val noteId = try {
						RealmUUID.from(dataObject.optString("noteId"))
					} catch (e : Exception) {
						RealmUUID.random()
					}
					journeyImporterScope.launch(Dispatchers.Default) {
						viewModel.storeJourneyData(noteId = noteId, journeyData = journeyData, dataText = dataText, dataJson = dataJson)
					}
				} catch (e : Exception) {
//						e.printStackTrace()
				}
			}
		}
	}

	val richTextEditor = remember {
		RichTextEditor(context).apply {
			scope.launch(Dispatchers.IO) { loadEditor() }
			setGetTextListener(getTextListener)
		}
	}

	var importDataDialogState by remember { mutableStateOf<ImportDataDialogState?>(null) }
	var showImportingDialog by remember { mutableStateOf(false) }
	var processedSize by remember { mutableStateOf(0) }
	var totalSize by remember { mutableStateOf(0) }

	suspend fun updateImporter(closeableFile: Closeable, importDataCallback: ImportDataViewModel.Companion.ImportDataCallback) {
		when (importDataCallback) {
			is ImportDataViewModel.Companion.ImportDataCallback.Importing -> {
				processedSize = importDataCallback.progress
				totalSize = importDataCallback.total
			}

			is ImportDataViewModel.Companion.ImportDataCallback.Success -> {
				showImportingDialog = false
				closeableFile.close()
				withContext(Dispatchers.Main) { Toast.makeText(context, "Data imported", Toast.LENGTH_SHORT).show() }
			}

			is ImportDataViewModel.Companion.ImportDataCallback.Error -> {
				showImportingDialog = false
				closeableFile.close()
				withContext(Dispatchers.Main) { Toast.makeText(context, "Error importing data", Toast.LENGTH_SHORT).show() }
			}
		}
	}

	val graphiteDataFilePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
		if (uri == null) Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show()
		else {
			processedSize = 0
			totalSize = 0
			showImportingDialog = true
			val seven7File = context.validateGraphiteExportFile(uri = uri)
			if (seven7File == null) {
				showImportingDialog = false
				Toast.makeText(context, "Invalid file", Toast.LENGTH_SHORT).show()
			}
			else {
				viewModel.importGraphiteData(seven7File = seven7File) { updateImporter(seven7File, importDataCallback = it) }
			}
		}
	}

	val journeyDataFilePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
		if (uri == null) Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show()
		else {
			processedSize = 0
			totalSize = 0
			showImportingDialog = true
			val zipFile = context.validateJourneyFile(uri = uri)
			if (zipFile == null) {
				showImportingDialog = false
				Toast.makeText(context, "Invalid file", Toast.LENGTH_SHORT).show()
			}
			else {
				viewModel.importJourneyData(
					zipFile = zipFile,
					transformData = { index, noteId, jsonString ->
						withContext(Dispatchers.Main) {
							Log.d("npr71", "importJourneyData: $index : ${richTextEditor.isReady.value}")
							richTextEditor.importData(
								importFrom = RichTextEditor.Companion.ImportFrom.Journey,
								noteId = noteId.toString(),
								data = jsonString,
								extra = index.toString()
							)
						}
					}
				) { updateImporter(zipFile, importDataCallback = it) }
			}
		}
	}

	val googleKeepDataFilePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
		if (uri == null) Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show()
		else {
			processedSize = 0
			totalSize = 0
			showImportingDialog = true
			val zipFile = context.validateGoogleKeepFile(uri = uri)
			if (zipFile == null) {
				showImportingDialog = false
				Toast.makeText(context, "Invalid file", Toast.LENGTH_SHORT).show()
			}
			else {
				viewModel.importGoogleKeepData(zipFile = zipFile) { updateImporter(zipFile, importDataCallback = it) }
			}
		}
	}

	GenericSettingsScaffold(
		title = "Import data",
		onClickBack = onClickBack,
	) {
		LazyColumn(
			modifier = Modifier.fillMaxSize()
		) {
			item {
				SettingsButton(
					title = "Graphite",
					subTitle = "Import data from Graphite",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_flat_import),
					onClick = {
						importDataDialogState = ImportDataDialogState(
							dialogContent = "Select the Graphite backup file to import. It will be a .7z file.",
							importFrom = "Graphite",
						)
					},
				)
			}
			item {
				SettingsButton(
					title = "Journey",
					subTitle = "Import data from Journey",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_flat_import),
					onClick = {
						importDataDialogState = ImportDataDialogState(
							dialogContent = "Select the Journey backup file to import.",
							importFrom = "Journey",
						)
					},
				)
			}
			item {
				SettingsButton(
					title = "Google Keep",
					subTitle = "Import data from Google Keep",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_flat_import),
					onClick = {
						importDataDialogState = ImportDataDialogState(
							dialogContent = "Select the Google Keep backup file to import.",
							importFrom = "Google Keep",
						)
					},
				)
			}
			item {
				SettingsButton(
					title = "Learn how to import data",
					subTitle = "Want to import data from other apps?",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_flat_knowledge_base),
					trailingIcon = SettingsButtonDefaults.settingsButtonTrailingIcon(icon = R.drawable.ic_flat_open_externally),
					onClick = { },
				)
			}
		}
	}

	ImportDataDialog(
		importDataDialogState = importDataDialogState,
		onClickSelectFile = {
			when (it?.importFrom) {
				"Graphite" -> graphiteDataFilePicker.launch(arrayOf("application/x-7z-compressed"))
				"Journey" -> journeyDataFilePicker.launch(arrayOf("application/zip"))
				"Google Keep" -> googleKeepDataFilePicker.launch(arrayOf("application/zip"))
			}
			importDataDialogState = null
		},
		onDismiss = { importDataDialogState = null },
	)

	ImportingDialog(
		showDialog = showImportingDialog,
		processedSize = processedSize,
		totalSize = totalSize,
	)
}
