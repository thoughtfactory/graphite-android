package com.syncodec.graphite.presentation.note.composable.bottomSheet

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.maps.android.compose.*
import com.syncodec.graphite.R
import com.syncodec.graphite.di.Repository
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.presentation.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.custom.bottomSheet.BottomSheetKeyCard
import com.syncodec.graphite.presentation.custom.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.custom.bottomSheet.BottomSheetTitleCard
import com.syncodec.graphite.presentation.note.NoteViewModel
import com.syncodec.graphite.utils.LocationState
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun MetadataBottomSheet(
	closeSheet: () -> Unit
) {
	val viewModel: NoteViewModel = viewModel()

	val id by viewModel.noteId
	val parentChapterId by viewModel.parentChapterId
	val createdTimestamp by viewModel.createdTimestamp
	val modifiedTimestamp by viewModel.modifiedTimestamp
	val title by viewModel.title
	val locationState by viewModel.locationState
	val latLng by viewModel.latLng
	val address by viewModel.address

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(360.dp)
			.background(MaterialTheme.colorScheme.surface)
	) {
		BottomSheetStrip()

		BottomSheetHeader(
			title = "Metadata",
			icon = R.drawable.ic_info
		)

		BottomSheetKeyCard(
			id = id,
			createdTimestamp = createdTimestamp ?: 0,
			modifiedTimestamp = modifiedTimestamp ?: 0
		)

		Spacer(modifier = Modifier.height(8.dp))

		BottomSheetTitleCard(
			title = title,
			placeholder = "Note title"
		) { viewModel.updateTitle(it) }

		Spacer(modifier = Modifier.height(8.dp))

		ParentCard(
			parentChapterId = parentChapterId,
		) {
			viewModel.showChapterSelectorDialog.value = true
			closeSheet()
		}

		Spacer(modifier = Modifier.height(8.dp))

		LocationCard(
			locationState = locationState,
			latLng = latLng,
			address = address,
			onRequestPermission = {},
			onRemoveLocation = {
				viewModel.removeLocation()
			},
			onReloadLocation = {
				viewModel.getLocation()
			},
			onSetLocation = {}
		)

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParentCard(
	parentChapterId: ObjectId?,
	onClick: () -> Unit
) {
	val scope = rememberCoroutineScope()
	var chapterTitle by remember { mutableStateOf<String?>(null) }

	LaunchedEffect(key1 = parentChapterId) {
		scope.launch(Dispatchers.IO) {
			chapterTitle = parentChapterId?.let { Repository.getChapterTitle(id = it) }
		}
	}

	Card(
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.background,
			contentColor = MaterialTheme.colorScheme.onBackground
		),
		onClick = onClick,
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
	) {
		Row(
			modifier = Modifier
				.padding(12.dp, 8.dp)
				.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			Column(
				modifier = Modifier
			) {
				Text(
					text = "$chapterTitle",
					style = MaterialTheme.typography.headlineMedium,
				)

				Text(
					text = "$parentChapterId",
					style = MaterialTheme.typography.bodyMedium,
				)
			}

			Spacer(modifier = Modifier.width(8.dp))

			Spacer(modifier = Modifier.weight(1f))

			Icon(
				painter = painterResource(id = R.drawable.ic_notebook),
				contentDescription = "Parent chapter",
				modifier = Modifier.requiredSize(32.dp),
				tint = MaterialTheme.colorScheme.onBackground
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationCard(
	locationState: LocationState,
	latLng: LatLng? = null,
	address: String? = null,
	onRequestPermission: () -> Unit,
	onRemoveLocation: () -> Unit,
	onReloadLocation: () -> Unit,
	onSetLocation: () -> Unit
) {
	val cameraPositionState = rememberCameraPositionState {}

	SideEffect {
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
		when (locationState) {
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
					OutlinedButton(onClick = onRemoveLocation) {
						Text(
							text = "Reload location",
							style = MaterialTheme.typography.labelLarge
						)
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
					OutlinedButton(onClick = onRequestPermission) {
						Text(
							text = "Request permission",
							style = MaterialTheme.typography.labelLarge
						)
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
			LocationState.LATLNG_NO_ADDRESS -> {
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
			LocationState.ADDRESS -> {
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
					OutlinedButton(onClick = onReloadLocation) {
						Text(
							text = "Reload location",
							style = MaterialTheme.typography.labelLarge
						)
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
