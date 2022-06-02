package com.syncodec.graphite.custom.calendarView

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.konstant.Konstant

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarWeek() {
	Box(
		modifier = Modifier.fillMaxWidth()
	) {
		LazyVerticalGrid(
			columns = GridCells.Fixed(7),
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
				.clip(RoundedCornerShape(12.dp))
				.background(MaterialTheme.colorScheme.primary),
		) {
			for (i in 0 until 7) {
				item {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.aspectRatio(1f),
						contentAlignment = Alignment.Center
					) {
						Text(
							text = Konstant.weekNameInitial[i],
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onPrimary,
							fontWeight = FontWeight.Bold
						)
					}
				}
			}
		}
	}
}
