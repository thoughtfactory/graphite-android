package com.syncodec.graphite.presentation.exp.calendar.composable.calendarView

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.CalendarMonth
import io.github.esentsov.PackagePrivate
import java.time.YearMonth
import java.time.format.DateTimeFormatter


@PackagePrivate
@Preview
@Composable
fun MonthHeader(
	calendarMonth : CalendarMonth = CalendarMonth(YearMonth.now(), listOf()),
) {
	Box(
		contentAlignment = Alignment.CenterStart,
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp, 20.dp, 16.dp, 8.dp),
	) {
		Text(
			text = calendarMonth.yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold,
		)
	}
}
