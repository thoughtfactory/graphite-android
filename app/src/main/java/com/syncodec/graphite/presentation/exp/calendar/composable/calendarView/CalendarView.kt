package com.syncodec.graphite.presentation.exp.calendar.composable.calendarView

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.syncodec.graphite.di.model.NoteObjectLite
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Preview
@Composable
fun CalendarView(
	selectedDay : LocalDate = LocalDate.now(),
	noteList : List<NoteObjectLite> = listOf(),
	onUpdateSelectedDay : () -> Unit = {}
) {

	val calendarState = rememberCalendarState(
		startMonth = YearMonth.of(1971, 4),
		endMonth = YearMonth.of(2071, 4),
		firstVisibleMonth = YearMonth.now(),
		firstDayOfWeek = firstDayOfWeekFromLocale(),
	)

	val daysOfWeek = remember { daysOfWeek() }

	var noteDayMap by remember { mutableStateOf<Map<LocalDate, List<NoteObjectLite>>>(mapOf()) }
	LaunchedEffect(key1 = noteList) {
		noteList.groupBy { LocalDateTime.ofInstant(Instant.ofEpochMilli(it.userTimestamp), ZoneId.systemDefault()).toLocalDate() }.let { noteDayMap = it }
		getContextNoteList(selectedDay.date.format(DateTimeFormatter.ofPattern("dd MMMM, yyyy")), noteDayMap[selectedDay.date] ?: listOf())
	}

	Column(
		modifier = Modifier.fillMaxSize()
	) {

		CalendarTop(
			daysOfWeek = daysOfWeek,
		)
		Spacer(modifier = Modifier.height(16.dp))
		Divider()

		VerticalCalendar(
			modifier = Modifier.weight(1f),
			dayContent = { day ->
				val noteMap = noteDayMap[day.date] ?: listOf()
				if (day.position == DayPosition.MonthDate) {
					Day(
						day = day,
						isSelected = day == selectedDay,
						itemCount = noteMap.size,
					) {
						selectedDay = day
						getContextNoteList(day.date.format(DateTimeFormatter.ofPattern("dd MMMM, yyyy")), noteMap)
					}
				}
			},
			state = calendarState,
			monthHeader = { month -> MonthHeader(calendarMonth = month.copy(weekDays = listOf())) },
		)
	}
}
