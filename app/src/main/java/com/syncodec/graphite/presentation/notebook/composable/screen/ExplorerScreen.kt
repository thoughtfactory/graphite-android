package com.syncodec.graphite.presentation.notebook.composable.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock.LoadingView
import com.syncodec.graphite.presentation.notebook.composable.buildingBlock.NoteListCard
import com.syncodec.graphite.presentation.notebook.NotebookViewModel
import com.syncodec.graphite.presentation.notebook.composable.buildingBlock.ChapterListCard


@Composable
fun ExplorerScreen() {
	val viewModel: NotebookViewModel = viewModel()

	val chapter by viewModel.currentChapterObject

	Crossfade(targetState = chapter) { chapterObject ->
		if (chapterObject == null) {
			LoadingView()
		} else {
			LazyColumn(
				modifier = Modifier.fillMaxSize()
			) {
				noteList(noteList = chapterObject.noteList.map { it.toLite() })
				chapterList(chapterList = chapterObject.chapterList)
			}
		}
	}
}

private fun LazyListScope.noteList(
	noteList: List<NoteObjectLite>,
) {
	noteList.forEach { noteObject ->
		item { Spacer(modifier = Modifier.height(4.dp)) }
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
				attachmentCount = 0,
				attachmentThumbnail = null,
				address = noteObject.address,
				latLng = noteObject.latLng,
				isVisible = true,
				selectedColor = MaterialTheme.colorScheme.surface,
				onClick = {},
				onLongClick = {},
			)
		}
	}
}

private fun LazyListScope.chapterList(
	chapterList: List<ChapterObject>
) {
	chapterList.forEach { chapterObject ->
		item { Spacer(modifier = Modifier.height(4.dp)) }
		item {
			ChapterListCard(
				id = chapterObject.id,
				timestamp = chapterObject.createdTimestamp,
				isLocked = chapterObject.isLocked,
				isSelected = false,
				isFavourite = chapterObject.isFavourite,
				isDeleted = false,
				isLast = false,
				title = chapterObject.title,
				description = chapterObject.description,
				color = Color(chapterObject.color ?: MaterialTheme.colorScheme.surface.toArgb()),
				noteCount = chapterObject.noteList.size,
				chapterCount = chapterObject.chapterList.size,
				isVisible = true,
				selectedColor = MaterialTheme.colorScheme.surface,
				onClick = { /*TODO*/ })
		}
	}
}
