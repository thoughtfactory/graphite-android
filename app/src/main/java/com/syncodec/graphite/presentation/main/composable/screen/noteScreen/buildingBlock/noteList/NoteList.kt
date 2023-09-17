package com.syncodec.graphite.presentation.main.composable.screen.noteScreen.buildingBlock.noteList

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.di.repository.group.RealmObjectGroupList
import com.syncodec.graphite.presentation.common.scrollBar.scrollbar
import com.syncodec.graphite.presentation.main.composable.buildingBlock.YearProgressBar
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookHeaderCard
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookTimelineSpacer
import com.syncodec.graphite.presentation.base.LocalAppDataStore
import com.syncodec.graphite.presentation.base.sortOn
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.timeStampToPrettyFull
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun NoteList(
	noteMap: RealmObjectGroupList<NoteObjectLite> = RealmObjectGroupList(),
	tagList: List<TagObject> = listOf(),
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onClickNote: (RealmUUID) -> Unit = {},
	onLongClickNote: (RealmUUID) -> Unit = {},
) {
	val appDataStore = LocalAppDataStore.current

	val sortOn1 by sortOn()
	val cardHeight by appDataStore.homeCardHeight.collectAsState(initial = null)

	val lazyListState = rememberLazyListState()

	cardHeight?.let { cardHeight1 ->
		LazyColumn(
			state = lazyListState,
			modifier = Modifier
				.fillMaxSize()
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

			noteMap
				.groupList
				.forEach { (title, noteList) ->
					stickyHeader(
						key = title
					) {
						if (noteList.isNotEmpty()) {
							Box(
								modifier = Modifier.animateItemPlacement(tween(470))
							) {
								NotebookHeaderCard(
									title = title,
									noEntries = "${noteList.size} ${if (noteList.size == 1) "entry" else "entries"}",
									color = MaterialTheme.colorScheme.background
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
						Box(
							modifier = Modifier.animateItemPlacement(tween(470))
						) {
							NoteListCard(
								id = noteObjectLite.id,
								timestamp = noteObjectLite.userTimestamp.timeStampToPrettyFull(),
								title = noteObjectLite.title,
								isFavourite = noteObjectLite.isFavourite,
								isLocked = noteObjectLite.isLocked,
								contentThumbnail = noteObjectLite.contentThumbnail,
								address = noteObjectLite.address,
								latLng = noteObjectLite.latLng,
								tagList = tagList.filter { noteObjectLite.id in it.objectIdList }.map { it.toLite() },
								isSelected = noteObjectLite.id in selectedIdList,
								cardHeight = cardHeight1,
								onClick = { onClickNote(noteObjectLite.id) },
								onLongClick = { onLongClickNote(noteObjectLite.id) }
							)
						}
					}

					item { NotebookTimelineSpacer(isVisible = true) }
				}

			item { Spacer(modifier = Modifier.height(96.dp)) }
		}
	}
}
