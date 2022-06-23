package com.syncodec.graphite.noteComponent.miscellaneous

import android.location.Geocoder
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.googleMap.rememberMapViewWithLifecycle
import com.syncodec.graphite.miscellaneous.locationAddressFilter
import com.syncodec.graphite.noteComponent.NoteActivity
import java.util.*


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MapLocationPopup(
	showMapLocationDialog: Boolean,
	latLng: LatLng?,
	address: String?,
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	val context = LocalContext.current
	var currentLatLng by remember { mutableStateOf(latLng) }
	var currentAddress by remember { mutableStateOf(address) }

	var map: GoogleMap? = null

	LaunchedEffect(key1 = currentLatLng) {
		if (currentLatLng != null) {
			try {
				val geocoder = Geocoder(context, Locale.getDefault())
				val addressList =
					geocoder.getFromLocation(currentLatLng!!.latitude, currentLatLng!!.longitude, 1)
				if (addressList.isNotEmpty()) {
					val addressFirst = addressList.firstOrNull()
					currentAddress = locationAddressFilter(addressFirst)
				}
			} catch (exception: Exception) {

			}
		}
	}

	if (showMapLocationDialog) {
		val mapView = rememberMapViewWithLifecycle()
		var isLoadedOnce by remember { mutableStateOf(false) }
		var isCameraIdle by remember { mutableStateOf(false) }

		val scaleMarker by animateFloatAsState(
			targetValue = if (isCameraIdle) 1f else 1.3f,
			animationSpec = tween(
				durationMillis = 200
			)
		)
		val translateY by animateFloatAsState(
			targetValue = if (isCameraIdle) 0f else 1f
		)

		Dialog(
			properties = DialogProperties(usePlatformDefaultWidth = false),
			onDismissRequest = { onAction(NoteActivity.Action.DISMISS_MAP_DIALOG, null) }
		) {
			Box(
				modifier = Modifier.fillMaxSize(0.9f),
			) {
				AndroidView(
					factory = { mapView },
					modifier = Modifier.clip(RoundedCornerShape(12.dp))
				) { mapView ->
					mapView.getMapAsync { googleMap ->
						map = googleMap
						googleMap.uiSettings.isZoomControlsEnabled = false

						if (!isLoadedOnce && latLng != null) {
							googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
							isLoadedOnce = true
						}

						googleMap.setOnCameraMoveListener { isCameraIdle = false }
						googleMap.setOnCameraIdleListener {
							isCameraIdle = true
							currentLatLng = LatLng(
								googleMap.cameraPosition.target.latitude,
								googleMap.cameraPosition.target.longitude
							)
						}
					}
				}

				Column(
					modifier = Modifier
						.padding(16.dp)
						.align(Alignment.BottomEnd)
				) {
					FloatingActionButton(
						onClick = {
							currentLatLng = latLng
							currentAddress = address
							latLng?.let { CameraUpdateFactory.newLatLng(it) }?.let {
								map?.animateCamera(it)
							}
						},
						containerColor = MaterialTheme.colorScheme.primary,
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_gps),
							contentDescription = "Get current location",
							tint = MaterialTheme.colorScheme.onPrimary,
							modifier = Modifier.requiredSize(24.dp)
						)
					}

					Spacer(modifier = Modifier.height(16.dp))

					FloatingActionButton(
						onClick = {
							onAction(
								NoteActivity.Action.DISMISS_MAP_DIALOG_AND_UPDATE_LOCATION,
								Pair(currentLatLng, currentAddress)
							)
						},
						containerColor = MaterialTheme.colorScheme.primary,
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_done),
							contentDescription = "Select location",
							tint = MaterialTheme.colorScheme.onPrimary,
							modifier = Modifier.requiredSize(24.dp)
						)
					}
				}

				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp)
						.clip(RoundedCornerShape(12.dp))
						.background(MaterialTheme.colorScheme.primary),
				) {
					Text(
						text = currentAddress ?: "address unavailable",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onPrimary,
						modifier = Modifier.padding(12.dp)
					)

					Spacer(modifier = Modifier.height(8.dp))

					Text(
						text = "${currentLatLng?.latitude}, ${currentLatLng?.longitude}",
						style = MaterialTheme.typography.bodySmall,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onPrimary,
						modifier = Modifier.padding(12.dp)
					)
				}

				Box(
					modifier = Modifier.fillMaxSize(),
					contentAlignment = Alignment.Center
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_map_pin),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.primary,
						modifier = Modifier
							.requiredSize(32.dp)
							.graphicsLayer {
								this.translationY = -25.dp.toPx() - (translateY * 20).dp.toPx()
								this.scaleX = scaleMarker
								this.scaleY = scaleMarker
							}
					)

					Icon(
						painter = painterResource(id = R.drawable.  ic_circle),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.primary,
						modifier = Modifier.requiredSize(8.dp)
					)
				}
			}
		}
	}
}
