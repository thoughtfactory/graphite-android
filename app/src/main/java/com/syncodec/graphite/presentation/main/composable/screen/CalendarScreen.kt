package com.syncodec.graphite.presentation.main.composable.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.AttachmentObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.common.calendar.Calendar
import com.syncodec.graphite.presentation.main.composable.LocalCompositionTagList
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookHeaderCard
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.listView.NoteListCard
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.listView.NotebookTimelineSpacer
import com.syncodec.graphite.utils.getToday
import com.syncodec.graphite.utils.timestampToCalendarDay
import com.syncodec.graphite.utils.timestampToDate
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun CalendarScreen(
	noteList: List<NoteObjectLite>,
	selectedItemList: List<String>,
	onClickNote : (RealmUUID) -> Unit,
	onLongClickNote : (RealmUUID) -> Unit,
) {
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp

	val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

	var selectedTimestamp: Long by remember { mutableStateOf(getToday()) }
	val noteDayMap: SnapshotStateMap<Long, MutableList<NoteObjectLite>> = remember { mutableStateMapOf() }
	val timestampSizeMap: SnapshotStateMap<Long, Int> = remember { mutableStateMapOf() }
	LaunchedEffect(key1 = null) {
		noteList.forEach { note ->
			val timestamp = timestampToCalendarDay(note.userTimestamp)
			if (noteDayMap.containsKey(timestamp)) {
				(noteDayMap[timestamp] ?: return@forEach).add(note)
			} else {
				noteDayMap[timestamp] = mutableListOf(note)
			}
		}
		noteDayMap.forEach { (timestamp, data) ->
			timestampSizeMap[timestamp] = data.size
		}
	}

	BottomSheetScaffold(
		scaffoldState = bottomSheetScaffoldState,
		sheetContent = {
			AnimatedContent(targetState = selectedTimestamp) {
				BottomSheetContent(
					noteList = noteList.filter { timestampToCalendarDay(it.userTimestamp) == selectedTimestamp },
					selectedTimestamp = it,
					selectedItemList = listOf(),
					onClickNote = onClickNote,
					onLongClickNote = onLongClickNote
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
	noteList: List<NoteObjectLite>,
	selectedTimestamp: Long,
	selectedItemList: List<String>,
	onClickNote: (RealmUUID) -> Unit,
	onLongClickNote: (RealmUUID) -> Unit,
) {
	val lastEntryKey = if (noteList.isNotEmpty()) noteList.last().id else null

	val tagList = LocalCompositionTagList.current

	LazyColumn {
		item { Spacer(modifier = Modifier.height(16.dp)) }
		item {
			NotebookHeaderCard(
				title = timestampToDate(selectedTimestamp),
				noEntries = if (noteList.isEmpty()) "No entries" else if (noteList.size == 1) "1 entry" else "${noteList.size} entries",
				color = MaterialTheme.colorScheme.surface,
			)
		}
		noteList.sortedBy { it.userTimestamp }.reversed().forEachIndexed { index, note ->
			item {
				var thumbnail by remember { mutableStateOf<Bitmap?>(null) }

				LaunchedEffect(key1 = note.id.hashCode() + note.thumbnail.hashCode()) {
					try {
						if (note.thumbnailType == AttachmentObject.Companion.Type.IMAGE.name) {
							thumbnail = note.thumbnail?.let { BitmapFactory.decodeByteArray(note.thumbnail, 0, it.size) }
						}
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
					isSelected = false,
					isFavourite = note.isFavourite,
					isDeleted = false,
					isLast = note.id == lastEntryKey,
					title = note.title,
					contentThumbnail = note.contentThumbnail,
					attachmentCount = note.attachmentCount,
					attachmentThumbnail = thumbnail,
					address = note.address,
					latLng = note.latLng,
					tagList = tagList.filter { it.RealmUUIDList.contains(note.id) },
					isVisible = true,
					isSwipable = false,
					selectedColor = MaterialTheme.colorScheme.surface,
					containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.71f),
					onClick = { onClickNote(note.id) },
					onLongClick = { onLongClickNote(note.id) },
				)

				NotebookTimelineSpacer(isVisible = note.id != lastEntryKey)

			}
		}
		item { Spacer(modifier = Modifier.height(194.dp)) }
	}
}
