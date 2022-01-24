package com.syncodec.momento.custom.calenderView

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.joda.time.LocalDate
import java.time.DayOfWeek
import java.time.YearMonth
import java.util.*

@Preview
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarMonth(
	year: Int = 2022,
	month: Int = 0
) {
	val isLeapYear = ((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)
	val daysInMonth: List<Int> = listOf(31, if (isLeapYear) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
	val localDate = LocalDate(year, month + 1, 1)
	val dayOfWeek = localDate.dayOfWeek % 7

	Column {
		CalendarWeek(
			// week 1
			startDay = dayOfWeek,
			startDate = 1,
		)
		CalendarWeek(
			// week 2
			startDate = 8 - dayOfWeek,
		)
		CalendarWeek(
			// week 3
			startDate = 15 - dayOfWeek,
		)
		CalendarWeek(
			//  week 4
			startDate = 22 - dayOfWeek,
		)

		if (month == 1) {
			CalendarWeek(
				//  week 5
				startDate = 29 - dayOfWeek,
				totalDays = ((daysInMonth[month] + dayOfWeek - 1) % 7) + 1
			)
			CalendarWeek(
				//  week 6
				startDay = null
			)
		} else {
			if (daysInMonth[month] < 36 - dayOfWeek) {
				CalendarWeek(
					// week 5
					startDate = 29 - dayOfWeek,
					totalDays = ((daysInMonth[month] + dayOfWeek - 1) % 7) + 1
				)
				CalendarWeek(
					startDay = null
				)
			} else {
				CalendarWeek(
					// week 4
					startDate = 29 - dayOfWeek,
				)
				CalendarWeek(
					// week 6
					startDate = 36 - dayOfWeek,
					totalDays = (daysInMonth[month] + dayOfWeek) % 7
				)
			}
		}
	}
}
