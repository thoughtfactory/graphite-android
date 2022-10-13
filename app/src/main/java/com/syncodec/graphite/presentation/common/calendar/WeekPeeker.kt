package com.syncodec.graphite.presentation.common.calendar

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.utils.weekNameShort
import com.syncodec.graphite.R
import com.syncodec.graphite.utils.timestampToDate
import org.joda.time.DateTime
import org.joda.time.ReadableInstant
import org.joda.time.Weeks
import java.util.Calendar


@OptIn(ExperimentalPagerApi::class)
@Preview
@Composable
fun WeekPeeker(visibleDay: Long) {

	val upperLimit = 4102444800000L //  Friday, January 1, 2100 0:00:00

	val pagerState = rememberPagerState(1)
	var isPeekerVisible by remember { mutableStateOf(true) }

	Weeks.weeksBetween(DateTime(0), DateTime(upperLimit))

	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {

		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			Spacer(modifier = Modifier.width(8.dp))
			Spacer(modifier = Modifier.width(48.dp))
			Spacer(modifier = Modifier.weight(1f))
			Text(
				text = "May 2022",
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onBackground
			)
			Spacer(modifier = Modifier.weight(1f))

			MenuButton(
				icon = R.drawable.ic_chevron_down,
				tint = MaterialTheme.colorScheme.onBackground
			) { isPeekerVisible = !isPeekerVisible }

			Spacer(modifier = Modifier.width(8.dp))
		}

		AnimatedVisibility(
			visible = isPeekerVisible,
			enter = expandVertically(tween(300)),
			exit = shrinkVertically(tween(300))
		) {
			HorizontalPager(
				count = 3,
				state = pagerState
			) {
				Week()
			}
		}
	}
}

@Composable
private fun Week() {
	Row(
		modifier = Modifier.fillMaxWidth()
	) {
		repeat(7) {
			Day(
				modifier = Modifier.weight(1f),
				day = it
			)
		}
	}
}

@Composable
private fun Day(
	modifier: Modifier,
	@androidx.annotation.IntRange(0, 6) day: Int
) {
	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.SpaceAround
	) {
		Spacer(modifier = Modifier.height(8.dp))
		Text(
			text = "$day",
			style = MaterialTheme.typography.titleLarge,
			color = MaterialTheme.colorScheme.onBackground
		)
		Spacer(modifier = Modifier.height(8.dp))
		Text(
			text = weekNameShort[day],
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onBackground
		)
		Spacer(modifier = Modifier.height(8.dp))
	}
}
