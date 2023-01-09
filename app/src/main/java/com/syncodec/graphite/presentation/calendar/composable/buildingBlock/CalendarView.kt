package com.syncodec.graphite.presentation.calendar.composable.buildingBlock

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.utils.Quadruple
import com.syncodec.graphite.utils.monthName
import com.syncodec.graphite.utils.weekNameInitial
import kotlinx.coroutines.launch
import org.checkerframework.common.value.qual.IntRange
import java.time.DayOfWeek
import java.time.LocalDate


val dayColor1 = Color(0xFFFFDCAE)
val dayColor2 = Color(0xFFADCF9F)
val dayColor3 = Color(0xFF76BA99)

@Preview
@OptIn(ExperimentalPagerApi::class)
@Composable
fun CalendarView(
	noteDayMapSize : Map<LocalDate, Int> = emptyMap(),
	onClickDay : (LocalDate?) -> Unit = {},
) {
	val scope = rememberCoroutineScope()

	val now = remember { LocalDate.now() }

	var selectedYear by remember { mutableStateOf(now.year) }
	val pagerState = rememberPagerState(initialPage = Int.MAX_VALUE / 2)
	val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = selectedYear - 1900)

	LaunchedEffect(key1 = pagerState.currentPage) {
		scope.launch {
			selectedYear = now.minusMonths((pagerState.currentPage - Int.MAX_VALUE / 2).toLong()).year
			lazyListState.animateScrollToItem(selectedYear - 1900)
		}
	}

	Column(
		modifier = Modifier.fillMaxSize()
	) {
		YearView(
			listState = lazyListState,
			selectedYearIndex = selectedYear - 1900,
		) {
			selectedYear = it
			scope.launch {
				val diff = now.year - selectedYear
				pagerState.animateScrollToPage(page = (Int.MAX_VALUE / 2) + (diff * 12))
			}
		}

		OutlinedButton(
			colors = ButtonDefaults.outlinedButtonColors(
				containerColor = Color.Companion.Transparent,
				contentColor = MaterialTheme.colorScheme.onBackground
			),
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp, 0.dp),
			onClick = {
				selectedYear = now.year
				scope.launch { pagerState.animateScrollToPage(page = Int.MAX_VALUE / 2) }
			},
		) {
			Text(
				text = "Today",
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onBackground
			)
		}

		HorizontalPager(
			count = Int.MAX_VALUE,
			state = pagerState,
			reverseLayout = true,
			modifier = Modifier.weight(1f),
		) {
			val (year, month, firstDay, monthLength) = now.minusMonths((it - Int.MAX_VALUE / 2).toLong()).let { date ->
				Quadruple(date.year, date.monthValue, date.withDayOfMonth(1).dayOfWeek, date.lengthOfMonth())
			}

			MonthView(
				year = year,
				month = month,
				firstDay = firstDay,
				monthLength = monthLength,
				noteDayMapSize = noteDayMapSize,
				onPrevious = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
				onNext = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
				onClickDay = onClickDay
			)
		}
	}
}

@Preview
@Composable
private fun YearView(
	listState : LazyListState = rememberLazyListState(),
	selectedYearIndex : Int = 0,
	onClickYear : (Int) -> Unit = {}
) {
	LazyRow(
		modifier = Modifier.fillMaxWidth(),
		state = listState
	) {
		items(300) {
			YearItem(
				year = it + 1900,
				isSelected = it == selectedYearIndex,
			) { onClickYear(it + 1900) }
		}
	}
}

@Preview
@Composable
private fun MonthView(
	year : Int = 1999,
	month : Int = 5,
	firstDay : DayOfWeek = DayOfWeek.SATURDAY,
	monthLength : Int = 31,
	noteDayMapSize : Map<LocalDate, Int> = emptyMap(),
	onPrevious : () -> Unit = {},
	onNext : () -> Unit = {},
	onClickDay : (LocalDate?) -> Unit = {}
) {
	Column(
		modifier = Modifier.fillMaxSize()
	) {
		MonthName(
			month = month,
			onPrevious = onPrevious,
			onNext = onNext,
		)

		Spacer(modifier = Modifier.height(8.dp))

		WeekHeader()

		Spacer(modifier = Modifier.height(4.dp))

		MonthContent(
			year = year,
			month = month,
			firstDay = firstDay,
			monthLength = monthLength,
			noteDayMapSize = noteDayMapSize,
			onClickDay = onClickDay
		)
	}
}

@Preview
@Composable
private fun MonthName(
	month : @IntRange(from = 1, to = 12) Int = 5,
	onPrevious : () -> Unit = {},
	onNext : () -> Unit = {},
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically
	) {
		IconButton(onClick = onPrevious) {
			Icon(
				painter = painterResource(id = R.drawable.ic_caret),
				contentDescription = "Previous month",
				tint = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier
					.requiredSize(IconButtonSize)
					.graphicsLayer { rotationY = 270f }
			)
		}
		Spacer(modifier = Modifier.weight(1f))
		Text(
			text = monthName[month - 1],
			style = MaterialTheme.typography.titleLarge,
			color = MaterialTheme.colorScheme.onBackground,
			fontWeight = FontWeight.Bold
		)
		Spacer(modifier = Modifier.weight(1f))
		IconButton(onClick = onNext) {
			Icon(
				painter = painterResource(id = R.drawable.ic_caret),
				contentDescription = "Next month",
				tint = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier
					.requiredSize(IconButtonSize)
					.graphicsLayer { rotationY = 90f }
			)
		}
	}
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearItem(
	year : Int = 1999,
	isSelected : Boolean = false,
	onClick : () -> Unit = {}
) {
	val containerColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
		animationSpec = tween(300)
	)

	val contentColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
		animationSpec = tween(300)
	)

	SuggestionChip(
		onClick = onClick,
		label = {
			Text(
				text = year.toString(),
				style = MaterialTheme.typography.bodyMedium,
				color = contentColor,
				fontWeight = FontWeight.Bold
			)
		},
		modifier = Modifier.padding(4.dp, 0.dp),
		colors = SuggestionChipDefaults.suggestionChipColors(
			containerColor = containerColor,
			labelColor = contentColor,
		)
	)
}

@Preview
@Composable
private fun MonthContent(
	year : Int = 1999,
	month : Int = 5,
	firstDay : DayOfWeek = DayOfWeek.SATURDAY,
	monthLength : Int = 31 ,
	noteDayMapSize : Map<LocalDate, Int> = emptyMap(),
	onClickDay : (LocalDate?) -> Unit = {}
) {
	val numberOfWeek = kotlin.math.ceil((firstDay.value + monthLength - 1) / 7.0).toInt()

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 6.dp)
	) {
		repeat(numberOfWeek) { week ->
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceEvenly
			) {
				repeat(7) { day ->
					val dayOfMonth = week * 7 + day - firstDay.value + 1

					val localDate = if (dayOfMonth in 1 .. monthLength) LocalDate.of(year, month, dayOfMonth) else null

					DayItem(
						dayOfMonth = dayOfMonth,
						isInMonth = dayOfMonth in 1 .. monthLength,
						itemCount = noteDayMapSize[localDate] ?: 0,
						modifier = Modifier
							.weight(1f)
							.aspectRatio(1f)
					) {onClickDay(localDate) }
				}
			}
		}
	}
}

@Preview
@Composable
private fun WeekHeader() {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(8.dp, 0.dp)
			.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
	) {
		weekNameInitial.forEach {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.weight(1f)
					.aspectRatio(1f)
			) {
				Text(
					text = it,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onPrimary,
					fontWeight = FontWeight.Bold
				)
			}
		}
	}
}

@Preview
@Composable
private fun DayItem(
	modifier : Modifier = Modifier,
	dayOfMonth : Int = 1,
	isInMonth : Boolean = true,
	itemCount : Int = 1,
	onClick : () -> Unit = {}
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		modifier = modifier
			.padding(2.dp)
			.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.47f), RoundedCornerShape(17))
			.clip(RoundedCornerShape(17))
			.clickable(onClick = onClick)
	) {

		if (itemCount > 0) {
			Spacer(modifier = Modifier.weight(1f))
			Spacer(modifier = Modifier.height(4.dp))
		}

		if (isInMonth) {
			Text(
				text = dayOfMonth.toString(),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold
			)
		}

		if (itemCount > 0) {
			Spacer(modifier = Modifier.height(4.dp))
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f)
					.background(
						when (itemCount) {
							in 1 .. 2 -> dayColor1
							in 3 .. 5 -> dayColor2
							else -> dayColor3
						}
					)
			)
		}
	}
}
