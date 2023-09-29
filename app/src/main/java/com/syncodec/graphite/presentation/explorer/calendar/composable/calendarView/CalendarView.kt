package com.syncodec.graphite.presentation.explorer.calendar.composable.calendarView

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.LocalDate
import java.time.YearMonth


@Preview
@Composable
fun CalendarView(
	modifier: Modifier = Modifier,
	selectedDate: LocalDate = LocalDate.now(),
	noteListDateCountMap: Map<LocalDate, Int> = mapOf(),
	onSelectDate: (LocalDate) -> Unit = {}
) {
	val calendarState = rememberCalendarState(
		startMonth = YearMonth.of(1971, 4),
		endMonth = YearMonth.of(2071, 4),
		firstVisibleMonth = YearMonth.now(),
		firstDayOfWeek = firstDayOfWeekFromLocale(),
	)

	val daysOfWeek = remember { daysOfWeek() }

	Column(
		modifier = modifier
	) {
		CalendarTop(daysOfWeek = daysOfWeek,)
		Spacer(modifier = Modifier.height(20.dp))
		Divider()

		VerticalCalendar(
			modifier = Modifier.weight(1f),
			dayContent = { day ->
				if (day.position == DayPosition.MonthDate) {
					Day(
						day = day,
						isSelected = day.date == selectedDate,
						itemCount = noteListDateCountMap[day.date] ?: 0,
						onClick = { onSelectDate(day.date) }
					)
				}
			},
			state = calendarState,
			monthHeader = { month -> MonthHeader(calendarMonth = month.copy(weekDays = listOf())) },
		)
	}
}
