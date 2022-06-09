package com.syncodec.graphite.searchComponent.screen

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.custom.notebook.NoteCard
import com.syncodec.graphite.custom.notebook.NotebookTimelineSpacer
import com.syncodec.graphite.database.note.NoteDbEntry
import com.syncodec.graphite.searchComponent.SearchActivity

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NoteScreen(
	noteList: SnapshotStateMap<NoteDbEntry, Boolean>,
	isSearching: Boolean,
	onAction: (SearchActivity.Action, Any?) -> Unit
) {
	var lastEntryKey by remember { mutableStateOf<String?>(null) }
	SideEffect {
		noteList.forEach { (note, isVisible) -> if (isVisible) lastEntryKey = note.key }
	}

	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item { Spacer(modifier = Modifier.height(12.dp)) }
		noteList.forEach { (note, isVisible) ->
			item {
				NoteCard(
					key = note.key,
					timestamp = note.userTimestamp,
					showFullTime = true,
					isLocked = false,
					isSelected = false,
					isArchived = note.isArchived,
					isFavourite = note.isFavourite,
					isDeleted = false,
					isLast = note.key == lastEntryKey,
					title = note.title,
					contentThumbnail = note.contentThumbnail,
					attachmentCount = note.attachmentKeyList.size,
					attachmentThumbnail = note.attachmentThumbnail,
					address = note.address,
					latLng = note.latLng,
					isVisible = isVisible,
					selectedColor = Color.Transparent,
					onClick = {
						onAction(
							SearchActivity.Action.CLICK_NOTE,
							Pair(note.key, note.notebookKey)
						)
					}
				)

				NotebookTimelineSpacer(isVisible = note.key != lastEntryKey && isVisible)
			}
		}

		item {
			AnimatedContent(
				targetState = isSearching,
				transitionSpec = {
					(fadeIn(tween(600)) with fadeOut(tween(600)))
						.using(SizeTransform(clip = false))
				}
			) {
				if (isSearching) {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.height(128.dp),
						contentAlignment = Alignment.Center
					) {
						CircularProgressIndicator(
							color = MaterialTheme.colorScheme.primary,
							strokeWidth = 4.dp
						)
					}
				} else {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.height(128.dp),
						contentAlignment = Alignment.Center
					) {
						Text(
							text = "End of search result",
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onBackground
						)
					}
				}
			}
		}
	}
}
