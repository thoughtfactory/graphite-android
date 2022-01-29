package com.syncodec.momento.diaryComponent.modalBottomSheet

import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import com.syncodec.momento.custom.BottomSheetHeader
import com.syncodec.momento.custom.BottomSheetStrip
import com.syncodec.momento.custom.googleMap.rememberMapViewWithLifecycle
import com.syncodec.momento.database.diary.WeatherData
import com.syncodec.momento.diaryComponent.DiaryViewModel
import com.syncodec.momento.miscellaneous.timeStampToPrettyFull
import compose.icons.TablerIcons
import compose.icons.WeatherIcons
import compose.icons.tablericons.InfoCircle
import compose.icons.tablericons.Map
import compose.icons.tablericons.X
import compose.icons.weathericons.Sunrise
import compose.icons.weathericons.Thermometer
import java.util.*
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MetadataBottomSheet() {
	val context = LocalContext.current
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp
	val screenWidth = configuration.screenWidthDp.dp

	val viewModel: DiaryViewModel = viewModel()

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
			createdTimestamp = viewModel.diary.createdTimestamp,
			modifiedTimestamp = viewModel.diary.modifiedTimestamp
		)

		Spacer(modifier = Modifier.height(8.dp))
		LocationCard(
			location = viewModel.location,
			address = viewModel.address
		) {
			viewModel.removeLocationData()
		}

		AnimatedVisibility(
			visible = viewModel.location != null
		) {
			if (viewModel.location != null) {
				Column {
					Spacer(modifier = Modifier.height(8.dp))
					MapCard(
						location = viewModel.location!!
					)
				}
			}
		}

		Spacer(modifier = Modifier.height(8.dp))
		WeatherCard(
			weatherData = viewModel.weatherData
		)

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
		backgroundColor = MaterialTheme.colorScheme.primaryContainer,
		shape = RoundedCornerShape(8.dp),
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
			.clip(RoundedCornerShape(12.dp)),
		onClick = { /*TODO*/ }
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
					color = MaterialTheme.colorScheme.onPrimaryContainer
				)
				Spacer(modifier = Modifier.weight(1f))
				Text(
					text = timeStampToPrettyFull(createdTimestamp),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onPrimaryContainer
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
					color = MaterialTheme.colorScheme.onPrimaryContainer
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
					color = MaterialTheme.colorScheme.onPrimaryContainer
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun LocationCard(
	location: Location?,
	address: String?,
	removeLocationData: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(128.dp)
			.padding(24.dp, 0.dp)
	) {
		Card(
			elevation = 0.dp,
			backgroundColor = MaterialTheme.colorScheme.primaryContainer,
			shape = RoundedCornerShape(12.dp),
			modifier = Modifier
				.weight(1f)
				.padding(0.dp, 0.dp, 4.dp, 0.dp)
				.clip(RoundedCornerShape(8.dp)),
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp)
			) {

				Text(
					text = address?.replace(", ", ",\n") ?: "Address unavailable",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onPrimaryContainer,
					maxLines = 5,
					modifier = Modifier
						.weight(1f)
				)

				Spacer(modifier = Modifier.height(8.dp))

				Text(
					text = if (location == null) "Location unavailable" else "${location.latitude}, ${location.longitude}",
					style = MaterialTheme.typography.bodySmall.copy(
						fontWeight = FontWeight.Bold
					),
					color = MaterialTheme.colorScheme.onPrimaryContainer,
					modifier = Modifier
						.fillMaxWidth()
				)
			}
		}

		Column(
			modifier = Modifier
				.padding(4.dp, 0.dp, 0.dp, 0.dp)
		) {
			FloatingActionButton(
				onClick = { /*TODO*/ },
				elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
				shape = RoundedCornerShape(12.dp),
				modifier = Modifier
					.requiredSize(60.dp)
			) {
				Icon(
					imageVector = TablerIcons.Map,
					contentDescription = "Pick location",
					tint = MaterialTheme.colorScheme.onPrimaryContainer,
					modifier = Modifier
						.requiredSize(20.dp)
				)
			}

			Spacer(modifier = Modifier.weight(1f))

			FloatingActionButton(
				onClick = { removeLocationData() },
				elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
				shape = RoundedCornerShape(12.dp),
				modifier = Modifier
					.requiredSize(60.dp)
			) {
				Icon(
					imageVector = TablerIcons.X,
					contentDescription = "Remove Location",
					tint = MaterialTheme.colorScheme.onPrimaryContainer,
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
	location: Location
) {
	val mapView = rememberMapViewWithLifecycle()

	Card(
		elevation = 0.dp,
		backgroundColor = MaterialTheme.colorScheme.primaryContainer,
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
			backgroundColor = MaterialTheme.colorScheme.primaryContainer,
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
					tint = MaterialTheme.colorScheme.onPrimaryContainer
				)

				Spacer(modifier = Modifier.width(8.dp))

				Text(
					text = "${weatherData?.temperature ?: "Temperature unavailable"}",
					style = MaterialTheme.typography.bodySmall.copy(
						fontWeight = FontWeight.Bold
					),
					color = MaterialTheme.colorScheme.onPrimaryContainer,
					modifier = Modifier
				)
			}
		}

		Spacer(modifier = Modifier.width(8.dp))

		Card(
			elevation = 0.dp,
			shape = RoundedCornerShape(12.dp),
			backgroundColor = MaterialTheme.colorScheme.primaryContainer,
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
					tint = MaterialTheme.colorScheme.onPrimaryContainer
				)

				Spacer(modifier = Modifier.width(8.dp))

				Text(
					text = weatherData?.description ?: "Weather data unavailable",
					style = MaterialTheme.typography.bodySmall.copy(
						fontWeight = FontWeight.Bold
					),
					color = MaterialTheme.colorScheme.onPrimaryContainer,
					modifier = Modifier
				)
			}
		}
	}
}
