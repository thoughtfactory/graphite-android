package com.syncodec.momento.noteComponent.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.syncodec.momento.custom.LoadingView
import com.syncodec.momento.konstant.ErrorCode
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.noteComponent.NoteActivity
import com.syncodec.momento.noteComponent.NoteViewModel
import com.syncodec.momento.noteComponent.miscellaneous.*
import com.syncodec.momento.noteComponent.toolbar.EditorToolbar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
@Composable
fun NoteEditorScreen() {
	val noteViewModel: NoteViewModel = viewModel()

	val richTextEditor = noteViewModel.activityState.richTextEditor
	val locationPermissionState = noteViewModel.activityState.locationPermissionState

	val status by noteViewModel.status
	val isReady by richTextEditor.isReady

	LaunchedEffect(key1 = isReady) {
		if (isReady) {
			when {
				locationPermissionState.hasPermission -> {
					noteViewModel.activityState.addressState.value = NoteActivity.AddressState.REQUESTED
					noteViewModel.getLocation()
				}
				locationPermissionState.shouldShowRationale -> {
					noteViewModel.activityState.addressState.value = NoteActivity.AddressState.SHOW_RATIONALE
				}
				!locationPermissionState.permissionRequested -> {
					noteViewModel.activityState.addressState.value = NoteActivity.AddressState.REQUEST_PERMISSION
				}
				else -> {
					noteViewModel.activityState.addressState.value = NoteActivity.AddressState.NO_PERMISSION
				}
			}

			richTextEditor.exec("editor.setEditable(true);")
			noteViewModel.status.value = Status.LOADED
		}
	}

	Crossfade(
		targetState = status,
		animationSpec = tween(
			durationMillis = 400
		)
	) {
		when(it) {
			Status.INIT -> LoadingView()
			Status.LOADING -> LoadingView()
			Status.LOADED -> {
				Column(
					modifier = Modifier
						.fillMaxSize()
				) {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.weight(1f)
					) {
						AndroidView(
							factory = { richTextEditor },
							update = { viewer ->
							},
							modifier = Modifier
								.fillMaxSize()
								.background(MaterialTheme.colorScheme.background)
						)
						AddressCard()
						NotificationLayout()
						MapLocationPopup()
					}
					EditorToolbar(
						richTextEditor = richTextEditor
					) { errorCode ->
						when (errorCode) {
							ErrorCode.Companion.ErrorCode.URL_RANGE_SELECTION_ERROR -> noteViewModel.activityState.notificationType.value =
								NotificationType.UrlSelectionNotification
						}
						noteViewModel.activityState.isNotificationVisible.value = true
					}
				}
			}
			Status.SAVING -> {}
			Status.SAVED -> {}
			Status.SUCCESS -> {}
			Status.ERROR -> {}
		}
	}
}
