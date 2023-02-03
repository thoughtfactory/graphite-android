package com.syncodec.graphite.presentation.settings.composable.dialog.importData

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.syncodec.graphite.di.repository.AttachmentRepository.Companion.attachmentDirPath
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons
import com.syncodec.graphite.utils.copyInDirectory
import com.syncodec.graphite.utils.getInverseBWColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.koin.androidx.compose.koinViewModel
import java.io.File


@Preview
@Composable
fun ImportDataDialog(
	showDialog : Boolean = true,
	onDismiss : () -> Unit = {},
) {
	val scope = rememberCoroutineScope()
	val context = LocalContext.current
	val viewModel : ImportDataViewModel = koinViewModel()

	var isImportingData by remember { mutableStateOf(false) }

	var totalSize by remember { mutableStateOf(0) }
	var processedSize by remember { mutableStateOf(0) }

	val openOverwriteFilePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
	}

	val openCreateNewFilePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
		scope.launch(Dispatchers.Default) {
			try {
				uri?.let {
					isImportingData = true
					context.contentResolver.openInputStream(it)?.also { inputStream ->
						val tmp7zFile = File(File(context.cacheDir, "restore"), "graphite.7z")
						tmp7zFile.parentFile?.mkdirs()
						tmp7zFile.delete()
						tmp7zFile.createNewFile()

						inputStream.copyTo(tmp7zFile.outputStream())
						inputStream.close()

						val restoreFolder = File(File(context.cacheDir, "restore"), "graphite")
						restoreFolder.deleteRecursively()
						restoreFolder.mkdirs()

						val sevenZFile = SevenZFile(tmp7zFile)

						viewModel.importData(
							restoreFolder = restoreFolder,
							sevenZFile = sevenZFile,
							progress = { total, processed ->
								scope.launch(Dispatchers.Main) {
									totalSize = total
									processedSize = processed
								}
							}
						) {
//							** Attachment
							try {
								val attachmentFolder = File(restoreFolder, "attachment")
								copyInDirectory(attachmentFolder, File(context.attachmentDirPath()))
							} catch (e : Exception) {
								e.printStackTrace()
							}
							scope.launch(Dispatchers.Main) {
								isImportingData = false
								onDismiss()
							}
						}
					}
				} ?: run {
					withContext(Dispatchers.Main) {
						Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show()
						isImportingData = false
					}
				}
			} catch (e : Exception) {
				withContext(Dispatchers.Main) {
					Toast.makeText(context, "Error importing data", Toast.LENGTH_SHORT).show()
					isImportingData = false
				}
			}
		}
	}

	GenericDialog(
		showDialog = showDialog && ! isImportingData,
		title = "Import Data",
		contentText = "Select an action to perform on data conflict",
		dualActionButton = {
			Column(
				modifier = Modifier.fillMaxWidth()
			) {
				Button(
					onClick = {},
					shape = MaterialTheme.shapes.medium,
					colors = ButtonDefaults.buttonColors(
						containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.31f),
						contentColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.31f).getInverseBWColor(),
					),
					modifier = Modifier.fillMaxWidth()
				) {
					Text(
						text = "Cancel",
						color = MaterialTheme.colorScheme.surface.copy(alpha = 0.31f).getInverseBWColor(),
					)
				}

				DualActionButtons(
					primaryText = "Create New",
					secondaryText = "Overwrite",
					onClickPrimary = { openCreateNewFilePicker.launch(arrayOf("application/x-7z-compressed")) },
					onClickSecondary = { openOverwriteFilePicker.launch(arrayOf("application/x-7z-compressed")) },
				)
			}
		},
		onDismissRequest = onDismiss
	)

	GenericDialog(
		showDialog = showDialog && isImportingData,
		title = "Importing Data",
		contentText = "Please wait while we are importing your data",
	) {
		Spacer(modifier = Modifier.height(6.dp))
		LinearProgressIndicator(
			progress = (processedSize.toFloat() / maxOf(1, totalSize).toFloat()),
			color = MaterialTheme.colorScheme.onBackground,
			trackColor = MaterialTheme.colorScheme.background,
			modifier = Modifier.fillMaxWidth(),
		)
	}
}
