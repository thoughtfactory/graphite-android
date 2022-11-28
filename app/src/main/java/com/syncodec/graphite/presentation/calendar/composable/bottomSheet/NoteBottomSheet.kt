package com.syncodec.graphite.presentation.calendar.composable.bottomSheet

import android.graphics.Bitmap
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.NoteObjectLite
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookHeaderCard
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.listView.NoteListCard
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.listView.NotebookTimelineSpacer
import com.syncodec.graphite.utils.AttachmentType
import com.syncodec.graphite.utils.tone
import io.realm.kotlin.types.RealmUUID
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale


@Composable
fun NoteBottomSheet(
	noteList: List<NoteObjectLite>,
	selectedDate: LocalDate,
	selectedItemList: List<RealmUUID>,
	onClickNote: (RealmUUID) -> Unit,
	onLongClickNote: (RealmUUID) -> Unit,
) {
	val lastEntryKey = if (noteList.isNotEmpty()) noteList.last().id else null

//	val tagList = LocalCompositionTagList.current

	LazyColumn(
		modifier = Modifier.fillMaxWidth()
	) {
		item { Spacer(modifier = Modifier.height(16.dp)) }
		item {
			val date = selectedDate.dayOfMonth
			val month = selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
			val year = selectedDate.year

			NotebookHeaderCard(
				title = "$date $month, $year",
				noEntries = if (noteList.isEmpty()) "No entries" else if (noteList.size == 1) "1 entry" else "${noteList.size} entries",
				color = MaterialTheme.colorScheme.surface,
			)
		}
		noteList.forEachIndexed { index, note ->
			item(
				key = note.id.toString(),
				contentType = NoteObjectLite::class,
			) {
				var thumbnail by remember { mutableStateOf<Bitmap?>(null) }

				LaunchedEffect(key1 = note.id.hashCode() + note.thumbnail.hashCode()) {
					try {
						if (note.thumbnailType == AttachmentType.IMAGE.name.lowercase()) thumbnail = note.thumbnail
					} catch (e : Exception) {
						e.printStackTrace()
					}
				}

				NoteListCard(
					id = note.id,
					timestamp = note.userTimestamp,
					showFullTime = false,
					isLocked = note.isLocked,
					isSelected = note.id in selectedItemList,
					isFavourite = note.isFavourite,
					isLast = note.id == lastEntryKey,
					title = note.title,
					contentThumbnail = note.contentThumbnail,
					attachmentThumbnail = thumbnail,
					address = note.address,
					latLng = note.latLng,
					tagList = listOf(),
					isVisible = true,
					isSwipable = false,
					selectedColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1),
					onClick = { onClickNote(note.id) },
					onLongClick = { onLongClickNote(note.id) },
				)

				NotebookTimelineSpacer(isVisible = note.id != lastEntryKey)
			}
		}
		item { Spacer(modifier = Modifier.height(194.dp)) }
	}
}
