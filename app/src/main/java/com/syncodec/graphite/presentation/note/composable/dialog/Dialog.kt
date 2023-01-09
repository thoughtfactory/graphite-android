package com.syncodec.graphite.presentation.note.composable.dialog

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.notification.NotePinNotification
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog
import com.syncodec.graphite.presentation.common.dialog.DiscardDialog
import com.syncodec.graphite.presentation.common.dialog.whereDialog.WhereDialog
import com.syncodec.graphite.presentation.common.permission.NotificationPermissionDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionAddress
import com.syncodec.graphite.presentation.note.composable.LocalCompositionCloseDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionContentThumbnail
import com.syncodec.graphite.presentation.note.composable.LocalCompositionIsViewing
import com.syncodec.graphite.presentation.note.composable.LocalCompositionLatLng
import com.syncodec.graphite.presentation.note.composable.LocalCompositionNoteId
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOnMoveChapter
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOnSelectChapter
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionParentChapterId
import com.syncodec.graphite.presentation.note.composable.LocalCompositionSelectChapterList
import com.syncodec.graphite.presentation.note.composable.LocalCompositionSelectChapterPath
import com.syncodec.graphite.presentation.note.composable.LocalCompositionSetUserTimestamp
import com.syncodec.graphite.presentation.note.composable.LocalCompositionShowChapterSelectionDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionShowDatePickerDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionShowDeleteDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionShowDiscardDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionShowLocationPickerDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionShowNotificationPermissionDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionShowShareDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionShowTimePickerDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionTitle
import com.syncodec.graphite.presentation.note.composable.LocalCompositionUserTimestamp
import com.syncodec.graphite.presentation.note.composable.LocalDeleteNote
import com.syncodec.graphite.presentation.note.composable.LocalDiscardChanges
import com.syncodec.graphite.presentation.note.composable.LocalOnShareAttachment
import com.syncodec.graphite.presentation.note.composable.LocalOnShareText
import com.syncodec.graphite.presentation.note.util.reverseGeocode
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.TimePickerDefaults
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset


enum class NoteDialogType {
	DATE_PICKER,
	TIME_PICKER,
	DELETE,
	SHARE,
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
	setLocation : (LatLng, String?) -> Unit
) {
	val scope = rememberCoroutineScope()
	val context = LocalContext.current

	val isViewing = LocalCompositionIsViewing.current

	val noteId = LocalCompositionNoteId.current
	val parentChapterId = LocalCompositionParentChapterId.current
	val title = LocalCompositionTitle.current
	val userTimestamp = LocalCompositionUserTimestamp.current
	val contentThumbnail = LocalCompositionContentThumbnail.current
	val latLng = LocalCompositionLatLng.current
	val address = LocalCompositionAddress.current

	val showLocationPickerDialog = LocalCompositionShowLocationPickerDialog.current

	val showDatePickerDialog = LocalCompositionShowDatePickerDialog.current
	val showTimePickerDialog = LocalCompositionShowTimePickerDialog.current
	val showNotificationPermissionDialog = LocalCompositionShowNotificationPermissionDialog.current
	val showShareDialog = LocalCompositionShowShareDialog.current
	val showDiscardDialog = LocalCompositionShowDiscardDialog.current
	val showDeleteDialog = LocalCompositionShowDeleteDialog.current

	val setUserTimestamp = LocalCompositionSetUserTimestamp.current

	val datePickerDialogState = rememberMaterialDialogState()
	val timePickerDialogState = rememberMaterialDialogState()

	val openDialog = LocalCompositionOpenDialog.current
	val closeDialog = LocalCompositionCloseDialog.current

	val deleteNote = LocalDeleteNote.current
	val discardChanges = LocalDiscardChanges.current
	val onShareText = LocalOnShareText.current
	val onShareAttachment = LocalOnShareAttachment.current

	LaunchedEffect(key1 = showDatePickerDialog) {
		if (showDatePickerDialog) datePickerDialogState.show() else datePickerDialogState.hide()
	}

	LaunchedEffect(key1 = showTimePickerDialog) {
		if (showTimePickerDialog) timePickerDialogState.show() else timePickerDialogState.hide()
	}

	var localDatetime = remember {
		userTimestamp?.let {
			LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.systemDefault())
		} ?: LocalDateTime.now(Clock.systemDefaultZone())
//		LocalDateTime.now(Clock.systemDefaultZone())
	}

	LaunchedEffect(key1 = userTimestamp) {
		localDatetime = userTimestamp?.let {
			Log.i("npr71", "setting localDatetime")
			LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.systemDefault())
		} ?: LocalDateTime.now(Clock.systemDefaultZone())
	}

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
						) {
							Toast.makeText(context, "Notification permission not available. Please enable permission from settings", Toast.LENGTH_SHORT).show()
						}
				}
			}
		} else {
			Toast.makeText(context, "Please save note before pinning", Toast.LENGTH_SHORT).show()
		}
	}

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
		onDismiss = { closeDialog(NoteDialogType.DISCARD) },
		onDiscard = discardChanges
	)

	ShareDialog(
		showDialog = showShareDialog,
		onShareText = onShareText,
		onShareAttachment = onShareAttachment
	) { closeDialog(NoteDialogType.SHARE) }


	userTimestamp?.let {
		MaterialDialog(
			dialogState = datePickerDialogState,
			buttons = {
				positiveButton(
					text = "Ok",
					textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground)
				)
				negativeButton(
					text = "Cancel",
					textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground)
				)
			},
			backgroundColor = MaterialTheme.colorScheme.background,
			shape = RoundedCornerShape(24.dp),
			elevation = 0.dp,
			onCloseRequest = { closeDialog(NoteDialogType.DATE_PICKER) }
		) {
			datepicker(
				initialDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.systemDefault()).toLocalDate(),
				colors = DatePickerDefaults.colors(
					headerBackgroundColor = MaterialTheme.colorScheme.primary,
					headerTextColor = MaterialTheme.colorScheme.onPrimary,
					calendarHeaderTextColor = MaterialTheme.colorScheme.onBackground,
					dateActiveBackgroundColor = MaterialTheme.colorScheme.primary,
					dateInactiveBackgroundColor = Color.Companion.Transparent,
					dateActiveTextColor = MaterialTheme.colorScheme.onPrimary,
					dateInactiveTextColor = MaterialTheme.colorScheme.onBackground,
				),
			) { date ->
//				setUserTimestamp(localDatetime.toInstant(OffsetDateTime.now().offset).toEpochMilli())
				setUserTimestamp(localDatetime.with(date).toInstant(OffsetDateTime.now().offset).toEpochMilli())
				closeDialog(NoteDialogType.DATE_PICKER)
				openDialog(NoteDialogType.TIME_PICKER, null)
			}
		}
	}

	userTimestamp?.let {timeStamp ->
		MaterialDialog(
			dialogState = timePickerDialogState,
			buttons = {
				positiveButton(
					text = "Ok",
					textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground)
				)
				negativeButton(
					text = "Cancel",
					textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground)
				)
			},
			backgroundColor = MaterialTheme.colorScheme.background,
			shape = RoundedCornerShape(24.dp),
			elevation = 0.dp,
			onCloseRequest = { closeDialog(NoteDialogType.DATE_PICKER) }
		) {
			timepicker(
				initialTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeStamp), ZoneOffset.systemDefault()).toLocalTime(),
				colors = TimePickerDefaults.colors(
					activeBackgroundColor = MaterialTheme.colorScheme.primary,
					inactiveBackgroundColor = MaterialTheme.colorScheme.surface,
					activeTextColor = MaterialTheme.colorScheme.onPrimary,
					inactiveTextColor = MaterialTheme.colorScheme.onSurface,
					inactivePeriodBackground = MaterialTheme.colorScheme.surface,
					selectorColor = MaterialTheme.colorScheme.primary,
					selectorTextColor = MaterialTheme.colorScheme.onPrimary,
					headerTextColor = MaterialTheme.colorScheme.onBackground,
					borderColor = MaterialTheme.colorScheme.onBackground,
				),
			) {
//				localDatetime = localDatetime.with(it)
				LocalDateTime.ofInstant(Instant.ofEpochMilli(timeStamp), ZoneOffset.systemDefault()).with(it).let {
					setUserTimestamp(it.toInstant(OffsetDateTime.now().offset).toEpochMilli())
				}
//				setUserTimestamp(localDatetime.with(it).toInstant(OffsetDateTime.now().offset).toEpochMilli())
				closeDialog(NoteDialogType.TIME_PICKER)
			}
		}
	}
}
