package com.syncodec.momento.mainComponent.screen

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
import com.syncodec.momento.MainActivity
import com.syncodec.momento.custom.calendarView.Calendar
import com.syncodec.momento.custom.notebook.NoteCard
import com.syncodec.momento.custom.notebook.NoteCardData
import com.syncodec.momento.custom.notebook.NotebookHeaderCard
import com.syncodec.momento.custom.notebook.NotebookTimelineSpacer
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.database.note.locationDataToLatLng
import com.syncodec.momento.mainComponent.miscellaneous.TopBar
import com.syncodec.momento.miscellaneous.TimeUtils


@OptIn(ExperimentalAnimationApi::class)
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun CalendarScreen(
	noteList: List<NoteDbEntry>,
	isSelected: Boolean,
	selectedItemList: List<String>,
	onClick: (MainActivity.Click, Any?) -> Unit
) {
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp
	val scope = rememberCoroutineScope()
	val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

	var selectedTimestamp: Long by remember { mutableStateOf(TimeUtils.getToday()) }
	val noteDbEntryDayMap: SnapshotStateMap<Long, MutableList<NoteDbEntry>> =
		remember { mutableStateMapOf() }
	val timestampSizeMap: SnapshotStateMap<Long, Int> = remember { mutableStateMapOf() }
	LaunchedEffect(key1 = null) {
		noteList.forEach { note ->
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
					selectedTimestamp = selectedTimestamp
				) { click, data -> onClick(click, data) }
			}
		},
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
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.background)
		) {
			Calendar(timestampSizeMap = timestampSizeMap) { timestamp -> selectedTimestamp = timestamp }
		}
	}
}

@Composable
private fun BottomSheetContent(
	noteDbEntryDayMap: List<NoteDbEntry>?,
	selectedTimestamp: Long,
	onClick: (MainActivity.Click, Any?) -> Unit
) {
	LazyColumn {
		item { Spacer(modifier = Modifier.height(16.dp)) }
		item {
			NotebookHeaderCard(
				title = TimeUtils.timestampToDate(selectedTimestamp),
				noEntries = if (noteDbEntryDayMap.isNullOrEmpty()) "No entries" else if (noteDbEntryDayMap.size == 1) "1 entry" else "${noteDbEntryDayMap.size} entries"
			)
		}

		noteDbEntryDayMap?.forEach { noteDbEntry ->
			val lastEntryKey =
				if (noteDbEntryDayMap.isNotEmpty()) noteDbEntryDayMap.last().key else null

			item {
				NoteCardData(
					key = noteDbEntry.key,
					timestamp = noteDbEntry.userTimestamp,
					showFullTime = false,
					isLocked = false,
					isSelected = false,
					isArchived = false,
					isFavourite = false,
					isDeleted = noteDbEntry.deletedTimestamp != -1L,
					isLast = noteDbEntry.key == lastEntryKey,
					title = noteDbEntry.title,
					contentThumbnail = noteDbEntry.contentThumbnail,
					attachmentCount = noteDbEntry.attachmentCount,
					attachmentThumbnail = noteDbEntry.attachmentThumbnail,
					address = noteDbEntry.address,
					latLng = locationDataToLatLng(noteDbEntry.location),
					isVisible = true,
					onClick = { onClick(MainActivity.Click.CLICK_NOTE, noteDbEntry.key) },
					onLongClick = { },
				).apply {
					NoteCard(noteCardData = this)
				}

				NotebookTimelineSpacer(isVisible = noteDbEntry.key != lastEntryKey)
			}
		}

		item { Spacer(modifier = Modifier.height(194.dp)) }
	}
}
