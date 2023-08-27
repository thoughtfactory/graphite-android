package com.syncodec.graphite.presentation.exp.calendar.composable.calendarView

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import io.github.esentsov.PackagePrivate
import java.time.LocalDate


@PackagePrivate
@Preview
@Composable
fun Day(
	day : CalendarDay = CalendarDay(LocalDate.now(), DayPosition.MonthDate),
	isSelected : Boolean = false,
	itemCount : Int = 0,
	onClick : () -> Unit = {},
) {
	val containerColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.47f) else MaterialTheme.colorScheme.background,
		animationSpec = tween(470),
		label = "containerColor_animation"
	)
	val contentColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onBackground,
		animationSpec = tween(470),
		label = "contentColor_animation"
	)

	Column(
		modifier = Modifier
			.aspectRatio(1f)
			.padding(4.dp)
			.background(color = containerColor, MaterialTheme.shapes.medium)
			.clip(MaterialTheme.shapes.medium)
			.clickable(onClick = onClick),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Spacer(modifier = Modifier.weight(1f))
		Text(
			text = day.date.dayOfMonth.toString(),
			style = MaterialTheme.typography.bodyMedium,
			color = contentColor,
		)
		if (itemCount > 0) {
			val color1 = if (isSelected) MaterialTheme.colorScheme.onSurface else Color(0xFFFD8A8A)
			val color2 = if (isSelected) MaterialTheme.colorScheme.onSurface else Color(0xFFF2D388)
			val color3 = if (isSelected) MaterialTheme.colorScheme.onSurface else Color(0xFF86C8BC)

			Row(
				horizontalArrangement = Arrangement.Center,
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.weight(1f),
			) {
				when (itemCount) {
					1 -> DayDot(color = color1)
					2 -> {
						DayDot(color = color1)
						Spacer(modifier = Modifier.width(2.dp))
						DayDot(color = color2)
					}

					3 -> {
						DayDot(color = color1)
						Spacer(modifier = Modifier.width(2.dp))
						DayDot(color = color2)
						Spacer(modifier = Modifier.width(2.dp))
						DayDot(color = color3)
					}

					else -> {
						Box(
							modifier = Modifier
								.height(8.dp)
								.padding(horizontal = 12.dp)
								.fillMaxWidth()
								.background(color = color3, shape = CircleShape)
						)
					}
				}
			}
		} else Spacer(modifier = Modifier.weight(1f))
	}
}

@Preview
@Composable
fun DayDot(color : Color = MaterialTheme.colorScheme.primary) {
	Box(
		modifier = Modifier
			.size(8.dp)
			.background(color = color, shape = CircleShape)
	)
}
