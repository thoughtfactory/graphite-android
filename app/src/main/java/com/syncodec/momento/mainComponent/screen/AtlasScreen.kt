package com.syncodec.momento.mainComponent.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.android.libraries.maps.model.PolylineOptions
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.miscellaneous.MainTopBar
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun AtlasScreen(
	mapView: MapView
) {
	val configuration = LocalConfiguration.current

	val screenHeight = configuration.screenHeightDp.dp
	val screenWidth = configuration.screenWidthDp.dp

	val viewModel: MainViewModel = viewModel()
	val scope = rememberCoroutineScope()
	val openSheet: (BottomSheetType) -> Unit = { bottomSheetType ->
		scope.launch {
			viewModel.mainActivityState.bottomSheetState.show()
		}
	}

	Scaffold(
		topBar = {
			MainTopBar()
		}
	) {
		Column(
			modifier = Modifier
				.fillMaxHeight()
				.fillMaxWidth()
				.background(Color.White)
		) {
			AndroidView({ mapView }) { mapView ->
				mapView.getMapAsync {
					val map = it
					map.uiSettings.isZoomControlsEnabled = false

					val pickUp = LatLng(-35.016, 143.321)
					val destination = LatLng(-32.491, 147.309)
					map.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 6f))
					val markerOptions = MarkerOptions()
						.title("Sydney Opera House")
						.position(pickUp)
					map.addMarker(markerOptions)

					val markerOptionsDestination = MarkerOptions()
						.title("Restaurant Hubert")
						.position(destination)
					map.addMarker(markerOptionsDestination)

					map.addPolyline(
						PolylineOptions().add(
							pickUp,
							LatLng(-34.747, 145.592),
							LatLng(-34.364, 147.891),
							LatLng(-33.501, 150.217),
							LatLng(-32.306, 149.248),
							destination
						)
					)
				}
			}
		}
	}
}
