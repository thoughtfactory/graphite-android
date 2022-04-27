package com.syncodec.momento.notebookComponent.screen

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.momento.custom.notebook.ChapterCard
import com.syncodec.momento.custom.notebook.NoteCard
import com.syncodec.momento.custom.notebook.NotebookHeaderCard
import com.syncodec.momento.custom.notebook.NotebookTimelineSpacer
import com.syncodec.momento.database.chapter.ChapterDbEntry
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.notebookComponent.NotebookActivity

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun NotebookScreen(
	noteList: List<NoteDbEntry>,
	chapterList: List<ChapterDbEntry>,
	selectedItemList: SnapshotStateList<String>,
	showNotes: Boolean,
	showChapters: Boolean,
	showFavourite: Boolean,
	showArchived: Boolean,
	showLocked: Boolean,
	onAction: (NotebookActivity.Action, Any?) -> Unit
) {
	LazyColumn(modifier = Modifier.fillMaxSize()) {
		stickyHeader {
			NotebookHeaderCard(
				title = "Notes",
				noEntries = if (noteList.isEmpty()) "No notes" else if (noteList.size == 1) "1 note" else "${noteList.size} notes",
				color = MaterialTheme.colorScheme.background
			) { onAction(NotebookActivity.Action.CLICK_NOTE_HEADER, null) }
		}

		if (showNotes) {
			noteList.sortedBy { it.userTimestamp }.reversed().forEach { noteDbEntry ->
				item {
					NoteCard(
						key = noteDbEntry.key,
						timestamp = noteDbEntry.userTimestamp,
						showFullTime = true,
						isLocked = false,
						isSelected = noteDbEntry.key in selectedItemList,
						isArchived = false,
						isFavourite = false,
						isDeleted = noteDbEntry.deletedTimestamp != -1L,
						isLast = noteDbEntry.key == noteList.last().key,
						title = noteDbEntry.title,
						contentThumbnail = noteDbEntry.contentThumbnail,
						attachmentCount = noteDbEntry.attachmentKeyList.size,
						attachmentThumbnail = noteDbEntry.attachmentThumbnail,
						address = noteDbEntry.address,
						latLng = noteDbEntry.latLng,
						isVisible = true,
						selectedColor = MaterialTheme.colorScheme.surface,
						onClick = { onAction(NotebookActivity.Action.CLICK_NOTE, noteDbEntry.key) },
						onLongClick = {
							onAction(NotebookActivity.Action.LONG_CLICK_NOTE, noteDbEntry.key)
						},
					)

					NotebookTimelineSpacer(isVisible = noteDbEntry.key != noteList.last().key)
				}
			}
		}

		item { Spacer(modifier = Modifier.height(12.dp)) }
		stickyHeader {
			NotebookHeaderCard(
				title = "Chapters",
				noEntries = if (chapterList.isEmpty()) "No chapters" else if (chapterList.size == 1) "1 chapter" else "${chapterList.size} chapters",
				color = MaterialTheme.colorScheme.background
			) { onAction(NotebookActivity.Action.CLICK_CHAPTER_HEADER, null) }
		}

		if (showChapters) {
			chapterList.forEach { chapterDbEntry ->
				item {
					ChapterCard(
						timestamp = chapterDbEntry.createdTimestamp,
						isLocked = chapterDbEntry.isLocked,
						isSelected = chapterDbEntry.key in selectedItemList,
						isArchived = chapterDbEntry.isArchived,
						isFavourite = chapterDbEntry.isFavourite,
						isDeleted = false,
						isLast = chapterDbEntry.key == chapterList.lastOrNull()?.key,
						title = chapterDbEntry.title,
						description = chapterDbEntry.description,
						noteCount = 0,
						chapterCount = 0,
						isVisible = true,
						selectedColor = MaterialTheme.colorScheme.surface,
						onClick = {
							onAction(NotebookActivity.Action.CLICK_CHAPTER, chapterDbEntry)
						},
						onLongClick = {
							onAction(NotebookActivity.Action.LONG_CLICK_CHAPTER, chapterDbEntry.key)
						},
					)

					NotebookTimelineSpacer(isVisible = chapterDbEntry.key != chapterList.last().key)
				}
			}

			item { Spacer(modifier = Modifier.height(194.dp)) }
		}
	}
}
