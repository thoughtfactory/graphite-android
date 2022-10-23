package com.syncodec.graphite.presentation.notebook.composable.screen

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.note.NoteActivity
import com.syncodec.graphite.presentation.notebook.NotebookActivity
import com.syncodec.graphite.presentation.notebook.composable.buildingBlock.ChapterListCard
import com.syncodec.graphite.presentation.notebook.composable.buildingBlock.NoteListCard
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.decodeBase64ToBitmap


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExplorerScreen(
	chapterObject : ChapterObject?,
	tagList : List<TagObject>
) {
	val activity : NotebookActivity = LocalContext.current as NotebookActivity

	var isNoteListVisible by remember { mutableStateOf(true) }
	var isChapterListVisible by remember { mutableStateOf(true) }

	AnimatedContent(
		targetState = chapterObject,
		transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(300)) }
	) { _chapterObject ->
		if (_chapterObject == null) {
			LoadingView()
		} else {
			if (_chapterObject.noteList.isEmpty() && _chapterObject.chapterList.isEmpty()) {
				EmptyView()
			} else {
				LazyColumn(
					modifier = Modifier.fillMaxSize()
				) {
					noteList(
						noteList = _chapterObject.noteList.map { it.toLite() },
						tagList = tagList,
						isVisible = isNoteListVisible,
						toggleVisibility = { isNoteListVisible = ! isNoteListVisible },
						onClick = {
							Intent(activity, NoteActivity::class.java).apply {
								putExtra(Extra.Companion.Constant.IS_NEW.name, false)
								putExtra(Extra.Companion.Constant.CHAPTER_ID.name, chapterObject?.id.toString())
								putExtra(Extra.Companion.Constant.NOTE_ID.name, it.id.toString())
								putExtra(Extra.Companion.Constant.FILTER.name, Extra.Companion.Filter.READ_CHAPTER.name)

								activity.startActivity(this)
							}
						},
						onLongClick = { }
					)
					chapterList(
						chapterList = _chapterObject.chapterList,
						tagList = tagList,
						isVisible = isChapterListVisible,
						toggleVisibility = { isChapterListVisible = ! isChapterListVisible },
						onClick = {
//							viewModel.loadChapter(chapterId = it.id)
							TODO()
						},
						onLongClick = { }
					)
				}
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.noteList(
	noteList : List<NoteObjectLite>,
	tagList : List<TagObject>,
	isVisible : Boolean,
	toggleVisibility : () -> Unit,
	onClick : (NoteObjectLite) -> Unit,
	onLongClick : (NoteObjectLite) -> Unit
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
	chapterList : List<ChapterObject>,
	tagList : List<TagObject>,
	isVisible : Boolean,
	toggleVisibility : () -> Unit,
	onClick : (ChapterObject) -> Unit,
	onLongClick : (ChapterObject) -> Unit
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
				color = chapterObject.color?.let { Color(it) },
				thumbnail = chapterObject.thumbnail?.decodeBase64ToBitmap(),
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

@Composable
private fun EmptyView() {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		Image(
			painter = painterResource(id = R.drawable.il_not_found),
			contentDescription = "No entries found",
			modifier = Modifier.size(screenWidth * 3 / 4)
		)
		Spacer(modifier = Modifier.height(16.dp))
		Text(
			text = "No entries found",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			fontWeight = FontWeight.Bold
		)
	}
}
