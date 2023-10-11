package com.syncodec.graphite.presentation.note.composable.bar.editor.dialog

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialog2
import com.syncodec.graphite.presentation.common.dialog.dialog2.GenericDialogDefaults
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Calendar
import java.util.TimeZone


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun DateTimePickerDialog(
	isDialogVisible: Boolean = false,
	onDismissRequest: () -> Unit = {},
	currentUserTimestamp: Long = Instant.now().toEpochMilli(),
	onSelectDateTime: (Long) -> Unit = {},
) {
	val scope = rememberCoroutineScope()

	val calendar by remember(currentUserTimestamp) { derivedStateOf { Calendar.getInstance().apply { timeInMillis = currentUserTimestamp } } }

	var isTimePickerDialogVisible by rememberSaveable { mutableStateOf(false) }

	val datePickerState = rememberDatePickerState(initialSelectedDateMillis = calendar.timeInMillis)
	val timePickerState = rememberTimePickerState(initialHour = calendar.get(Calendar.HOUR_OF_DAY), initialMinute = calendar.get(Calendar.MINUTE), is24Hour = true)

	fun onSelectDate() {
		val hour = calendar.get(Calendar.HOUR_OF_DAY)
		val minute = calendar.get(Calendar.MINUTE)
		datePickerState.selectedDateMillis
		datePickerState.selectedDateMillis?.let {
			calendar.timeZone = TimeZone.getTimeZone("UTC")
			calendar.timeInMillis = it
			calendar.timeZone = TimeZone.getDefault()
			calendar.set(Calendar.HOUR_OF_DAY, hour)
			calendar.set(Calendar.MINUTE, minute)
		}
		onSelectDateTime(calendar.timeInMillis)
		onDismissRequest()
		isTimePickerDialogVisible = true
	}

	fun onSelectTime() {
		calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
		calendar.set(Calendar.MINUTE, timePickerState.minute)
		onSelectDateTime(calendar.timeInMillis)
		isTimePickerDialogVisible = false
		scope.launch { timePickerState.settle() }
	}

	if (isDialogVisible) {
		DatePickerDialog(
			onDismissRequest = onDismissRequest,
			confirmButton = {
				Button(
					onClick = ::onSelectDate,
					shape = MaterialTheme.shapes.medium,
					colors = ButtonDefaults.buttonColors(
						containerColor = MaterialTheme.colorScheme.primaryContainer,
						contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
						disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.47f),
						disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
					),
					modifier = Modifier.padding(end = 12.dp, bottom = 12.dp)
				) {
					Text(text = stringResource(R.string.select))
				}
			},
			dismissButton = {
				TextButton(
					onClick = onDismissRequest,
					shape = MaterialTheme.shapes.medium,
					colors = ButtonDefaults.buttonColors(
						containerColor = Color.Transparent,
						contentColor = MaterialTheme.colorScheme.onBackground,
						disabledContainerColor = Color.Transparent,
						disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
					),
					modifier = Modifier.padding(bottom = 12.dp)
				) {
					Text(text = stringResource(R.string.dismiss))
				}
			},
			colors = DatePickerDefaults.colors(
				containerColor = MaterialTheme.colorScheme.background
			)
		) {
			DatePicker(
				state = datePickerState,
			)
		}
	}

	GenericDialog2(
		isDialogVisible = isTimePickerDialogVisible,
		onDismissRequest = { isTimePickerDialogVisible = false },
		primaryButton = GenericDialogDefaults.genericDialogButtonPrimary(text = stringResource(R.string.select), onClick = ::onSelectTime),
		secondaryButton = GenericDialogDefaults.genericDialogButtonSecondary(text = stringResource(R.string.select), onClick = { isTimePickerDialogVisible = false }),
	) {
		TimePicker(
			state = timePickerState,
			colors = TimePickerDefaults.colors(
				clockDialColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.47f),
				clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
				clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
				selectorColor = MaterialTheme.colorScheme.onSurface,
				containerColor = MaterialTheme.colorScheme.primaryContainer,
				periodSelectorBorderColor = MaterialTheme.colorScheme.primaryContainer,
				periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
				periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.background,
				periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
				periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onBackground,
				timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
				timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
				timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
				timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
			)
		)
	}
}
