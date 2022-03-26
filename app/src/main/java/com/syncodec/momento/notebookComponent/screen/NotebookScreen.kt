package com.syncodec.momento.notebookComponent.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.momento.custom.notebook.*
import com.syncodec.momento.database.chapter.ChapterDbEntry
import com.syncodec.momento.database.note.NoteDbEntry
import com.syncodec.momento.miscellaneous.filterData
import com.syncodec.momento.notebookComponent.NotebookActivity

@OptIn(ExperimentalFoundationApi::class)
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
	onClick: (NotebookActivity.Click, Any?) -> Unit
) {
	LazyColumn(
		modifier = Modifier
			.fillMaxSize(),
	) {
		item { Spacer(modifier = Modifier.height(12.dp)) }
		stickyHeader {
			NotebookHeaderCard(
				title = "Notes",
				noEntries = if (noteList.isEmpty()) "No notes" else if (noteList.size == 1) "1 note" else "${noteList.size} notes"
			) { onClick(NotebookActivity.Click.NOTE_HEADER, null) }
		}
		itemsIndexed(noteList) { index, note ->
			val showEntry: Boolean = filterData(
				showArchived = showArchived,
				isArchived = false,
				showFavourite = showFavourite,
				isFavourite = false,
				showLocked = showLocked,
				isLocked = false
			) and showNotes

			NoteCardData(
				timestamp = note.userTimestamp,
				showFullTime = true,
				isLocked = false,
				isSelected = note.key in selectedItemList,
				isArchived = false,
				isFavourite = false,
				isDeleted = note.deletedTimestamp != -1L,
				isLast = index == noteList.size - 1,
				title = note.title,
				contentThumbnail = note.contentThumbnail,
				attachmentCount = note.attachmentCount,
				attachmentThumbnail = note.attachmentThumbnail,
				address = note.address,
				isVisible = showEntry,
				onClick = { onClick(NotebookActivity.Click.CLICK_NOTE, note.key) },
				onLongClick = { onClick(NotebookActivity.Click.LONG_CLICK_NOTE, note.key) },
			).apply { NoteCard(noteCardData = this) }

			NotebookTimelineSpacer(isVisible = (index != noteList.size - 1) and showEntry)
		}

		stickyHeader {
			NotebookHeaderCard(
				title = "Chapters",
				noEntries = if (chapterList.isEmpty()) "No chapters" else if (chapterList.size == 1) "1 chapter" else "${chapterList.size} chapters"
			) { onClick(NotebookActivity.Click.CHAPTER_HEADER, null) }
		}
		itemsIndexed(chapterList) { index, chapter ->
			val showEntry: Boolean = filterData(
				showArchived = showArchived,
				isArchived = chapter.isArchived,
				showFavourite = showFavourite,
				isFavourite = chapter.isFavourite,
				showLocked = showLocked,
				isLocked = chapter.isLocked
			) and showChapters

			ChapterCardData(
				timestamp = chapter.createdTimestamp,
				isLocked = chapter.isLocked,
				isSelected = chapter.key in selectedItemList,
				isArchived = chapter.isArchived,
				isFavourite =chapter.isFavourite,
				isDeleted =chapter.deletedTimestamp != -1L,
				isLast = index == chapterList.size-1,
				title =chapter.title,
				description =chapter.description,
				noteCount = 0,
				chapterCount = 0,
				isVisible = showEntry,
				onClick = { onClick(NotebookActivity.Click.CLICK_CHAPTER, chapter.key) },
				onLongClick = { onClick(NotebookActivity.Click.LONG_CLICK_CHAPTER, chapter.key) },
			).apply { ChapterCard(chapterCardData = this) }

			NotebookTimelineSpacer(isVisible = (index != chapterList.size - 1) and showEntry)
		}

		item { Spacer(modifier = Modifier.height(194.dp)) }
	}
}
