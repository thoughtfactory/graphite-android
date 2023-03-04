package com.syncodec.graphite.presentation.note.screen.editorScreen.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.presentation.common.dialog.DiscardDialog
import com.syncodec.graphite.presentation.common.dialog.whereDialog.WhereDialog
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


enum class EditorDialogType {
	DatePicker,
	TimePicker,
	LocationPermission,
	LocationPicker,
	Where,
	DiscardChanges,
}

@Preview
@Composable
fun Dialog(
	userTimestamp : Long? = null,
	isDatePickerDialogVisible : Boolean = false,
	isTimePickerDialogVisible : Boolean = false,
	isLocationPermissionDialogVisible : Boolean = false,
	isLocationPickerDialogVisible : Boolean = false,
	isWhereDialogVisible : Boolean = false,
	isDiscardChangesDialogVisible : Boolean = false,
	setUserTimestamp : (Long) -> Unit = {},
	setParentChapter : (ChapterObjectLite?) -> Unit = {},
	setLocation : (LatLng, String?) -> Unit = { _, _ -> },
	onDiscardChanges : () -> Unit = {},
	openDialog : (EditorDialogType) -> Unit = {},
	closeDialog : (EditorDialogType) -> Unit = {},
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
			onCloseRequest = { closeDialog(EditorDialogType.DatePicker) }
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
				closeDialog(EditorDialogType.DatePicker)
				openDialog(EditorDialogType.TimePicker)
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
			onCloseRequest = { closeDialog(EditorDialogType.TimePicker) }
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
				closeDialog(EditorDialogType.TimePicker)
			}
		}
	}

	LocationPermissionDialog(showDialog = isLocationPermissionDialogVisible) { closeDialog(EditorDialogType.LocationPermission) }

	LocationPickerDialog(
		showDialog = isLocationPickerDialogVisible,
		setLocation = setLocation,
	) { closeDialog(EditorDialogType.LocationPicker) }

	WhereDialog(
		showDialog = isWhereDialogVisible,
		parentChapter = null,
		onSetChapter = setParentChapter,
		onDismiss = { closeDialog(EditorDialogType.Where) }
	)

	DiscardDialog(
		showDialog = isDiscardChangesDialogVisible,
		onDiscard = onDiscardChanges,
		onDismiss = { closeDialog(EditorDialogType.DiscardChanges) }
	)
}
