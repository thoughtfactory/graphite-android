package com.syncodec.graphite.presentation.note.screen.editorScreen.bottomSheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
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
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.permission.LocationPermissionDialog
import com.syncodec.graphite.utils.LocationData
import com.syncodec.graphite.utils.roundTo
import io.github.esentsov.PackagePrivate


@PackagePrivate
@Preview
@Composable
fun LocationBottomSheet(
	locationData : LocationData = LocationData.Init,
	onRemoveLocation : () -> Unit = {},
	onReloadLocation : () -> Unit = {},
	onSetLocationManually : () -> Unit = {},
) {
	GenericBottomSheet(
		title = "Location",
		icon = R.drawable.ic_map_marker,
	) {
		LocationCard(
			locationData = locationData,
			onRemoveLocation = onRemoveLocation,
			onReloadLocation = onReloadLocation,
			onSetLocationManually = onSetLocationManually,
		)
	}
}

@Preview
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun LocationCard(
	locationData : LocationData = LocationData.Init,
	onRemoveLocation : () -> Unit = {},
	onReloadLocation : () -> Unit = {},
	onSetLocationManually : () -> Unit = {},
) {
	var showLocationPermissionDialog by remember { mutableStateOf(false) }

	LocationPermissionDialog(
		showDialog = showLocationPermissionDialog,
		onDismiss = { showLocationPermissionDialog = false },
		onPermissionAvailable = onReloadLocation
	)

	AnimatedContent(
		targetState = locationData,
		modifier = Modifier.fillMaxWidth()
	) {
		when (it) {
			is LocationData.Init -> LocationViewGeneric(
				message = "Initializing...",
				onReloadLocation = onReloadLocation,
				onSetLocationManually = onSetLocationManually
			)

			is LocationData.Loading -> LocationViewGeneric(
				message = "Getting location... Reload or set location manually.",
				onReloadLocation = onReloadLocation,
				onSetLocationManually = onSetLocationManually
			)

			is LocationData.SuccessOnlyLatLng -> LocationViewSuccess(
				latLng = it.latLng,
				address = null,
				onRemoveLocation = onRemoveLocation,
				onReloadLocation = onReloadLocation,
				onSetLocationManually = onSetLocationManually,
			)

			is LocationData.SuccessOnlyAddress -> LocationViewSuccess(
				latLng = null,
				address = it.address,
				onRemoveLocation = onRemoveLocation,
				onReloadLocation = onReloadLocation,
				onSetLocationManually = onSetLocationManually,
			)

			is LocationData.Success -> LocationViewSuccess(
				latLng = it.latLng,
				address = it.address,
				onRemoveLocation = onRemoveLocation,
				onReloadLocation = onReloadLocation,
				onSetLocationManually = onSetLocationManually,
			)

			is LocationData.SuccessNoData -> LocationViewGeneric(
				message = "No location data. Reload or set location manually.",
				onReloadLocation = onReloadLocation,
				onSetLocationManually = onSetLocationManually
			)

			is LocationData.NoPermission -> LocationViewNoPermission(
				onRequestPermission = { showLocationPermissionDialog = true },
				onSetLocationManually = onSetLocationManually
			)


			is LocationData.Error -> LocationViewGeneric(
				message = it.message,
				onReloadLocation = onReloadLocation,
				onSetLocationManually = onSetLocationManually
			)
		}
	}
}

@Preview
@Composable
private fun LocationViewGeneric(
	message : String = "Location",
	onReloadLocation : () -> Unit = {},
	onSetLocationManually : () -> Unit = {},
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
	) {
		Text(
			text = message,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
		)
		Spacer(modifier = Modifier.height(16.dp))
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.End
		) {
			OutlinedButton(
				onClick = onReloadLocation,
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f),
			) {
				Text(text = "Reload location")
			}
			Spacer(modifier = Modifier.width(8.dp))
			Button(
				onClick = onSetLocationManually,
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f)
			) {
				Text(text = "Set location")
			}
		}
	}
}

@Preview
@Composable
private fun LocationViewNoPermission(
	onRequestPermission : () -> Unit = {},
	onSetLocationManually : () -> Unit = {},
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
	) {
		Text(
			text = "Request permission to connect your notes with location or set location manually",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
		)
		Spacer(modifier = Modifier.height(16.dp))
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.End
		) {
			OutlinedButton(
				onClick = onRequestPermission,
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f)
			) {
				Text(text = "Request")
			}
			Spacer(modifier = Modifier.width(8.dp))
			Button(
				onClick = onSetLocationManually,
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f)
			) {
				Text(text = "Set location")
			}
		}
	}
}

@Preview
@Composable
private fun LocationViewNotPro(
	onReloadLocation : () -> Unit = {},
	onSetLocationManually : () -> Unit = {},
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.fillMaxWidth()
		) {
			Icon(
				painterResource(id = R.drawable.ic_pro_star),
				contentDescription = "Pro",
				tint = Color.Unspecified,
				modifier = Modifier.size(36.dp)
			)
			Spacer(modifier = Modifier.width(4.dp))
			Text(
				text = "Join Graphite Pro to connect your notes with location automatically",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface,
			)
		}
		Spacer(modifier = Modifier.height(4.dp))
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.End
		) {
			OutlinedButton(
				onClick = onReloadLocation,
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f)
			) {
				Text(text = "Load")
			}
			Spacer(modifier = Modifier.width(8.dp))
			Button(
				onClick = onSetLocationManually,
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f)
			) {
				Text(text = "Set")
			}
		}
	}
}

@Preview
@Composable
private fun LocationViewSuccess(
	latLng : LatLng? = null,
	address : String? = null,
	onRemoveLocation : () -> Unit = {},
	onReloadLocation : () -> Unit = {},
	onSetLocationManually : () -> Unit = {},
) {
	Column(
		modifier = Modifier.fillMaxWidth(),
	) {
		if (latLng != null) {
			LocationGoogleMap(
				latLng = latLng,
				onSetLocationManually = onSetLocationManually
			)
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = "${latLng.latitude?.roundTo(6)}, ${latLng.longitude?.roundTo(6)}",
				style = MaterialTheme.typography.bodyMedium
			)
			Spacer(modifier = Modifier.height(4.dp))
		}
		address?.let {
			Text(
				text = it,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface,
			)
		}
		Spacer(modifier = Modifier.height(16.dp))
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.End
		) {
			OutlinedButton(
				onClick = onRemoveLocation,
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f)
			) {
				Text(text = "Remove")
			}
			Spacer(modifier = Modifier.width(8.dp))
			Button(
				onClick = onReloadLocation,
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f)
			) {
				Text(text = "Reload")
			}
		}
	}
}

@Preview
@Composable
private fun LocationGoogleMap(
	latLng : LatLng? = null,
	onSetLocationManually : () -> Unit = {}
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
			modifier = Modifier
				.fillMaxWidth()
				.height(128.dp)
				.clip(MaterialTheme.shapes.medium),
		) {
			Marker(
				state = MarkerState(position = cameraPositionState.position.target),
			)
		}

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(128.dp)
				.clip(MaterialTheme.shapes.medium)
				.clickable { onSetLocationManually() }
		)
	}
}
