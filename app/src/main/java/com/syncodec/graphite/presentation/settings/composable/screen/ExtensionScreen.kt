package com.syncodec.graphite.presentation.settings.composable.screen

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.syncodec.graphite.R
import com.syncodec.graphite.notification.WriteNoteNotification
import com.syncodec.graphite.presentation.base.LocalAppDataStore
import com.syncodec.graphite.presentation.base.LocalIsPro
import com.syncodec.graphite.presentation.common.permission.NotificationPermissionDialog
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScaffold
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButtonDefaults
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsSwitch


@OptIn(ExperimentalPermissionsApi::class)
@Preview
@Composable
fun ExtensionScreen() {
	val context = LocalContext.current
	val appDatastore = LocalAppDataStore.current
	val isPro = LocalIsPro.current

	val isYearProgressBarEnabled by appDatastore.getYearProgress.collectAsState(initial = false)
	val isAutoGeolocationEnabled by appDatastore.getGeolocation.collectAsState(initial = false)
	val isWriteFromNotificationEnabled by appDatastore.getNoteFromNotification.collectAsState(initial = false)

	var isNotificationPermissionDialogVisible by remember { mutableStateOf(false) }

	fun onClickAddFromNotification() {
		when {
			isWriteFromNotificationEnabled -> {
				WriteNoteNotification.cancelNotification(context = context)
				appDatastore.putNoteFromNotification(false)
			}
			isPro -> {
				WriteNoteNotification.pinIt(context = context)
				appDatastore.putNoteFromNotification(true)
			}
			else -> Toast.makeText(context, context.getText(R.string.toast_write_from_notification), Toast.LENGTH_SHORT).show()
		}
	}

	GenericSettingsScaffold(
		title = stringResource(id = R.string.extensions),
	) {
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			item {
				SettingsSwitch(
					title = stringResource(R.string.year_progress_bar),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_progress_bar),
					checked = isYearProgressBarEnabled,
					onCheckChanged = { appDatastore.putYearProgress(!isYearProgressBarEnabled) }
				)
			}
			item {
				SettingsSwitch(
					title = stringResource(R.string.geo_tag_notes_automatically),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_map_marker_dot),
					checked = isAutoGeolocationEnabled,
					onCheckChanged = { appDatastore.putGeolocation(!isAutoGeolocationEnabled) }
				)
			}
			item {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
					val permissionState = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
					SettingsSwitch(
						title = stringResource(R.string.add_note_from_notification),
						leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_write_from_notification),
						checked = isWriteFromNotificationEnabled,
						onCheckChanged = {
							if (permissionState.status.isGranted) onClickAddFromNotification()
							else isNotificationPermissionDialogVisible = true
						}
					)
				} else {
					SettingsSwitch(
						title = stringResource(R.string.add_note_from_notification),
						leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_write_from_notification),
						checked = isWriteFromNotificationEnabled,
						onCheckChanged = { onClickAddFromNotification() }
					)
				}
			}
		}
	}

	NotificationPermissionDialog(
		isDialogVisible = isNotificationPermissionDialogVisible,
		onDismissRequest = { isNotificationPermissionDialogVisible = false },
	)
}
