package com.syncodec.momento.custom.calendarView

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.syncodec.momento.konstant.Konstant
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronLeft
import compose.icons.tablericons.ChevronRight
import kotlinx.coroutines.launch


@OptIn(ExperimentalPagerApi::class)
@Composable
fun CalendarHeader(
	pagerState: PagerState,
	onClick: (Click, Any?) -> Unit
) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		CalendarHeaderYear(pagerState = pagerState) { click, data -> onClick(click, data) }
		CalendarHeaderMonth(pagerState = pagerState) { click, data -> onClick(click, data) }
	}
}

@OptIn(ExperimentalPagerApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
private fun CalendarHeaderYear(
	pagerState: PagerState,
	onClick: (Click, Any?) -> Unit
) {
	val scope = rememberCoroutineScope()
	val yearState = rememberLazyListState()
	LaunchedEffect(key1 = null) {
		scope.launch { yearState.scrollToItem(pagerState.currentPage / 12, -1) }
	}

	val currentYear = (pagerState.currentPage / 12) + 1900

	LazyRow(
		state = yearState,
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.padding(0.dp, 8.dp, 0.dp, 0.dp),
	) {
		item { Spacer(modifier = Modifier.width(8.dp)) }
		for (year in 1901 until 2100) {
			item {
				val animateFloat by animateFloatAsState(targetValue = if (currentYear == year) 1.3f else 1f)
				Card(
					modifier = Modifier
						.padding(if (currentYear == year) 16.dp else 4.dp, 0.dp)
						.graphicsLayer {
							this.scaleX = animateFloat
							this.scaleY = animateFloat
						},
					shape = RoundedCornerShape(50),
					border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
					backgroundColor = if (currentYear == year) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
					onClick = { onClick(Click.SELECT_YEAR, year) }
				) {
					Text(
						text = "$year",
						style = MaterialTheme.typography.bodyMedium,
						color = if (currentYear == year) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold,
						modifier = Modifier
							.padding(16.dp, 8.dp)
					)
				}
			}
		}
		item { Spacer(modifier = Modifier.width(8.dp)) }
	}
}

@OptIn(ExperimentalPagerApi::class, ExperimentalAnimationApi::class)
@Composable
private fun CalendarHeaderMonth(
	pagerState: PagerState,
	onClick: (Click, Any?) -> Unit
) {
	FlowRow(
		mainAxisAlignment = MainAxisAlignment.SpaceBetween,
		crossAxisAlignment = FlowCrossAxisAlignment.Center,
		modifier = Modifier
			.fillMaxWidth()
			.padding(8.dp, 0.dp),
	) {
		IconButton(onClick = { onClick(Click.PREVIOUS_MONTH, null) }) {
			Icon(
				imageVector = TablerIcons.ChevronLeft,
				contentDescription = "Previous month",
				tint = MaterialTheme.colorScheme.primary
			)
		}

		AnimatedContent(
			targetState = pagerState.currentPage,
			transitionSpec = {
				if (targetState > initialState) {
					slideInHorizontally { height -> height } + fadeIn() with slideOutHorizontally { height -> -height } + fadeOut()
				} else {
					slideInHorizontally { height -> -height } + fadeIn() with slideOutHorizontally { height -> height } + fadeOut()
				}.using(
					SizeTransform(clip = false)
				)
			}
		) { currentPage ->
			Text(
				text = Konstant.monthName[currentPage % 12],
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.primary,
				textAlign = TextAlign.Center,
				maxLines = 1,
			)
		}

		IconButton(onClick = { onClick(Click.NEXT_MONTH, null) }) {
			Icon(
				imageVector = TablerIcons.ChevronRight,
				contentDescription = "Next month",
				tint = MaterialTheme.colorScheme.primary
			)
		}
	}
}
