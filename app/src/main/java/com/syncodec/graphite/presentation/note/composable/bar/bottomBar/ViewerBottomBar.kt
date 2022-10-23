package com.syncodec.graphite.presentation.note.composable.bar.bottomBar

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.notification.NotePinNotification
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.note.composable.LocalCompositionNoteId
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.note.composable.LocalEditNote
import com.syncodec.graphite.presentation.note.composable.dialog.NoteDialogType
import com.syncodec.graphite.utils.tone


@Composable
fun ViewerBottomBar() {

	val context = LocalContext.current
	val openDialog = LocalCompositionOpenDialog.current

	val noteId = LocalCompositionNoteId.current
	val editNote = LocalEditNote.current

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1))
	) {
		Spacer(modifier = Modifier.height(16.dp))
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(2.dp)
		) {
			Spacer(modifier = Modifier.width(16.dp))

			MenuButton(
				icon = R.drawable.ic_pin,
				contentDescription = "Pin Note in Notification",
				isChecked = NotePinNotification.isNotificationPinned(context, noteId),
				onClick = { openDialog(NoteDialogType.NOTIFICATION_PERMISSION) }
			)

			MenuButton(
				icon = R.drawable.ic_share,
				contentDescription = "Share Note",
				onClick = {}
			)

			MenuButton(
				icon = R.drawable.ic_printer,
				contentDescription = "Print Note",
				onClick = {}
			)

			Spacer(modifier = Modifier.weight(1f))

			MenuButton(
				icon = R.drawable.ic_pencil,
				contentDescription = "Edit Note",
				onClick = editNote
			)

			Spacer(modifier = Modifier.width(16.dp))
		}
		Spacer(modifier = Modifier.height(16.dp))
	}
}
