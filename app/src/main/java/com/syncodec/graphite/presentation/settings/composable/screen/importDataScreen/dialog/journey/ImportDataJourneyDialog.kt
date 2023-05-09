package com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen.dialog.journey

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.importer.JourneyNote
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import com.syncodec.graphite.presentation.common.richText.RichTextEditor
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipFile
import org.json.JSONObject
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.time.Instant


@Preview
@Composable
fun ImportDataJourneyDialog(
	showDialog : Boolean = true,
	onDismiss : () -> Unit = {},
) {
	val context = LocalContext.current
	val viewModel : ImportDataJourneyViewModel = koinViewModel()
	val scope = rememberCoroutineScope()
	val uriHandler = LocalUriHandler.current

	var isImportingData by remember { mutableStateOf(false) }
	var importingDataMessage by remember { mutableStateOf("") }
	var totalSize by remember { mutableStateOf(0) }
	var processedSize by remember { mutableStateOf(0) }

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
				scope.launch(Dispatchers.Default) {
					try {
						val journeyData = dataObject.optJSONObject("importData") ?: JSONObject()
						val noteId = try {
							RealmUUID.from(dataObject.optString("noteId"))
						} catch (e : Exception) {
							RealmUUID.random()
						}
						NoteObject().apply {
							this.id = noteId
							this.createdTimestamp = journeyData.optLong("date_journal").let { if (it == 0L) Instant.now().toEpochMilli() else it }
							this.modifiedTimestamp = journeyData.optLong("date_modified").let { if (it == 0L) Instant.now().toEpochMilli() else it }
							this.userTimestamp = this.createdTimestamp
							val lat = journeyData.optDouble("lat")
							val lng = journeyData.optDouble("lon")
							this.setLatLng(LatLng(lat, lng))
							this.address = journeyData.optString("address")
							this.contentThumbnail = dataText?.substring(0, minOf(256, dataText.length))
							this.content = dataJson.toString()
							this.isFavourite = journeyData.optBoolean("favorite")

							viewModel.noteConcurrentQueue.send(Pair(this, false))
							withContext(Dispatchers.Main) {
								processedSize += 1
								importingDataMessage = "Importing data... $processedSize of $totalSize"
								if (processedSize == totalSize) {
									isImportingData = false
									onDismiss()
								}
							}
						}
					} catch (e : Exception) {
//						e.printStackTrace()
					}
				}
			}
		}
	}

	val richTextEditor = remember {
		RichTextEditor.headlessInstance(context).apply {
			scope.launch(Dispatchers.IO) { loadEditor() }
			setGetTextListener(getTextListener)
		}
	}

	fun importData(zipFile : ZipFile, onSuccessListener : () -> Unit, onFailureListener : () -> Unit) {
		scope.launch(Dispatchers.Default) {
			withContext(Dispatchers.Main) {
				importingDataMessage = "Importing data... 0 of ?"
				isImportingData = true
			}

			val journeyCacheDir = File(context.cacheDir, "journey").also {
				it.deleteRecursively()
				it.mkdirs()
			}
			try {

				val entries = zipFile.entries.toList().filter { it.name.endsWith(".json") }
				val totalSize1 = entries.size
				withContext(Dispatchers.Main) {
					totalSize = totalSize1
					processedSize = 0
					importingDataMessage = "Importing data... 0 of $totalSize1"
				}
				entries.forEachIndexed { index, zipArchiveEntry ->
					try {
						zipFile.getInputStream(zipArchiveEntry).bufferedReader().use { reader ->
							reader.readText()
						}.let { jsonString ->
							val journeyNote = viewModel.objectMapper.readValue(jsonString, JourneyNote::class.java)
							val noteId = RealmUUID.random()
							journeyNote.photos?.forEach {
								val photoFile = File(journeyCacheDir, "${RealmUUID.random()}${it?.split(".")?.lastOrNull()?.let { ".$it" } ?: ""}")
								zipFile.getInputStream(zipFile.getEntry(it)).use { inputStream ->
									photoFile.outputStream().use { outputStream ->
										inputStream.copyTo(outputStream)
									}
								}
								viewModel.attachmentConcurrentQueue.send(Pair(noteId, photoFile))
							}
							withContext(Dispatchers.Main) {
								richTextEditor.importData(
									importFrom = RichTextEditor.Companion.ImportFrom.Journey,
									noteId = noteId.toString(),
									data = jsonString,
									extra = index.toString(),
								)
							}
						}
					} catch (e : Exception) {
					}
				}
				onSuccessListener()
			} catch (e : Exception) {
				onFailureListener()
			}
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
						integrityCallback = { isSafe, exception -> },
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
						onFailureListener = {
							this.launch(Dispatchers.Main) {
								Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
							}
						}
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
		contentText = "Select an exported Journey data file to import. It will a zip file. The data will be imported in the default notebook.",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Select",
				secondaryText = "Cancel",
				onClickPrimary = { openFilePicker.launch(arrayOf("application/zip")) },
				onClickSecondary = onDismiss,
			)
		},
		thirdActionButton = {
			this.apply {
				Row(
					verticalAlignment = Alignment.Bottom,
					modifier = Modifier.fillMaxWidth(),
				) {
					ClickableText(
						text = buildAnnotatedString {
							val text = "Learn how to import data"
							this.addStyle(
								style = SpanStyle(
									color = MaterialTheme.colorScheme.primary,
									textDecoration = TextDecoration.Underline,
									fontStyle = FontStyle.Italic,
									fontSize = MaterialTheme.typography.bodyMedium.fontSize,
									fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
								),
								start = 0,
								end = text.length
							)
							append(text)
						},
						onClick = {
							try {
								uriHandler.openUri("https://graphite.syncodec.com/#/data/import/journey")
							} catch (e : Exception) {
								Toast.makeText(context, "Error opening link", Toast.LENGTH_SHORT).show()
							}
						},
					)
					Spacer(modifier = Modifier.requiredWidth(4.dp))
					Icon(
						painter = painterResource(id = R.drawable.ic_launch),
						contentDescription = "Learn how to import data",
						tint = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier.requiredSize(16.dp)
					)
				}
				Spacer(modifier = Modifier.height(8.dp))
			}
		},
		onDismissRequest = onDismiss
	)

	GenericDialog(
		showDialog = showDialog && isImportingData,
		title = "Importing data",
		contentText = importingDataMessage,
	) {
		Spacer(modifier = Modifier.height(12.dp))

		LinearProgressIndicator(
			progress = processedSize.toFloat() / (totalSize + 1).toFloat(),
			color = MaterialTheme.colorScheme.onBackground,
			trackColor = MaterialTheme.colorScheme.background,
			modifier = Modifier.fillMaxWidth(),
		)
	}
}
