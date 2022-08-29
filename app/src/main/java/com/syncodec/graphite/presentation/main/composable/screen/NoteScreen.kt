package com.syncodec.graphite.presentation.main.composable.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.AttachmentObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.custom.lazyView.LazyStaggeredVerticalGrid
import com.syncodec.graphite.presentation.custom.lazyView.isScrollingUp
import com.syncodec.graphite.presentation.main.composable.buildingBlock.NoteFloatingActionButton
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NoEntryCard
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookHeaderCard
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.gridView.NoteGridCard
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.listView.NoteListCard
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.listView.NotebookTimelineSpacer
import com.syncodec.graphite.utils.ViewType
import com.syncodec.graphite.utils.timeStampToPrettyDay
import io.realm.kotlin.types.ObjectId


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
	notebookId: ObjectId? = null,
	noteDayMap: Map<Long, List<NoteObjectLite>>,
	viewType: ViewType,
	onClickFab: () -> Unit,
	onClickNote: (ObjectId) -> Unit,
	onLongClickNote: (ObjectId) -> Unit
) {

	val lazyListState = rememberLazyListState()
	val lazyGridState = rememberLazyGridState()

	Scaffold(
		floatingActionButton = {
			NoteFloatingActionButton(isExpanded = lazyListState.isScrollingUp() || lazyGridState.isScrollingUp(), onClick = onClickFab)
		},
		floatingActionButtonPosition = FabPosition.End
	) {
		Box(modifier = Modifier.padding(it)) {
			Crossfade(
				targetState = noteDayMap.isEmpty(),
				animationSpec = tween(600)
			) {
				if (it) {
					NoEntryCard()
				} else {
					Crossfade(targetState = viewType) {
						when (it) {
							ViewType.LIST -> ListView(
								lazyListState = lazyListState,
								noteDayMap = noteDayMap,
								onClickNote = onClickNote,
								onLongClickNote = onLongClickNote
							)
							ViewType.GRID -> GridView(
								lazyGridState = lazyGridState,
								noteDayMap = noteDayMap,
								onClickNote = onClickNote,
								onLongClickNote = onLongClickNote
							)
						}
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListView(
	lazyListState: LazyListState,
	noteDayMap: Map<Long, List<NoteObjectLite>>,
	onClickNote: (ObjectId) -> Unit,
	onLongClickNote: (ObjectId) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize(),
		state = lazyListState
	) {
//		 item {
//	         QuoteCard()
//	 	}
		noteDayMap.toSortedMap(Comparator.reverseOrder()).forEach { (day, noteList) ->
			val sortedList = noteList.sortedBy { -it.userTimestamp }

			val entrySize = sortedList.size

			stickyHeader {
				if (entrySize != 0) {
					NotebookHeaderCard(
						title = day.timeStampToPrettyDay(),
						noEntries = "$entrySize ${if (entrySize == 1) "entry" else "entries"}",
						color = MaterialTheme.colorScheme.background
					)
				}
			}

			val lastEntryKey = if (entrySize != 0) sortedList.last().id else null

			sortedList.forEach { note ->
				item {
					var thumbnail by remember { mutableStateOf<Bitmap?>(null) }

					LaunchedEffect(key1 = note.id) {
						try {
							if (note.thumbnailType == AttachmentObject.Companion.Type.IMAGE.name) {
								thumbnail = note.thumbnail?.let { BitmapFactory.decodeByteArray(note.thumbnail, 0, it.size) }
							}
						} catch (e: Exception) {
							e.printStackTrace()
						}
					}

					NoteListCard(
						id = note.id,
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
						isVisible = true,
						selectedColor = MaterialTheme.colorScheme.surface,
						onClick = { onClickNote(note.id) },
						onLongClick = { onLongClickNote(note.id) },
					)

					NotebookTimelineSpacer(isVisible = note.id != lastEntryKey)
				}
			}
		}

		item { Spacer(modifier = Modifier.height(128.dp)) }
	}
}

@Composable
private fun GridView(
	lazyGridState: LazyGridState,
	noteDayMap: Map<Long, List<NoteObjectLite>>,
	onClickNote: (ObjectId) -> Unit,
	onLongClickNote: (ObjectId) -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.padding(4.dp)
	) {
		LazyStaggeredVerticalGrid(columnCount = 2) {
			noteDayMap.toSortedMap(Comparator.reverseOrder()).forEach { (day, noteList) ->
				val sortedList = noteList.sortedBy { -it.userTimestamp }

				sortedList.forEach { note ->
					item(note.id) {
						NoteGridCard(
							id = note.id,
							timestamp = note.userTimestamp,
							showFullTime = false,
							isLocked = note.isLocked,
							isSelected = false,
							isFavourite = note.isFavourite,
							isDeleted = false,
							isLast = false,
							title = note.title,
							contentThumbnail = note.contentThumbnail,
							attachmentCount = 0,
							attachmentThumbnail = null,
							address = note.address,
							latLng = note.latLng,
							selectedColor = MaterialTheme.colorScheme.surface,
							onClick = { /*TODO*/ }
						)
					}
				}
			}
		}
	}
}
