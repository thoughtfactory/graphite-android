package com.syncodec.momento.custom.calendarView2

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerDefaults
import com.google.accompanist.pager.rememberPagerState

@Preview
@OptIn(ExperimentalPagerApi::class)
@Composable
fun Calendar(
	startMonth: Int = 0,
	startYear: Int = 2022,
) {
	val pagerState = rememberPagerState(
		initialPage = ((startYear - 1900) * 12) + startMonth
	)
	HorizontalPager(
		state = pagerState,
		count = 300 * 12,
		flingBehavior = rememberFlingBehaviorMultiplier(
			multiplier = 0.01f,
			baseFlingBehavior = PagerDefaults.flingBehavior(pagerState)
		),
		modifier = Modifier
			.fillMaxSize()
	) { page ->
		Box(
			modifier = Modifier
				.fillMaxSize(),
			contentAlignment = Alignment.TopCenter
		) {
			CalendarMonth(
				month = page % 12,
				year = (page / 12) + 1900
			)
		}
	}
}

private class FlingBehaviourMultiplier(
	private val multiplier: Float,
	private val baseFlingBehavior: FlingBehavior
) : FlingBehavior {
	override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
		return with(baseFlingBehavior) {
			performFling(initialVelocity * multiplier)
		}
	}
}

@Composable
fun rememberFlingBehaviorMultiplier(
	multiplier: Float,
	baseFlingBehavior: FlingBehavior
): FlingBehavior = remember(multiplier, baseFlingBehavior) {
	FlingBehaviourMultiplier(multiplier, baseFlingBehavior)
}
