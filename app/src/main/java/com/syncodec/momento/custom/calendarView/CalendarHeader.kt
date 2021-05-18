package com.syncodec.momento.custom.calendarView

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
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
	pagerState: PagerState
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.secondaryContainer)
	) {
		CalendarHeaderYear(pagerState = pagerState)
		CalendarHeaderMonth(pagerState = pagerState)
	}
}

@OptIn(ExperimentalPagerApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
private fun CalendarHeaderYear(
	pagerState: PagerState
) {
	val scope = rememberCoroutineScope()
	val yearState = rememberLazyListState()
	SideEffect {
		scope.launch {
			yearState.scrollToItem(pagerState.currentPage / 12, -1)
		}
	}

	val currentYear = (pagerState.currentPage / 12) + 1900
	LazyRow(
		modifier = Modifier
			.fillMaxWidth()
			.padding(0.dp, 8.dp),
		state = yearState,
		verticalAlignment = Alignment.CenterVertically
	) {
		item { Spacer(modifier = Modifier.width(8.dp)) }
		for (year in 1901 until 2100) {
			item {
				Card(
					modifier = Modifier
						.padding(if (currentYear == year) 16.dp else 4.dp, 0.dp)
						.graphicsLayer {
							this.scaleX = if (currentYear == year) 1.3f else 1f
							this.scaleY = if (currentYear == year) 1.3f else 1f
						},
					shape = RoundedCornerShape(50),
					border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
					backgroundColor = if (currentYear == year) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
					onClick = {}
				) {
					Text(
						text = "$year",
						style = MaterialTheme.typography.bodyMedium,
						color = if (currentYear == year) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
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

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun CalendarHeaderMonth(
	pagerState: PagerState
) {
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
			text = Konstant.monthName[pagerState.currentPage % 12],
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
}
