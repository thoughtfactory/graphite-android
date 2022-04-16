package com.syncodec.momento.mainComponent.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.clustering.ClusterManager
import com.syncodec.momento.MainActivity
import com.syncodec.momento.R
import com.syncodec.momento.custom.notebook.NoteCard
import com.syncodec.momento.custom.notebook.NotebookHeaderCard
import com.syncodec.momento.custom.notebook.NotebookTimelineSpacer
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.mainComponent.miscellaneous.AtlasClusterItem
import com.syncodec.momento.mainComponent.miscellaneous.ClusterRenderer
import com.syncodec.momento.miscellaneous.GoogleMapUtils.Companion.isMarkerVisible


@SuppressLint("MissingPermission")
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun AtlasScreen(
	mapView: MapView,
	noteMap: Map<String, NoteDbEntry>,
	selectedItemList: List<String>,
	onAction: (MainActivity.Action, Any?) -> Unit
) {
	val context = LocalContext.current
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp
	val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

	val markerMap: SnapshotStateList<NoteDbEntry> = remember { mutableStateListOf() }

	var swLatLng by remember { mutableStateOf(LatLng(-90.0, -180.0)) }
	var neLatLng by remember { mutableStateOf(LatLng(90.0, 180.0)) }

	LaunchedEffect(key1 = null) {
		var minLat = 90.0
		var maxLat = -90.0
		var minLng = 180.0
		var maxLng = -180.0
		noteMap.forEach { (_, note) ->
			val latLng = note.latLng
			if (latLng != null && latLng.latitude != 90.0 && latLng.longitude != 180.0) {
				minLat = minOf(latLng.latitude, minLat)
				maxLat = maxOf(latLng.latitude, maxLat)
				minLng = minOf(latLng.longitude, minLng)
				maxLng = maxOf(latLng.longitude, maxLng)
			}
		}
		swLatLng = LatLng(minLat, minLng)
		neLatLng = LatLng(maxLat, maxLng)
	}

	BottomSheetScaffold(
		scaffoldState = bottomSheetScaffoldState,
		sheetContent = {
			BottomSheetContent(
				markerMap = markerMap,
				selectedItemList = selectedItemList,
				onAction = onAction
			)
		},
		modifier = Modifier,
		sheetElevation = 32.dp,
		sheetPeekHeight = screenHeight.times(0.2f),
		sheetBackgroundColor = MaterialTheme.colorScheme.surface
	) {
		AndroidView(
			factory = { mapView },
			modifier = Modifier
				.fillMaxSize()
				.padding(0.dp, 0.dp, 0.dp, screenHeight.times(0.2f)),
		) { mapView ->
			mapView.getMapAsync {
				val map = it

				try {
					map.setMapStyle(
						MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_light)
					)
				} catch (exception: Exception) {
				}

				map.uiSettings.isZoomControlsEnabled = false
				map.uiSettings.isCompassEnabled = false
				map.uiSettings.isIndoorLevelPickerEnabled = false
				map.uiSettings.isMapToolbarEnabled = false
				map.uiSettings.isMyLocationButtonEnabled = false
				map.uiSettings.isTiltGesturesEnabled = false
				map.isMyLocationEnabled = false

				if (swLatLng.latitude != -90.0 && swLatLng.longitude != -180.0 && neLatLng.latitude != 90.0 && neLatLng.longitude != 180.0) {
					map.animateCamera(
						CameraUpdateFactory.newLatLngBounds(
							LatLngBounds(swLatLng, neLatLng),
							128
						)
					)
				}

				val clusterManager: ClusterManager<AtlasClusterItem> = ClusterManager(context, map)
				val clusterRenderer: ClusterRenderer<AtlasClusterItem> =
					ClusterRenderer(context, map, clusterManager)

				clusterManager.renderer = clusterRenderer

				noteMap.forEach { (_, note) ->
					note.latLng?.let { it1 ->
						AtlasClusterItem(latLng = it1, itemTitle = null)
					}?.also { clusterManager.addItem(it) }
				}

				map.setOnCameraMoveListener { clusterManager.cluster() }

				map.setOnCameraIdleListener {
					markerMap.clear()
					noteMap.forEach { (_, note) ->
						val latLng = note.latLng
						if (latLng != null && map.isMarkerVisible(latLng)) {
							markerMap.add(note)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun BottomSheetContent(
	markerMap: SnapshotStateList<NoteDbEntry>,
	selectedItemList: List<String>,
	onAction: (MainActivity.Action, String) -> Unit
) {
	val lastEntryKey = if (markerMap.size != 0) markerMap.last().key else null

	LazyColumn {
		item { Spacer(modifier = Modifier.height(16.dp)) }
		item {
			NotebookHeaderCard(
				title = "In Visible In Region",
				noEntries = if (markerMap.isEmpty()) "No entries" else if (markerMap.size == 1) "1 entry" else "${markerMap.size} entries",
				color = MaterialTheme.colorScheme.surface,
			)
		}
		markerMap.forEachIndexed { index, note ->
			item {
				NoteCard(
					key = note.key,
					timestamp = note.userTimestamp,
					showFullTime = true,
					isLocked = false,
					isSelected = note.key in selectedItemList,
					isArchived = false,
					isFavourite = false,
					isDeleted = note.deletedTimestamp != -1L,
					isLast = index == markerMap.size - 1,
					title = note.title,
					contentThumbnail = note.contentThumbnail,
					attachmentCount = note.attachmentKeyList.size,
					attachmentThumbnail = note.attachmentThumbnail,
					address = note.address,
					latLng = note.latLng,
					isVisible = true,
					selectedColor = MaterialTheme.colorScheme.background,
					onClick = { onAction(MainActivity.Action.CLICK_NOTE, note.key) },
					onLongClick = { onAction(MainActivity.Action.LONG_CLICK_NOTE, note.key) },
				)

				NotebookTimelineSpacer(isVisible = note.key != lastEntryKey)
			}
		}
		item { Spacer(modifier = Modifier.height(194.dp)) }
	}
}
