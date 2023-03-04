package com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen.dialog.googleKeep

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
import com.syncodec.graphite.di.model.NoteObject
import com.syncodec.graphite.di.model.importer.GoogleKeepNote
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipFile
import org.koin.androidx.compose.koinViewModel
import java.io.File


@Preview
@Composable
fun ImportDataGoogleKeepDialog(
	showDialog : Boolean = true,
	onDismiss : () -> Unit = {},
) {
	val context = LocalContext.current
	val viewModel : ImportDataGoogleKeepViewModel = koinViewModel()
	val scope = rememberCoroutineScope()
	val uriHandler = LocalUriHandler.current

	var isImportingData by remember { mutableStateOf(false) }
	var importingDataMessage by remember { mutableStateOf("") }
	var totalSize by remember { mutableStateOf(0) }
	var processedSize by remember { mutableStateOf(0) }

	val openFilePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
		uri?.let { uri1 ->
			scope.launch(Dispatchers.Default) {
				withContext(Dispatchers.Main) {
					importingDataMessage = "Importing data... 0 of ?"
					isImportingData = true
				}
				context.contentResolver.openInputStream(uri1)?.let { inputStream ->
					val importDataDir = File(context.cacheDir, "importData")
					importDataDir.mkdirs()
					val googleKeepFile = File.createTempFile("google_keep", ".zip", importDataDir).also { fileOut ->
						fileOut.outputStream().use { inputStream.copyTo(it) }
					}
					inputStream.close()
					val googleKeepZipFile = ZipFile(googleKeepFile)
					val entries = googleKeepZipFile.entries.toList()
						.filter { it.name.let { it.startsWith("Takeout/Keep/") && it.endsWith(".json") } }

					withContext(Dispatchers.Main) {
						importingDataMessage = "Importing data... 0 of ${entries.size}"
						totalSize = entries.size
						processedSize = 0
					}

					entries.forEachIndexed { index, zipArchiveEntry ->
						try {
							googleKeepZipFile.getInputStream(zipArchiveEntry).bufferedReader().use { reader ->
								val jsonString = reader.readText()

								val googleKeepNote = viewModel.objectMapper.readValue(jsonString, GoogleKeepNote::class.java)
								NoteObject().apply {
									googleKeepNote.creationTimestampUsec?.let { this.createdTimestamp = it / 1000 }
									googleKeepNote.userEditedTimestampUsec?.let { this.modifiedTimestamp = it / 1000 }
									googleKeepNote.userEditedTimestampUsec?.let { this.userTimestamp = it / 1000 }
									this.title = googleKeepNote.title
									this.content = googleKeepNote.encodeToTipTapFormat()
									this.contentThumbnail = googleKeepNote.textContent?.let { it.substring(0, minOf(256, it.length)) }
									this.isFavourite = googleKeepNote.isPinned ?: false

									val attachmentList =
										googleKeepNote
											.attachments
											?.map { googleKeepZipFile.getEntries("Takeout/Keep/${it.filePath}").toList() }
											?.flatten() ?: listOf()

									googleKeepNote.attachments?.forEach {
										googleKeepZipFile.getEntries("Takeout/Keep/${it.filePath}")
									}

									viewModel.concurrentQueue.send(Triple(this, googleKeepZipFile, attachmentList))
									withContext(Dispatchers.Main) {
										importingDataMessage = "Importing data... ${index + 1} of ${entries.size}"
										processedSize += 1
									}
								}
							}
						} catch (e : Exception) {
//							e.printStackTrace()
						}
					}

					withContext(Dispatchers.Main) {
						isImportingData = false
						onDismiss()
					}
				}
			}
		}
	}

	GenericDialog(
		showDialog = showDialog && ! isImportingData,
		title = "Import from Google Keep",
		contentText = "Select an exported Google Keep data file to import. It will a zip file. The data will be imported in the default notebook.",
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
								uriHandler.openUri("https://graphite.syncodec.com/#/data/import/keep")
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
