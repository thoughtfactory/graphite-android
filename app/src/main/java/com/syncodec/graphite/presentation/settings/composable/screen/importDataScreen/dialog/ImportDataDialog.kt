package com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


@Preview
@Composable
fun ImportDataDialog(
	showDialog : Boolean = true,
	onDismiss : () -> Unit = {},
) {
//	val scope = rememberCoroutineScope()
//	val context = LocalContext.current
//	val viewModel : ImportDataViewModel = koinViewModel()
//
//	var isImportingData by remember { mutableStateOf(false) }
//
//	var totalSize by remember { mutableStateOf(0) }
//	var processedSize by remember { mutableStateOf(0) }
//
//	val openOverwriteFilePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
//		scope.launch(Dispatchers.Default) {
//			try {
//				uri?.let {
//					isImportingData = true
//					context.contentResolver.openInputStream(it)?.use {
//						uri.getFileName(context)?.let { it1 ->
//							val file = File(context.cacheDir, it1).also { it.delete(); it.createNewFile() }
//							file.outputStream().use { it2 -> it.copyTo(it2) }
//
//							viewModel.importData(context, file.name, context.cacheDir.path) { success, exception ->
//								if (success) ProcessPhoenix.triggerRebirth(context)
//								else scope.launch(Dispatchers.Main) {
//									Toast.makeText(context, "Error restoring snapshot", Toast.LENGTH_LONG).show()
//								}
//							}
//						}
//					}
//				}
//			} catch (e : Exception) {
//				e.printStackTrace()
//			}
//		}
//	}
//
//	val openCreateNewFilePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
//		scope.launch(Dispatchers.Default) {
//			try {
//				uri?.let {
//					isImportingData = true
//					context.contentResolver.openInputStream(it)?.also { inputStream ->
//						val tmp7zFile = File(File(context.cacheDir, "restore"), "graphite.7z")
//						tmp7zFile.parentFile?.mkdirs()
//						tmp7zFile.delete()
//						tmp7zFile.createNewFile()
//
//						inputStream.copyTo(tmp7zFile.outputStream())
//						inputStream.close()
//
//						val restoreFolder = File(File(context.cacheDir, "restore"), "graphite")
//						restoreFolder.deleteRecursively()
//						restoreFolder.mkdirs()
//
//						val sevenZFile = SevenZFile(tmp7zFile)
//
//						viewModel.importData(
//							restoreFolder = restoreFolder,
//							sevenZFile = sevenZFile,
//							progress = { total, processed ->
//								scope.launch(Dispatchers.Main) {
//									totalSize = total
//									processedSize = processed
//								}
//							}
//						) {
////							** Attachment
////							try {
////								val attachmentFolder = File(restoreFolder, "attachment")
////								copyInDirectory(attachmentFolder, File(context.attachmentDirPath()))
////							} catch (e : Exception) {
////								e.printStackTrace()
////							}
////							scope.launch(Dispatchers.Main) {
////								isImportingData = false
////								onDismiss()
////							}
//						}
//					}
//				} ?: run {
//					withContext(Dispatchers.Main) {
//						Toast.makeText(context, "No file selected", Toast.LENGTH_SHORT).show()
//						isImportingData = false
//					}
//				}
//			} catch (e : Exception) {
//				withContext(Dispatchers.Main) {
//					Toast.makeText(context, "Error importing data", Toast.LENGTH_SHORT).show()
//					isImportingData = false
//				}
//			}
//		}
//	}
//
//	GenericDialog(
//		showDialog = showDialog && ! isImportingData,
//		title = "Import Data",
//		contentText = "Select an action to perform on data conflict.",
//		dualActionButton = {
//			Column(
//				modifier = Modifier.fillMaxWidth()
//			) {
//				Button(
//					onClick = {},
//					shape = MaterialTheme.shapes.medium,
//					colors = ButtonDefaults.buttonColors(
//						containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.31f),
//						contentColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.31f).getInverseBWColor(),
//					),
//					modifier = Modifier.fillMaxWidth()
//				) {
//					Text(
//						text = "Cancel",
//						color = MaterialTheme.colorScheme.surface.copy(alpha = 0.31f).getInverseBWColor(),
//					)
//				}
//
//				DualActionButtons(
//					primaryText = "Create New",
//					secondaryText = "Overwrite",
//					onClickPrimary = { openCreateNewFilePicker.launch(arrayOf("application/x-7z-compressed")) },
//					onClickSecondary = { openOverwriteFilePicker.launch(arrayOf("*/*")) },
//				)
//			}
//		},
//		onDismissRequest = onDismiss
//	) {
//		Column(
//			modifier = Modifier.fillMaxWidth()
//		) {
//			Spacer(modifier = Modifier.height(12.dp))
//			KeyValueText(
//				key = "Overwrite",
//				value = "Entries with save ID will be overwritten.",
//			)
//			KeyValueText(
//				key = "Create New",
//				value = "New entry will be created in case if entry with similar ID exists.",
//			)
//			KeyValueText(
//				key = "Scratch",
//				value = "Whole database will be deleted and data from the imported file will be written.",
//			)
//		}
//	}
//
//	GenericDialog(
//		showDialog = showDialog && isImportingData,
//		title = "Importing Data",
//		contentText = "Please wait while we are importing your data",
//	) {
//		Spacer(modifier = Modifier.height(6.dp))
//		LinearProgressIndicator(
//			progress = (processedSize.toFloat() / maxOf(1, totalSize).toFloat()),
//			color = MaterialTheme.colorScheme.onBackground,
//			trackColor = MaterialTheme.colorScheme.background,
//			modifier = Modifier.fillMaxWidth(),
//		)
//	}
}
