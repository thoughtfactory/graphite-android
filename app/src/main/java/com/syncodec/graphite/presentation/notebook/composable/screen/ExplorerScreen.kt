package com.syncodec.graphite.presentation.notebook.composable.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock.LoadingView
import com.syncodec.graphite.presentation.notebook.NotebookViewModel
import com.syncodec.graphite.presentation.notebook.composable.buildingBlock.NoteCard


@Composable
fun ExplorerScreen() {
	val viewModel: NotebookViewModel = viewModel()

	val notebook by viewModel.notebook

	Crossfade(targetState = notebook) { chapterObject ->
		if (chapterObject == null) {
			LoadingView()
		} else {
			LazyColumn(
				modifier = Modifier.fillMaxSize()
			) {
				chapterObject.noteList.map { it.toLite() }.forEach { noteObject ->
					item { Spacer(modifier = Modifier.height(4.dp)) }
					item {
						NoteCard(
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
		}
	}
}
