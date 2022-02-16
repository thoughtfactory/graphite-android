package com.syncodec.momento.diaryComponent.modalBottomSheet

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import com.syncodec.momento.BuildConfig
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.custom.button.MenuBottomSheetButton
import com.syncodec.momento.custom.button.MenuBottomSheetButtonData
import com.syncodec.momento.database.diary.WeatherData
import com.syncodec.momento.diaryComponent.DiaryActivity
import com.syncodec.momento.diaryComponent.DiaryViewModel
import com.syncodec.momento.miscellaneous.roundTo
import com.syncodec.momento.miscellaneous.timeStampToPrettyFull
import compose.icons.TablerIcons
import compose.icons.WeatherIcons
import compose.icons.tablericons.*
import compose.icons.weathericons.Sunrise
import compose.icons.weathericons.Thermometer
import java.util.*
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MetadataBottomSheet() {
	val viewModel: DiaryViewModel = viewModel()
	val menuBottomSheetButtonDataList: List<MenuBottomSheetButtonData> = listOf(
		MenuBottomSheetButtonData(title = "Archive", imageVector = TablerIcons.Archive, highlight = viewModel.isArchived) {
			viewModel.isArchived = !viewModel.isArchived
		},
		MenuBottomSheetButtonData(title = "Favourite", imageVector = TablerIcons.Heart, highlight = viewModel.isFavourite) {
			viewModel.isFavourite = !viewModel.isFavourite
		},
		MenuBottomSheetButtonData(title = "Move in vault", imageVector = TablerIcons.Container, highlight = viewModel.isLocked) {
			viewModel.isLocked = !viewModel.isLocked
		},
		MenuBottomSheetButtonData(title = "Move to trash", imageVector = TablerIcons.Trash, highlight = viewModel.deletedTimestamp != -1L) {
			viewModel.deletedTimestamp = System.currentTimeMillis()
		}
	)

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
			createdTimestamp = viewModel.note.createdTimestamp,
			modifiedTimestamp = viewModel.note.modifiedTimestamp
		)

		Spacer(modifier = Modifier.height(8.dp))

		LocationCard()

		AnimatedVisibility(
			visible = viewModel.location != null
		) {
			if (viewModel.location != null) {
				Column {
					Spacer(modifier = Modifier.height(8.dp))
					MapCard(
						location = viewModel.location!!,
						mapView = viewModel.diaryActivityState.mapView
					)
				}
			}
		}

		Spacer(modifier = Modifier.height(8.dp))

		if (viewModel.weatherData != null) {
			WeatherCard(
				weatherData = viewModel.weatherData
			)
			Spacer(modifier = Modifier.height(8.dp))
		}

		StateCard(menuBottomSheetButtonDataList = menuBottomSheetButtonDataList)

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
					style = MaterialTheme.typography.bodySmall.copy(
						fontWeight = FontWeight.Bold
					),
					color = MaterialTheme.colorScheme.onSecondaryContainer
				)
				Spacer(modifier = Modifier.weight(1f))
				Text(
					text = timeStampToPrettyFull(createdTimestamp),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSecondaryContainer
				)
			}

			Spacer(modifier = Modifier.height(8.dp))

			Row(
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					text = "Last edited on : ",
					style = MaterialTheme.typography.bodySmall.copy(
						fontWeight = FontWeight.Bold
					),
					color = MaterialTheme.colorScheme.onSecondaryContainer
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
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSecondaryContainer
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalPermissionsApi::class)
@Composable
private fun LocationCard() {
	val context = LocalContext.current
	val viewModel: DiaryViewModel = viewModel()

	val addressState by viewModel.diaryActivityState.addressState

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
				DiaryActivity.AddressState.NO_PERMISSION -> true
				DiaryActivity.AddressState.REQUEST_PERMISSION -> true
				DiaryActivity.AddressState.SHOW_RATIONALE -> true
				DiaryActivity.AddressState.REMOVED -> true
				else -> false
			},
			onClick = {
				when (addressState) {
					DiaryActivity.AddressState.NO_PERMISSION -> {
						Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
							data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
							context.startActivity(this)
						}
					}
					DiaryActivity.AddressState.REQUEST_PERMISSION -> viewModel.diaryActivityState.locationPermissionState.launchPermissionRequest()
					DiaryActivity.AddressState.SHOW_RATIONALE -> viewModel.diaryActivityState.locationPermissionState.launchPermissionRequest()
					DiaryActivity.AddressState.REMOVED -> viewModel.getLocation()
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
						DiaryActivity.AddressState.OFF -> "AddressState : OFF"
						DiaryActivity.AddressState.INIT -> "Getting address..."
						DiaryActivity.AddressState.NO_PERMISSION -> "Location permission unavailable. Click to open settings."
						DiaryActivity.AddressState.REQUEST_PERMISSION -> "Location permission unavailable. Click to provide permission."
						DiaryActivity.AddressState.SHOW_RATIONALE -> "Location permission unavailable. Click to provide permission."
						DiaryActivity.AddressState.REQUESTED -> "Getting address..."
						DiaryActivity.AddressState.LOCATION -> "Address unavailable"
						DiaryActivity.AddressState.SUCCESS -> viewModel.address!!
						DiaryActivity.AddressState.ERROR -> "Error getting address"
						DiaryActivity.AddressState.REMOVED -> "Click to get address"
					},
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSecondaryContainer,
					modifier = Modifier
						.weight(1f)
				)

				Spacer(modifier = Modifier.height(8.dp))

				if (addressState == DiaryActivity.AddressState.REQUESTED ||
					addressState == DiaryActivity.AddressState.LOCATION ||
					addressState == DiaryActivity.AddressState.SUCCESS
				) {
					Text(
						text = if (viewModel.location == null) "Location unavailable"
						else "${viewModel.location!!.latitude.roundTo(6)}, ${viewModel.location!!.longitude.roundTo(6)}",
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
				modifier = Modifier
					.requiredSize(60.dp),
				onClick = { viewModel.diaryActivityState.showMapLocationDialog.value = true }
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
				modifier = Modifier
					.requiredSize(60.dp),
				onClick = { viewModel.removeLocationData() }
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
	location: Location,
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

				val latLng = LatLng(location.latitude, location.longitude)
				googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
				val markerOptions = MarkerOptions()
					.position(latLng)
				googleMap.addMarker(markerOptions)
			}
		}

	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun WeatherCard(
	weatherData: WeatherData?
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
	) {
		Card(
			elevation = 0.dp,
			shape = RoundedCornerShape(12.dp),
			backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
			modifier = Modifier
				.height(60.dp)
				.weight(1f),
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp, 8.dp)
			) {
				Icon(
					imageVector = WeatherIcons.Thermometer,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSecondaryContainer
				)

				Spacer(modifier = Modifier.width(8.dp))

				Text(
					text = "${weatherData?.temperature ?: "Temperature unavailable"}",
					style = MaterialTheme.typography.bodySmall.copy(
						fontWeight = FontWeight.Bold
					),
					color = MaterialTheme.colorScheme.onSecondaryContainer,
					modifier = Modifier
				)
			}
		}

		Spacer(modifier = Modifier.width(8.dp))

		Card(
			elevation = 0.dp,
			shape = RoundedCornerShape(12.dp),
			backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
			modifier = Modifier
				.height(60.dp)
				.weight(1f)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp, 8.dp)
			) {
				Icon(
					imageVector = WeatherIcons.Sunrise,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSecondaryContainer
				)

				Spacer(modifier = Modifier.width(8.dp))

				Text(
					text = weatherData?.description ?: "Weather data unavailable",
					style = MaterialTheme.typography.bodySmall.copy(
						fontWeight = FontWeight.Bold
					),
					color = MaterialTheme.colorScheme.onSecondaryContainer,
					modifier = Modifier
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
private fun StateCard(
	menuBottomSheetButtonDataList: List<MenuBottomSheetButtonData>
) {
	FlowRow(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp),
		mainAxisAlignment = MainAxisAlignment.SpaceBetween,
	) {
		menuBottomSheetButtonDataList.forEach {
			MenuBottomSheetButton(
				menuBottomSheetButtonData = it,
				modifier = Modifier
					.width(80.dp)
			)
		}
	}
}
