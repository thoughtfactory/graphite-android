package com.syncodec.graphite.presentation.settings.composable.screen.localBackupScreen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.info.PlainTextWarning
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsContentTitle
import com.syncodec.graphite.presentation.settings.composable.dialog.SettingsDialogType
import com.syncodec.graphite.presentation.settings.composable.screen.localBackupScreen.dialog.RestoreSnapshotDialog
import com.syncodec.graphite.presentation.settings.composable.screen.localBackupScreen.dialog.RestoringSnapshotDialog
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.share
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun LocalBackupScreen(
	openDialog : (SettingsDialogType) -> Unit = {},
	closeDialog : (SettingsDialogType) -> Unit = {}
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val dataStoreInstance = remember { DataStoreInstance(context = context) }
	val uriHandler = LocalUriHandler.current
	val viewModel : LocalBackupViewModel = koinViewModel()

	val showPlainTextWarning by dataStoreInstance.showPlainTextWarningLocal.collectAsState(initial = false)

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
		viewModel.generateSnapshot { sevenZFile ->
			val snapshotFile = documentFile.createFile("application/x-7z-compressed", sevenZFile.name)
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

	fun restoreSnapshot(snapshotFile : DocumentFile) {
		showRestoringSnapshotDialog = true

		context.contentResolver.openInputStream(snapshotFile.uri)?.let {
			viewModel.restore(it) {
				scope.launch(Dispatchers.Main) {
					showRestoringSnapshotDialog = false
					Toast.makeText(context, "Error restoring snapshot", Toast.LENGTH_SHORT).show()
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
			PlainTextWarning(
				isVisible = showPlainTextWarning,
				modifier = Modifier.padding(horizontal = 12.dp)
			) { dataStoreInstance.putShowPlainTextWarningLocal(false) }
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
			snapshotList.map {
				Pair(
					it, try {
						it.name?.split("_")?.last()?.split(".")?.first()?.toLong() ?: -1
					} catch (e : Exception) {
						- 1
					}
				)
			}
				.sortedByDescending { it.second }
				.forEachIndexed { index, pair ->
					SnapshotButton(
						fileName = pair.first.name ?: "graphite_snapshot",
						createdTimestamp = pair.second,
						isLatest = index == 0,
					) {
						snapshotToRestore = pair.first
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
fun SnapshotButton(
	fileName : String = "graphite_snapshot_1683227400000.7z",
	createdTimestamp : Long = - 1,
	isLatest : Boolean = false,
	onClick : () -> Unit = {}
) {
	val formatter = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
	val date = formatter.format(Date(maxOf(createdTimestamp, 0L)))

	SettingButton(
		text = fileName,
		infoText = when {
			createdTimestamp == - 1L -> "Created: Unknown"
			isLatest -> "$date\nLatest"
			else -> date
		},
		icon = R.drawable.ic_snapshot_stored,
		subText = "Restore",
		onClick = onClick
	)
}
