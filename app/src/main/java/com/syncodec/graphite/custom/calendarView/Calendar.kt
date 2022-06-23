package com.syncodec.graphite.custom.calendarView

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import java.util.*


enum class Click {
	PREVIOUS_MONTH,
	NEXT_MONTH,
	SELECT_YEAR,
	SELECT_DATE
}

@OptIn(ExperimentalPagerApi::class,)
@Composable
fun Calendar(
	timestampSizeMap: Map<Long, Int>,
	onClick: (Long) -> Unit
) {
	val scope = rememberCoroutineScope()

	val calendar = Calendar.getInstance()
	val currentYear = calendar.get(Calendar.YEAR)
	val currentMonth = calendar.get(Calendar.MONTH)
	val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

	val pagerState = rememberPagerState(initialPage = ((currentYear - 1900) * 12) + currentMonth)

	Column(modifier = Modifier.fillMaxSize()) {

		CalendarHeader(
			pagerState = pagerState
		) { click, data ->
			when (click) {
				Click.PREVIOUS_MONTH -> scope.launch {
					pagerState.animateScrollToPage(maxOf(0, pagerState.currentPage - 1))
				}
				Click.NEXT_MONTH -> scope.launch {
					pagerState.animateScrollToPage(
						minOf(pagerState.pageCount - 1, pagerState.currentPage + 1)
					)
				}
				Click.SELECT_YEAR -> scope.launch {
					pagerState.scrollToPage(((data as Int - 1900) * 12) + (pagerState.currentPage % 12))
				}
			}
		}

		CalendarWeek()

		HorizontalPager(
			state = pagerState,
			count = 300 * 12,
			modifier = Modifier.fillMaxSize()
		) { page ->
			Box(
				modifier = Modifier.fillMaxSize(),
				contentAlignment = Alignment.TopCenter
			) {
				CalendarMonth(
					year = (page / 12) + 1900,
					month = page % 12,
					currentYear = currentYear,
					currentMonth = currentMonth,
					currentDayOfMonth = currentDayOfMonth,
					timestampSizeMap = timestampSizeMap
				) { onClick(it) }
			}
		}
	}
}
