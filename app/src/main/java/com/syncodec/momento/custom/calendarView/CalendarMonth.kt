package com.syncodec.momento.custom.calendarView

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.joda.time.LocalDate

@Preview
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarMonth(
	month: Int = 0,
	year: Int = 2022
) {
	val isLeapYear = ((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)
	val daysInMonth: List<Int> = listOf(31, if (isLeapYear) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

	val localDate = LocalDate(year, month + 1, 1)
	val dayOfWeek = localDate.dayOfWeek % 7

	Column(
		modifier = Modifier
			.background(MaterialTheme.colorScheme.secondaryContainer),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		LazyVerticalGrid(
			cells = GridCells.Fixed(7),
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp, 0.dp),
		) {
			for (i in 0 until dayOfWeek) {
				item {
					Box(
						modifier = Modifier
							.fillMaxSize()
							.aspectRatio(1f),
					)
				}
			}

			for (i in 0 until daysInMonth[month]) {
				item {
					Box(
						modifier = Modifier
							.fillMaxSize()
							.aspectRatio(1f)
							.clip(RoundedCornerShape(12.dp))
							.clickable { },
						contentAlignment = Alignment.Center
					) {
						Text(
							text = "${i + 1}",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSecondaryContainer,
							fontWeight = FontWeight.Bold
						)
					}
				}
			}

			for (i in 0 until (42 - dayOfWeek - daysInMonth[month])) {
				item {
					Box(
						modifier = Modifier
							.fillMaxSize()
							.aspectRatio(1f),
					)
				}
			}
		}
	}
}
