package com.syncodec.graphite.presentation.note.screen.viewerScreen.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog
import com.syncodec.graphite.presentation.common.dialog.whereDialog.WhereDialog
import com.syncodec.graphite.presentation.common.permission.NotificationPermissionDialog
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.TimePickerDefaults
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset


enum class ViewerDialogType {
	DatePicker,
	TimePicker,
	Where,
	Delete,
	NotificationPermission,
}

@Preview
@Composable
fun Dialog(
	userTimestamp : Long? = null,
	isDatePickerDialogVisible : Boolean = false,
	isTimePickerDialogVisible : Boolean = false,
	isWhereDialogVisible : Boolean = false,
	isDeleteDialogVisible : Boolean = false,
	isNotificationPermissionDialogVisible : Boolean = false,
	parentChapter : ChapterObjectLite? = null,
	setUserTimestamp : (Long) -> Unit = {},
	setParentChapter : (ChapterObjectLite?) -> Unit = {},
	onDelete : () -> Unit = {},
	onNotificationPermissionAvailable : () -> Unit = {},
	openDialog : (ViewerDialogType) -> Unit = {},
	closeDialog : (ViewerDialogType) -> Unit = {},
) {

	val datePickerDialogState = rememberMaterialDialogState()
	val timePickerDialogState = rememberMaterialDialogState()

	LaunchedEffect(key1 = isDatePickerDialogVisible) {
		if (isDatePickerDialogVisible) datePickerDialogState.show() else datePickerDialogState.hide()
	}

	LaunchedEffect(key1 = isTimePickerDialogVisible) {
		if (isTimePickerDialogVisible) timePickerDialogState.show() else timePickerDialogState.hide()
	}

	var localDatetime = remember {
		userTimestamp?.let {
			LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.systemDefault())
		} ?: LocalDateTime.now(Clock.systemDefaultZone())
	}

	LaunchedEffect(key1 = userTimestamp) {
		localDatetime = userTimestamp?.let {
			LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.systemDefault())
		} ?: LocalDateTime.now(Clock.systemDefaultZone())
	}

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
			shape = MaterialTheme.shapes.extraLarge,
			elevation = 0.dp,
			onCloseRequest = { closeDialog(ViewerDialogType.DatePicker) }
		) {
			datepicker(
				initialDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.systemDefault()).toLocalDate(),
				colors = DatePickerDefaults.colors(
					headerBackgroundColor = MaterialTheme.colorScheme.primary,
					headerTextColor = MaterialTheme.colorScheme.onPrimary,
					calendarHeaderTextColor = MaterialTheme.colorScheme.onBackground,
					dateActiveBackgroundColor = MaterialTheme.colorScheme.primary,
					dateInactiveBackgroundColor = Color.Transparent,
					dateActiveTextColor = MaterialTheme.colorScheme.onPrimary,
					dateInactiveTextColor = MaterialTheme.colorScheme.onBackground,
				),
			) { date ->
				setUserTimestamp(localDatetime.with(date).toInstant(OffsetDateTime.now().offset).toEpochMilli())
				closeDialog(ViewerDialogType.DatePicker)
				openDialog(ViewerDialogType.TimePicker)
			}
		}
	}

	userTimestamp?.let { timeStamp ->
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
			shape = MaterialTheme.shapes.extraLarge,
			elevation = 0.dp,
			onCloseRequest = { closeDialog(ViewerDialogType.TimePicker) }
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
				LocalDateTime.ofInstant(Instant.ofEpochMilli(timeStamp), ZoneOffset.systemDefault()).with(it).let {
					setUserTimestamp(it.toInstant(OffsetDateTime.now().offset).toEpochMilli())
				}
				closeDialog(ViewerDialogType.TimePicker)
			}
		}
	}

	WhereDialog(
		showDialog = isWhereDialogVisible,
		parentChapter = parentChapter,
		onSetChapter = setParentChapter,
		onDismiss = { closeDialog(ViewerDialogType.Where) }
	)

	DeleteDialog(
		showDialog = isDeleteDialogVisible,
		message = "Are you sure you want to delete this note? This action cannot be undone. This will also delete all the attachments associated with this note.",
		onDismiss = { closeDialog(ViewerDialogType.Delete) },
		onDelete = onDelete
	)

	NotificationPermissionDialog(
	) {
		closeDialog(ViewerDialogType.NotificationPermission)
		onNotificationPermissionAvailable()
	}
}
