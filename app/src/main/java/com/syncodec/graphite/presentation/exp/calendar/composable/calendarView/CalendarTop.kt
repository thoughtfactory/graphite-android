package com.syncodec.graphite.presentation.exp.calendar.composable.calendarView

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import io.github.esentsov.PackagePrivate
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale


@PackagePrivate
@Composable
fun CalendarTop(
	daysOfWeek : List<DayOfWeek>,
) {
	Row(
		modifier = Modifier.fillMaxWidth()
	) {
		for (dayOfWeek in daysOfWeek) {
			Text(
				textAlign = TextAlign.Center,
				text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
				modifier = Modifier.weight(1f),
			)
		}
	}
}
