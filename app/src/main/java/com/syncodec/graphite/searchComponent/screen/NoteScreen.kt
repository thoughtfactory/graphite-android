package com.syncodec.graphite.searchComponent.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.custom.notebook.NoteCard
import com.syncodec.graphite.custom.notebook.NotebookTimelineSpacer
import com.syncodec.graphite.database.note.NoteDbEntry

@Composable
fun NoteScreen(
	noteList: SnapshotStateMap<NoteDbEntry, Boolean>
) {
	var lastEntryKey: String? = null
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
					onClick = { /*TODO*/ }
				)

				NotebookTimelineSpacer(isVisible = note.key != lastEntryKey && isVisible)
			}
		}
	}
}
