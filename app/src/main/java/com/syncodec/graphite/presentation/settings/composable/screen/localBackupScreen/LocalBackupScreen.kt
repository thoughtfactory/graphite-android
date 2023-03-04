package com.syncodec.graphite.presentation.settings.composable.screen.localBackupScreen

import android.content.Intent
import android.net.Uri
import android.os.FileObserver
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.jakewharton.processphoenix.ProcessPhoenix
import com.syncodec.graphite.R
import com.syncodec.graphite.di.repository.AttachmentRepository.Companion.attachmentDirPath
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsContentTitle
import com.syncodec.graphite.presentation.settings.composable.dialog.SettingsDialogType
import com.syncodec.graphite.presentation.settings.composable.screen.localBackupScreen.dialog.RestoreSnapshotDialog
import com.syncodec.graphite.presentation.settings.composable.screen.localBackupScreen.dialog.RestoringSnapshotDialog
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.utils.RecursiveFileObserver
import com.syncodec.graphite.utils.compress7z
import com.syncodec.graphite.utils.copyInDirectory
import com.syncodec.graphite.utils.extract7z
import com.syncodec.graphite.utils.share
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.koin.androidx.compose.koinViewModel
import java.io.File


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun LocalBackupScreen(
	openDialog : (SettingsDialogType) -> Unit = {},
	closeDialog : (SettingsDialogType) -> Unit = {}
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val uriHandler = LocalUriHandler.current
	val viewModel : LocalBackupViewModel = koinViewModel()

	var backupDirPath by remember { mutableStateOf(context.contentResolver.persistedUriPermissions.firstOrNull()?.uri?.path) }
	var snapshotList by remember { mutableStateOf(listOf<DocumentFile>()) }
	var refreshing by remember { mutableStateOf(false) }

	var showRestoreSnapshotDialog by remember { mutableStateOf(false) }
	var showRestoringSnapshotDialog by remember { mutableStateOf(false) }
	var snapshotToRestore by remember { mutableStateOf<DocumentFile?>(null) }

	fun readBackupDirectory(uri : Uri) {
		if (! refreshing) {
			scope.launch(Dispatchers.IO) {
				try {
					withContext(Dispatchers.Main) { refreshing = true }
					val documentTree = DocumentFile.fromTreeUri(context, uri)
					documentTree?.listFiles()?.filter { it.name?.startsWith("graphite_snapshot") == true }?.let {
						withContext(Dispatchers.Main) { snapshotList = it.toList(); refreshing = false }
					} ?: withContext(Dispatchers.Main) { snapshotList = listOf(); refreshing = false }
				} catch (e : Exception) {
					withContext(Dispatchers.Main) { snapshotList = listOf(); refreshing = false }
				}
			}
		}
	}

	val setupBackupDirectory = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { uri ->
		try {
			uri?.let {
				context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
				backupDirPath = context.contentResolver.persistedUriPermissions.firstOrNull()?.uri?.path
				readBackupDirectory(it)
			}
			Toast.makeText(context, "Backup folder set to $backupDirPath", Toast.LENGTH_SHORT).show()
		} catch (e : Exception) {
			Toast.makeText(context, "Error setting backup folder", Toast.LENGTH_SHORT).show()
		}
	}

	val pullRefreshState = rememberPullRefreshState(
		refreshing = refreshing,
		onRefresh = { context.contentResolver.persistedUriPermissions.firstOrNull()?.uri?.let { readBackupDirectory(it) } }
	)

	LaunchedEffect(key1 = null) {
		context.contentResolver.persistedUriPermissions.firstOrNull()?.uri?.let { readBackupDirectory(it) }
	}

	fun generateSnapshot(documentFile : DocumentFile) {
		scope.launch(Dispatchers.IO) {
			val exportSnapshotDir = File(context.cacheDir, "exportSnapshot").also {
				it.deleteRecursively()
				it.mkdirs()
			}
			val fileName = "graphite_snapshot_${System.currentTimeMillis()}"
			val currentSnapshotDir = File(exportSnapshotDir, fileName).also {
				it.mkdirs()
			}

			val observer = RecursiveFileObserver(
				mPath = currentSnapshotDir.path,
				mask = FileObserver.CLOSE_WRITE,
				mListener = object : RecursiveFileObserver.EventListener {
					override fun onEvent(event : Int, file : File?) {
						if (event == FileObserver.CLOSE_WRITE && file == File(currentSnapshotDir, "$fileName.realm")) {
							val attachmentFolder = File(currentSnapshotDir, "attachment").also { it.mkdirs() }
							copyInDirectory(File(context.attachmentDirPath()), attachmentFolder)

							val sevenZFile = File(exportSnapshotDir, "${fileName}.7z")
							val sevenZOutput = SevenZOutputFile(sevenZFile)
							compress7z(currentSnapshotDir, sevenZOutput) { progress, total -> }

							val snapshotFile = documentFile.createFile("application/x-7z-compressed", "${fileName}.7z")
							snapshotFile?.let {
								sevenZFile.inputStream().use { inputStream ->
									context.contentResolver.openOutputStream(it.uri)?.use { outputStream ->
										inputStream.copyTo(outputStream)
										outputStream.close()
									}
									inputStream.close()
								}
							}

							readBackupDirectory(documentFile.uri)
						}
					}
				}
			)

			observer.startWatching()
			viewModel.getRealmSnapshot("$fileName.realm", currentSnapshotDir.path)
		}
	}

	fun restoreSnapshot(snapshotFile : DocumentFile) {
		showRestoringSnapshotDialog = true
		scope.launch(Dispatchers.Default) {
			val importSnapshotDir = File(context.cacheDir, "importSnapshot").also {
				it.deleteRecursively()
				it.mkdirs()
			}

			val sevenZImportFile = File(importSnapshotDir, snapshotFile.name ?: "graphite_snapshot.7z")

			context.contentResolver.openInputStream(snapshotFile.uri)?.use { inputStream ->
				sevenZImportFile.outputStream().use { outputStream ->
					inputStream.copyTo(outputStream)
					outputStream.close()
				}
				inputStream.close()
			}

			val sevenZFile = SevenZFile(sevenZImportFile)
			val snapshotDir = File(importSnapshotDir, "snapshot")
			extract7z(sevenZFile, snapshotDir) { progress, total -> }

			viewModel.clearAttachment()

			snapshotDir.listFiles()?.firstOrNull { it.name.endsWith(".realm") }?.let {
				viewModel.restoreRealmSnapshot(context, it.name, snapshotDir.path) { isSuccess, exception ->
					if (isSuccess) {
						snapshotDir.listFiles()?.firstOrNull { it.name == "attachment" }?.let { attachmentDir ->
							scope.launch(Dispatchers.IO) {
								viewModel.restoreAttachment(attachmentDir)
								withContext(Dispatchers.Main) {
									Toast.makeText(context, "Snapshot restored successfully. Restarting", Toast.LENGTH_SHORT).show()
									delay(1700)
									showRestoringSnapshotDialog = false
									ProcessPhoenix.triggerRebirth(context)
								}
							}
						}
					} else {
						scope.launch(Dispatchers.Main) {
							Toast.makeText(context, "Error restoring snapshot", Toast.LENGTH_SHORT).show()
							showRestoringSnapshotDialog = false
						}
					}
				}
			}
		}
	}

	fun shareSnapshot(snapshotFile : DocumentFile) {
		scope.launch(Dispatchers.Default) {
			val importSnapshotDir = File(context.cacheDir, "importSnapshot").also {
				it.deleteRecursively()
				it.mkdirs()
			}

			val sevenZImportFile = File(importSnapshotDir, snapshotFile.name ?: "graphite_snapshot.7z")

			context.contentResolver.openInputStream(snapshotFile.uri)?.use { inputStream ->
				sevenZImportFile.outputStream().use { outputStream ->
					inputStream.copyTo(outputStream)
					outputStream.close()
				}
				inputStream.close()
			}

			sevenZImportFile.share(context)
		}
	}

	Box(
		modifier = Modifier.pullRefresh(state = pullRefreshState)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
		) {
			PlainTextWarning()
			SettingButton(text = "Automatic Backup", icon = R.drawable.ic_hourglass, subText = "Coming Soon")
			SettingButton(text = "Set Backup Directory", icon = R.drawable.ic_folder, subText = backupDirPath ?: "Setup") {
				setupBackupDirectory.launch(null)
			}
			SettingButton(text = "Remove Backup Directory", icon = R.drawable.ic_folder_remove, subText = "Remove") {
				try {
					context.contentResolver.persistedUriPermissions.firstOrNull()?.uri?.let {
						context.contentResolver.releasePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
					}
					backupDirPath = null
					snapshotList = listOf()
					Toast.makeText(context, "Backup folder removed. You have to manually delete its content.", Toast.LENGTH_LONG).show()
				} catch (e : Exception) {
					Toast.makeText(context, "Error removing backup folder", Toast.LENGTH_SHORT).show()
				}
			}
			SettingButton(text = "Take Snapshot", icon = R.drawable.ic_easy, subText = "Save everything from database") {
				context.contentResolver.persistedUriPermissions.firstOrNull()?.uri?.let {
					DocumentFile.fromTreeUri(context, it)?.let { it1 -> generateSnapshot(it1) }
				}
			}

			SettingButton(text = "Learn how to access a snapshot on PC", icon = R.drawable.ic_snapshot) {
				try {
					uriHandler.openUri("https://graphite.syncodec.com/#/data/access_on_computer")
				} catch (e : Exception) {
					Toast.makeText(context, "Error opening link", Toast.LENGTH_SHORT).show()
				}
			}

			SettingsContentTitle(title = "SNAPSHOT WAREHOUSE")

			snapshotList.forEach { snapshotFile ->
				SnapshotButton(fileName = snapshotFile.name ?: "") {
					snapshotToRestore = snapshotFile
					showRestoreSnapshotDialog = true
				}
			}
		}

		PullRefreshIndicator(
			refreshing = refreshing,
			state = pullRefreshState,
			backgroundColor = MaterialTheme.colorScheme.surface,
			contentColor = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.align(Alignment.TopCenter)
		)

		RestoreSnapshotDialog(
			showDialog = showRestoreSnapshotDialog,
			onShare = { snapshotToRestore?.let { shareSnapshot(snapshotFile = it) } },
			onRestore = { snapshotToRestore?.let { restoreSnapshot(snapshotFile = it) } },
			onDismiss = { showRestoreSnapshotDialog = false }
		)

		RestoringSnapshotDialog(showDialog = showRestoringSnapshotDialog)
	}
}

@Preview
@Composable
fun PlainTextWarning() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp, 8.dp)
			.background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.large)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier,
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_warning),
					contentDescription = "Plain Text Warning",
					tint = MaterialTheme.colorScheme.onErrorContainer,
				)
				Spacer(modifier = Modifier.width(12.dp))
				Text(
					text = "Unencrypted Data",
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onErrorContainer,
				)
			}

			Spacer(modifier = Modifier.height(8.dp))

			Text(
				text = "Snapshot files are not encrypted and can be accessed by anyone with access to your device.\n" +
						"Encrypted snapshots are coming soon.",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onErrorContainer
			)
		}
	}
}

@Preview
@Composable
private fun SnapshotButton(
	fileName : String = "graphite_snapshot 2021-05-04 07:13:47.realm",
	onClick : () -> Unit = {}
) {
	Box(
		modifier = Modifier.clickable { onClick() }
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(0.dp, 12.dp, 0.dp, 12.dp)
		) {
			Spacer(modifier = Modifier.width(16.dp))
			Icon(
				painter = painterResource(id = R.drawable.ic_snapshot_stored),
				contentDescription = "Snapshot",
				tint = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier.requiredSize(IconButtonSize)
			)
			Spacer(modifier = Modifier.width(24.dp))
			Text(
				text = fileName,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold,
				modifier = Modifier.weight(1f),
			)
			Spacer(modifier = Modifier.width(8.dp))
			Icon(
				painter = painterResource(id = R.drawable.ic_caret),
				contentDescription = fileName,
				tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
				modifier = Modifier
					.requiredSize(IconButtonSize)
					.graphicsLayer { rotationZ = 90f }
			)
			Spacer(modifier = Modifier.width(16.dp))
		}
	}
}
