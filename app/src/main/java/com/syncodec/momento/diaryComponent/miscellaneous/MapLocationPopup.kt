package com.syncodec.momento.diaryComponent.miscellaneous

import android.util.Log
import android.widget.Toast
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.model.LatLng
import com.syncodec.momento.R
import com.syncodec.momento.custom.googleMap.rememberMapViewWithLifecycle
import com.syncodec.momento.diaryComponent.DiaryActivity
import com.syncodec.momento.diaryComponent.DiaryViewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.Circle
import compose.icons.tablericons.CurrentLocation
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MapLocationPopup() {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	var map: GoogleMap? = null

	val viewModel: DiaryViewModel = viewModel()
	val location = viewModel.location
	var address by remember { mutableStateOf(viewModel.address) }

	if (viewModel.diaryActivityState.showMapLocationDialog.value) {
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
			onDismissRequest = { viewModel.diaryActivityState.showMapLocationDialog.value = false }
		) {
			Box(
				modifier = Modifier
					.fillMaxSize(0.9f),
			) {
				AndroidView(
					factory = { mapView },
					modifier = Modifier
						.clip(RoundedCornerShape(12.dp))
				) { mapView ->
					mapView.getMapAsync {
						map = it
						map!!.uiSettings.isZoomControlsEnabled = false

						if (!isLoadedOnce && viewModel.location != null) {
							map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location!!.latitude, location.longitude), 15f))
							isLoadedOnce = true
						}

						map!!.setOnCameraMoveListener { isCameraIdle = false }
						map!!.setOnCameraIdleListener {
							isCameraIdle = true

							viewModel.reverseGeocode(
								latitude = map!!.cameraPosition.target.latitude,
								longitude = map!!.cameraPosition.target.longitude,
								onAddressAvailable = { _address ->
									scope.launch {
										address = if (_address != null) {
											"${_address.featureName} ${_address.thoroughfare}, ${_address.locality}, ${_address.subAdminArea}, ${_address.adminArea} ${_address.postalCode}, ${_address.countryName}"
										} else {
											null
										}
									}
								},
								onIoException = {
									Log.i("Diary Activity", "Reverse Geocode : IO Exception : Maybe network unavailable")
								},
								onException = {
									Log.e("Diary Activity", "Reverse Geocode : Exception")
								}
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
							if (viewModel.location != null) {
								map!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location!!.latitude, location.longitude), 15f))
							} else {
								Toast.makeText(context, "Location unavailable", Toast.LENGTH_LONG).show()
							}
						},

						) {
						Icon(
							imageVector = TablerIcons.CurrentLocation,
							contentDescription = null
						)
					}

					Spacer(modifier = Modifier.height(16.dp))

					FloatingActionButton(
						onClick = {
							viewModel.location = location
							viewModel.address = address
							if (viewModel.address != null) {
								viewModel.diaryActivityState.addressState.value = DiaryActivity.AddressState.SUCCESS
							}
							viewModel.diaryActivityState.showMapLocationDialog.value = false
						},
					) {
						Icon(
							imageVector = TablerIcons.Check,
							contentDescription = null
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
						modifier = Modifier
							.padding(12.dp)
					)
				}

				Box(
					modifier = Modifier
						.fillMaxSize(),
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
						modifier = Modifier
							.requiredSize(8.dp)
					)
				}

			}
		}
	}
}
