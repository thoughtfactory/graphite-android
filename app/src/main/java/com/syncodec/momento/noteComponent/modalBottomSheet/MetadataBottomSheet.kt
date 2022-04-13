package com.syncodec.momento.noteComponent.modalBottomSheet

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.syncodec.momento.custom.bottomSheet.BottomSheetHeader
import com.syncodec.momento.custom.bottomSheet.BottomSheetStrip
import com.syncodec.momento.database.note.LocationData
import com.syncodec.momento.miscellaneous.TimeUtils.Companion.timeStampToPrettyFull
import com.syncodec.momento.miscellaneous.roundTo
import com.syncodec.momento.noteComponent.NoteActivity
import com.syncodec.momento.noteComponent.NoteViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.InfoCircle
import compose.icons.tablericons.Map
import compose.icons.tablericons.X
import java.util.*
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun MetadataBottomSheet(
	onClick: (NoteActivity.Action, Any?) -> Unit
) {
	val viewModel: NoteViewModel = viewModel()

	val noteDbEntry by viewModel.knotDbEntry.collectAsState()

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.background),
		horizontalAlignment = Alignment.CenterHorizontally
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Metadata",
			imageVector = TablerIcons.InfoCircle
		)

		TimestampCard(
			createdTimestamp = noteDbEntry?.createdTimestamp ?: -1,
			modifiedTimestamp = noteDbEntry?.modifiedTimestamp ?: -1
		)

		Spacer(modifier = Modifier.height(8.dp))

		LocationCard(
			addressState = viewModel.activityState.addressState.value,
			address = noteDbEntry?.address,
			latLng = if (noteDbEntry != null && noteDbEntry!!.location != null && noteDbEntry!!.location!!.latitude != null && noteDbEntry!!.location!!.longitude != null) {
				LatLng(noteDbEntry!!.location!!.latitude!!, noteDbEntry!!.location!!.longitude!!)
			} else {
				null
			}
		) { click, data -> onClick(click, data) }

		AnimatedVisibility(
			visible = noteDbEntry?.location != null,
			enter = expandVertically(tween(600)) + scaleIn(tween(600)),
			exit = shrinkVertically(tween(600)) + scaleOut(tween(600))
		) {
//          WARN    Don't remove from if block or else null pointer exception
			if (noteDbEntry?.location != null) {
				Column {
					Spacer(modifier = Modifier.height(8.dp))
					MapCard(
						location = noteDbEntry!!.location!!,
						mapView = viewModel.activityState.mapView
					)
				}
			}
		}

		Spacer(modifier = Modifier.height(32.dp))

	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimestampCard(
	createdTimestamp: Long,
	modifiedTimestamp: Long
) {
	Card(
		elevation = 0.dp,
		backgroundColor = MaterialTheme.colorScheme.background,
		border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondaryContainer),
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
				val minutes = TimeUnit.MILLISECONDS.toMinutes(calendar.timeInMillis - modifiedTimestamp)

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

@OptIn(ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
@Composable
private fun LocationCard(
	addressState: NoteActivity.AddressState,
	address: String?,
	latLng: LatLng?,
	onClick: (NoteActivity.Action, Any?) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(128.dp)
			.padding(24.dp, 0.dp)
	) {
		Card(
			elevation = 0.dp,
			backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
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
			onClick = { onClick(NoteActivity.Action.ADDRESS_CARD, null) }
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp)
			) {
				Text(
					text = when (addressState) {
						NoteActivity.AddressState.OFF -> "AddressState : OFF"
						NoteActivity.AddressState.INIT -> "Getting address..."
						NoteActivity.AddressState.NO_PERMISSION -> "Location permission unavailable. Click to open settings."
						NoteActivity.AddressState.REQUEST_PERMISSION -> "Location permission unavailable. Click to provide permission."
						NoteActivity.AddressState.SHOW_RATIONALE -> "Location permission unavailable. Click to provide permission."
						NoteActivity.AddressState.REQUESTED -> "Getting address..."
						NoteActivity.AddressState.LOCATION -> "Address unavailable"
						NoteActivity.AddressState.SUCCESS -> address!!
						NoteActivity.AddressState.ERROR -> "Error getting address"
						NoteActivity.AddressState.REMOVED -> "Click to get address"
					},
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSecondaryContainer,
					modifier = Modifier
						.weight(1f)
				)

				Spacer(modifier = Modifier.height(8.dp))

				if (addressState == NoteActivity.AddressState.REQUESTED ||
					addressState == NoteActivity.AddressState.LOCATION ||
					addressState == NoteActivity.AddressState.SUCCESS
				) {
					Text(
						text = if (latLng == null) "Location unavailable"
						else "${latLng.latitude.roundTo(6)}, ${latLng.longitude.roundTo(6)}",
						style = MaterialTheme.typography.bodySmall.copy(
							fontWeight = FontWeight.Bold
						),
						color = MaterialTheme.colorScheme.onSecondaryContainer,
						modifier = Modifier
							.fillMaxWidth()
					)
				}
			}
		}

		Column(
			modifier = Modifier
				.padding(4.dp, 0.dp, 0.dp, 0.dp)
		) {
			Card(
				elevation = 0.dp,
				shape = RoundedCornerShape(12.dp),
				backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
				modifier = Modifier.requiredSize(60.dp),
				onClick = { onClick(NoteActivity.Action.OPEN_MAP_DIALOG, null) }
			) {
				Icon(
					imageVector = TablerIcons.Map,
					contentDescription = "Pick location",
					tint = MaterialTheme.colorScheme.onSecondaryContainer,
					modifier = Modifier
						.requiredSize(20.dp)
				)
			}

			Spacer(modifier = Modifier.weight(1f))

			Card(
				elevation = 0.dp,
				shape = RoundedCornerShape(12.dp),
				backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
				modifier = Modifier.requiredSize(60.dp),
				onClick = { onClick(NoteActivity.Action.REMOVE_LOCATION, null) }
			) {
				Icon(
					imageVector = TablerIcons.X,
					contentDescription = "Remove Location",
					tint = MaterialTheme.colorScheme.onSecondaryContainer,
					modifier = Modifier
						.requiredSize(20.dp)
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MapCard(
	location: LocationData,
	mapView: MapView
) {
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

				val latLng = LatLng(location.latitude!!, location.longitude!!)
				googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
				val markerOptions = MarkerOptions()
					.position(latLng)
				googleMap.addMarker(markerOptions)
			}
		}

	}
}
