package com.syncodec.graphite.presentation.notebook.composable.screen

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.custom.LoadingView
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.notebook.NotebookActivity
import com.syncodec.graphite.presentation.notebook.composable.buildingBlock.NoteListCard
import com.syncodec.graphite.presentation.notebook.NotebookViewModel
import com.syncodec.graphite.presentation.notebook.composable.buildingBlock.ChapterListCard
import com.syncodec.graphite.utils.Extra


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExplorerScreen() {
	val activity: NotebookActivity = LocalContext.current as NotebookActivity
	val viewModel: NotebookViewModel = viewModel()

	val chapterObject by viewModel.chapterObject
	val tagList = viewModel.tagObjectList

	var isNoteListVisible by remember { mutableStateOf(true) }
	var isChapterListVisible by remember { mutableStateOf(true) }

	AnimatedContent(
		targetState = chapterObject,
		transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(300)) }
	) { _chapterObject ->
		if (_chapterObject == null) {
			LoadingView()
		} else {
			LazyColumn(
				modifier = Modifier.fillMaxSize()
			) {
				noteList(
					noteList = _chapterObject.noteList.map { it.toLite() },
					tagList = tagList,
					isVisible = isNoteListVisible,
					toggleVisibility = { isNoteListVisible = !isNoteListVisible },
					onClick = {
						Intent(activity, NoteActivity::class.java).apply {
							putExtra(Extra.Companion.Constant.IS_NEW.name, false)
							putExtra(Extra.Companion.Constant.CHAPTER_ID.name, chapterObject?.id.toString())
							putExtra(Extra.Companion.Constant.NOTE_ID.name, it.id.toString())
							putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.READ_CHAPTER.ordinal)

							activity.startActivity(this)
						}
					},
					onLongClick = { }
				)
				chapterList(
					chapterList = _chapterObject.chapterList,
					tagList = tagList,
					isVisible = isChapterListVisible,
					toggleVisibility = { isChapterListVisible = !isChapterListVisible },
					onClick = { viewModel.loadChapter(chapterId = it.id) },
					onLongClick = { }
				)
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.noteList(
	noteList: List<NoteObjectLite>,
	tagList: SnapshotStateList<TagObject>,
	isVisible: Boolean,
	toggleVisibility: () -> Unit,
	onClick: (NoteObjectLite) -> Unit,
	onLongClick: (NoteObjectLite) -> Unit
) {
	stickyHeader {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.background)
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(8.dp)
					.background(MaterialTheme.colorScheme.background)
					.clip(RoundedCornerShape(12.dp))
					.clickable { toggleVisibility() }
			) {
				Row(
					verticalAlignment = Alignment.Bottom,
					modifier = Modifier
						.fillMaxWidth()
						.padding(8.dp)
				) {
					Text(
						text = "Notes",
						style = MaterialTheme.typography.titleLarge,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold
					)

					Spacer(modifier = Modifier.weight(1f))

					Text(
						text = "${noteList.size} notes",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold
					)
				}
			}
		}
	}

	noteList.forEach { noteObject ->
		item {
			NoteListCard(
				id = noteObject.id,
				timestamp = noteObject.userTimestamp,
				showFullTime = true,
				isLocked = noteObject.isLocked,
				isSelected = false,
				isFavourite = noteObject.isFavourite,
				isDeleted = false,
				isLast = false,
				title = noteObject.title,
				contentThumbnail = noteObject.contentThumbnail,
				attachmentCount = noteObject.attachmentCount,
				attachmentThumbnail = null,
				address = noteObject.address,
				latLng = noteObject.latLng,
				tagList = tagList.filter { it.objectIdList.contains(noteObject.id) }.map { it.toLite() },
				isVisible = isVisible,
				selectedColor = MaterialTheme.colorScheme.surface,
				onClick = { onClick(noteObject) },
				onLongClick = { onLongClick(noteObject) },
			)
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.chapterList(
	chapterList: List<ChapterObject>,
	tagList: List<TagObject>,
	isVisible: Boolean,
	toggleVisibility: () -> Unit,
	onClick: (ChapterObject) -> Unit,
	onLongClick: (ChapterObject) -> Unit
) {
	stickyHeader {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.background)
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(8.dp)
					.clip(RoundedCornerShape(12.dp))
					.clickable { toggleVisibility() }
			) {
				Row(
					verticalAlignment = Alignment.Bottom,
					modifier = Modifier
						.fillMaxWidth()
						.padding(8.dp)
				) {
					Text(
						text = "Chapters",
						style = MaterialTheme.typography.titleLarge,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold
					)

					Spacer(modifier = Modifier.weight(1f))

					Text(
						text = "${chapterList.size} chapters",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold
					)
				}
			}
		}
	}

	chapterList.forEach { chapterObject ->
		item {
			ChapterListCard(
				id = chapterObject.id,
				timestamp = chapterObject.createdTimestamp,
				isSelected = false,
				isLocked = chapterObject.isLocked,
				isFavourite = chapterObject.isFavourite,
				isDeleted = false,
				isLast = false,
				title = chapterObject.title,
				description = chapterObject.description,
				color = Color(chapterObject.color),
				noteCount = chapterObject.noteList.size,
				chapterCount = chapterObject.chapterList.size,
				tagList = tagList.filter { it.objectIdList.contains(chapterObject.id) }.map { it.toLite() },
				isVisible = isVisible,
				selectedColor = MaterialTheme.colorScheme.surface,
				onClick = { onClick(chapterObject) },
				onLongClick = { onLongClick(chapterObject) }
			)
		}
	}
}
