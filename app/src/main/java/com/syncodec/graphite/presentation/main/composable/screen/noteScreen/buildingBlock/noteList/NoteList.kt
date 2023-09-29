package com.syncodec.graphite.presentation.main.composable.screen.noteScreen.buildingBlock.noteList

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.group.RealmObjectGroupList
import com.syncodec.graphite.presentation.base.sortOn
import com.syncodec.graphite.presentation.common.component.note.NoteGroupHeader
import com.syncodec.graphite.presentation.common.component.note.NoteListCard2
import com.syncodec.graphite.presentation.common.scrollBar.scrollbar
import com.syncodec.graphite.presentation.main.composable.buildingBlock.YearProgressBar
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.timeStampToPrettyFull
import com.syncodec.graphite.utils.timeStampToTime
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun NoteList(
	modifier: Modifier = Modifier,
	lazyListState : LazyListState = rememberLazyListState(),
	noteGroupList: RealmObjectGroupList<NoteObjectLite> = RealmObjectGroupList(),
	tagList: List<TagObject> = listOf(),
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onClickNote: (RealmUUID) -> Unit = {},
	onLongClickNote: (RealmUUID) -> Unit = {},
) {
	val sortOn1 by sortOn()

	LazyColumn(
		state = lazyListState,
		contentPadding = PaddingValues(horizontal = 8.dp),
		modifier = modifier
			.scrollbar(
				state = lazyListState,
				horizontal = false,
				thickness = 8.dp,
				fixedKnobRatio = 0.13f,
				knobColor = MaterialTheme.colorScheme.onBackground,
				trackColor = Color.Transparent,
			)
	) {
		item { YearProgressBar(showCard = !isSelecting) }

		noteGroupList
			.groupList
			.forEach { (title, noteList) ->
				stickyHeader(
					key = title
				) {
					if (noteList.isNotEmpty()) {
						Box(
							modifier = Modifier.animateItemPlacement(tween(470))
						) {
							NoteGroupHeader(
								text = title,
								subText = "${noteList.size} ${if (noteList.size == 1) "entry" else "entries"}",
							)
						}
					}
				}

				items(
					items = noteList.sortedBy {
						when (sortOn1) {
							SortOn.Title -> it.title?.lowercase() ?: "."
							SortOn.Timestamp -> it.userTimestamp.timeStampToPrettyFull()
							SortOn.Modified -> it.modifiedTimestamp.timeStampToPrettyFull()
							else -> it.userTimestamp.timeStampToPrettyFull()
						}
					},
					key = { it.id.toString() },
					contentType = { NoteObjectLite::class }
				) { noteObjectLite ->
					NoteListCard2(
						id = noteObjectLite.id,
						timestamp = when (sortOn1) {
							SortOn.Title -> noteObjectLite.userTimestamp.timeStampToPrettyFull()
							SortOn.Timestamp -> noteObjectLite.userTimestamp.timeStampToTime()
							SortOn.Modified -> noteObjectLite.userTimestamp.timeStampToPrettyFull()
							else -> noteObjectLite.userTimestamp.timeStampToPrettyFull()
						},
						title = noteObjectLite.title,
						contentThumbnail = noteObjectLite.contentThumbnail,
						address = noteObjectLite.address,
						latLng = noteObjectLite.latLng,
						isFavourite = noteObjectLite.isFavourite,
						isLocked = noteObjectLite.isLocked,
						tagList = tagList.filter { noteObjectLite.id in it.objectIdList }.map { it.toLite() },
						selected = noteObjectLite.id in selectedIdList,
						onClick = { onClickNote(noteObjectLite.id) },
						onLongClick = { onLongClickNote(noteObjectLite.id) },
						modifier = Modifier
							.animateItemPlacement(tween(470))
							.padding(2.dp)
					)
				}
			}

		item { Spacer(modifier = Modifier.height(96.dp)) }
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun NoteList(
	modifier: Modifier = Modifier,
	lazyListState : LazyListState = rememberLazyListState(),
	noteList: List<NoteObjectLite> = listOf(),
	tagList: List<TagObject> = listOf(),
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onClickNote: (RealmUUID) -> Unit = {},
	onLongClickNote: (RealmUUID) -> Unit = {},
) {
	val sortOn1 by sortOn()

	LazyColumn(
		state = lazyListState,
		contentPadding = PaddingValues(horizontal = 8.dp),
		modifier = modifier
			.scrollbar(
				state = lazyListState,
				horizontal = false,
				thickness = 8.dp,
				fixedKnobRatio = 0.13f,
				knobColor = MaterialTheme.colorScheme.onBackground,
				trackColor = Color.Transparent,
			)
	) {
		items(
			items = noteList,
			key = { it.id.toString() },
			contentType = { NoteObjectLite::class }
		) { noteObjectLite ->
			NoteListCard2(
				id = noteObjectLite.id,
				timestamp = when (sortOn1) {
					SortOn.Title -> noteObjectLite.userTimestamp.timeStampToPrettyFull()
					SortOn.Timestamp -> noteObjectLite.userTimestamp.timeStampToTime()
					SortOn.Modified -> noteObjectLite.userTimestamp.timeStampToPrettyFull()
					else -> noteObjectLite.userTimestamp.timeStampToPrettyFull()
				},
				title = noteObjectLite.title,
				contentThumbnail = noteObjectLite.contentThumbnail,
				address = noteObjectLite.address,
				latLng = noteObjectLite.latLng,
				isFavourite = noteObjectLite.isFavourite,
				isLocked = noteObjectLite.isLocked,
				tagList = tagList.filter { noteObjectLite.id in it.objectIdList }.map { it.toLite() },
				selected = noteObjectLite.id in selectedIdList,
				onClick = { onClickNote(noteObjectLite.id) },
				onLongClick = { onLongClickNote(noteObjectLite.id) },
				modifier = Modifier
					.animateItemPlacement(tween(470))
					.padding(2.dp)
			)
		}

		item { Spacer(modifier = Modifier.height(96.dp)) }
	}
}
