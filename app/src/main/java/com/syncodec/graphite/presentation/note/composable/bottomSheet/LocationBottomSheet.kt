package com.syncodec.graphite.presentation.note.composable.bottomSheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.note.NoteViewModel
import com.syncodec.graphite.utils.LocationState


@Composable
fun LocationBottomSheet(
	closeSheet: () -> Unit
) {
	val viewModel: NoteViewModel = viewModel()

	val isViewer by viewModel.isViewer

	val locationState by viewModel.locationState
	val latLng by viewModel.latLng
	val address by viewModel.address

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
			isViewer = isViewer,
			onRequestPermission = { viewModel.getLocation(tryShowRationale = true) },
			onRemoveLocation = { viewModel.removeLocation() },
			onReloadLocation = { viewModel.getLocation() },
			onSetLocation = { viewModel.showSetLocationDialog.value = true }
		)

		Spacer(modifier = Modifier.height(32.dp))
	}
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
private fun LocationCard(
	locationState: LocationState,
	latLng: LatLng? = null,
	address: String? = null,
	isViewer: Boolean,
	onRequestPermission: () -> Unit,
	onRemoveLocation: () -> Unit,
	onReloadLocation: () -> Unit,
	onSetLocation: () -> Unit
) {
	val cameraPositionState = rememberCameraPositionState {}

	LaunchedEffect(key1 = latLng) {
		if (latLng?.toGLatLng() != null) {
			latLng.toGLatLng()?.let {
				cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 15f))
			}
		}
	}

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
		AnimatedContent(targetState = locationState) {
			when(it) {
				LocationState.INIT -> {
					Column(
						modifier = Modifier
							.padding(12.dp)
							.fillMaxWidth(),
					) {
						Text(
							text = "Getting location...",
							style = MaterialTheme.typography.bodyMedium
						)
						Spacer(modifier = Modifier.height(4.dp))
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.End
						) {
							OutlinedButton(onClick = onRemoveLocation) {
								Text(
									text = "Reload location",
									style = MaterialTheme.typography.bodyMedium,
									fontWeight = FontWeight.Bold
								)
							}
							Spacer(modifier = Modifier.width(8.dp))
							Button(onClick = onSetLocation) {
								Text(
									text = "Set location",
									style = MaterialTheme.typography.labelLarge
								)
							}
						}
					}
				}
				LocationState.NO_PERMISSION -> {
					Column(
						modifier = Modifier
							.padding(12.dp)
							.fillMaxWidth(),
					) {
						Text(
							text = "Location permission unavailable",
							style = MaterialTheme.typography.bodyMedium
						)
						Spacer(modifier = Modifier.height(4.dp))
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.End
						) {
							OutlinedButton(onClick = onRequestPermission) {
								Text(
									text = "Request permission",
									style = MaterialTheme.typography.labelLarge
								)
							}
							Spacer(modifier = Modifier.width(8.dp))
							Button(onClick = onSetLocation) {
								Text(
									text = "Set location",
									style = MaterialTheme.typography.labelLarge
								)
							}
						}
					}
				}
				LocationState.LATLNG -> {
					Column(
						modifier = Modifier
							.padding(12.dp)
							.fillMaxWidth(),
					) {
						LocationGoogleMap(latLng = latLng)
						Spacer(modifier = Modifier.height(8.dp))
						Text(
							text = "${latLng?.latitude}, ${latLng?.longitude}\nGetting address...",
							style = MaterialTheme.typography.bodyMedium
						)
						Spacer(modifier = Modifier.height(4.dp))
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.End
						) {
							OutlinedButton(onClick = onRemoveLocation) {
								Text(
									text = "Remove location",
									style = MaterialTheme.typography.labelLarge
								)
							}
							Spacer(modifier = Modifier.width(8.dp))
							Button(onClick = onSetLocation) {
								Text(
									text = "Set location",
									style = MaterialTheme.typography.labelLarge
								)
							}
						}
					}
				}
				LocationState.ONLY_LATLNG -> {
					Column(
						modifier = Modifier
							.padding(12.dp)
							.fillMaxWidth(),
					) {
						LocationGoogleMap(latLng = latLng)
						Spacer(modifier = Modifier.height(8.dp))
						Text(
							text = "${latLng?.latitude}, ${latLng?.longitude}",
							style = MaterialTheme.typography.bodyMedium
						)
						Spacer(modifier = Modifier.height(4.dp))
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.End
						) {
							OutlinedButton(onClick = onRemoveLocation) {
								Text(
									text = "Remove location",
									style = MaterialTheme.typography.labelLarge
								)
							}
							Spacer(modifier = Modifier.width(8.dp))
							Button(onClick = onSetLocation) {
								Text(
									text = "Set location",
									style = MaterialTheme.typography.labelLarge
								)
							}
						}
					}
				}
				LocationState.ONLY_ADDRESS -> {
					Column(
						modifier = Modifier
							.padding(12.dp)
							.fillMaxWidth(),
					) {
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.End
						) {
							OutlinedButton(onClick = onRemoveLocation) {
								Text(
									text = "Remove location",
									style = MaterialTheme.typography.labelLarge
								)
							}
							Spacer(modifier = Modifier.width(8.dp))
							Button(onClick = onSetLocation) {
								Text(
									text = "Set location",
									style = MaterialTheme.typography.labelLarge
								)
							}
						}
					}
				}
				LocationState.SUCCESS -> {
					Column(
						modifier = Modifier
							.padding(12.dp)
							.fillMaxWidth(),
					) {
						LocationGoogleMap(latLng = latLng)
						Spacer(modifier = Modifier.height(8.dp))
						Text(
							text = "${latLng?.latitude}, ${latLng?.longitude}\n$address",
							style = MaterialTheme.typography.bodyMedium
						)
						Spacer(modifier = Modifier.height(4.dp))
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.End
						) {
							OutlinedButton(onClick = onRemoveLocation) {
								Text(
									text = "Remove location",
									style = MaterialTheme.typography.labelLarge
								)
							}
							Spacer(modifier = Modifier.width(8.dp))
							Button(onClick = onSetLocation) {
								Text(
									text = "Set location",
									style = MaterialTheme.typography.labelLarge
								)
							}
						}
					}
				}
				LocationState.ERROR -> {
					Column(
						modifier = Modifier
							.padding(12.dp)
							.fillMaxWidth(),
					) {
						Text(
							text = "Error getting location...",
							style = MaterialTheme.typography.bodyMedium
						)
						Spacer(modifier = Modifier.height(4.dp))
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.End
						) {
							OutlinedButton(onClick = onReloadLocation) {
								Text(
									text = "Reload location",
									style = MaterialTheme.typography.labelLarge
								)
							}
							Spacer(modifier = Modifier.width(8.dp))
							Button(onClick = onSetLocation) {
								Text(
									text = "Set location",
									style = MaterialTheme.typography.labelLarge
								)
							}
						}
					}
				}
				LocationState.REMOVED -> {
					Column(
						modifier = Modifier
							.padding(12.dp)
							.fillMaxWidth(),
					) {
						Text(
							text = "Location removed...",
							style = MaterialTheme.typography.bodyMedium
						)
						Spacer(modifier = Modifier.height(4.dp))
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.End
						) {
							OutlinedButton(onClick = onReloadLocation) {
								Text(
									text = "Reload location",
									style = MaterialTheme.typography.labelLarge
								)
							}
							Spacer(modifier = Modifier.width(8.dp))
							Button(onClick = onSetLocation) {
								Text(
									text = "Set location",
									style = MaterialTheme.typography.labelLarge
								)
							}
						}
					}
				}
			}
		}
	}
}

@Composable
private fun LocationGoogleMap(
	latLng: LatLng?
) {
	val cameraPositionState = rememberCameraPositionState {}

	SideEffect {
		if (latLng?.toGLatLng() != null) {
			latLng.toGLatLng()?.let {
				cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 15f))
			}
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
			)
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
				.clickable { }
		)
	}
}
