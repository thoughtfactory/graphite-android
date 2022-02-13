package com.syncodec.momento.custom.calendarView2

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.syncodec.momento.konstant.Konstant.Companion.monthName
import com.syncodec.momento.konstant.Konstant.Companion.weekName
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronLeft
import compose.icons.tablericons.ChevronRight
import org.joda.time.LocalDate
import java.time.Month

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


	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp)
			.clip(RoundedCornerShape(12.dp))
			.background(MaterialTheme.colorScheme.secondaryContainer)
	) {
		Column(
			modifier = Modifier
				.padding(12.dp),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Spacer(modifier = Modifier.height(4.dp))
			FlowRow(
				modifier = Modifier
					.fillMaxWidth(),
				mainAxisAlignment = MainAxisAlignment.SpaceBetween,
				crossAxisAlignment = FlowCrossAxisAlignment.Center
			) {
				IconButton(onClick = { /*TODO*/ }) {
					Icon(
						imageVector = TablerIcons.ChevronLeft,
						contentDescription = "Previous month",
						tint = MaterialTheme.colorScheme.onSecondaryContainer
					)
				}

				Text(
					text = monthName[month],
					style = MaterialTheme.typography.titleLarge,
					color = MaterialTheme.colorScheme.onSecondaryContainer,
					fontWeight = FontWeight.Bold
				)

				IconButton(onClick = { /*TODO*/ }) {
					Icon(
						imageVector = TablerIcons.ChevronRight,
						contentDescription = "Next month",
						tint = MaterialTheme.colorScheme.onSecondaryContainer
					)
				}
			}

			Spacer(modifier = Modifier.height(16.dp))

			LazyVerticalGrid(
				cells = GridCells.Fixed(7),
				modifier = Modifier
					.fillMaxWidth()
					.clip(RoundedCornerShape(12.dp))
					.background(MaterialTheme.colorScheme.background.copy(alpha = 0.71f))
			) {
				for (i in 0 until 7) {
					item {
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.aspectRatio(1f),
							contentAlignment = Alignment.Center
						) {
							Text(
								text = weekName[i],
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSecondaryContainer,
								fontWeight = FontWeight.Bold
							)
						}
					}
				}
			}

			LazyVerticalGrid(
				cells = GridCells.Fixed(7),
				modifier = Modifier
					.fillMaxWidth()
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
}
