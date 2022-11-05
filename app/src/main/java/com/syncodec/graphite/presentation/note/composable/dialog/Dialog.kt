package com.syncodec.graphite.presentation.note.composable.dialog

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.notification.NotePinNotification
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog
import com.syncodec.graphite.presentation.common.dialog.DiscardDialog
import com.syncodec.graphite.presentation.common.permission.NotificationPermissionDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionAddress
import com.syncodec.graphite.presentation.note.composable.LocalCompositionCloseDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionContentThumbnail
import com.syncodec.graphite.presentation.note.composable.LocalCompositionIsViewing
import com.syncodec.graphite.presentation.note.composable.LocalCompositionLatLng
import com.syncodec.graphite.presentation.note.composable.LocalCompositionNoteId
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOnMoveChapter
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOnSelectChapter
import com.syncodec.graphite.presentation.note.composable.LocalCompositionParentChapterId
import com.syncodec.graphite.presentation.note.composable.LocalCompositionSelectChapterList
import com.syncodec.graphite.presentation.note.composable.LocalCompositionSelectChapterPath
import com.syncodec.graphite.presentation.note.composable.LocalCompositionShowChapterSelectionDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionShowDeleteDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionShowDiscardDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionShowLocationPickerDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionShowNotificationPermissionDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionTitle
import com.syncodec.graphite.presentation.note.composable.LocalDeleteNote
import com.syncodec.graphite.presentation.note.composable.LocalDiscardChanges
import com.syncodec.graphite.presentation.note.util.reverseGeocode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


enum class NoteDialogType {
	DELETE,
	EXPORT,
	LOCATION_PERMISSION,
	LOCATION_PICKER,
	TAG,
	DISCARD,
	NOTIFICATION_PERMISSION,
	CHAPTER_SELECTION,
	PRINT
}

@Composable
fun NoteDialog(
	setLocation:(LatLng, String?) -> Unit
) {
	val scope = rememberCoroutineScope()
	val context = LocalContext.current

	val isViewing = LocalCompositionIsViewing.current

	val noteId = LocalCompositionNoteId.current
	val parentChapterId = LocalCompositionParentChapterId.current
	val title = LocalCompositionTitle.current
	val contentThumbnail = LocalCompositionContentThumbnail.current
	val latLng = LocalCompositionLatLng.current
	val address = LocalCompositionAddress.current

	val chapterList = LocalCompositionSelectChapterList.current
	val chapterPath = LocalCompositionSelectChapterPath.current

	val showLocationPickerDialog = LocalCompositionShowLocationPickerDialog.current

	val showNotificationPermissionDialog = LocalCompositionShowNotificationPermissionDialog.current
	val showChapterSelectionDialog = LocalCompositionShowChapterSelectionDialog.current
	val showDiscardDialog = LocalCompositionShowDiscardDialog.current
	val showDeleteDialog = LocalCompositionShowDeleteDialog.current

	val onSelectChapter = LocalCompositionOnSelectChapter.current
	val onMoveChapter = LocalCompositionOnMoveChapter.current

	val closeDialog = LocalCompositionCloseDialog.current

	val deleteNote = LocalDeleteNote.current
	val discardChanges = LocalDiscardChanges.current

	LocationPickerDialog(
		showDialog = showLocationPickerDialog,
		latLng = latLng,
		address = address,
		reverseGeocode = { _latLng, onAddressAvailable ->
			if (_latLng.latitude != null && _latLng.longitude != null) {
				scope.launch(Dispatchers.IO) {
					context.reverseGeocode(
						latitude = _latLng.latitude ?: return@launch,
						longitude = _latLng.longitude ?: return@launch,
						onAddressAvailable = onAddressAvailable
					)
				}
			}
		},
		onDismiss = { closeDialog(NoteDialogType.LOCATION_PICKER) },
		onConfirm = { _latLng, _address ->
			setLocation(_latLng, _address)
			closeDialog(NoteDialogType.LOCATION_PICKER)
		}
	)

	NotificationPermissionDialog(
		showDialog = showNotificationPermissionDialog,
		onDismiss = { closeDialog(NoteDialogType.NOTIFICATION_PERMISSION) },
	) {
		closeDialog(NoteDialogType.NOTIFICATION_PERMISSION)
		if (isViewing == true) {
			noteId?.let {
				parentChapterId?.let { it1 ->
					NotePinNotification
						.showSimpleNotification(
							context = context,
							noteId = it,
							chapterId = it1,
							title = title ?: "Untitled",
							content = contentThumbnail ?: "No content",
							notificationId = noteId.hashCode() ?: 0,
						) { Toast.makeText(context, "Notification permission not available. Please enable permission from settings", Toast.LENGTH_SHORT).show() }
				}
			}
		} else {
			Toast.makeText(context, "Please save note before pinning", Toast.LENGTH_SHORT).show()
		}
	}

	ChapterSelectionDialog(
		showDialog = showChapterSelectionDialog,
		chapterList = chapterList,
		chapterPath = chapterPath,
		onSelectChapter = onSelectChapter,
		onMove = onMoveChapter,
		onDismiss = { closeDialog(NoteDialogType.CHAPTER_SELECTION) }
	)

	DeleteDialog(
		showDialog = showDeleteDialog,
		message = "Are you sure you want to delete this note? This operation is non reversible.",
		onDismiss = { closeDialog(NoteDialogType.DELETE) },
	) {
		closeDialog(NoteDialogType.DELETE)
		deleteNote()
	}

	DiscardDialog(
		showDiscardDialog = showDiscardDialog,
		onDismiss = { closeDialog(NoteDialogType.DISCARD) }
	) {
		closeDialog(NoteDialogType.DISCARD)
		discardChanges()
	}
}
