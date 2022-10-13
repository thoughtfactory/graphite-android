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
import com.syncodec.graphite.presentation.common.lazyView.isScrollingUp
import com.syncodec.graphite.presentation.main.composable.buildingBlock.NoteFloatingActionButton
import com.syncodec.graphite.presentation.main.composable.buildingBlock.YearProgressBar
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
	notebookId : ObjectId? = null,
	noteDayMap : Map<Long, List<NoteObjectLite>>,
	viewType : ViewType,
	onClickFab : () -> Unit,
	onClickNote : (ObjectId) -> Unit,
	onLongClickNote : (ObjectId) -> Unit
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
	lazyListState : LazyListState,
	noteDayMap : Map<Long, List<NoteObjectLite>>,
	onClickNote : (ObjectId) -> Unit,
	onLongClickNote : (ObjectId) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize(),
		state = lazyListState
	) {
//		 item {
//	         QuoteCard()
//	 	}
		item {
			YearProgressBar(showCard = true)
		}
		noteDayMap.toSortedMap(Comparator.reverseOrder()).forEach { (day, noteList) ->
			val sortedList = noteList.sortedBy { - it.userTimestamp }

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
						containerColor = MaterialTheme.colorScheme.background,
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
	lazyGridState : LazyGridState,
	noteDayMap : Map<Long, List<NoteObjectLite>>,
	onClickNote : (ObjectId) -> Unit,
	onLongClickNote : (ObjectId) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		noteDayMap.forEach { (day, noteList) ->
			item {
				NotebookHeaderCard(
					title = day.timeStampToPrettyDay(),
					noEntries = noteList.size.toString(),
					color = MaterialTheme.colorScheme.background
				)
			}

			for (i in 0 until (noteList.size / 2) + 1) {
				item {
					Row(
						modifier = Modifier.fillMaxWidth()
					) {
						val note1 = noteList.getOrNull(i * 2)
						val note2 = noteList.getOrNull(i * 2 + 1)

						Box(
							modifier = Modifier.weight(1f)
						) {
							if (note1 != null) {
								NoteGridCard(
									id = note1.id,
									timestamp = note1.userTimestamp,
									showFullTime = false,
									isLocked = note1.isLocked,
									isSelected = false,
									isFavourite = note1.isFavourite,
									isDeleted = false,
									isLast = false,
									title = note1.title,
									contentThumbnail = note1.contentThumbnail,
									attachmentCount = note1.attachmentCount,
									attachmentThumbnail = null,
									address = note1.address,
									latLng = note1.latLng,
									isVisible = true,
									selectedColor = MaterialTheme.colorScheme.surface,
									onClick = { onClickNote(note1.id) },
									onLongClick = { onLongClickNote(note1.id) },
								)
							}
						}
						Box(
							modifier = Modifier.weight(1f)
						) {
							if (note2 != null) {
								NoteGridCard(
									id = note2.id,
									timestamp = note2.userTimestamp,
									showFullTime = false,
									isLocked = note2.isLocked,
									isSelected = false,
									isFavourite = note2.isFavourite,
									isDeleted = false,
									isLast = false,
									title = note2.title,
									contentThumbnail = note2.contentThumbnail,
									attachmentCount = note2.attachmentCount,
									attachmentThumbnail = null,
									address = note2.address,
									latLng = note2.latLng,
									isVisible = true,
									selectedColor = MaterialTheme.colorScheme.surface,
									onClick = { onClickNote(note2.id) },
									onLongClick = { onLongClickNote(note2.id) },
								)
							}
						}
					}
				}
			}
		}
	}
}
