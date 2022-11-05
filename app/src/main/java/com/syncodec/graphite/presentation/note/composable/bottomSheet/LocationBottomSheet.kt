package com.syncodec.graphite.presentation.note.composable.bottomSheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.permission.LocationPermissionDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionAddress
import com.syncodec.graphite.presentation.note.composable.LocalCompositionLocationState
import com.syncodec.graphite.presentation.note.composable.LocalCompositionLatLng
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.note.composable.dialog.NoteDialogType
import com.syncodec.graphite.utils.LocationState
import com.syncodec.graphite.utils.roundTo


@Composable
fun LocationBottomSheet(
	onRemoveLocation : () -> Unit,
	onReloadLocation : () -> Unit,
) {
	val openDialog = LocalCompositionOpenDialog.current

	val locationState = LocalCompositionLocationState.current
	val latLng = LocalCompositionLatLng.current
	val address = LocalCompositionAddress.current

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {
		BottomSheetStrip()

		BottomSheetHeader(
			title = "Location",
			icon = R.drawable.ic_map_marker
		)

		Spacer(modifier = Modifier.height(8.dp))

		LocationCard(
			locationState = locationState,
			latLng = latLng,
			address = address,
			onRemoveLocation = onRemoveLocation,
			onReloadLocation = onReloadLocation
		) { openDialog(NoteDialogType.LOCATION_PICKER, null) }

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
private fun LocationCard(
	locationState : LocationState,
	latLng : LatLng? = null,
	address : String? = null,
	onRemoveLocation : () -> Unit,
	onReloadLocation : () -> Unit,
	onSetLocationManually : () -> Unit
) {
	var showLocationPermissionDialog by remember { mutableStateOf(false) }

	LocationPermissionDialog(
		showDialog = showLocationPermissionDialog,
		onDismiss = { showLocationPermissionDialog = false },
		onPermissionAvailable = onReloadLocation
	)

	Card(
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.background,
			contentColor = MaterialTheme.colorScheme.onBackground
		),
		onClick = { },
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
	) {
		AnimatedContent(
			targetState = locationState,
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
		) {
			when (it) {
				LocationState.INIT -> LocationViewGeneric(
					message = "Getting location...",
					onReloadLocation = onReloadLocation,
					onSetLocationManually = onSetLocationManually
				)

				LocationState.NO_PERMISSION -> LocationViewNoPermission(
					onRequestPermission = { showLocationPermissionDialog = true },
					onSetLocationManually = onSetLocationManually
				)

				LocationState.LOADING -> LocationViewGeneric(
					message = "Getting location... Reload or set location manually.",
					onReloadLocation = onReloadLocation,
					onSetLocationManually = onSetLocationManually
				)

				LocationState.DISABLED -> LocationViewDisabled(onSetLocationManually = onSetLocationManually)
				LocationState.LATLNG -> LocationViewSuccess(
					latLng = latLng,
					address = address,
					onRemoveLocation = onRemoveLocation,
					onReloadLocation = onReloadLocation,
					onSetLocationManually = onSetLocationManually,
				)

				LocationState.ONLY_LATLNG -> LocationViewSuccess(
					latLng = latLng,
					address = address,
					onRemoveLocation = onRemoveLocation,
					onReloadLocation = onReloadLocation,
					onSetLocationManually = onSetLocationManually,
				)

				LocationState.ONLY_ADDRESS -> LocationViewSuccess(
					latLng = latLng,
					address = address,
					onRemoveLocation = onRemoveLocation,
					onReloadLocation = onReloadLocation,
					onSetLocationManually = onSetLocationManually,
				)

				LocationState.SUCCESS -> LocationViewSuccess(
					latLng = latLng,
					address = address,
					onRemoveLocation = onRemoveLocation,
					onReloadLocation = onReloadLocation,
					onSetLocationManually = onSetLocationManually,
				)

				LocationState.KNOWN_ERROR -> LocationViewGeneric(
					message = "Error2 getting location. Reload or set location manually.",
					onReloadLocation = onReloadLocation,
					onSetLocationManually = onSetLocationManually
				)

				LocationState.REMOVED -> LocationViewGeneric(
					message = "Location removed. Reload or set location manually.",
					onReloadLocation = onReloadLocation,
					onSetLocationManually = onSetLocationManually
				)

				LocationState.UNKNOW_ERROR -> LocationViewGeneric(
					message = "Error getting location. Reload or set location manually.",
					onReloadLocation = onReloadLocation,
					onSetLocationManually = onSetLocationManually
				)
			}
		}
	}
}

@Composable
private fun LocationViewGeneric(
	message : String = "Location",
	onReloadLocation : () -> Unit,
	onSetLocationManually : () -> Unit
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
	) {
		Text(
			text = message,
			style = MaterialTheme.typography.bodyMedium
		)
		Spacer(modifier = Modifier.height(8.dp))
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.End
		) {
			OutlinedButton(onClick = onReloadLocation) {
				Text(text = "Reload location")
			}
			Spacer(modifier = Modifier.width(8.dp))
			Button(onClick = onSetLocationManually) {
				Text(text = "Set location")
			}
		}
	}
}

@Composable
private fun LocationViewDisabled(
	onSetLocationManually : () -> Unit
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
	) {
		Text(
			text = "Location is disabled. Enable it from settings or set location manually.",
			style = MaterialTheme.typography.bodyMedium
		)
		Spacer(modifier = Modifier.height(8.dp))
		Button(onClick = onSetLocationManually) {
			Text(text = "Set location")
		}
	}
}

@Composable
private fun LocationViewNoPermission(
	onRequestPermission : () -> Unit,
	onSetLocationManually : () -> Unit
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
	) {
		Text(
			text = "Request permission to connect your notes with location or set location manually",
			style = MaterialTheme.typography.bodyMedium
		)
		Spacer(modifier = Modifier.height(8.dp))
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.End
		) {
			OutlinedButton(onClick = onRequestPermission) {
				Text(text = "Request permission")
			}
			Spacer(modifier = Modifier.width(8.dp))
			Button(onClick = onSetLocationManually) {
				Text(text = "Set location")
			}
		}
	}
}

@Composable
private fun LocationViewSuccess(
	latLng : LatLng?,
	address : String?,
	onRemoveLocation : () -> Unit,
	onReloadLocation : () -> Unit,
	onSetLocationManually : () -> Unit
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
	) {
		if (latLng != null) {
			LocationGoogleMap(
				latLng = latLng,
				onSetLocationManually = onSetLocationManually
			)
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = "${latLng.latitude?.roundTo(6)}, ${latLng.longitude?.roundTo(6)}",
				style = MaterialTheme.typography.bodyMedium
			)
			Spacer(modifier = Modifier.height(4.dp))
		}
		if (address != null) {
			Text(
				text = address,
				style = MaterialTheme.typography.bodyMedium
			)
			Spacer(modifier = Modifier.height(4.dp))
		}
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.End
		) {
			OutlinedButton(onClick = onRemoveLocation) {
				Text(text = "Remove location")
			}
			Spacer(modifier = Modifier.width(8.dp))
			Button(onClick = onReloadLocation) {
				Text(text = "Reload location")
			}
		}
	}
}

@Composable
private fun LocationGoogleMap(
	latLng : LatLng?,
	onSetLocationManually : () -> Unit
) {
	val context = LocalContext.current
	val cameraPositionState = rememberCameraPositionState()

	LaunchedEffect(key1 = latLng) {
		latLng?.toGLatLng()?.let {
			cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 13f))
		}
	}

	Box(modifier = Modifier) {
		GoogleMap(
			modifier = Modifier
				.fillMaxWidth()
				.height(128.dp)
				.clip(RoundedCornerShape(12.dp)),
			cameraPositionState = cameraPositionState,
			googleMapOptionsFactory = {
				GoogleMapOptions().apply {
					this.rotateGesturesEnabled(false)
					this.rotateGesturesEnabled(false)
					this.scrollGesturesEnabledDuringRotateOrZoom(false)
					this.tiltGesturesEnabled(false)
					this.zoomGesturesEnabled(false)
				}
			},
			uiSettings = MapUiSettings(
				compassEnabled = false,
				indoorLevelPickerEnabled = false,
				mapToolbarEnabled = false,
				myLocationButtonEnabled = false,
				rotationGesturesEnabled = false,
				scrollGesturesEnabled = false,
				scrollGesturesEnabledDuringRotateOrZoom = false,
				tiltGesturesEnabled = false,
				zoomControlsEnabled = false,
				zoomGesturesEnabled = false
			),
			properties = MapProperties(
				mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, if (isSystemInDarkTheme()) R.raw.map_style_dark else R.raw.map_style_light)
			),
		) {
			Marker(
				state = MarkerState(position = cameraPositionState.position.target),
			)
		}
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(128.dp)
				.clip(RoundedCornerShape(12.dp))
				.clickable { onSetLocationManually() }
		)
	}
}
