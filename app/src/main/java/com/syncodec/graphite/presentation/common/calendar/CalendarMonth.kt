package com.syncodec.graphite.presentation.common.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.utils.tone
import org.joda.time.LocalDate
import java.util.*

private val sizeColorMap: Map<Int, Color> = mapOf(
	0 to Color(0xFFFED049),
	1 to Color(0xFF3D84B8),
	2 to Color(0xFFFF6464)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarMonth(
	year: Int,
	month: Int,
	currentYear: Int,
	currentMonth: Int,
	currentDayOfMonth: Int,
	timestampSizeMap: Map<Long, Int>,
	onClick: (Long) -> Unit,
) {
	val isLeapYear = ((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)
	val daysInMonth: List<Int> =
		listOf(31, if (isLeapYear) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

	val localDate = LocalDate(year, month + 1, 1)
	val dayOfWeek = localDate.dayOfWeek % 7

	Column(
		modifier = Modifier,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		LazyVerticalGrid(
			columns = GridCells.Fixed(7),
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp, 0.dp),
		) {
			for (i in 0 until dayOfWeek) {
				item {
					Box(
						modifier = Modifier
							.fillMaxSize()
							.padding(1.dp)
							.aspectRatio(1f),
					)
				}
			}
			for (i in 0 until daysInMonth[month]) {
				item {
					val calendar = Calendar.getInstance()
					calendar.set(Calendar.YEAR, year)
					calendar.set(Calendar.MONTH, month)
					calendar.set(Calendar.DAY_OF_MONTH, i + 1)
					calendar.set(Calendar.HOUR_OF_DAY, 0)
					calendar.set(Calendar.MINUTE, 0)
					calendar.set(Calendar.SECOND, 0)
					calendar.set(Calendar.MILLISECOND, 0)

					DayButton(
						timestamp = calendar.timeInMillis,
						highlight = year == currentYear && month == currentMonth && i + 1 == currentDayOfMonth,
						totalEntries = timestampSizeMap[calendar.timeInMillis] ?: 0,
						dayOfMonth = i,
					) { onClick(it) }
				}
			}
			for (i in 0 until (42 - dayOfWeek - daysInMonth[month])) {
				item {
					Box(
						modifier = Modifier
							.fillMaxSize()
							.padding(1.dp)
							.aspectRatio(1f),
					)
				}
			}
		}
	}
}

@Composable
private fun DayButton(
	timestamp: Long,
	highlight: Boolean,
	totalEntries: Int,
	dayOfMonth: Int,
	onClick: (Long) -> Unit
) {
	val dayOfMonthBackground = if (highlight) MaterialTheme.colorScheme.primary
	else MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
	val dayOfMonthForeground = if (highlight) MaterialTheme.colorScheme.onPrimary
	else contentColorFor(
		backgroundColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
	)

	Box(
		modifier = Modifier
			.fillMaxSize()
			.padding(1.dp)
			.aspectRatio(1f)
			.clip(RoundedCornerShape(12.dp))
			.background(dayOfMonthBackground)
			.clickable { onClick(timestamp) },
		contentAlignment = Alignment.Center
	) {
		Text(
			text = "${dayOfMonth + 1}",
			style = MaterialTheme.typography.bodySmall,
			color = dayOfMonthForeground,
			fontWeight = FontWeight.Bold
		)

		Row(
			verticalAlignment = Alignment.Bottom,
			horizontalArrangement = Arrangement.Center,
			modifier = Modifier
				.fillMaxSize()
				.padding(0.dp, 0.dp, 0.dp, 4.dp),
		) {
			Spacer(modifier = Modifier.width(2.dp))
			repeat((minOf(totalEntries, 3)+1)/2) {
				Box(
					modifier = Modifier
						.requiredSize(6.dp)
						.clip(RoundedCornerShape(50))
						.background(sizeColorMap[it]!!)
				)
				Spacer(modifier = Modifier.width(2.dp))
			}
		}
	}
}
