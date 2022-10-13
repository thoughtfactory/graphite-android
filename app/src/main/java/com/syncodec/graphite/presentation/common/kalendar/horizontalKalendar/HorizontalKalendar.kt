package com.syncodec.graphite.presentation.common.kalendar.horizontalKalendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import java.util.Calendar


@OptIn(ExperimentalPagerApi::class)
@Composable
fun HorizontalKalendar() {
	val scope = rememberCoroutineScope()

	val calendar = Calendar.getInstance()
	val currentYear = calendar.get(Calendar.YEAR)
	val currentMonth = calendar.get(Calendar.MONTH)
	val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

	val pagerState = rememberPagerState(initialPage = ((currentYear - 1900) * 12) + currentMonth)

}
