package com.syncodec.momento.noteComponent.miscellaneous

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.syncodec.momento.R
import com.syncodec.momento.custom.googleMap.rememberMapViewWithLifecycle
import com.syncodec.momento.noteComponent.NoteActivity
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.Circle
import compose.icons.tablericons.CurrentLocation

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MapLocationPopup(
	showMapLocationDialog: Boolean,
	latLng: LatLng?,
	address: String?,
	onClick: (NoteActivity.Click, Any?) -> Unit
) {
	var map: GoogleMap?

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
			onDismissRequest = { onClick(NoteActivity.Click.DISMISS_MAP_DIALOG, null) }
		) {
			Box(
				modifier = Modifier.fillMaxSize(0.9f),
			) {
				AndroidView(
					factory = { mapView },
					modifier = Modifier.clip(RoundedCornerShape(12.dp))
				) { mapView ->
					mapView.getMapAsync {
						map = it
						map!!.uiSettings.isZoomControlsEnabled = false

						if (!isLoadedOnce && latLng != null) {
							map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
							isLoadedOnce = true
						}

						map!!.setOnCameraMoveListener { isCameraIdle = false }
						map!!.setOnCameraIdleListener {
							isCameraIdle = true
							onClick(NoteActivity.Click.REVERSE_GEOCODE, LatLng(map!!.cameraPosition.target.latitude, map!!.cameraPosition.target.longitude))
						}
					}
				}

				Column(
					modifier = Modifier
						.padding(16.dp)
						.align(Alignment.BottomEnd)
				) {
					FloatingActionButton(
						onClick = { onClick(NoteActivity.Click.REFRESH_LOCATION, null) },
						containerColor = MaterialTheme.colorScheme.primaryContainer,
						) {
						Icon(
							imageVector = TablerIcons.CurrentLocation,
							contentDescription = "Get current location",
							tint = MaterialTheme.colorScheme.onPrimaryContainer
						)
					}

					Spacer(modifier = Modifier.height(16.dp))

					FloatingActionButton(
						onClick = { onClick(NoteActivity.Click.DISMISS_MAP_DIALOG, null) },
						containerColor = MaterialTheme.colorScheme.primaryContainer,
					) {
						Icon(
							imageVector = TablerIcons.Check,
							contentDescription = "Select location",
							tint = MaterialTheme.colorScheme.onPrimaryContainer
						)
					}
				}

				Box(
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp)
						.clip(RoundedCornerShape(12.dp))
						.background(MaterialTheme.colorScheme.primaryContainer)
				) {
					Text(
						text = if (address == null) "address unavailable" else address!!,
						style = MaterialTheme.typography.bodySmall,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onPrimaryContainer,
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
						imageVector = TablerIcons.Circle,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.primary,
						modifier = Modifier.requiredSize(8.dp)
					)
				}
			}
		}
	}
}
