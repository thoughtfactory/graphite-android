package com.syncodec.momento.custom.calenderView

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun CalendarWeek(
	startDay: Int? = 0,
	startDate: Int = 1,
	totalDays: Int = 7
) {
	Row {
		val calendarDayModifier = Modifier
			.weight(1f)
			.aspectRatio(1f)
			.padding(1.dp)

		if (startDay == null) {
			for (dayNo in 0 until totalDays) {
				CalendarDay(modifier = calendarDayModifier)
			}
		} else {
			for (dayNo in 0 until startDay) {
				CalendarDay(modifier = calendarDayModifier)
			}
			for (dayNo in startDay until totalDays) {
				CalendarDay(modifier = calendarDayModifier, date = startDate + dayNo - startDay)
			}
			if (totalDays!=7) {
				for (dayNo in totalDays until 7) {
					CalendarDay(modifier = calendarDayModifier)
				}
			}
		}
	}
}
