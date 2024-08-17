package com.syncodec.graphite.presentation.main.composable.screen.noteScreen.buildingBlock.noteList

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.main.composable.buildingBlock.YearProgressBar
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookHeaderCard
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookTimelineSpacer
import com.syncodec.graphite.utils.DataStoreInstance
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.timeStampToPrettyFull
import com.syncodec.graphite.utils.timeStampToTime
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun NoteList(
	noteMap : Map<String, List<NoteObjectLite>> = mapOf(),
	isRefreshing : Boolean = false,
	isSelecting : Boolean = false,
	selectedIdList : List<RealmUUID> = listOf(),
	tagList : List<TagObject> = listOf(),
	onClickNote : (RealmUUID) -> Unit = {},
	onLongClickNote : (RealmUUID) -> Unit = {},
) {
	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }
	val sortOn by dataStoreInstance.getSortOn.collectAsState(null)

	LazyColumn(
		state = rememberLazyListState(),
		modifier = Modifier.fillMaxSize()
	) {
		item {
			YearProgressBar(showCard = ! isSelecting)
		}

//		item(
//			key = "whats_new_card",
//		) {
//			WhatsNewCard(showCard = ! isSelecting)
//		}

		noteMap.forEach { (header, noteList) ->
			val sortedList = noteList.sortedBy { - it.userTimestamp }

			val entrySize = sortedList.size

			stickyHeader(
				key = header,
				contentType = header
			) {
				if (entrySize > 0) {
					NotebookHeaderCard(
						title = header,
						noEntries = "$entrySize ${if (entrySize == 1) "entry" else "entries"}",
						color = MaterialTheme.colorScheme.background
					)
				}
			}
			sortedList.forEach { note ->
				item(
					key = note.id.toString(),
					contentType = note
				) {
					val createdTimestamp = when (sortOn) {
						SortOn.Title -> note.userTimestamp.timeStampToPrettyFull()
						SortOn.CreatedTimestamp -> note.userTimestamp.timeStampToTime()
						SortOn.ModifiedTimestamp -> note.modifiedTimestamp.timeStampToTime()
						else -> note.userTimestamp.timeStampToPrettyFull()
					}

					Box(
						modifier = Modifier.animateItemPlacement(tween(300))
					) {
						NoteListCard(
							id = note.id,
							timestamp = createdTimestamp,
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
