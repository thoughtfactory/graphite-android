package com.syncodec.momento.custom.calenderView

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.YearMonth
import java.util.*

@Preview
@Composable
fun CalendarView(
	modifier: Modifier = Modifier,
	calendarState: LazyListState = rememberLazyListState(),
	startYear: Int = 1900,
	endYear: Int = 2200
) {
	val monthList = MonthIndex.values()

	Column {
		CalendarWeekBar()

		LazyColumn(
			state = calendarState,
			modifier = modifier
		) {
			itemsIndexed((startYear until endYear).toList()) { _, year ->
				for (month in 0 until 12) {
					Spacer(modifier = Modifier.height(8.dp))
					Text(
						text = "${monthList[month].name} $year",
						color = MaterialTheme.colorScheme.primary,
						modifier = Modifier
							.padding(4.dp)
					)
					CalendarMonth(
						year = year,
						month = month
					)
				}
			}
		}
	}
}

enum class MonthIndex {
	January,
	February,
	March,
	April,
	May,
	June,
	July,
	August,
	September,
	October,
	November,
	December
}
