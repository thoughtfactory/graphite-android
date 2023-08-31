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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
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
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.ui.LocalIsDarkTheme
import com.syncodec.graphite.utils.GoogleMapUtil
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import io.realm.kotlin.types.RealmUUID


@OptIn(MapsComposeExperimentalApi::class)
@Preview
@Composable
fun AtlasView2(
	modifier: Modifier = Modifier,
	noteList: List<NoteObjectLite> = listOf(),
	onClickNote: (RealmUUID) -> Unit = {},
	onUpdateCameraBound: (LatLngBounds?) -> Unit = {}
) {
	val context = LocalContext.current
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
			items = noteList.mapNotNull { note -> note.latLng?.toGLatLng()?.let { AtlasNoteClusterItem(note = note, latLng = it, itemTitle = note.title) } },
			clusterContent = { cluster: Cluster<AtlasNoteClusterItem> ->
				val bitmap = cluster
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
						text = cluster.size.toString(),
						color = MaterialTheme.colorScheme.onSurface,
						style = MaterialTheme.typography.labelMedium,
						modifier = Modifier
							.background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp), MaterialTheme.shapes.extraSmall)
							.requiredSize(48.dp)
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
							.requiredSize(48.dp)
							.padding(2.dp)
					)
				}
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
}
