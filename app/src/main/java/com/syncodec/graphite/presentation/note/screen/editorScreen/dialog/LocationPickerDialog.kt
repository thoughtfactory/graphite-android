package com.syncodec.graphite.presentation.note.screen.editorScreen.dialog

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.GoogleMapOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.common.text.KeyValueText
import com.syncodec.graphite.utils.ContentStatus
import com.syncodec.graphite.utils.locationAddressFilter
import com.syncodec.graphite.utils.reverseGeocode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
fun LocationPickerDialog(
	showDialog : Boolean = true,
	setLocation : (LatLng, String?) -> Unit = { _, _ -> },
	onDismiss : () -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	var job by remember { mutableStateOf<Job?>(null) }
	val cameraPositionState = rememberCameraPositionState()

	var addressContentStatus by remember { mutableStateOf<ContentStatus<String>>(ContentStatus.Init) }

	val iconSize by animateFloatAsState(targetValue = if (cameraPositionState.isMoving) 1.31f else 1f)
	val iconDisplacement by animateFloatAsState(targetValue = if (cameraPositionState.isMoving) 1.47f else 1f)

	BackHandler(enabled = showDialog, onBack = { onDismiss() })

	LaunchedEffect(key1 = cameraPositionState.isMoving) {
		if (! cameraPositionState.isMoving) {
			job?.cancel()
			job = scope.launch(Dispatchers.IO) {
				addressContentStatus = ContentStatus.Loading
				context.reverseGeocode(
					latitude = cameraPositionState.position.target.latitude,
					longitude = cameraPositionState.position.target.longitude,
					onAddressAvailable = { address ->
						locationAddressFilter(address)?.let { addressContentStatus = ContentStatus.Loaded(it) } ?: run {
							addressContentStatus = ContentStatus.LoadedEmpty
						}
					},
					onIoException = { addressContentStatus = ContentStatus.Error("Network Error") },
					onException = { addressContentStatus = ContentStatus.Error("Unknown Error") }
				)
			}
		}
	}

	AnimatedVisibility(
		visible = showDialog,
		enter = fadeIn(tween(300)),
		exit = fadeOut(tween(300)),
		modifier = Modifier.fillMaxSize()
	) {
		GenericScaffold(
			floatingActionButton = {
				Row(
					verticalAlignment = Alignment.Bottom,
					modifier = Modifier.fillMaxWidth(),
				) {
					KeyValueText(
						key = "Address",
						value = addressContentStatus.let {
							when (it) {
								is ContentStatus.Init -> "Loading..."
								is ContentStatus.Loading -> "Loading..."
								is ContentStatus.Loaded -> it.dataOrNull
								is ContentStatus.LoadedEmpty -> "No address found"
								is ContentStatus.Error -> it.message
							}
						},
						containerColor = MaterialTheme.colorScheme.primaryContainer,
						contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
						modifier = Modifier
							.weight(1f)
							.shadow(6.dp, shape = MaterialTheme.shapes.medium)
					)

					Spacer(modifier = Modifier.width(8.dp))

					FloatingActionButton(
						onClick = {
							setLocation(
								LatLng(
									latitude = cameraPositionState.position.target.latitude,
									longitude = cameraPositionState.position.target.longitude,
								),
								addressContentStatus.let {
									when (it) {
										is ContentStatus.Init -> null
										is ContentStatus.Loading -> null
										is ContentStatus.Loaded -> it.dataOrNull
										is ContentStatus.LoadedEmpty -> null
										is ContentStatus.Error -> null
									}
								}
							)
						}
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_check),
							contentDescription = "Select location",
						)
					}
				}
			},
		) {
			GoogleMap(
				cameraPositionState = cameraPositionState,
				googleMapOptionsFactory = {
					GoogleMapOptions().apply {
						this.rotateGesturesEnabled(false)
						this.rotateGesturesEnabled(false)
						this.scrollGesturesEnabledDuringRotateOrZoom(false)
						this.tiltGesturesEnabled(false)
						this.zoomGesturesEnabled(true)
						this.scrollGesturesEnabled(false)
					}
				},
				uiSettings = MapUiSettings(
					compassEnabled = false,
					indoorLevelPickerEnabled = false,
					mapToolbarEnabled = false,
					myLocationButtonEnabled = false,
					rotationGesturesEnabled = false,
					scrollGesturesEnabled = true,
					scrollGesturesEnabledDuringRotateOrZoom = false,
					tiltGesturesEnabled = false,
					zoomControlsEnabled = false,
					zoomGesturesEnabled = true
				),
				properties = MapProperties(),
				modifier = Modifier.fillMaxSize()
			)
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier.fillMaxSize(),
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_ring),
					contentDescription = "Map center",
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.requiredSize(8.dp)
				)

				Icon(
					painter = painterResource(id = R.drawable.ic_map_pin),
					contentDescription = "Map center",
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier
						.requiredSize(40.dp)
						.graphicsLayer {
							translationY = - (28 * iconDisplacement).dp.toPx()
							scaleX = iconSize
							scaleY = iconSize
						}
				)

				TopAppBar(
					navigationIcon = {
						GenericButton(
							icon = R.drawable.ic_close,
							onClick = onDismiss
						)
					},
					title = { /*TODO*/ },
					colors = TopAppBarDefaults.topAppBarColors(
						containerColor = Color.Transparent,
					),
					modifier = Modifier
						.fillMaxWidth()
						.align(Alignment.TopCenter)
				)
			}
		}
	}
}
