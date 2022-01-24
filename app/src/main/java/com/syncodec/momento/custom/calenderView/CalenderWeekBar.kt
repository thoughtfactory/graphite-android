package com.syncodec.momento.custom.calenderView

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun CalendarWeekBar(
	modifier: Modifier = Modifier
) {
	val dayList: List<String> = listOf(
		"Sun",
		"Mon",
		"Tue",
		"Wed",
		"Thu",
		"Fri",
		"Sat",
	)

	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically
	) {
		val calendarWeekBarItemModifier = Modifier
			.weight(1f)
			.wrapContentHeight()
			.padding(2.dp)

		dayList.forEach { day ->
			CalendarWeekBarItem(
				modifier = calendarWeekBarItemModifier,
				day = day
			)
		}
	}
}

@Preview
@Composable
private fun CalendarWeekBarItem(
	modifier: Modifier = Modifier,
	day: String = "Sun"
) {
	Box(
		modifier = modifier
	) {
		Text(
			text = day,
			textAlign = TextAlign.Center,
			style = MaterialTheme.typography.labelSmall,
			color = MaterialTheme.colorScheme.onPrimaryContainer,
			modifier = Modifier
				.fillMaxWidth()
				.wrapContentHeight(align = Alignment.CenterVertically)
		)
	}
}
