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
import com.syncodec.momento.custom.notebook.NoteCardData
import com.syncodec.momento.custom.notebook.NotebookHeaderCard
import com.syncodec.momento.custom.notebook.NotebookTimelineSpacer
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.database.note.locationDataToLatLng
import com.syncodec.momento.mainComponent.miscellaneous.AtlasClusterItem
import com.syncodec.momento.mainComponent.miscellaneous.ClusterRenderer
import com.syncodec.momento.mainComponent.miscellaneous.TopBar
import com.syncodec.momento.miscellaneous.GoogleMapUtils.Companion.isMarkerVisible


@SuppressLint("MissingPermission")
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun AtlasScreen(
	mapView: MapView,
	noteList: List<NoteDbEntry>,
	isSelected: Boolean,
	selectedItemList: List<String>,
	onClick: (MainActivity.Click, Any?) -> Unit
) {
	val context = LocalContext.current
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp
	val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

	val markerMap: SnapshotStateList<NoteCardData> = remember { mutableStateListOf() }

	var swLatLng by remember { mutableStateOf(LatLng(-90.0, -180.0)) }
	var neLatLng by remember { mutableStateOf(LatLng(90.0, 180.0)) }

	LaunchedEffect(key1 = null) {
		var minLat = 90.0
		var maxLat = -90.0
		var minLng = 180.0
		var maxLng = -180.0
		noteList.forEach {
			val latLng = locationDataToLatLng(it.location)
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
		sheetContent = { BottomSheetContent(markerMap) },
		modifier = Modifier,
		topBar = {
			TopBar(
				isSelected = isSelected,
				selectedItemSize = selectedItemList.size
			) { click, data -> onClick(click, data) }
		},
		sheetElevation = 32.dp,
		sheetPeekHeight = screenHeight.times(0.2f),
		sheetBackgroundColor = MaterialTheme.colorScheme.background
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

				noteList.forEach {
					locationDataToLatLng(it.location)?.let { it1 ->
						AtlasClusterItem(latLng = it1, itemTitle = null)
					}?.also {
						clusterManager.addItem(it)
					}
				}

				map.setOnCameraMoveListener { clusterManager.cluster() }

				map.setOnCameraIdleListener {
					markerMap.clear()
					noteList.forEach { noteDbEntry ->
						val latLng = locationDataToLatLng(noteDbEntry.location)
						if (latLng != null && map.isMarkerVisible(latLng)) {
							NoteCardData(
								key = noteDbEntry.key,
								timestamp = noteDbEntry.userTimestamp,
								showFullTime = false,
								isLocked = false,
								isSelected = noteDbEntry.key in selectedItemList,
								isArchived = false,
								isFavourite = false,
								isDeleted = noteDbEntry.deletedTimestamp != -1L,
								isLast = false,
								title = noteDbEntry.title,
								contentThumbnail = noteDbEntry.contentThumbnail,
								attachmentCount = noteDbEntry.attachmentCount,
								attachmentThumbnail = noteDbEntry.attachmentThumbnail,
								address = noteDbEntry.address,
								latLng = locationDataToLatLng(noteDbEntry.location),
								isVisible = true,
								onClick = {
									onClick(
										MainActivity.Click.CLICK_NOTE,
										noteDbEntry.key
									)
								},
								onLongClick = {
									onClick(
										MainActivity.Click.LONG_CLICK_NOTE,
										noteDbEntry.key
									)
								},
							).apply { markerMap.add(this) }
						}
					}
				}
			}
		}
	}
}

@Composable
private fun BottomSheetContent(markerMap: SnapshotStateList<NoteCardData>) {
	val lastEntryKey = if (markerMap.size != 0) markerMap.last().key else null

	LazyColumn {
		item { Spacer(modifier = Modifier.height(16.dp)) }
		item {
			NotebookHeaderCard(
				title = "In Visible In Region",
				noEntries = if (markerMap.isEmpty()) "No entries" else if (markerMap.size == 1) "1 entry" else "${markerMap.size} entries"
			)
		}
		markerMap.forEachIndexed { index, noteCardData ->
			item {
				NoteCard(noteCardData = noteCardData.copy(isLast = index == markerMap.size - 1))
				NotebookTimelineSpacer(isVisible = noteCardData.key != lastEntryKey)
			}
		}
		item { Spacer(modifier = Modifier.height(194.dp)) }
	}
}
