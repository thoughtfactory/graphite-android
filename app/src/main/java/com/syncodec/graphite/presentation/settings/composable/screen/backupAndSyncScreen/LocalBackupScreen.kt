package com.syncodec.graphite.presentation.settings.composable.screen.backupAndSyncScreen

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.documentfile.provider.DocumentFile
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.di.snapshot.SnapshotInator
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScaffold
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButtonDefaults
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SnapshotItem
import com.syncodec.graphite.presentation.settings.composable.dialog.RestoreSnapshotDialog
import com.syncodec.graphite.presentation.settings.composable.dialog.RestoringSnapshotDialog
import com.syncodec.graphite.presentation.settings.composable.viewModel.LocalBackupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel


@Preview
@Composable
fun LocalBackupScreen() {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val viewModel: LocalBackupViewModel = koinViewModel()

	var backupDirPath by remember { mutableStateOf<String?>(null) }
	var snapshotList by remember { mutableStateOf<Map<DocumentFile, SnapshotInator.Companion.SnapshotMetadata?>>(mapOf()) }
	var selectedSnapshot by remember { mutableStateOf<Pair<DocumentFile, SnapshotInator.Companion.SnapshotMetadata?>?>(null) }

	var isRestoreSnapshotDialogVisible by remember { mutableStateOf(false) }
	var isRestoringSnapshotDialogVisible by remember { mutableStateOf(false) }

	LaunchedEffect(key1 = Unit) {
		backupDirPath = context.contentResolver.persistedUriPermissions.firstOrNull()?.uri?.path
		snapshotList = viewModel.readBackupFolder()
	}

	val setupBackupDirectory = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { uri ->
		try {
			uri?.let {
				context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
				backupDirPath = context.contentResolver.persistedUriPermissions.firstOrNull()?.uri?.path
				scope.launch(Dispatchers.IO) {
					val newSnapshotList = viewModel.readBackupFolder()
					withContext(Dispatchers.Main) { snapshotList = newSnapshotList }
				}
			}
			Toast.makeText(context, "${context.getText(R.string.toast_back_folder_set_to)} $backupDirPath", Toast.LENGTH_SHORT).show()
		} catch (e: Exception) {
			Toast.makeText(context, context.getText(R.string.toast_error_setting_backup_folder), Toast.LENGTH_SHORT).show()
		}
	}

	GenericSettingsScaffold(
		title = stringResource(id = R.string.local_backup),
	) {
		LazyColumn(
			modifier = Modifier.fillMaxSize()
		) {
			item {
				SettingsButton(
					title = stringResource(id = R.string.automatic_backup),
					subTitle = stringResource(id = R.string.coming_soon),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_hourglass),
					onClick = { },
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.setup_backup_folder),
					subTitle = backupDirPath ?: stringResource(id = R.string.setup_backup_folder_sub),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_folder_plus),
					onClick = { setupBackupDirectory.launch(null) },
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.remove_backup_folder),
					subTitle = stringResource(id = R.string.remove_backup_folder_sub),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_folder_minus),
					onClick = {
						try {
							context.contentResolver.persistedUriPermissions.firstOrNull()?.uri?.let {
								context.contentResolver.releasePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
							}
							backupDirPath = null
							Toast.makeText(context, context.getText(R.string.toast_backup_folder_removed), Toast.LENGTH_LONG).show()
						} catch (e: Exception) {
							if (BuildConfig.DEBUG) e.printStackTrace()
							Toast.makeText(context, context.getText(R.string.toast_error_removing_backup_folder), Toast.LENGTH_SHORT).show()
						}
					},
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.take_snapshot),
					subTitle = stringResource(id = R.string.snapshots_are_not_encrypted),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_wand),
					onClick = {
						scope.launch(Dispatchers.IO) {
							viewModel.takeSnapshot()
							withContext(Dispatchers.Main) { snapshotList = viewModel.readBackupFolder() }
						}
					},
				)
			}
			item { Divider() }
			item {
				SettingsButton(
					title = stringResource(id = R.string.snapshot_warehouse),
					trailingIcon = null,
					enabled = false
				)
			}
			snapshotList.forEach { (documentFile, snapshotMetadata) ->
				item {
					SnapshotItem(
						snapshotMetadata = snapshotMetadata,
						documentFile = documentFile,
						onClick = {
							selectedSnapshot = Pair(documentFile, snapshotMetadata)
							isRestoreSnapshotDialogVisible = true
						}
					)
				}
			}
		}
	}

	RestoreSnapshotDialog(
		isDialogVisible = isRestoreSnapshotDialogVisible,
		onDismissRequest = { isRestoreSnapshotDialogVisible = false },
		documentFile = selectedSnapshot?.first,
		snapshotMetadata = selectedSnapshot?.second,
		onClickRestore = { documentFile ->
			scope.launch(Dispatchers.IO) {
				withContext(Dispatchers.Main) { isRestoreSnapshotDialogVisible = false; isRestoringSnapshotDialogVisible = true }
				val isRestored = viewModel.restoreSnapshot(snapshotFile = documentFile)
				if (!isRestored) scope.launch(Dispatchers.Main) {
					Toast.makeText(context, context.getText(R.string.toast_error_restoring_snapshot), Toast.LENGTH_SHORT).show()
					isRestoringSnapshotDialogVisible = false
				}
			}
		}
	)

	RestoringSnapshotDialog(isDialogVisible = isRestoringSnapshotDialogVisible)
}
