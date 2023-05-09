package com.syncodec.graphite.presentation.main.composable.screen.noteScreen.buildingBlock.noteGrid

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
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
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookHeaderCard
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.timeStampToPrettyFull
import com.syncodec.graphite.utils.timeStampToTime
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun NoteGrid(
	noteMap : Map<String, List<NoteObjectLite>> = mapOf(),
	isSelecting : Boolean = false,
	selectedIdList : List<RealmUUID> = listOf(),
	tagList : List<TagObject> = listOf(),
	onClickNote : (RealmUUID) -> Unit = {},
	onLongClickNote : (RealmUUID) -> Unit = {},
) {
	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }
	val sortOn by dataStoreInstance.getSortOn.collectAsState(null)

	LazyVerticalStaggeredGrid(
		state = rememberLazyStaggeredGridState(),
		columns = StaggeredGridCells.Fixed(2),
		contentPadding = PaddingValues(8.dp, 0.dp),
		modifier = Modifier.fillMaxSize()
	) {
		StaggeredGridItemSpan.FullLine
		noteMap.forEach { (header, noteList) ->

			val noteListSize = noteList.size
			if (noteListSize > 0) {
				item(
					span = StaggeredGridItemSpan.FullLine,
				) {
					NotebookHeaderCard(
						title = header,
						noEntries = "$noteListSize ${if (noteListSize == 1) "entry" else "entries"}",
						color = MaterialTheme.colorScheme.background,
						showTimelineLine = false
					)
				}
			}

			noteList.forEach { note ->
				item(
					key = note.id.toString(),
					contentType = note
				) {
					val timestamp = when (sortOn) {
						SortOn.Title -> note.userTimestamp.timeStampToPrettyFull()
						SortOn.Timestamp -> note.userTimestamp.timeStampToTime()
						SortOn.Modified -> note.modifiedTimestamp.timeStampToTime()
						else -> note.userTimestamp.timeStampToPrettyFull()
					}

					NoteGridCard(
						id = note.id,
						timestamp = timestamp,
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

		item(span = StaggeredGridItemSpan.FullLine) { Spacer(modifier = Modifier.height(96.dp)) }
	}
}
