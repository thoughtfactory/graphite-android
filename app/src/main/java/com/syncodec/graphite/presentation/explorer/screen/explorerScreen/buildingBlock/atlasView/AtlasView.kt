package com.syncodec.graphite.presentation.explorer.screen.explorerScreen.buildingBlock.atlasView

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.ui.LocalIsDarkTheme
import com.syncodec.graphite.utils.AtlasNoteClusterItem
import com.syncodec.graphite.utils.GoogleMapUtil
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.isMarkerVisible
import com.syncodec.graphite.utils.share
import kotlinx.coroutines.Dispatchers
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
	val isDarkTheme = LocalIsDarkTheme.current

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
				mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, GoogleMapUtil.getMapStyle(isDarkTheme)),
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

	var googleMap1 : GoogleMap? by remember { mutableStateOf(null) }

	LaunchedEffect(cameraPositionState.isMoving, noteList) {
		if (! cameraPositionState.isMoving)
			getContextNoteList("In visible region", noteList.filter { note -> note.latLng?.toGLatLng().let { googleMap1?.isMarkerVisible(it) == true } })
	}

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
			MapEffect(key1 = null) { googleMap -> googleMap1 = googleMap }

			Clustering(
				items = noteList.mapNotNull { note ->
					note.latLng?.toGLatLng()?.let { it1 -> AtlasNoteClusterItem(note = note, latLng = it1, itemTitle = null) }
				},
				clusterContent = { noteClusterItemCluster ->
					val bitmap = noteClusterItemCluster
						.items
						.firstNotNullOfOrNull { it.note.thumbnail?.decodeBase64ToBitmap() }
					Box(
						modifier = Modifier.size(52.dp)
					) {
						bitmap?.let { bitmap ->
							Image(
								painter = rememberAsyncImagePainter(bitmap),
								contentDescription = null,
								contentScale = ContentScale.Crop,
								modifier = Modifier
									.align(Alignment.Center)
									.padding(4.dp)
									.clip(MaterialTheme.shapes.small),
							)
						} ?: Image(
							painter = painterResource(id = R.drawable.ic_note_cluster),
							contentDescription = null,
							modifier = Modifier
								.align(Alignment.Center)
								.padding(2.dp),
						)
						Text(
							text = noteClusterItemCluster.size.toString(),
							color = MaterialTheme.colorScheme.onSurface,
							style = MaterialTheme.typography.labelMedium,
							modifier = Modifier
								.background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp), MaterialTheme.shapes.extraSmall)
								.padding(4.dp, 2.dp)
								.align(Alignment.TopEnd)
						)
					}
				},
				clusterItemContent = {
					Box(
						modifier = Modifier.size(52.dp)
					) {
						it.note.thumbnail?.decodeBase64ToBitmap()?.let { bitmap ->
							Image(
								painter = rememberAsyncImagePainter(bitmap),
								contentDescription = null,
								contentScale = ContentScale.Crop,
								modifier = Modifier
									.align(Alignment.Center)
									.padding(2.dp)
									.clip(MaterialTheme.shapes.small),
							)
						} ?: Image(
							painter = painterResource(id = R.drawable.ic_note_cluster),
							contentDescription = null,
							modifier = Modifier
								.align(Alignment.Center)
								.padding(2.dp),
						)
					}
				},
			)
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
				}, modifier = Modifier.align(Alignment.TopEnd)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_camera), contentDescription = "Zoom to fit all markers"
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
				}, modifier = Modifier.align(Alignment.BottomEnd)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_expand), contentDescription = "Zoom to fit all markers"
				)
			}
		}
	}
}
