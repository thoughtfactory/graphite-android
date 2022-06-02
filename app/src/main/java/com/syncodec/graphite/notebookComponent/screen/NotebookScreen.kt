package com.syncodec.graphite.notebookComponent.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.notebook.ChapterCard
import com.syncodec.graphite.custom.notebook.NoteCard
import com.syncodec.graphite.custom.notebook.NotebookHeaderCard
import com.syncodec.graphite.custom.notebook.NotebookTimelineSpacer
import com.syncodec.graphite.database.chapter.ChapterDbEntry
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.notebookComponent.NotebookActivity

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun NotebookScreen(
	noteList: List<NoteDbEntry>,
	chapterList: List<ChapterDbEntry>,
	selectedItemList: SnapshotStateList<String>,
	showNotes: Boolean,
	showChapters: Boolean,
	onAction: (NotebookActivity.Action, Any?) -> Unit
) {
	Crossfade(targetState = noteList.isEmpty() && chapterList.isEmpty()) {
		if (it) {
			NoNotebookCard()
		} else {
			LazyColumn(modifier = Modifier.fillMaxSize()) {
				stickyHeader {
					NotebookHeaderCard(
						title = "Notes",
						noEntries = if (noteList.isEmpty()) "No notes" else if (noteList.size == 1) "1 note" else "${noteList.size} notes",
						color = MaterialTheme.colorScheme.background
					) { onAction(NotebookActivity.Action.CLICK_NOTE_HEADER, null) }
				}

				if (showNotes) {
					noteList.sortedBy { it.userTimestamp }.reversed().forEach { note ->
						item {
							NoteCard(
								key = note.key,
								timestamp = note.userTimestamp,
								showFullTime = true,
								isLocked = note.isLocked,
								isSelected = note.key in selectedItemList,
								isArchived = note.isArchived,
								isFavourite = note.isFavourite,
								isDeleted = note.deletedTimestamp != -1L,
								isLast = note.key == noteList.last().key,
								title = note.title,
								contentThumbnail = note.contentThumbnail,
								attachmentCount = note.attachmentKeyList.size,
								attachmentThumbnail = note.attachmentThumbnail,
								address = note.address,
								latLng = note.latLng,
								isVisible = true,
								selectedColor = MaterialTheme.colorScheme.surface,
								onClick = {
									onAction(
										NotebookActivity.Action.CLICK_NOTE,
										note.key
									)
								},
								onLongClick = {
									onAction(NotebookActivity.Action.LONG_CLICK_NOTE, note.key)
								},
							)

							NotebookTimelineSpacer(isVisible = note.key != noteList.last().key)
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
	}
}

@Composable
private fun NoNotebookCard() {
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Spacer(modifier = Modifier.weight(1f))
		Image(
			painter = painterResource(id = R.drawable.il_notebook),
			contentDescription = "No entries found",
			contentScale = ContentScale.Fit,
			modifier = Modifier.fillMaxWidth(0.64f),
		)

		Spacer(modifier = Modifier.height(24.dp))

		Text(
			text = "Keep a diary, and someday it'll keep you",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.fillMaxWidth(0.71f)
		)

		Spacer(modifier = Modifier.height(16.dp))

		Text(
			text = "~ Mae West",
			style = MaterialTheme.typography.bodySmall,
			fontStyle = FontStyle.Italic,
			textAlign = TextAlign.End,
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.fillMaxWidth(0.71f)
		)
		Spacer(modifier = Modifier.weight(1f))
	}
}
