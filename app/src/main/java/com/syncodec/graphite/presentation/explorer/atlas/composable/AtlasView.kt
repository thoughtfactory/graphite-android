package com.syncodec.graphite.presentation.explorer.atlas.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.NoteObjectLite
import com.syncodec.graphite.presentation.base.LocalIsDarkTheme
import com.syncodec.graphite.utils.Location


@OptIn(MapsComposeExperimentalApi::class)
@Preview
@Composable
fun AtlasView2(
	modifier: Modifier = Modifier,
	noteList: List<NoteObjectLite> = listOf(),
	onUpdateCameraBound: (LatLngBounds?) -> Unit = {}
) {
	val context = LocalContext.current
	val isDarkTheme = LocalIsDarkTheme.current

	val clusteringList by remember(noteList) { derivedStateOf { noteList.mapNotNull { note -> note.latLng?.toGLatLng()?.let { AtlasNoteClusterItem(note = note, latLng = it, itemTitle = note.title) } } } }

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
				mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, Location.getMapStyle(isDarkTheme)),
				mapType = MapType.NORMAL,
				maxZoomPreference = 18f,
				minZoomPreference = 1f,
			)
		)
	}

	val cameraPositionState: CameraPositionState = rememberCameraPositionState {
		position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 1f)
	}

	LaunchedEffect(key1 = cameraPositionState.isMoving) {
		if (!cameraPositionState.isMoving) onUpdateCameraBound(cameraPositionState.projection?.visibleRegion?.latLngBounds)
	}

	GoogleMap(
		cameraPositionState = cameraPositionState,
		contentDescription = null,
		properties = mapProperties,
		uiSettings = mapUiSettings,
		modifier = modifier,
	) {
		Clustering(
			items = clusteringList,
			clusterContent = { cluster: Cluster<AtlasNoteClusterItem> ->
				Box(
					modifier = Modifier.size(64.dp)
				) {
					Image(
						painter = painterResource(id = R.drawable.ic_map_note),
						contentDescription = null,
						modifier = Modifier
							.align(Alignment.Center)
							.requiredSize(64.dp)
							.padding(4.dp),
					)
					Text(
						text = cluster.size.toString(),
						color = MaterialTheme.colorScheme.onSurface,
						style = MaterialTheme.typography.labelMedium,
						fontWeight = FontWeight.Bold,
						modifier = Modifier
							.background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp), MaterialTheme.shapes.extraSmall)
							.padding(6.dp, 4.dp)
							.align(Alignment.BottomEnd)
					)
				}
			},
			clusterItemContent = {
				Image(
					painter = painterResource(id = R.drawable.ic_map_note),
					contentDescription = null,
					modifier = Modifier
						.requiredSize(64.dp)
						.padding(4.dp)
				)
			}
		)
	}
}

private class AtlasNoteClusterItem(
	val note: NoteObjectLite,
	val latLng: LatLng,
	val itemTitle: String?,
) : ClusterItem {
	override fun getPosition(): LatLng = latLng
	override fun getTitle(): String? = itemTitle
	override fun getSnippet(): String? = null
	override fun getZIndex(): Float = 0f
}
