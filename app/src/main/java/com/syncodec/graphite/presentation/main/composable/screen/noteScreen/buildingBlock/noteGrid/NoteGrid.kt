package com.syncodec.graphite.presentation.main.composable.screen.noteScreen.buildingBlock.noteGrid

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.local.NoteObjectLite
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.di.repository.group.RealmObjectGroupList
import com.syncodec.graphite.presentation.base.sortOn
import com.syncodec.graphite.presentation.common.component.LocalComponentColumnCount
import com.syncodec.graphite.presentation.common.component.note.NoteGridCard2
import com.syncodec.graphite.presentation.common.component.note.NoteGroupHeader
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.timeStampToPrettyFull
import com.syncodec.graphite.utils.timeStampToTime
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun NoteGrid(
	modifier: Modifier = Modifier,
	lazyStaggeredGridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
	noteGroupList: RealmObjectGroupList<NoteObjectLite> = RealmObjectGroupList(),
	tagList: List<TagObject> = listOf(),
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onClickNote: (RealmUUID) -> Unit = {},
	onLongClickNote: (RealmUUID) -> Unit = {},
) {
	val sortOn1 by sortOn()

	val columnCount = LocalComponentColumnCount.current

	LazyVerticalStaggeredGrid(
		state = lazyStaggeredGridState,
		columns = StaggeredGridCells.Fixed(columnCount),
		contentPadding = PaddingValues(horizontal = 8.dp),
		modifier = Modifier.fillMaxSize()
	) {

		noteGroupList
			.groupList
			.forEach { (title, noteList) ->
				if (noteList.isNotEmpty()) {
					item(
						span = StaggeredGridItemSpan.FullLine,
						key = title,
						contentType = { 0 }
					) {
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
					items = noteList,
					key = { it.id.toString() },
					contentType = { 1 }
				) { noteObjectLite ->
					Box(
						modifier = Modifier.animateItemPlacement(tween(470))
					) {
						NoteGridCard2(
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
							modifier = Modifier.padding(2.dp)
						)
					}

				}
			}

		item(span = StaggeredGridItemSpan.FullLine) { Spacer(modifier = Modifier.height(96.dp)) }
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun NoteGrid(
	modifier: Modifier = Modifier,
	lazyStaggeredGridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
	noteList: List<NoteObjectLite> = listOf(),
	tagList: List<TagObject> = listOf(),
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onClickNote: (RealmUUID) -> Unit = {},
	onLongClickNote: (RealmUUID) -> Unit = {},
) {
	val sortOn1 by sortOn()

	val columnCount = LocalComponentColumnCount.current

	LazyVerticalStaggeredGrid(
		state = lazyStaggeredGridState,
		columns = StaggeredGridCells.Fixed(columnCount),
		contentPadding = PaddingValues(horizontal = 8.dp),
		modifier = Modifier.fillMaxSize()
	) {

		items(
			items = noteList,
			key = { it.id.toString() },
			contentType = { 1 }
		) { noteObjectLite ->
			Box(
				modifier = Modifier.animateItemPlacement(tween(470))
			) {
				NoteGridCard2(
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
					modifier = Modifier.padding(2.dp)
				)
			}

		}

		item(span = StaggeredGridItemSpan.FullLine) { Spacer(modifier = Modifier.height(96.dp)) }
	}
}
