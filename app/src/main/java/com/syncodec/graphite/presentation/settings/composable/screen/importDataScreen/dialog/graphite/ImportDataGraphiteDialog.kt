package com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen.dialog.graphite

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
import androidx.compose.material3.Text
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
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import com.syncodec.graphite.utils.extract7z
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.koin.androidx.compose.koinViewModel
import java.io.File


@Preview
@Composable
fun ImportDataGraphiteDialog(
	showDialog : Boolean = true,
	onDismiss : () -> Unit = {},
) {
	val context = LocalContext.current
	val viewModel : ImportDataGraphiteViewModel = koinViewModel()
	val scope = rememberCoroutineScope()
	val uriHandler = LocalUriHandler.current

	var isImportingData by remember { mutableStateOf(false) }
	var overwriteData by remember { mutableStateOf(false) }
	var importingDataMessage by remember { mutableStateOf("") }
	var totalSize by remember { mutableStateOf(0) }
	var processedSize by remember { mutableStateOf(0) }

	val openFilePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
		uri?.let { uri1 ->
			scope.launch(Dispatchers.Default) {
				withContext(Dispatchers.Main) {
					isImportingData = true
					importingDataMessage = "Importing data... 0 of ?"
				}
				context.contentResolver.openInputStream(uri1)?.let { inputStream ->
					val graphiteFile = File.createTempFile("graphite", ".7z", context.cacheDir).also { fileOut ->
						fileOut.outputStream().use { inputStream.copyTo(it) }
					}
					inputStream.close()
					val graphite7zFile = SevenZFile(graphiteFile)
					File.createTempFile("graphite", "", context.cacheDir).also {
						it.deleteRecursively()
						it.mkdirs()
						extract7z(graphite7zFile, it)
						viewModel.importData(
							file = it,
							callback = {
								withContext(Dispatchers.Main) {
									if (it) Toast.makeText(context, "Import successful", Toast.LENGTH_SHORT).show()
									else Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
									isImportingData = false
									onDismiss()
								}
							},
							progress = { _processedSize, _totalSize ->
								withContext(Dispatchers.Main) {
									totalSize = _totalSize
									processedSize = _processedSize
									importingDataMessage = "Importing data... $_processedSize of $_totalSize"
								}
							}
						)
					}
				}
			}
		} ?: Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show()
	}

	GenericDialog(
		showDialog = showDialog && ! isImportingData,
		title = "Import from Graphite",
		contentText = "Select an exported Graphite data file to import. It will a 7z file.",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Select",
				secondaryText = "Cancel",
				onClickPrimary = { openFilePicker.launch(arrayOf("application/x-7z-compressed")) },
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
								uriHandler.openUri("https://graphite.syncodec.com/#/data/import/graphite")
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
	) {
		Text(
			text = "Entries with the same ID will be overwritten",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier,
		)

//		Spacer(modifier = Modifier.height(12.dp))
//		Row(
//			verticalAlignment = Alignment.CenterVertically,
//			modifier = Modifier
//				.fillMaxWidth()
//				.clip(MaterialTheme.shapes.medium)
//				.clickable { overwriteData = ! overwriteData },
//		) {
//			Checkbox(
//				checked = overwriteData,
//				onCheckedChange = { overwriteData = it },
//				modifier = Modifier.requiredSize(32.dp),
//				interactionSource = remember { MutableInteractionSource() },
//			)
//			Spacer(modifier = Modifier.width(8.dp))
//			Text(
//				text = "Overwrite data with same ID",
//				style = MaterialTheme.typography.bodyMedium,
//				color = MaterialTheme.colorScheme.onBackground,
//				modifier = Modifier.weight(1f),
//			)
//		}
	}

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
