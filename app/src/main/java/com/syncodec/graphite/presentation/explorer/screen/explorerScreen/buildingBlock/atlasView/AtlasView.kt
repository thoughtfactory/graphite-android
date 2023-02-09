package com.syncodec.graphite.presentation.explorer.screen.explorerScreen.buildingBlock.atlasView

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.utils.AtlasClusterItem
import com.syncodec.graphite.utils.ClusterRenderer
import com.syncodec.graphite.utils.isMarkerVisible
import com.syncodec.graphite.utils.share
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@OptIn(MapsComposeExperimentalApi::class)
@Preview
@Composable
fun AtlasView(
	noteList : List<NoteObjectLite> = listOf(),
	getContextNoteList : (String, List<NoteObjectLite>) -> Unit = { _, _ -> },
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val mapUiSettings by remember {
		mutableStateOf(
			MapUiSettings(
				compassEnabled = false,
				indoorLevelPickerEnabled = false,
				mapToolbarEnabled = false,
				myLocationButtonEnabled = false,
				rotationGesturesEnabled = false,
				scrollGesturesEnabled = true,
				scrollGesturesEnabledDuringRotateOrZoom = true,
				tiltGesturesEnabled = false,
				zoomControlsEnabled = false,
				zoomGesturesEnabled = true,
			)
		)
	}
	val mapProperties by remember {
		mutableStateOf(
			MapProperties(
				isBuildingEnabled = false,
				isIndoorEnabled = false,
				isMyLocationEnabled = false,
				isTrafficEnabled = false,
				latLngBoundsForCameraTarget = null,
				mapStyleOptions = null,
				mapType = MapType.NORMAL,
				maxZoomPreference = 17f,
				minZoomPreference = 1f,
			)
		)
	}

	val cameraPositionState : CameraPositionState = rememberCameraPositionState {
		position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 1f)
	}
	var latLngBounds by remember { mutableStateOf<LatLngBounds?>(null) }

	fun calculateLatLngBounds(latLngList : List<LatLng>) : LatLngBounds {
		val builder = LatLngBounds.Builder()
		for (latLng in latLngList) {
			builder.include(latLng)
		}
		return builder.build()
	}

	var isMapLoaded by remember { mutableStateOf(false) }
	LaunchedEffect(key1 = noteList, key2 = isMapLoaded) {
		scope.launch(Dispatchers.IO) {
			if (noteList.isNotEmpty() && isMapLoaded) {
				val latLngList = noteList.mapNotNull { it.latLng?.toGLatLng() }
				if (latLngList.isNotEmpty()) {
					latLngBounds = calculateLatLngBounds(latLngList).also {
						withContext(Dispatchers.Main) { cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(it, 71)) }
					}
				}
			}
		}
	}

	var clusterCoroutine by remember { mutableStateOf<CoroutineScope?>(null) }
	var googleMap1 : GoogleMap? by remember { mutableStateOf(null) }

	Box(
		modifier = Modifier.fillMaxSize()
	) {
		GoogleMap(
			cameraPositionState = cameraPositionState,
			properties = mapProperties,
			uiSettings = mapUiSettings,
			onMapLoaded = { isMapLoaded = true },
			modifier = Modifier.matchParentSize(),
		) {
			var clusterManager by remember { mutableStateOf<ClusterManager<AtlasClusterItem>?>(null) }

			MapEffect(key1 = null) { googleMap -> googleMap1 = googleMap }

			MapEffect(key1 = noteList) { googleMap ->
				clusterCoroutine?.cancel()
				clusterCoroutine = this

				googleMap.clear()
				if (clusterManager == null) {
					clusterManager = ClusterManager<AtlasClusterItem>(context, googleMap).also {
						val clusterRenderer : ClusterRenderer<AtlasClusterItem> = ClusterRenderer(context, googleMap, it)
						clusterRenderer.minClusterSize = 1
						it.renderer = clusterRenderer
						it.cluster()
					}
				}
				getContextNoteList("In visible region", noteList.filter { note -> note.latLng?.toGLatLng().let { googleMap.isMarkerVisible(it) } })

				googleMap.setOnCameraMoveListener { clusterManager?.cluster() }

				noteList
					.mapNotNull { it.latLng?.toGLatLng()?.let { it1 -> AtlasClusterItem(latLng = it1, itemTitle = null) } }
					.let {
						clusterManager?.clearItems()
						clusterManager?.addItems(it)
						clusterManager?.cluster()
					}

				googleMap.setOnCameraIdleListener {
					getContextNoteList("In visible region", noteList.filter { note -> note.latLng?.toGLatLng().let { googleMap.isMarkerVisible(it) } })
				}
			}
		}

		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp)
		) {
			FloatingActionButton(
				onClick = {
					scope.launch(Dispatchers.IO) {
						googleMap1?.snapshot {
							File.createTempFile("atlas_snapshot", ".png").let { file ->
								file.outputStream().use { outputStream ->
									it?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
									file.share(context)
								}
							}
						}
					}
				},
				modifier = Modifier.align(Alignment.TopEnd)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_camera),
					contentDescription = "Zoom to fit all markers"
				)
			}

			FloatingActionButton(
				onClick = {
					scope.launch {
						val latLngList = noteList.mapNotNull { it.latLng?.toGLatLng() }
						if (latLngList.isNotEmpty()) {
							latLngBounds = calculateLatLngBounds(latLngList).also {
								cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(it, 171))
							}
						}
					}
				},
				modifier = Modifier.align(Alignment.BottomEnd)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_expand),
					contentDescription = "Zoom to fit all markers"
				)
			}
		}
	}
}
