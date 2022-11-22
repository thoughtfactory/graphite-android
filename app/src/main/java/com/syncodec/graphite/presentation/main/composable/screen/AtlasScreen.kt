package com.syncodec.graphite.presentation.main.composable.screen

import android.graphics.Bitmap
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.AttachmentObject
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.main.composable.LocalCompositionTagList
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookHeaderCard
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.listView.NoteListCard
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.listView.NotebookTimelineSpacer
import com.syncodec.graphite.utils.AtlasClusterItem
import com.syncodec.graphite.utils.ClusterRenderer
import com.syncodec.graphite.utils.isMarkerVisible
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterialApi::class, MapsComposeExperimentalApi::class)
@Composable
fun AtlasScreen(
	noteList : List<NoteObjectLite>,
	onClickNote: (RealmUUID) -> Unit,
	onLongClickNote: (RealmUUID) -> Unit,
) {
//	TODO note update not reflected in atlas directly
	val context = LocalContext.current
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp

	val selectedRealmUUIDList = LocalCompositionSelectedObjectIdList.current

	val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

	var swLatLng by remember { mutableStateOf(LatLng(- 90.0, - 180.0)) }
	var neLatLng by remember { mutableStateOf(LatLng(90.0, 180.0)) }

	val markerMap : SnapshotStateList<NoteObjectLite> = remember { mutableStateListOf() }
	val cameraPositionState = rememberCameraPositionState()

	var isDataReady by remember { mutableStateOf(true) }

	LaunchedEffect(key1 = noteList) {
		isDataReady = false

		var minLat = 90.0
		var maxLat = - 90.0
		var minLng = 180.0
		var maxLng = - 180.0
		noteList.forEach { note ->
			val latLng = note.latLng
			if (latLng?.latitude != null && latLng.longitude != null && latLng.latitude != 90.0 && latLng.longitude != 180.0) {
				minLat = minOf(latLng.latitude !!, minLat)
				maxLat = maxOf(latLng.latitude !!, maxLat)
				minLng = minOf(latLng.longitude !!, minLng)
				maxLng = maxOf(latLng.longitude !!, maxLng)
			}
		}
		swLatLng = LatLng(minLat, minLng)
		neLatLng = LatLng(maxLat, maxLng)

		isDataReady = true
	}

	BottomSheetScaffold(
		scaffoldState = bottomSheetScaffoldState,
		sheetContent = {
			BottomSheetContent(
				noteList = markerMap,
				selectedItemList = selectedRealmUUIDList,
				onClickNote = onClickNote,
				onLongClickNote = onLongClickNote
			)
		},
		modifier = Modifier, sheetElevation = 32.dp, sheetPeekHeight = screenHeight.times(0.2f), sheetBackgroundColor = MaterialTheme.colorScheme.surface
	) {
		GoogleMap(
			googleMapOptionsFactory = { GoogleMapOptions() },
			cameraPositionState = cameraPositionState,
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
				zoomGesturesEnabled = true,
			),
			properties = MapProperties(mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, if (isSystemInDarkTheme()) R.raw.map_style_dark else R.raw.map_style_light)),
			modifier = Modifier
				.fillMaxSize()
				.padding(0.dp, 0.dp, 0.dp, screenHeight.times(0.2f)),
		) {
			MapEffect(key1 = isDataReady) {
				if (isDataReady) {
					if (swLatLng.latitude != - 90.0 && swLatLng.longitude != - 180.0 && neLatLng.latitude != 90.0 && neLatLng.longitude != 180.0) {
						try {
							cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(LatLngBounds(swLatLng.toGLatLng() !!, neLatLng.toGLatLng() !!), 128))
						} catch (e : Exception) {
//							e.printStackTrace()
						}
					}
				}
			}
			MapEffect(key1 = noteList) {
				it.clear()
				val clusterManager : ClusterManager<AtlasClusterItem> = ClusterManager(context, it)
				val clusterRenderer : ClusterRenderer<AtlasClusterItem> = ClusterRenderer(context, it, clusterManager)

				clusterManager.renderer = clusterRenderer
				clusterManager.cluster()

				it.setOnCameraMoveListener { clusterManager.cluster() }
				it.setOnCameraIdleListener {
					markerMap.clear()
					noteList.forEach { note ->
						val latLng = note.latLng
						if (latLng != null && it.isMarkerVisible(latLng.toGLatLng())) markerMap.add(note)
					}
				}

				noteList.forEach { note ->
					note.latLng?.toGLatLng()?.let { it1 -> AtlasClusterItem(latLng = it1, itemTitle = null) }?.also { clusterManager.addItem(it) }
				}
			}
		}
	}
}

@Composable
private fun BottomSheetContent(
	noteList : List<NoteObjectLite>,
	selectedItemList : List<RealmUUID>,
	onClickNote : (RealmUUID) -> Unit,
	onLongClickNote: (RealmUUID) -> Unit,
) {
	val context = LocalContext.current

	val lastEntryKey = if (noteList.isNotEmpty()) noteList.last().id else null

	val tagList = LocalCompositionTagList.current

	LazyColumn {
		item { Spacer(modifier = Modifier.height(16.dp)) }
		item {
			NotebookHeaderCard(
				title = "In Visible In Region",
				noEntries = if (noteList.isEmpty()) "No entries" else if (noteList.size == 1) "1 entry" else "${noteList.size} entries",
				color = MaterialTheme.colorScheme.surface,
			)
		}
		noteList.sortedBy { it.userTimestamp }.reversed().forEachIndexed { index, note ->
			item {
				var thumbnail by remember { mutableStateOf<Bitmap?>(null) }

				LaunchedEffect(key1 = note.id.hashCode() + note.thumbnail.hashCode()) {
					try {
						if (note.thumbnailType == AttachmentObject.Companion.Type.IMAGE.name) thumbnail = note.thumbnail
					} catch (e : Exception) {
						e.printStackTrace()
					}
				}

				NoteListCard(
					id = note.id,
					parentChapterId = note.parentChapterId,
					timestamp = note.userTimestamp,
					showFullTime = false,
					isLocked = note.isLocked,
					isSelected = note.id in selectedItemList,
					isFavourite = note.isFavourite,
					isLast = note.id == lastEntryKey,
					title = note.title,
					contentThumbnail = note.contentThumbnail,
					attachmentCount = note.attachmentCount,
					attachmentThumbnail = thumbnail,
					address = note.address,
					latLng = note.latLng,
					tagList = tagList.filter { it.objectIdList.contains(note.id) },
					isVisible = true,
					isSwipable = false,
					selectedColor = MaterialTheme.colorScheme.surface,
					onClick = { onClickNote(note.id) },
				) { onLongClickNote(note.id) }

				NotebookTimelineSpacer(isVisible = note.id != lastEntryKey)

			}
		}
		item { Spacer(modifier = Modifier.height(194.dp)) }
	}
}
