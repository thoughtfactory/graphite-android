package com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen.dialog.journey

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.importer.JourneyNote
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import com.syncodec.graphite.presentation.common.richText.rememberRichTextEditor
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipFile
import org.json.JSONObject
import org.koin.androidx.compose.koinViewModel
import java.io.File


@Preview
@Composable
fun ImportDataJourneyDialog(
	showDialog : Boolean = true,
	onDismiss : () -> Unit = {},
) {
	val context = LocalContext.current
	val viewModel : ImportDataJourneyViewModel = koinViewModel()
	val scope = rememberCoroutineScope()

	var isImportingData by remember { mutableStateOf(false) }
	var importingDataMessage by remember { mutableStateOf("") }
	var integrityMessage by remember { mutableStateOf(setOf<String>()) }
	var isIntegrityCheckedFailed by remember { mutableStateOf(false) }
	var totalSize by remember { mutableStateOf(0L) }
	var processedSize by remember { mutableStateOf(0L) }

	val richTextEditor = rememberRichTextEditor()
	richTextEditor.setGetTextListener(
		object : RichTextEditor.GetTextListener {
			override fun onGetData(requestData : RichTextEditor.Companion.RequestData, data : String?) {
				scope.launch(Dispatchers.Default) {
					try {
						data?.let { data ->
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
								val lng = importData.optDouble("lon")
								this.setLatLng(LatLng(lat, lng))
								this.address = importData.optString("address")
								this.contentThumbnail = dataText.substring(0, minOf(256, dataText.length))
								this.content = dataJson?.toString()
								this.thumbnail
								this.thumbnailType
								this.isFavourite = importData.optBoolean("favorite")
								this.isLocked

								viewModel.concurrentQueue.send(this)
							}
						}
					} catch (e : Exception) {
					}
				}
			}
		}
	)

	fun importData(zipFile : ZipFile, onSuccessListener : () -> Unit, onFailureListener : () -> Unit) {
		try {
			zipFile.entries.toList().filter { ! it.isDirectory && it.name.endsWith(".json") }.let { entries ->
				scope.launch(Dispatchers.Main) { totalSize = entries.size.toLong() }
				scope.launch(Dispatchers.Main) { processedSize = entries.size.toLong() }
				entries.forEach { zipArchiveEntry ->
					try {
						zipFile.getInputStream(zipArchiveEntry).bufferedReader().use { reader ->
							reader.readText()
						}.let { jsonString ->
							val journeyNote = viewModel.objectMapper.readValue(jsonString, JourneyNote::class.java)
							val noteId = RealmUUID.random()
							richTextEditor.importData(
								importFrom = RichTextEditor.Companion.ImportFrom.Journey,
								noteId = noteId.toString(),
								data = jsonString,
							)
							scope.launch(Dispatchers.Main) { processedSize += 1 }
						}
					} catch (e : Exception) {
						e.printStackTrace()
					}
				}

				scope.launch(Dispatchers.Main) { totalSize = 0 }
				scope.launch(Dispatchers.Main) { processedSize = 0 }
			}
			onSuccessListener()
		} catch (e : Exception) {
			onFailureListener()
		}
	}

	val openFilePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
		uri?.let { uri1 ->
			scope.launch(Dispatchers.Default) {
				withContext(Dispatchers.Main) {
					importingDataMessage = "Checking file integrity..."
					isImportingData = true
				}
				context.contentResolver.openInputStream(uri1)?.let { inputStream ->
					val journeyFile = File.createTempFile("journey", ".zip", context.cacheDir).also { fileOut ->
						fileOut.outputStream().use { inputStream.copyTo(it) }
					}
					inputStream.close()
					val journeyZipFile = ZipFile(journeyFile)
					viewModel.checkFileIntegrity(
						zipFile = journeyZipFile,
						integrityCallback = { isSafe, exception ->
							this.launch(Dispatchers.Main) {
								integrityMessage.toMutableSet().apply {
									add(exception?.message ?: "Unknown error")
									integrityMessage = this
								}
							}

							if (exception is ImportDataJourneyViewModel.Companion.ImportFromJourneyException) {
								integrityMessage.toMutableSet().apply {
									add(exception.message ?: "Unknown error")
									integrityMessage = this
								}
								isIntegrityCheckedFailed = true
							}
						},
						onSuccessListener = {
							importData(zipFile = journeyZipFile,
								onSuccessListener = {
									this.launch(Dispatchers.Main) {
										Toast.makeText(context, "Imported successfully", Toast.LENGTH_SHORT).show()
									}
								},
								onFailureListener = {
									this.launch(Dispatchers.Main) {
										Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
									}
								}
							)

						},
						onFailureListener = {}
					)
				}
				try {
				} catch (e : Exception) {
					e.printStackTrace()
				}
			}
		} ?: Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show()
	}

	GenericDialog(
		showDialog = showDialog && ! isImportingData,
		title = "Import from Journey",
		contentText = "Select an exported Journey data file to import. It will a zip file. The file will be imported in the default notebook.",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Select",
				secondaryText = "Cancel",
				onClickPrimary = { openFilePicker.launch(arrayOf("application/zip")) },
				onClickSecondary = onDismiss,
			)
		},
		onDismissRequest = onDismiss
	)

	GenericDialog(
		showDialog = showDialog && isImportingData,
		title = "Importing data",
		contentText = importingDataMessage,
		onDismissRequest = onDismiss
	) {
		Column(modifier = Modifier) {
			integrityMessage.forEach { message ->
				Text(
					text = message,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.error
				)
			}
		}

		Spacer(modifier = Modifier.height(8.dp))

		LinearProgressIndicator(
			progress = processedSize.toFloat() / (totalSize + 1).toFloat(),
			color = MaterialTheme.colorScheme.onBackground,
			trackColor = MaterialTheme.colorScheme.background,
			modifier = Modifier.fillMaxWidth(),
		)
	}
}
