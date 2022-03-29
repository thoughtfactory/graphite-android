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
import com.google.android.gms.maps.model.LatLng
import com.syncodec.momento.custom.LoadingView
import com.syncodec.momento.konstant.ErrorCode
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.miscellaneous.DataStore
import com.syncodec.momento.miscellaneous.logger
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
fun NoteEditorScreen(
	onClick: (NoteActivity.Click, Any?) -> Unit
) {
	val context = LocalContext.current
	val viewModel: NoteViewModel = viewModel()

	val dataStore = DataStore(context = context)
	val typography by dataStore.getTypography.collectAsState(initial = null)

	val activityState = viewModel.activityState
	val noteDbEntry by viewModel.knotDbEntry.collectAsState()

	val richTextEditor = viewModel.activityState.richTextEditor
	val locationPermissionState = viewModel.activityState.locationPermissionState

	val status by viewModel.status
	val isReady by richTextEditor.isReady

	val textColor = MaterialTheme.colorScheme.onBackground.toHexString()

	LaunchedEffect(key1 = isReady && typography != null) {
		if (isReady) {
			when {
				locationPermissionState.hasPermission -> {
					activityState.addressState.value = NoteActivity.AddressState.REQUESTED
					viewModel.getLocation()
				}
				locationPermissionState.shouldShowRationale -> {
					activityState.addressState.value = NoteActivity.AddressState.SHOW_RATIONALE
				}
				!locationPermissionState.permissionRequested -> {
					activityState.addressState.value = NoteActivity.AddressState.REQUEST_PERMISSION
				}
				else -> {
					activityState.addressState.value = NoteActivity.AddressState.NO_PERMISSION
				}
			}

			when (typography) {
				0 -> richTextEditor.exec("editor.setBaseFontFamily(\"overlock\");")
				1 -> richTextEditor.exec("editor.setBaseFontFamily(\"source_sans_pro\");")
				2 -> richTextEditor.exec("editor.setBaseFontFamily(\"ubuntu\");")
				3 -> richTextEditor.exec("editor.setBaseFontFamily('atwriter');")
				else -> richTextEditor.exec("editor.setBaseFontFamily(\"source_sans_pro\");")
			}
			richTextEditor.exec("editor.setBaseFontColor('$textColor');")
			viewModel.status.value = Status.LOADED
		}
	}

	Crossfade(
		targetState = status,
		animationSpec = tween(durationMillis = 400),
		modifier = Modifier.fillMaxSize()
	) {
		when (it) {
			Status.INIT -> LoadingView()
			Status.LOADING -> LoadingView()
			Status.LOADED -> {
				Column(
					modifier = Modifier.fillMaxSize()
				) {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.weight(1f)
					) {
						AndroidView(
							factory = { richTextEditor },
							update = { viewer -> },
							modifier = Modifier
								.fillMaxSize()
								.background(MaterialTheme.colorScheme.background)
						)
						AddressCard(
							addressState = viewModel.activityState.addressState.value,
							showAddressCard = viewModel.activityState.showAddressCard.value,
							address = noteDbEntry?.address,
							locationData = noteDbEntry?.location
						) { onClick(it, null) }
						NotificationLayout()

						logger("address : ${noteDbEntry?.address}")

						MapLocationPopup(
							showMapLocationDialog = activityState.showMapLocationDialog.value,
							latLng = if (noteDbEntry?.location?.latitude != null && noteDbEntry?.location?.longitude != null)
								LatLng(noteDbEntry!!.location!!.latitude!!, noteDbEntry!!.location!!.longitude!!) else null,
							address = noteDbEntry?.address
						) { click, data ->  onClick(click, data) }
					}

					EditorToolbar(
						richTextEditor = richTextEditor,
						userTimestamp = noteDbEntry?.userTimestamp ?: -1,
						tagList = mapOf(),
						onClick = { onClick(it, null) },
					) { errorCode ->
						when (errorCode) {
							ErrorCode.Companion.ErrorCode.URL_RANGE_SELECTION_ERROR -> viewModel.activityState.notificationType.value =
								NotificationType.UrlSelectionNotification
						}
						activityState.isNotificationVisible.value = true
					}
				}
			}
			Status.ERROR -> {}
		}
	}
}
