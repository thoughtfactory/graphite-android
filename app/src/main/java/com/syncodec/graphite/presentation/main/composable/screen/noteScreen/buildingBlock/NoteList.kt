package com.syncodec.graphite.presentation.main.composable.screen.noteScreen.buildingBlock

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.main.composable.buildingBlock.WhatsNewCard
import com.syncodec.graphite.presentation.main.composable.buildingBlock.YearProgressBar
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookHeaderCard
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.listView.NotebookTimelineSpacer
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.timeStampToPrettyDay
import com.syncodec.graphite.utils.timeStampToPrettyFull
import com.syncodec.graphite.utils.timestampToCalendarDay
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun NoteList(
	noteList : List<NoteObjectLite> = listOf(),
	isRefreshing : Boolean = false,
	isSelecting : Boolean = false,
	selectedIdList : List<RealmUUID> = listOf(),
	tagList : List<TagObject> = listOf(),
	onClickNote : (RealmUUID) -> Unit = {},
	onLongClickNote : (RealmUUID) -> Unit = {},
	onRefresh : () -> Unit = {},
) {
	val context = LocalContext.current

	val dataStoreInstance = remember { DataStoreInstance(context = context) }
	val sortBy by dataStoreInstance.getSortBy.collectAsState(null)
	val sortOn by dataStoreInstance.getSortOn.collectAsState(null)

	var noteMap : Map<String, List<NoteObjectLite>> by remember { mutableStateOf(mapOf()) }
	LaunchedEffect(keys = arrayOf(noteList, sortBy, sortOn)) {
		when (sortOn) {
			SortOn.Title -> (if (sortBy == SortBy.Ascending) noteList.sortedBy { it.title } else noteList.sortedByDescending { it.title }).let {
				noteMap = it.groupBy { it.title?.firstOrNull()?.lowercase() ?: "." }
			}

			SortOn.Timestamp -> (if (sortBy == SortBy.Ascending) noteList.sortedBy { it.userTimestamp } else noteList.sortedByDescending { it.userTimestamp }).let {
				noteMap = it.groupBy { timestampToCalendarDay(it.userTimestamp).timeStampToPrettyDay() }
			}

			SortOn.Modified -> (if (sortBy == SortBy.Ascending) noteList.sortedBy { it.modifiedTimestamp } else noteList.sortedByDescending { it.modifiedTimestamp }).let {
				noteMap = it.groupBy { timestampToCalendarDay(it.modifiedTimestamp).timeStampToPrettyDay() }
			}

			else -> (if (sortBy == SortBy.Ascending) noteList.sortedBy { it.userTimestamp } else noteList.sortedByDescending { it.userTimestamp }).let {
				noteMap = it.groupBy { timestampToCalendarDay(it.userTimestamp).timeStampToPrettyDay() }
			}

		}
	}

	SwipeRefresh(
		state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
		onRefresh = onRefresh
	) {
		LazyColumn(
			state = rememberLazyListState(),
			modifier = Modifier.fillMaxSize()
		) {
			item {
				YearProgressBar(showCard = ! isSelecting)
			}

			item(
				key = "whats_new_card",
			) {
				WhatsNewCard(showCard = ! isSelecting)
			}

			noteMap.forEach { (header, noteList) ->
				val sortedList = noteList.sortedBy { - it.userTimestamp }

				val entrySize = sortedList.size

				stickyHeader(
					key = header,
					contentType = header
				) {
					if (entrySize != 0) {
						NotebookHeaderCard(
							title = header,
							noEntries = "$entrySize ${if (entrySize == 1) "entry" else "entries"}",
							color = MaterialTheme.colorScheme.background
						)
					}
				}

				val lastEntryKey = if (entrySize != 0) sortedList.last().id else null

				sortedList.forEach { note ->
					item(
						key = note.id.toString(),
						contentType = note
					) {
						Box(
							modifier = Modifier.animateItemPlacement(tween(300))
						) {
							NoteListCard(
								id = note.id,
								timestamp = note.userTimestamp.timeStampToPrettyFull(),
								title = note.title,
								isFavourite = note.isFavourite,
								isLocked = note.isLocked,
								contentThumbnail = note.contentThumbnail,
								thumbnail = note.thumbnail,
								address = note.address,
								latLng = note.latLng,
								tagList = tagList.filter { note.id in it.objectIdList },
								isSelected = note.id in selectedIdList,
								onClick = { onClickNote(note.id) },
								onLongClick = { onLongClickNote(note.id) }
							)
						}
					}
				}

				item { NotebookTimelineSpacer(isVisible = true) }
			}

			item { Spacer(modifier = Modifier.height(96.dp)) }
		}
	}
}
