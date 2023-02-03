package com.syncodec.graphite.presentation.settings.composable.screen.localBackup

import android.content.Intent
import android.net.Uri
import android.os.FileObserver
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.syncodec.graphite.R
import com.syncodec.graphite.di.repository.AttachmentRepository.Companion.attachmentDirPath
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsContentTitle
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.utils.compress7z
import com.syncodec.graphite.utils.copyInDirectory
import com.syncodec.graphite.utils.timeStampToPrettyNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.koin.androidx.compose.koinViewModel
import java.io.File


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun LocalBackupScreen() {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val viewModel : LocalBackupViewModel = koinViewModel()

	var backupDirPath by remember { mutableStateOf(context.contentResolver.persistedUriPermissions.firstOrNull()?.uri?.path) }
	var snapshotList by remember { mutableStateOf(listOf<DocumentFile>()) }
	var refreshing by remember { mutableStateOf(false) }

	val setupBackupDirectory = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { uri ->
		try {
			uri?.let {
				context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
				backupDirPath = context.contentResolver.persistedUriPermissions.firstOrNull()?.uri?.path
			}
			Toast.makeText(context, "Backup folder set to ${backupDirPath}", Toast.LENGTH_SHORT).show()
		} catch (e : Exception) {
			Toast.makeText(context, "Error setting backup folder", Toast.LENGTH_SHORT).show()
		}
	}

	fun readBackupDirectory(uri : Uri) {
		if (! refreshing) {
			scope.launch(Dispatchers.IO) {
				withContext(Dispatchers.Main) { refreshing = true }
				val documentTree = DocumentFile.fromTreeUri(context, uri)
				documentTree?.listFiles()?.filter { it.name?.startsWith("graphite_snapshot") == true }?.let {
					withContext(Dispatchers.Main) { snapshotList = it.toList(); refreshing = false }
				} ?: withContext(Dispatchers.Main) { snapshotList = listOf(); refreshing = false }
			}
		}
	}

	val pullRefreshState = rememberPullRefreshState(
		refreshing = refreshing,
		onRefresh = {
			context.contentResolver.persistedUriPermissions.firstOrNull()?.uri?.let { readBackupDirectory(it) }
		}
	)

	LaunchedEffect(key1 = null) {
		context.contentResolver.persistedUriPermissions.firstOrNull()?.uri?.let { readBackupDirectory(it) }
	}

	Column(
		modifier = Modifier.fillMaxSize()
	) {
		SettingButton(text = "Automatic Backup", icon = R.drawable.ic_hourglass, subText = "Coming Soon")
		SettingButton(text = "Set Backup Directory", icon = R.drawable.ic_folder, subText = backupDirPath ?: "Setup") {
			setupBackupDirectory.launch(null)
		}
		SettingButton(text = "Remove Backup Directory", icon = R.drawable.ic_folder_remove, subText = "Remove") {
			try {
				context.contentResolver.releasePersistableUriPermission(
					context.contentResolver.persistedUriPermissions.firstOrNull()?.uri !!,
					Intent.FLAG_GRANT_READ_URI_PERMISSION
				)
				backupDirPath = null
				Toast.makeText(context, "Backup folder removed. You have to manually delete its content.", Toast.LENGTH_LONG).show()
			} catch (e : Exception) {
				Toast.makeText(context, "Error removing backup folder", Toast.LENGTH_SHORT).show()
			}
		}
		SettingButton(text = "Take Snapshot", icon = R.drawable.ic_easy, subText = "Save everything from database") {
			scope.launch(Dispatchers.IO) {
				val tmpExportDir = File(context.cacheDir, "tmpExport")
				val fileName = "graphite_snapshot ${System.currentTimeMillis().timeStampToPrettyNumber()}"

				val observer : FileObserver = object : FileObserver(File(context.cacheDir.path), CLOSE_WRITE) {
					override fun onEvent(event : Int, file : String?) {
						if (event == CLOSE_WRITE) {
							val attachmentFolder = File(tmpExportDir, "attachment")
							copyInDirectory(File(context.attachmentDirPath()), attachmentFolder)

							val sevenZFile = File(context.cacheDir, "${fileName}.7z")
							val sevenZOutput = SevenZOutputFile(sevenZFile)
							compress7z(tmpExportDir, sevenZOutput)

							val documentTree = DocumentFile.fromTreeUri(context, context.contentResolver.persistedUriPermissions.firstOrNull()?.uri !!)
							documentTree?.let {
								val documentFile = it.createFile("application/octet-stream", fileName)
								documentFile?.let {
									context.contentResolver.openOutputStream(it.uri)?.let { outputStream ->
										file?.let { fileName ->
											sevenZFile.let { outputFile ->
												outputFile.inputStream().copyTo(outputStream)
												outputStream.close()
//												outputFile.delete()
											}
										}
										readBackupDirectory(context.contentResolver.persistedUriPermissions.firstOrNull()?.uri !!)
										this.stopWatching()
									}
								}
							}
						}
					}
				}
				observer.startWatching()
				viewModel.getRealmSnapshot("database.realm", tmpExportDir.path)
			}
		}

		SettingsContentTitle(title = "SNAPSHOT WAREHOUSE")
		Box(
			modifier = Modifier.pullRefresh(state = pullRefreshState)
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.verticalScroll(rememberScrollState())
			) {
				snapshotList.forEach {
					SnapshotButton(fileName = it.name ?: "") {}
				}
			}
			PullRefreshIndicator(
				refreshing = refreshing,
				state = pullRefreshState,
				backgroundColor = MaterialTheme.colorScheme.surface,
				contentColor = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.align(Alignment.TopCenter)
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
