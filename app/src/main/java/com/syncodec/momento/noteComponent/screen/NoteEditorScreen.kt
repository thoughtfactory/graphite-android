package com.syncodec.momento.noteComponent.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.syncodec.momento.custom.LoadingView
import com.syncodec.momento.konstant.ErrorCode
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.miscellaneous.DataStore
import com.syncodec.momento.miscellaneous.toHexString
import com.syncodec.momento.noteComponent.NoteActivity
import com.syncodec.momento.noteComponent.NoteViewModel
import com.syncodec.momento.noteComponent.miscellaneous.AddressCard
import com.syncodec.momento.noteComponent.miscellaneous.MapLocationPopup
import com.syncodec.momento.noteComponent.miscellaneous.NotificationLayout
import com.syncodec.momento.noteComponent.miscellaneous.NotificationType
import com.syncodec.momento.noteComponent.toolbar.EditorToolbar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
@Composable
fun NoteEditorScreen() {
	val context = LocalContext.current
	val noteViewModel: NoteViewModel = viewModel()

	val dataStore = DataStore(context = context)
	val typography by dataStore.getTypography.collectAsState(initial = null)

	val richTextEditor = noteViewModel.activityState.richTextEditor
	val locationPermissionState = noteViewModel.activityState.locationPermissionState

	val status by noteViewModel.status
	val isReady by richTextEditor.isReady

	val textColor = MaterialTheme.colorScheme.onBackground.toHexString()

	LaunchedEffect(key1 = isReady && typography!=null) {
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

			when(typography) {
				0 -> richTextEditor.exec("editor.setBaseFontFamily(\"overlock\");")
				1 -> richTextEditor.exec("editor.setBaseFontFamily(\"source_sans_pro\");")
				2 -> richTextEditor.exec("editor.setBaseFontFamily(\"ubuntu\");")
				3 -> richTextEditor.exec("editor.setBaseFontFamily('atwriter');")
				else -> richTextEditor.exec("editor.setBaseFontFamily(\"source_sans_pro\");")
			}
			richTextEditor.exec("editor.setBaseFontColor('$textColor');")
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
