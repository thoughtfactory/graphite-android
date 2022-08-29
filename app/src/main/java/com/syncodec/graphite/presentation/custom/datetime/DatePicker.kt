package com.syncodec.graphite.presentation.custom.datetime

import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun DatePicker(onDateSelected: (LocalDate) -> Unit, onDismissRequest: () -> Unit) {
	val selDate = remember { mutableStateOf(LocalDate.now()) }

	//todo - add strings to resource after POC
	Dialog(onDismissRequest = { onDismissRequest() }, properties = DialogProperties()) {
		Column(
			modifier = Modifier
				.wrapContentSize()
				.background(
					color = MaterialTheme.colorScheme.surface,
					shape = RoundedCornerShape(size = 16.dp)
				)
		) {
			Column(
				Modifier
					.defaultMinSize(minHeight = 72.dp)
					.fillMaxWidth()
					.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
					.background(MaterialTheme.colorScheme.primary)
					.padding(16.dp)
			) {
				Text(
					text = "Select date".uppercase(Locale.ENGLISH),
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onPrimary
				)

				Spacer(modifier = Modifier.size(24.dp))

				Text(
					text = selDate.value.format(DateTimeFormatter.ofPattern("MMM d, YYYY")),
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onPrimary
				)

				Spacer(modifier = Modifier.size(16.dp))
			}

			CustomCalendarView(onDateSelected = {
				selDate.value = it
			})

			Spacer(modifier = Modifier.size(8.dp))

			Row(
				modifier = Modifier
					.align(Alignment.End)
					.padding(bottom = 16.dp, end = 16.dp)
			) {
				TextButton(
					onClick = onDismissRequest
				) {
					//TODO - hardcode string
					Text(
						text = "Cancel",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onPrimary
					)
				}

				TextButton(
					onClick = {
						onDateSelected(selDate.value)
						onDismissRequest()
					}
				) {
					//TODO - hardcode string
					Text(
						text = "OK",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onPrimary
					)
				}

			}
		}
	}
}

@Composable
fun CustomCalendarView(onDateSelected: (LocalDate) -> Unit) {
	AndroidView(
		modifier = Modifier.wrapContentSize(),
		factory = { context ->
			CalendarView(context)
		},
		update = { view ->
				view.setOnDateChangeListener { _, year, month, dayOfMonth ->
					onDateSelected(
						LocalDate
							.now()
							.withMonth(month + 1)
							.withYear(year)
							.withDayOfMonth(dayOfMonth)
					)
				}
		}
	)
}
