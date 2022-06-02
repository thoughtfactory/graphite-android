package com.syncodec.graphite.mainComponent.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.mainComponent.MainActivity
import com.syncodec.graphite.custom.calendarView.Calendar
import com.syncodec.graphite.custom.notebook.NoteCard
import com.syncodec.graphite.custom.notebook.NotebookHeaderCard
import com.syncodec.graphite.custom.notebook.NotebookTimelineSpacer
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.miscellaneous.TimeUtils


@OptIn(ExperimentalAnimationApi::class)
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun CalendarScreen(
	noteMap: Map<String, NoteDbEntry>,
	selectedItemList: List<String>,
	onAction: (MainActivity.Action, Any?) -> Unit
) {
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp
	val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

	var selectedTimestamp: Long by remember { mutableStateOf(TimeUtils.getToday()) }
	val noteDbEntryDayMap: SnapshotStateMap<Long, MutableList<NoteDbEntry>> =
		remember { mutableStateMapOf() }
	val timestampSizeMap: SnapshotStateMap<Long, Int> = remember { mutableStateMapOf() }
	LaunchedEffect(key1 = null) {
		noteMap.forEach { (_, note) ->
			val timestamp = TimeUtils.timestampToCalendarDay(note.userTimestamp)
			if (noteDbEntryDayMap.containsKey(timestamp)) {
				noteDbEntryDayMap[timestamp]!!.add(note)
			} else {
				noteDbEntryDayMap[timestamp] = mutableListOf(note)
			}
		}
		noteDbEntryDayMap.forEach { (timestamp, data) ->
			timestampSizeMap[timestamp] = data.size
		}
	}

	BottomSheetScaffold(
		scaffoldState = bottomSheetScaffoldState,
		sheetContent = {
			AnimatedContent(targetState = selectedTimestamp) {
				BottomSheetContent(
					noteDbEntryDayMap = noteDbEntryDayMap[it],
					selectedTimestamp = selectedTimestamp,
					selectedItemList = selectedItemList,
					onAction = onAction
				)
			}
		},
		modifier = Modifier,
		sheetElevation = 32.dp,
		sheetPeekHeight = screenHeight.times(0.2f),
		sheetBackgroundColor = MaterialTheme.colorScheme.surface
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.background)
		) {
			Calendar(timestampSizeMap = timestampSizeMap) { timestamp ->
				selectedTimestamp = timestamp
			}
		}
	}
}

@Composable
private fun BottomSheetContent(
	noteDbEntryDayMap: List<NoteDbEntry>?,
	selectedTimestamp: Long,
	selectedItemList: List<String>,
	onAction: (MainActivity.Action, Any?) -> Unit
) {
	LazyColumn {
		item { Spacer(modifier = Modifier.height(16.dp)) }
		item {
			NotebookHeaderCard(
				title = TimeUtils.timestampToDate(selectedTimestamp),
				noEntries = if (noteDbEntryDayMap.isNullOrEmpty()) "No entries" else if (noteDbEntryDayMap.size == 1) "1 entry" else "${noteDbEntryDayMap.size} entries",
				color = MaterialTheme.colorScheme.surface
			)
		}

		noteDbEntryDayMap?.sortedBy { it.userTimestamp }?.reversed()?.forEach { note ->
			val lastEntryKey =
				if (noteDbEntryDayMap.isNotEmpty()) noteDbEntryDayMap.last().key else null

			item {
				NoteCard(
					key = note.key,
					timestamp = note.userTimestamp,
					showFullTime = false,
					isLocked = false,
					isSelected = note.key in selectedItemList,
					isArchived = false,
					isFavourite = false,
					isDeleted = note.deletedTimestamp != -1L,
					isLast = note.key == lastEntryKey,
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
