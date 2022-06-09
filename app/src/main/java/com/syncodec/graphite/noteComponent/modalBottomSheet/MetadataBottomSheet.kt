package com.syncodec.graphite.noteComponent.modalBottomSheet

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.custom.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.miscellaneous.TimeUtils.Companion.timeStampToPrettyFull
import com.syncodec.graphite.miscellaneous.roundTo
import com.syncodec.graphite.noteComponent.NoteActivity
import java.util.*
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MetadataBottomSheet(
	createdTimestamp: Long,
	modifiedTimestamp: Long,
	latLng: LatLng?,
	address: String?,
	addressState: NoteActivity.AddressState,
	mapView: MapView,
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color = MaterialTheme.colorScheme.surface,
		modifier = Modifier.heightIn(360.dp),
	) {
		Column(
			modifier = Modifier,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			BottomSheetStrip()

			BottomSheetHeader(
				title = "Metadata",
				icon = R.drawable.ic_info
			)

			TimestampCard(
				createdTimestamp = createdTimestamp,
				modifiedTimestamp = modifiedTimestamp
			)

			Spacer(modifier = Modifier.height(8.dp))

			LocationCard(
				addressState = addressState,
				address = address,
				latLng = if (latLng != null)
					LatLng(latLng.latitude, latLng.longitude)
				else null,
				onAction = onAction
			)

			AnimatedVisibility(
				visible = latLng != null,
				enter = expandVertically(tween(600)) + scaleIn(tween(600)),
				exit = shrinkVertically(tween(600)) + scaleOut(tween(600))
			) {
//          WARN    Don't remove from if block or else null pointer exception
				if (latLng != null) {
					Column {
						Spacer(modifier = Modifier.height(8.dp))
						MapCard(
							latLng = latLng,
							mapView = mapView
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(32.dp))
		}
	}
}

@Composable
fun TimestampCard(
	createdTimestamp: Long,
	modifiedTimestamp: Long
) {
	Card(
		elevation = 0.dp,
		backgroundColor = MaterialTheme.colorScheme.background,
		shape = RoundedCornerShape(12.dp),
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					text = "Created on : ",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground
				)
				Spacer(modifier = Modifier.weight(1f))
				Text(
					text = timeStampToPrettyFull(createdTimestamp),
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onBackground
				)
			}

			Spacer(modifier = Modifier.height(8.dp))

			Row(
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					text = "Last edited on : ",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground
				)
				Spacer(modifier = Modifier.weight(1f))

				val calendar = Calendar.getInstance()
				val days = TimeUnit.MILLISECONDS.toDays(calendar.timeInMillis - modifiedTimestamp)
				val hours = TimeUnit.MILLISECONDS.toHours(calendar.timeInMillis - modifiedTimestamp)
				val minutes =
					TimeUnit.MILLISECONDS.toMinutes(calendar.timeInMillis - modifiedTimestamp)

				val modifiedTimestampPretty = if (days in 1..7) {
					"About $days days ago"
				} else if (days > 7) {
					timeStampToPrettyFull(modifiedTimestamp)
				} else {
					if (hours in 1..24) {
						"About $hours hour${if (hours == 1L) "" else "s"} ago"
					} else {
						if (minutes in 1..60) {
							"About $minutes minute${if (minutes == 1L) "" else "s"} ago"
						} else {
							"About few seconds ago"
						}
					}
				}

				Text(
					text = modifiedTimestampPretty,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onBackground
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun LocationCard(
	addressState: NoteActivity.AddressState,
	address: String?,
	latLng: LatLng?,
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(128.dp)
			.padding(24.dp, 0.dp)
	) {
		Card(
			elevation = 0.dp,
			backgroundColor = MaterialTheme.colorScheme.background,
			shape = RoundedCornerShape(12.dp),
			modifier = Modifier
				.height(128.dp)
				.weight(1f)
				.padding(0.dp, 0.dp, 4.dp, 0.dp),
			enabled = when (addressState) {
				NoteActivity.AddressState.NO_PERMISSION -> true
				NoteActivity.AddressState.REQUEST_PERMISSION -> true
				NoteActivity.AddressState.SHOW_RATIONALE -> true
				NoteActivity.AddressState.REMOVED -> true
				else -> false
			},
			onClick = {
				if (addressState == NoteActivity.AddressState.REQUEST_PERMISSION) {
					onAction(NoteActivity.Action.REQUEST_LOCATION_PERMISSION, true)
				} else {
					onAction(NoteActivity.Action.TRY_GET_LOCATION, true)
				}
			}
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp)
			) {
				Text(
					text = when (addressState) {
						NoteActivity.AddressState.OFF -> "Initializing..."
						NoteActivity.AddressState.INIT -> "Getting address..."
						NoteActivity.AddressState.NO_PERMISSION -> "Location permission unavailable. Click to open settings."
						NoteActivity.AddressState.REQUEST_PERMISSION -> "Location permission unavailable. Click to provide permission."
						NoteActivity.AddressState.SHOW_RATIONALE -> "Location permission unavailable. Click to provide permission."
						NoteActivity.AddressState.PERMISSION_REQUESTED -> "Getting location..."
						NoteActivity.AddressState.LOCATION_REQUESTED -> "Getting location..."
						NoteActivity.AddressState.LOCATION -> "Address unavailable"
						NoteActivity.AddressState.SUCCESS -> address ?: "Error getting location..."
						NoteActivity.AddressState.ERROR -> "Error getting address"
						NoteActivity.AddressState.REMOVED -> "Click to get address"
					},
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.weight(1f)
				)

				Spacer(modifier = Modifier.height(8.dp))

				if (addressState == NoteActivity.AddressState.PERMISSION_REQUESTED ||
					addressState == NoteActivity.AddressState.LOCATION ||
					addressState == NoteActivity.AddressState.SUCCESS
				) {
					Text(
						text = if (latLng == null) "Location unavailable"
						else "${latLng.latitude.roundTo(6)}, ${latLng.longitude.roundTo(6)}",
						style = MaterialTheme.typography.bodySmall.copy(
							fontWeight = FontWeight.Bold
						),
						color = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier.fillMaxWidth()
					)
				}
			}
		}

		Column(
			modifier = Modifier.padding(4.dp, 0.dp, 0.dp, 0.dp)
		) {
			Card(
				elevation = 0.dp,
				shape = RoundedCornerShape(12.dp),
				backgroundColor = MaterialTheme.colorScheme.background,
				modifier = Modifier.requiredSize(60.dp),
				onClick = { onAction(NoteActivity.Action.OPEN_MAP_DIALOG, null) }
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_atlas),
					contentDescription = "Pick location",
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.requiredSize(20.dp)
				)
			}

			Spacer(modifier = Modifier.weight(1f))

			Card(
				elevation = 0.dp,
				shape = RoundedCornerShape(12.dp),
				backgroundColor = MaterialTheme.colorScheme.background,
				modifier = Modifier.requiredSize(60.dp),
				onClick = { onAction(NoteActivity.Action.REMOVE_LOCATION, null) }
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_close),
					contentDescription = "Remove Location",
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.requiredSize(20.dp)
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MapCard(
	latLng: LatLng,
	mapView: MapView
) {
	val context = LocalContext.current

	Card(
		elevation = 0.dp,
		backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
		shape = RoundedCornerShape(16.dp),
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
			.height(128.dp)
			.clip(RoundedCornerShape(12.dp)),
		onClick = { }
	) {
		AndroidView({ mapView }) { mapView ->
			mapView.getMapAsync { googleMap ->
				googleMap.uiSettings.isZoomControlsEnabled = false
				googleMap.uiSettings.setAllGesturesEnabled(false)

				googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
				val markerOptions = MarkerOptions().position(latLng)
				googleMap.addMarker(markerOptions)
					?.setIcon(
						BitmapDescriptorFactory.fromBitmap(
							Bitmap.createScaledBitmap(
								BitmapFactory.decodeResource(
									context.resources,
									R.drawable.ic_map_marker_colored
								), 80, 80, false
							)
						)
					)
			}
		}

	}
}
