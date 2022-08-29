package com.syncodec.graphite.presentation.custom.calendar

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.syncodec.graphite.R
import com.syncodec.graphite.utils.monthName
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

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
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
				FilterChip(
					label = {
						Text(
							text = "$year",
							style = MaterialTheme.typography.bodyMedium,
							fontWeight = FontWeight.Bold,
							modifier = Modifier
						)
					},
					selected = currentYear == year,
					onClick = { onClick(Click.SELECT_YEAR, year) },
					modifier = Modifier.padding(4.dp, 0.dp)
				)
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
				painter = painterResource(id = R.drawable.ic_chevron_left),
				contentDescription = "Previous month",
				tint = MaterialTheme.colorScheme.primary,
				modifier = Modifier.requiredSize(24.dp)
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
				text = monthName[currentPage % 12],
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.primary,
				textAlign = TextAlign.Center,
				maxLines = 1,
			)
		}

		IconButton(onClick = { onClick(Click.NEXT_MONTH, null) }) {
			Icon(
				painter = painterResource(id = R.drawable.ic_chevron_right),
				contentDescription = "Next month",
				tint = MaterialTheme.colorScheme.primary,
				modifier = Modifier.requiredSize(24.dp)
			)
		}
	}
}
