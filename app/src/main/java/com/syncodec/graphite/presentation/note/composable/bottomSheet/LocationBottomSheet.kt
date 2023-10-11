package com.syncodec.graphite.presentation.note.composable.bottomSheet

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetInfo2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.base.LocalIsDarkTheme
import com.syncodec.graphite.utils.Location
import com.syncodec.graphite.utils.LocationData


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun EditorLocationBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	locationData: LocationData = LocationData.Init,
	onClickSelectLocation: () -> Unit = {},
	onClickRemoveLocation: () -> Unit = {},
	onClickReloadLocation: () -> Unit = {},
) {

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = "Location",
		) {
			when (locationData) {
				is LocationData.Init -> ElseLocationDataView(text = stringResource(id = R.string.location_initializing), onClickSelectLocation = onClickSelectLocation, onClickReloadLocation = onClickReloadLocation)
				is LocationData.Loading -> ElseLocationDataView(text = stringResource(id = R.string.location_loading), onClickSelectLocation = onClickSelectLocation, onClickReloadLocation = onClickReloadLocation)
				is LocationData.SuccessOnlyLatLng -> LocationSuccess(latLng = locationData.latLng, onClickRemoveLocation = onClickRemoveLocation, onClickSelectLocation = onClickSelectLocation)
				is LocationData.SuccessOnlyAddress -> LocationSuccess(address = locationData.address, onClickRemoveLocation = onClickRemoveLocation, onClickSelectLocation = onClickSelectLocation)
				is LocationData.Success -> LocationSuccess(latLng = locationData.latLng, address = locationData.address, onClickRemoveLocation = onClickRemoveLocation, onClickSelectLocation = onClickSelectLocation)
				is LocationData.Removed -> ElseLocationDataView(text = stringResource(id = R.string.location_removed), onClickSelectLocation = onClickSelectLocation, onClickReloadLocation = onClickReloadLocation)
				is LocationData.NoPermission -> NoPermissionView(onClickSelectLocation = onClickSelectLocation)
				is LocationData.AutoFetchDisabled -> ElseLocationDataView(text = stringResource(id = R.string.location_auto_fetch_disabled), onClickSelectLocation = onClickSelectLocation, onClickReloadLocation = onClickReloadLocation)
				is LocationData.Error -> LocationErrorView(onClickSelectLocation = onClickSelectLocation, onClickReloadLocation = onClickReloadLocation)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ViewerLocationBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	locationData: LocationData = LocationData.Init,
) {
	val context = LocalContext.current
	val isDarkTheme = LocalIsDarkTheme.current
	val clipboardManager = LocalClipboardManager.current

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = "Location",
		) {
			val mapProperties = remember {
				MapProperties(
					isBuildingEnabled = false,
					isIndoorEnabled = false,
					isMyLocationEnabled = false,
					isTrafficEnabled = false,
					latLngBoundsForCameraTarget = null,
					mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, Location.getMapStyle(isDarkTheme)),
					mapType = MapType.NORMAL,
					maxZoomPreference = 16f,
					minZoomPreference = 16f,
				)
			}
			val mapUiSettings = remember {
				MapUiSettings(
					compassEnabled = false,
					indoorLevelPickerEnabled = false,
					mapToolbarEnabled = false,
					myLocationButtonEnabled = false,
					rotationGesturesEnabled = false,
					scrollGesturesEnabled = false,
					scrollGesturesEnabledDuringRotateOrZoom = false,
					tiltGesturesEnabled = false,
					zoomControlsEnabled = false,
					zoomGesturesEnabled = false,
				)
			}

			Column(
				modifier = Modifier.fillMaxWidth(),
			) {
				locationData.getLatLngOrNull()?.let { latLng1 ->
					val cameraPositionState: CameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(latLng1.toGLatLng(), 11f) }

					GoogleMap(
						modifier = Modifier
							.fillMaxWidth()
							.height(256.dp)
							.clip(MaterialTheme.shapes.medium),
						properties = mapProperties,
						uiSettings = mapUiSettings,
						cameraPositionState = cameraPositionState,
					) {
						Marker(
							state = MarkerState(position = latLng1.toGLatLng()),
						)
					}
				}

				Spacer(modifier = Modifier.height(8.dp))

				GenericBottomSheetInfo2(
					key = "LatLng",
					value = locationData.getLatLngOrNull()?.toString() ?: "Unavailable",
					onLongClick = {
						locationData.getLatLngOrNull()?.let {
							val annotatedString = buildAnnotatedString { append("geo: ${it.latitude}, ${it.longitude}") }
							clipboardManager.setText(annotatedString)
						}
					},
				)

				GenericBottomSheetInfo2(
					key = "Address",
					value = locationData.getAddressOrNull() ?: "Unavailable",
					onLongClick = {
						locationData.getAddressOrNull()?.let {
							val annotatedString = buildAnnotatedString { append(it) }
							clipboardManager.setText(annotatedString)
						}
					},
				)
			}
		}
	}
}


@Preview
@Composable
private fun ElseLocationDataView(
	text: String = "Loading",
	onClickSelectLocation: () -> Unit = {},
	onClickReloadLocation: () -> Unit = {},
) {
	Column {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
				.padding(16.dp)
		) {
			Text(
				text = text,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface,
			)
		}

		Spacer(modifier = Modifier.height(8.dp))

		Row(
			modifier = Modifier.fillMaxWidth()
		) {
			OutlinedButton(
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f),
				onClick = onClickSelectLocation,
			) {
				Text(text = stringResource(id = R.string.set_location_manually))
			}
			Spacer(modifier = Modifier.width(8.dp))
			Button(
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f),
				onClick = onClickReloadLocation,
			) {
				Text(text = stringResource(id = R.string.reload_location))
			}
		}
	}
}

@Preview
@Composable
private fun LocationSuccess(
	latLng: LatLng? = null,
	address: String? = null,
	onClickRemoveLocation: () -> Unit = {},
	onClickSelectLocation: () -> Unit = {},
) {
	val context = LocalContext.current
	val isDarkTheme = LocalIsDarkTheme.current
	val clipboardManager = LocalClipboardManager.current

	val mapProperties = remember {
		MapProperties(
			isBuildingEnabled = false,
			isIndoorEnabled = false,
			isMyLocationEnabled = false,
			isTrafficEnabled = false,
			latLngBoundsForCameraTarget = null,
			mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, Location.getMapStyle(isDarkTheme)),
			mapType = MapType.NORMAL,
			maxZoomPreference = 16f,
			minZoomPreference = 16f,
		)
	}
	val mapUiSettings = remember {
		MapUiSettings(
			compassEnabled = false,
			indoorLevelPickerEnabled = false,
			mapToolbarEnabled = false,
			myLocationButtonEnabled = false,
			rotationGesturesEnabled = false,
			scrollGesturesEnabled = false,
			scrollGesturesEnabledDuringRotateOrZoom = false,
			tiltGesturesEnabled = false,
			zoomControlsEnabled = false,
			zoomGesturesEnabled = false,
		)
	}

	Column(
		modifier = Modifier.fillMaxWidth(),
	) {
		latLng?.let { latLng1 ->
			val cameraPositionState: CameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(latLng1.toGLatLng(), 11f) }

			GoogleMap(
				modifier = Modifier
					.fillMaxWidth()
					.height(256.dp)
					.clip(MaterialTheme.shapes.medium),
				properties = mapProperties,
				uiSettings = mapUiSettings,
				cameraPositionState = cameraPositionState,
			) {
				Marker(
					state = MarkerState(position = latLng1.toGLatLng()),
				)
			}
		}

		Spacer(modifier = Modifier.height(8.dp))

		GenericBottomSheetInfo2(
			key = "LatLng",
			value = latLng?.toString() ?: "Unavailable",
			onLongClick = {
				latLng?.let {
					val annotatedString = buildAnnotatedString { append("geo: ${it.latitude}, ${it.longitude}") }
					clipboardManager.setText(annotatedString)
				}
			},
		)

		GenericBottomSheetInfo2(
			key = "Address",
			value = address ?: "Unavailable",
			onLongClick = {
				address?.let {
					val annotatedString = buildAnnotatedString { append(it) }
					clipboardManager.setText(annotatedString)
				}
			},
		)

		Spacer(modifier = Modifier.height(8.dp))

		Row(
			modifier = Modifier.fillMaxWidth()
		) {
			OutlinedButton(
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f),
				onClick = onClickRemoveLocation,
			) {
				Text(text = stringResource(id = R.string.remove_location))
			}
			Spacer(modifier = Modifier.width(8.dp))
			Button(
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f),
				onClick = onClickSelectLocation,
			) {
				Text(text = stringResource(id = R.string.set_location_manually))
			}
		}
	}
}

@Preview
@Composable
private fun NoPermissionView(
	onClickSelectLocation: () -> Unit = {},
) {
	val context = LocalContext.current

	val containerColor = MaterialTheme.colorScheme.surface
	val contentColor = MaterialTheme.colorScheme.onSurface

	Column(
		modifier = Modifier
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.background(containerColor, MaterialTheme.shapes.medium)
				.padding(16.dp)
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_fa_map_marker_slash),
				contentDescription = "Location permission unavailable",
				tint = contentColor,
				modifier = Modifier.requiredSize(40.dp)
			)
			Spacer(modifier = Modifier.width(12.dp))
			Text(
				text = stringResource(id = R.string.get_location_permission_message),
				style = MaterialTheme.typography.bodyMedium,
				color = contentColor,
			)
		}

		Spacer(modifier = Modifier.height(8.dp))

		Row(
			modifier = Modifier.fillMaxWidth()
		) {
			OutlinedButton(
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f),
				onClick = onClickSelectLocation,
			) {
				Text(text = stringResource(id = R.string.set_location_manually))
			}
			Spacer(modifier = Modifier.width(8.dp))
			Button(
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f),
				onClick = {
					Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
						addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
						this.data = Uri.fromParts("package", context.packageName, null)
						ContextCompat.startActivity(context, this, null)
					}
				},
			) {
				Text(text = stringResource(id = R.string.request_permission))
			}
		}
	}
}

@Preview
@Composable
private fun AutoFetchDisabledView(
	onClickSetLocation: () -> Unit = {},
) {
	val context = LocalContext.current

	val containerColor = MaterialTheme.colorScheme.surface
	val contentColor = MaterialTheme.colorScheme.onSurface

	Column(
		modifier = Modifier
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.background(containerColor, MaterialTheme.shapes.medium)
				.padding(16.dp)
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_fa_map_marker_slash),
				contentDescription = "Location permission unavailable",
				tint = contentColor,
				modifier = Modifier.requiredSize(40.dp)
			)
			Spacer(modifier = Modifier.width(12.dp))
			Text(
				text = stringResource(id = R.string.get_location_permission_message),
				style = MaterialTheme.typography.bodyMedium,
				color = contentColor,
			)
		}

		Spacer(modifier = Modifier.height(8.dp))

		Row(
			modifier = Modifier
				.fillMaxWidth()
		) {
			OutlinedButton(
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f),
				onClick = onClickSetLocation,
			) {
				Text(text = stringResource(id = R.string.set_location_manually))
			}
			Spacer(modifier = Modifier.width(8.dp))
			Button(
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f),
				onClick = {
					Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
						addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
						this.data = Uri.fromParts("package", context.packageName, null)
						ContextCompat.startActivity(context, this, null)
					}
				},
			) {
				Text(text = stringResource(id = R.string.request_permission))
			}
		}
	}
}

@Composable
private fun LocationErrorView(
	onClickSelectLocation: () -> Unit = {},
	onClickReloadLocation: () -> Unit = {},
) {
	val containerColor = MaterialTheme.colorScheme.surface
	val contentColor = MaterialTheme.colorScheme.onSurface

	Column(
		modifier = Modifier
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.background(containerColor, MaterialTheme.shapes.medium)
				.padding(16.dp)
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_fa_map_marker_error),
				contentDescription = stringResource(id = R.string.error_fetching_location_content_description),
				tint = contentColor,
				modifier = Modifier.requiredSize(40.dp)
			)
			Spacer(modifier = Modifier.width(12.dp))
			Text(
				text = stringResource(id = R.string.error_fetching_location),
				style = MaterialTheme.typography.bodyMedium,
				color = contentColor,
			)
		}

		Spacer(modifier = Modifier.height(8.dp))

		Row(
			modifier = Modifier.fillMaxWidth()
		) {
			OutlinedButton(
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f),
				onClick = onClickSelectLocation,
			) {
				Text(text = stringResource(id = R.string.set_location_manually))
			}
			Spacer(modifier = Modifier.width(8.dp))
			Button(
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f),
				onClick = onClickReloadLocation,
			) {
				Text(text = stringResource(id = R.string.reload_location))
			}
		}
	}
}
